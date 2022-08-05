# Inherently safe APIs


Many security vulnerabilities are caused by an unsafe use of an API.

> **Example**: XSS vulnerabilities are caused by application code that passes an
> improperly sanitized or escaped string to an *injection sink*, like the
> `.innerHTML` property. This type of API is prone to injection vulnerabilities
> – it accepts an arbitrary string, and places the responsibility on developers
> to ensure that strings passed to the API are safely constructed in all call
> sites.

We address this risk by providing alternative, **inherently safe APIs**. They
have largely equivalent functionality, but their design entirely removes the
*potential* for introducing a vulnerability of a given class in the application
code. If the application code using the API compiles without errors, it is known
to be free of the vulnerability in question.

Inherently safe APIs developed at Google include:

*   **HTML templating systems in strict-contextual escaping mode** (e.g,.
    [strict Closure Templates](https://github.com/google/closure-templates/blob/master/documentation/concepts/auto-escaping.md),
    [Angular](https://angular.io/guide/security#xss)) ensure that rendering of
    templates cannot result in XSS vulnerabilities, even if template parameters
    are under the control of an attacker.

*   Safe DOM API wrappers (e.g.
    [`safevalues/dom`](https://github.com/google/safevalues) for TSJS) ensure
    that only safe values can be passed to DOM APIs that can lead to XSS.

*   Both strict contextual templates and safe DOM API wrappers accept values of
    [**Safe HTML types**](safehtml-types.md). These types (`SafeHtml`,
    `SafeUrl`, etc) stipulate that their value is safe to use (with respect to
    XSS vulnerabilities) in corresponding contexts. They come with builder APIs
    that ensure that all instances satisfy the safety contract.
