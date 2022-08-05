# Safe HTML Types


When developing web applications a common source of security vulnerabilites
comes from passing user-supplied data directly to security-sensitive contexts,
without proper validation or sanitization. Some examples of security-sensitive
contexts include assigning to `element.innerHTML` (client-side) or interpolation
in an HTML template (server-side), both of which can introduce
[cross-site scripting (XSS)][XSS] vulnerabilities.

To distinguish between unsafe user-supplied data and data that has been
validated, sanitized or otherwise deemed safe for use in a given
security-sensitive context, we provide the *Safe HTML Types*. These types are
simple wrappers around strings, and represent values that are known -- by
construction or via escaping or sanitization -- to be safe to use in various
HTML contexts and with various DOM APIs. The types can be summarized as follows:

*   [**SafeHtml**](#safehtml): String that is safe to use in HTML contexts in
    DOM APIs and HTML documents.
*   [**SafeScript**](#safescript): JavaScript code that is safe to evaluate and
    use as the content of an HTML script element.
*   [**SafeStyle**](#safestyle): Sequence of CSS declarations safe to use in
    style contexts in an HTML document or in DOM APIs.
*   [**SafeStyleSheet**](#safestylesheet): A complete CSS style sheet, safe to
    use in style contexts in an HTML document or DOM APIs.
*   [**SafeUrl**](#safeurl): String that is safe to use in URL navigation
    contexts (`document.location`, `a.href`).
*   [**TrustedResourceUrl**](#trustedresourceurl): String that is safe to use in
    all URL contexts in DOM APIs and HTML documents; even as a reference to
    resources that may load in the current origin (e.g. scripts and
    stylesheets).

When passing data to a security-sensitive context, developers must prove that
their data is safe to use in the given context by providing an appropriate Safe
HTML Type wrapping the data, instead of passing the data directly. In turn,
constructing any of the Safe HTML Types is only possible in ways that ensure
that the wrapped data is safe, for example by sanitizing or escaping it.

Usage of the Safe HTML Types is enforced through secure design of libraries and
frameworks, and by conformance checks that ban direct use of security-sensitive
DOM APIs and insecure coding patterns, and instead direct developers to safe
alternatives.

Safe HTML Types can be converted to and from protocol messages, and thus
propagated across languages and systems.

## Types

The following sections go into more detail about each type, their associated
security contract, and how to construct them.

### SafeHtml {#safehtml}

A SafeHtml is a string-like object that carries the security type contract that
its value as a string will not cause untrusted script execution when evaluated
as HTML in a browser. See its full security contract
[here](safehtml-contracts.md#safehtml).

### SafeScript {#safescript}

A SafeScript is a string-like object which represents JavaScript code that
carries the security type contract that its value, as a string, will not cause
execution of unconstrained attacker controlled code (XSS) when evaluated as
JavaScript in a browser. See its full security contract
[here](safehtml-contracts.md#safescript).

### SafeStyle {#safestyle}

A SafeStyle is a string-like object that represents a sequence of CSS
declarations (`property_name1: property_value1; property_name2: property_value2;
...`) and that carries the security type contract that its value will not cause
untrusted script execution (XSS) when evaluated as CSS in a browser. See its
full security contract [here](safehtml-contracts.md#safestyle).

### SafeStyleSheet {#safestylesheet}

A SafeStyleSheet is a string-like object which represents a CSS style sheet and
that carries the security type contract that its value, as a string, will not
cause untrusted script execution (XSS) when evaluated as CSS in a browser. See
its full security contract [here](safehtml-contracts.md#safestylesheet).

### SafeUrl {#safeurl}

A SafeUrl is a string-like object that carries the security type contract that
its value as a string will not cause untrusted script execution when evaluated
as a hyperlink URL in a browser. See its full security contract
[here](safehtml-contracts.md#safeurl).

### TrustedResourceUrl {#trustedresourceurl}

A TrustedResourceUrl is a string-like object representing a URL referencing the
application’s own, trusted resources. It can be used to safely load scripts, CSS
and other sensitive resources without the risk of untrusted code execution. See
its full security contract [here](safehtml-contracts.md#trustedresourceurl).

Tip: TrustedResourceUrls can use absolute paths (starting with '/') and relative
paths. This allows the same binary to be used for different hosts without
hard-coding the hostname in a string literal or flag value.

## Unchecked conversion from string {#unchecked}

The public API of these types is expected to be sufficiently flexible for the
majority of cases where a Safe HTML Type needs to be created. However, some
scenarios where we'd like to create instances of these types cannot reasonably
be supported with a generic API. An example of such a scenario is transforming
the SafeHtml emitted by a sanitizer.

For such cases it is possible to use an unchecked conversion from string to a
Safe HTML Type. Use of unchecked conversions should go through a security review
and access to the conversion functions is generally controlled through BUILD
visibility or conformance. It is important that uses of unchecked conversions
are designed such that a security reviewer can readily verify that converted
values indeed satisfy the claimed type contract.

If you think you need to use an unchecked conversion, please read the
[safe HTML unchecked conversions guidelines](safehtml-unchecked.md).

## Use in tests

In case the API of the types is impossible to use in a test, or just too
inconvenient, then you can use conversion functions which take a string and
return one of the safe HTML types. These are exported via a `testonly` BUILD
target. However, we recommend you avoid using these, as the real API ensures
that the produced values hold on to their contract and thus can avoid bugs
creeping into code.

[Closure DOM APIs]: https://github.com/google/closure-library/blob/master/closure/goog/dom/safe.js
[XSS]: https://www.google.com/about/appsecurity/learning/xss/
