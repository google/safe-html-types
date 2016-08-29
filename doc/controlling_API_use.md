# Controlling Inappropriate Use of APIs

**Audience note:** This document is primarily aimed at security engineers.
It may be also be of interest to other developers who want to understand
the background and rationale for locking down access to certain APIs.


## Overview

To prevent the introduction of specific classes of security vulnerabilities,
security team have developed [inherently secure
APIs](security_reviewers_guide.md#inherently_secure_apis) whose use by design
cannot result in such vulnerabilities. To accommodate exceptional use cases that
cannot be expressed in terms of the secure APIs, [unconstrained, potentially
insecure APIs](security_reviewers_guide.md#unchecked_conversions) are available.

For this approach to be effective, all uses of such potentially insecure APIs
must be security reviewed by a human reviewer, and their use should be limited
to scenarios where it is strictly necessary. For the purposes of this document,
we refer to any use of such an API that does not satisfy these two conditions as
an *inappropriate use*.

In the security teams's experience, it is insufficient to rely on documentation
and developer education alone to avoid the introduction of inappropriate API
use.  See the [Appendix](#appendix_a) for some examples.

This document describes a number of patterns we have used to restrict and
control inappropriate use of such APIs.

## BUILD visibility

Often, an API can be structured such that the potentially insecure API is
factored out into a separate BUILD target. The implementation of this API in
turn gains access to the internals of the actual API through language-level
visibility mechanisms.

The BUILD target exposing the potentially insecure API is BUILD visibility-
restricted, enforcing the requirement that at least the first use from a
specific package is security reviewed.  This review can then ensure that the new
use adheres to [guidelines](safehtml-unchecked.md) for appropriate use.

A common use case requires that a type's public API only exposes builders and
factory methods whose implementations strongly guarantee that only
contract-compliant instances are created.  For exceptional use cases that cannot
be expressed in terms of one of the "standard" builders and factory methods, we
provide an unconstrained "backdoor" factory method. In contrast to the
inherently secure builders, this backdoor factory method essentially accepts as
its argument(s) the internal state of the to-be-created type instance, and hence
fully relies on calling code to ensure the resulting instance's type contract.
The implementation of the backdoor method typically calls a private constructor
of the type, to which it has access through language-level visibility
mechanisms.

As an example, consider the implementation of the
[SafeHtml type](safehtml-types.md) in various languages.

### Java

*   The [type's
    constructor][SafeHtml.SafeHtml(String)]
    is package-private, and as such visible to the type's builders and factory
    methods in the same package (e.g.,
    [`SafeHtmls`][SafeHtmls.create].
*   The potentially unsafe "backdoor" API (e.g.,
    [`UncheckedConversions`][UncheckedConversions.safeHtmlFromStringKnownToSatisfyTypeContract])
    lives in the same package, and hence has access to the type's package
    private constructor.
*   However, the class exposing the backdoor API lives in a separate BUILD
    target
    which is BUILD visibility-restricted.


### JS

The approach for JS is essentially the same as Java, except that we add some
indirection to account for the less stringent static visibility checking in
JSCompiler.  See:


*   [`SafeHtml.createSafeHtmlSecurityPrivateDoNotAccessOrElse`][SafeHtml.createSafeHtmlSecurityPrivateDoNotAccessOrElse]
*   [`uncheckedconversions.js`][uncheckedconversions visibility]


## Java Error Prone-checked restrictions

In some cases, the approach in the previous section is not possible; for
instance, if the method whose use must be restricted is in a public API that
cannot be refactored.

In Java, we can use [Error Prone](http://github.com/google/error-prone) to restrict call sites of
specific methods:

*   Create an Error Prone checker that disallows call-sites to particular
    methods.

    The checker should be declared `UNSUPPRESSIBLE` (to prevent it from being
    disabled via `@SuppressWarnings`).
*   Expose the checker as a [`java_plugin`](http:http://bazel.build/docs/be/java.html#java_plugin)
    which is in turn referenced in the `exported_plugins` of the rule that
    provides the API that needs to be restricted
*   Optionally, the checker can be suppressed through an annotation
    which is BUILD visibility-restricted to be usable only
    in [testing code](http://bazel.build/docs/be/common-definitions.html#common.testonly)


When introducing such a mechanism, it can be a useful pattern to introduce
separate annotations to distinguish uses that have actually been reviewed, from
existing, unreviewed, potentially vulnerable legacy uses


## Presubmit checks


In some cases, it is not feasible to use language-level visibility and
[BUILD visibility](http://bazel.build/docs/be/common-definitions.html#common.visibility) mechanisms to restrict
inappropriate use.

For instance, for the protocol buffer representation of the
[safe HTML types](safehtml-types.md), we needed to approximate package-private
visibility for protocol buffer fields.  There is no visibility concept in
protobuf, and we resorted to a
[global presubmit](http://dev.chromium.org/developers/how-tos/depottools/presubmit-scripts]).



## Appendix: Why documentation is insufficient {#appendix_a}

It's insufficient to rely on documentation to
prevent inappropriate/incorrect use of APIs.  For example:

*   Google Web Toolkit includes a method,
    [`SafeHtmlUtils.fromTrustedString`][SafeHtmlUtils.fromTrustedString],
    whose uses can result in XSS vulnerabilities and hence need to be security
    reviewed.  This requirement has been widely ignored in our codebase: A large
    number of uses (> 1000s) have been introduced, most of which are
    inappropriate, and some of which have resulted in actual security bugs
*   It appears to even be insufficient to use purposely cumbersone method names
    like `do_not_access_or_else...`.
    One would think that no reasonable person would inappropriately use those
    methods.
    *   We used this approach for intended-to-be-private fields in protocol
        buffers, but found that within weeks of their introduction, several
        uses had appeared in our codebase.
    *   Similarly, a GWT class whose name clearly specifies that it should only
        be used in generated code nevertheless has uses in our codebase.




[SafeHtml.SafeHtml(String)]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/SafeHtml.html#SafeHtml(String)
[SafeHtmls.create]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/SafeHtmls.html#create
[UncheckedConversions.safeHtmlFromStringKnownToSatisfyTypeContract]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/UncheckedConversions.html#safeHtmlFromStringKnownToSatisfyTypeContract
[SafeHtml.createSafeHtmlSecurityPrivateDoNotAccessOrElse]: https://github.com/google/closure-library/blob/98e9bc2e3034e5d37af8a024631c7f823d58c87a/closure/goog/html/safehtml.js#L615
[uncheckedconversions visibility]: https://github.com/google/closure-library/blob/98e9bc2e3034e5d37af8a024631c7f823d58c87a/closure/goog/html/uncheckedconversions.js#L28
[SafeHtmlUtils.fromTrustedString]: http://static.javadoc.io/com.google.gwt/gwt-user/2.7.0/com/google/gwt/safehtml/shared/SafeHtmlUtils.html#fromTrustedString%28java.lang.String%29



