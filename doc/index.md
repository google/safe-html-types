# Introduction to Safe Coding

At Google, we have developed a practical approach called "safe coding" to
prevent security vulnerabilities. Safe coding is:

*   **Scalable** – It eliminates entire classes of security vulnerabilities,
    such as [cross-site scripting (XSS)](https://www.google.com/about/appsecurity/learning/xss/), which have
    traditionally been notoriously difficult to avoid in large-scale
    development[^1].
*   **Proactive** – It prevents vulnerabilities from ever being introduced
    during development.

The two elements of safe coding are:


[^1]:
    In 2014, XSS accounted for ca. 65% of vulnerabilities reported through
    Google's [Vulnerability Reward ("Bug Bounty") Program][bug stats].




<a href="safe-apis.md">

**Inherently safe APIs**</a>

Client code using these APIs can under no (reasonable) circumstances suffer from
the vulnerability in question




<a href="safe-coding.md">

**Enforcing safe coding**</a>

Ensures that inherently safe APIs are used correctly and comprehensively
throughout a code base

## Benefits of safe coding

The benefits of the safe coding approach are twofold:

*   **Confidence** that a particular class of vulnerabilities does not exist in
    an application and will not be introduced as the code base evolves.
*   In some scenarios, a drastic **reduction in actual bugs** as safe coding
    style is adopted.

### Confined, limited potential for bugs, and improved reviewability

Inherently safe APIs are designed to ensure that application code written on top
of the API cannot have certain security vulnerabilities. For this reason,
comprehensive use of safe APIs removes the potential for such bugs in
application code. The only circumstances under which such a bug can occur, is if
there is a flaw in the implementation of the API itself or of some other
lower-level library or framework.

**What does this mean for code reviews?**

A code reviewer does not have to inspect application code in order to assess an
application with respect to the vulnerabilities in question. Instead, the only
code that needs to be reviewed are the implementations of the APIs (HTML
template systems, etc), and any uses of reviewed exceptions of unsafe API usage.

### Actual reduction in bugs

Adoption of "XSS-proof" safe APIs has resulted in a very significant reduction
of actual bugs in large-scale applications. Several projects have refactored code and templates to adhere to an XSS-proof
coding style, and noted a substantial drop in discovered XSS vulnerabilities
which has held stable over some years. The few remaining vulnerabilities were
not due to flaws in application code, but rather in low-level library and
framework code.

## Further reading

* [Securing the Tangled Web](http://research.google.com/pubs/archive/42934.pdf)
* [Strict Closure Templates](https://developers.google.com/closure/templates/docs/security#strict)


[bug stats]: https://software-security.sans.org/downloads/appsec-2011-files/vrp-presentation.pdf
