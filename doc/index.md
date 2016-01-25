# Preventing Security Bugs At Scale through Strict Safe Coding Practices


Certain classes of security vulnerabilities, such as [cross-site scripting](http://www.google.com/about/appsecurity/learning/xss/),
have traditionally been notoriously difficult to avoid in large-scale
development[^1]. At Google, we have developed a practical approach to prevent
such vulnerabilities from ever being introduced during development.  This
approach is based on *inherently safe APIs* which guarantee that client code
using the API can under no (reasonable) circumstances suffer from the
vulnerability in question, and *strict, statically enforced safe coding
practices* to ensure that such APIs are used correctly and comprehensively
throughout a code base.

This document provides a short overview of this approach and its benefits.



[^1]:
    In 2014, there were [several hundred][bug stats]
    *externally discovered* XSS vulnerabilities in Google applications. That
    is, several hundred vulnerabilities that were *not* found through code
    review nor testing, were shipped to production, and *then* discovered and
    reported by an external party.


## Inherently safe APIs

Fundamentally, many security vulnerabilities arise from unsafe use of an
underlying API. For example, XSS vulnerabilities are due to application code
that passes a string containing improperly sanitized or escaped data to an XSS-
related *injection sink*, for instance by assigning such a string to the
`.innerHTML` property. As such, these APIs are inherently prone to injection
vulnerabilities -- the API accepts an arbitrary string, and places the onus on
developers to ensure that in all call sites, strings passed to the API are
safely constructed.

We comprehensively address this risk by providing alternative APIs that are
*inherently safe*, with largely equivalent functionality. The API's design
ensures that any arbitrary (though type-correct, where applicable) use of the
API is free of the vulnerability in question. Consistent use of the safe API
entirely removes even the *potential* for any vulnerability of the respective
type to be present in application code -- if application code written on top of
the API compiles without error, it is known to be free of the vulnerability in
question.

Inherently safe APIs developed at Google include:

* HTML templating systems in strict-contextual escaping mode (e.g,.
  [strict Closure Templates]
  (https://developers.google.com/closure/templates/docs/security#strict),
  [Angular](https://docs.angularjs.org/api/ng/service/$sce)) ensure that
  rendering of templates cannot result in XSS vulnerabilities, even if template
  parameters are under the control of an attacker.

* Safe DOM API wrappers (`goog.dom.safe`)
  ensure that only safe values can reach the underlying,
  XSS-vulnerability-prone, raw DOM API.

* Both strict contextual templates and safe DOM API wrappers accept values of
  ["Safe HTML" types](http://github.com/google/safe-html-types/blob/master/doc/safehtml-types.md).
  These types (`SafeHtml`, `SafeUrl`, etc)
  carry contracts stipulating that their value is safe to use (with respect to
  XSS vulnerabilities) in corresponding contexts.  These types come with builder
  APIs that in turn ensure that instances of the type indeed satisfy the type's
  contract. In addition, strict HTML templates produce contract-compliant values
  of type `SafeHtml`.

## Safe coding style

Given the inherently safe APIs introduced in the previous section, a coding
style that ensures code is free of vulnerabilities in a particular class simply
amounts to comprehensive and correct use of these safe APIs (such as strict
HTML templates and `goog.dom.safe` wrapper APIs), and complete avoidance of the
underlying raw, vulnerability-prone APIs (such as `.innerHTML` assignment).

Adherence to the safe, "vulnerability-proof" coding style is statically
enforced. In some cases, this is accomplished through the implementation
language's type system; in some cases, we use custom static checkers such as
[Error Prone](http://github.com/google/error-prone) or [JSConformance](http://github.com/google/closure-compiler/wiki/JS-Conformance-Framework).

### Reviewed exceptions

While designed to satisfy the vast majority of use cases, these safe APIs are,
by necessity, more constrained than the underlying, vulnerability-prone APIs.
To accommodate use cases that cannot be expressed in terms of the safe APIs, we
provide unconstrained APIs equivalent to the underlying, vulnerability-prone
APIs. Usage of such unconstrained APIs carries a risk of vulnerabilities, and
hence must be security reviewed, and designed to be effectively reviewable, as
per the guidelines in
[Safe HTML unchecked conversions](safehtml-unchecked.md). To track
usage of such unconstrained APIs and to ensure security review indeed takes
place, their access is typically restricted through BUILD visibility (see
["Controlling API use"](controlling_API_use.md)).

## Benefits

The benefits of this approach are twofold:

*  *Confidence* that a particular class of vulnerabilities does not exist in an
   application and will not get introduced as the code base evolves.
*  In some scenarios, drastic *reduction in actual bugs* as safe coding style is
   adopted.

### Actual reduction in bugs

Adoption of safe APIs, in particular of an "XSS-proof" coding style, has
resulted in very significant reduction of actual bugs in large-scale
applications.
Several projects have refactored code and templates to adhere to an XSS-proof
coding style, and noted a substantial drop in discovered XSS vulnerabilities
which has held stable over some years.  The few remaining vulnerabilities were
not due to flaws in application code, but rather in low-level library and
framework code.


### Confined, limited potential for bugs, and improved reviewability

Since these safe APIs are designed such that application code written on top of
the API cannot have certain security vulnerabilities, comprehensive use of the
safe API essentially removes the potential for such bugs out of application code
altogether; if such a bug were present, it would have to be due to a flaw in
the implementation of the API or some other lower-level library or framework.

This in turn implies that a code reviewer does not have to inspect application
code in order to assess an application with respect to the vulnerabilities in
question.  The only code that needs to be reviewed are implementations of the
APIs (HTML template systems, etc), and any uses of the
"reviewed exceptions" APIs mentioned above.

## Further reading

* [Securing the Tangled Web](http://research.google.com/pubs/archive/42934.pdf)
* [Strict Closure Templates](http://developers.google.com/closure/templates/docs/security#strict)


[bug stats]: https://software-security.sans.org/downloads/appsec-2011-files/vrp-presentation.pdf



