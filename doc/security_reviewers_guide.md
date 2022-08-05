# The Security Reviewer's Guide to Hardened APIs

There are many APIs and framework features whose use by application code is
a-priori prone to the potential introduction of security vulnerabilities, such
as XSS or SQL injection. To mitigate this risk, the
[ISE Hardening Team](http://g/ise-hardening) has developed API and framework
design patterns that aim to drastically reduce the amount of code that can
potentially harbor vulnerabilities. This reduces the actual risk of
vulnerabilities, and improves reviewability.

However, these patterns cannot entirely prevent the introduction of
vulnerabilities. To this end, we provide guide documents that describe in detail
which remaining aspects of code still require human review.

The following domain-specific review guides are available:

*   [Security Reviewer's Guide to SafeHtml and Strict Templating](security_reviewers_guide_safehtml.md)
*   [Trusted SQL](safesql-types.md#unchecked)

This document provides an overview of the techniques and patterns used to create
vulnerability-resistant APIs and to ensure and facilitate effective human
security review of residual vulnerability risks. This document, as well as the
domain-specific review guides, are intended for an audience of experienced
security reviewers. I.e., we assume that the reader is already reasonably
familiar with the nature of the vulnerabilities under discussion.


## Inherently-secure APIs {#inherently_secure_apis}

Our high-level goals are twofold:

1.  To largely eliminate the potential for the introduction of specific classes
    of vulnerabilities in application code.
2.  To make it practical for a time-constrained human reviewer to assert with a
    reasonably high degree of confidence that even large application code-bases
    are free of particular classes of vulnerabilities. In other words, to allow
    in-depth security reviews to scale to large code-bases.

We achieve these goals through the design of APIs and framework features such
that application code using said API simply cannot, under reasonable
assumptions, have the class of vulnerability we're concerned about. We refer to
an API with this property as *inherently secure*.

The potential for the vulnerabilities we are concerned about, for instance
injection vulnerabilities such as https://www.google.com/about/appsecurity/learning/xss/ and https://www.owasp.org/index.php/SQL_Injection, are unfortunately often
inherent in the design of the underlying APIs. For example, in the web platform
with its many injection-prone APIs, or in string-based, injection-prone SQL
query APIs. Since it is in most cases impossible/impractical for us to change
the underlying API, we resort to introducing wrapper APIs and framework features
(such as HTML templating systems) that provide an inherently-safe abstraction
around the vulnerability-prone underlying API.

By design, uses of an inherently-secure API cannot result in a vulnerability of
the relevant class. As such, none of the uses/call-sites of the
inherently-secure API (nor their control-flow nor data-flow fan-in) need to be
security reviewed. This is the key property that makes high-confidence security
assessments of application code practical (with respect to the vulnerability
class in question). In practice, there will be (typically very few) scenarios
that cannot be satisfied by the inherently safe API; we discuss below how those
are accommodated in a reviewable manner.

To take advantage of the bug-prevention and auditability benefits of this
approach, direct use of the underlying, vulnerability-prone API must be
avoided -- essentially, there should be one call-site of that underlying API,
namely from within its inherently-secure wrapper. We use
[BUILD-visibility and other mechanisms](controlling_API_use.md) to prevent the
inadvertent introduction of direct call-sites of the "raw" underlying API.

### Security-contract types

In many cases, an API can make its guarantees (in this case, that any arbitrary,
well-typed use is free of a particular class of bugs) only under certain
constraints on the API's use; in particular, it is often required that an API
method's parameter satisfies certain properties. We extensively use types to
capture such constraints. In many cases, the underlying datum is a plain string;
in this case we use simple wrapper types for strings, used to represent a
string-like value with a particular security-relevant contract attached.

Care must be taken that all of a type's instances indeed satisfy its contract.
Responsibility for this lies with code that produces instances of the type,
notably constructors, factory methods and builders. Typically, these factory
methods and builders themselves expose an inherently-safe API, in that they
guarantee that any arbitrary, well-typed use of theirs produces values that
adhere to their type contract.

Types frequently serve as attestations of a property that is established at the
code site where a value first originates; the type contract essentially
"tele-ports" the assertion of that property to a code site where that value is
used, without the need to reason about the entire (typically complex) data flow
in-between. This is particularly valuable for properties that cannot be
efficiently, or not even in principle, established through a run-time predicate;
this notably applies to assertions of the provenance of a value (such as "this
value originated from a compile-time-constant expression and can hence be
considered trustworthy").

### Exceptions to the rule, and unchecked conversions {#unchecked_conversions}

Of course in practice, there are always exceptions to the rule: Sometimes, a
desired scenario simply cannot be expressed in terms of the inherently-secure
API. We use two mechanisms to accommodate such cases:

*   *Allowlisted, potentially-unsafe "backdoor" APIs*: Essentially, a mechanism
    that permits application code to use an underlying, vulnerability-prone API,
    without the constraints imposed by its corresponding inherently-secure API.
    Code using the potentially-unsafe API must be security reviewed to ensure
    that it does not introduce an actual vulnerability.

*   *Allowlisted, unchecked conversions for security-contract types*: An
    unchecked conversion is a factory method for a security-contract type that
    creates an instance of the type from raw, unconstrained data (e.g., a
    string) without imposing any inherent constraints. As such, callers of the
    unchecked conversion are responsible for ensuring the resulting type's
    contract, and hence must be security reviewed.

Both mechanisms should only be used in ways that can be meaningfully security
reviewed, as detailed in the [guidelines](safehtml-unchecked.md) for their use.
In our experience, developers cannot in general be relied upon to only use
potentially-unsafe APIs in ways that are both actually safe, and can be
meaningfully reviewed to be so. Hence we guard and allowlist their use through
[BUILD-visibility or custom static checks](controlling_API_use.md). Use of
potentially-unsafe APIs should be exceedingly rare in typical application code.

## High-level security review approach

To assess an app's freedom from a particular class of vulnerabilities, a
security reviewer must:

*   Verify that the application code and its dependencies exclusively use APIs
    that are inherently-secure with respect to the vulnerability; notably that
    there are no inappropriate uses of the raw, underlying API. Since the latter
    are generally guarded through a compile-time mechanism, this amounts to
    verification that the application code is subject to that guard (in some
    cases, this is a per-project opt-in, in others it's given because the guard
    is globally in place for our codebase).

*   Review uses of potentially-unsafe APIs in the application code and its
    dependencies, if any.

    *   Restrict APIs based on build visibility rules ideally enforced by
        pre-submit or pre-release checks.
    *   Make sure your code-review process includes CCing your security team
        on changes to allowlisted modules.
    *   Review all call-sites periodically ideally using tools to triage call-sites.
        This provides a strong incentive to not allowlist too many packages.

*   Consider the risk of vulnerabilities due to caveats and limitations related
    to the inherently secure API.

In the domain specific reviewer guides, we provide detailed checklists on what
to look for.

## General caveats and limitations

*   Many inherently-secure APIs rely on type contracts of parameters supplied to
    the API. Client code that violates type encapsulation (e.g. using
    reflection) to produce instances of such types that do not satisfy their
    contract will in turn subvert the guarantees made by the API, and hence may
    result in security bugs. Such code should be rare (non-existent) in our
    codebase. Special consideration concerns code that deserializes serialized
    forms of security-contract types (in particular protobufs).

*   Several APIs have limitations in the guarantees they can make, and may not
    prevent certain subclasses of vulnerabilities, or certain related types of
    security issues. In some cases, there are plans to address these limitations
    eventually, while in others they are inherent. I.e., there's some fine print
    to read.
