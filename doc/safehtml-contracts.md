# Security contracts for Safe HTML Types

[TOC]

This page contains the security contract that each Safe HTML Type satisfies. See
[Safe HTML Types](safehtml-types.md) for an overview.

## SafeHtml {#safehtml}

A SafeHtml is a string-like object that carries the security type contract that
its value as a string will not cause untrusted script execution when evaluated
as HTML in a browser.

Values of this type are guaranteed to be safe to use in HTML contexts, such as
assignment to the innerHTML DOM property, or interpolation into an HTML template
in HTML PC_DATA context, in the sense that the use will not result in a
Cross-Site-Scripting vulnerability.

## SafeScript {#safescript}

A SafeScript is a string-like object which represents JavaScript code that
carries the security type contract that its value, as a string, will not cause
execution of unconstrained attacker controlled code (XSS) when evaluated as
JavaScript in a browser.

A SafeScript's string representation can safely be interpolated as the content
of a script element within HTML. The SafeScript string should not be escaped
before interpolation.

Note that the SafeScript might contain text that is attacker-controlled but that
text should have been interpolated with appropriate escaping, sanitization
and/or validation into the right location in the script, such that it is highly
constrained in its effect (for example, it had to match a set of allowlisted
words).

In order to ensure that an attacker cannot influence the SafeScript value, a
SafeScript can only be instantiated from compile-time constant string literals
or security-reviewed unchecked conversions, but never from arbitrary string
values potentially representing untrusted user input.

In this case, producers of SafeScript must ensure themselves that the SafeScript
does not contain unsafe script. Note in particular that `<` is dangerous, even
when inside JavaScript strings, and so should always be forbidden or JavaScript
escaped in user controlled input. For example, if
`</script><script>evil</script>` were interpolated inside a JavaScript string,
it would break out of the context of the original script element and `evil`
would execute. Also note that within an HTML script (raw text) element, HTML
character references such as `&lt;` are not allowed. See
[Restrictions for contents of script elements](http://www.w3.org/TR/html5/scripting-1.html#restrictions-for-contents-of-script-elements).

## SafeStyle {#safestyle}

A SafeStyle is a string-like object that represents a sequence of CSS
declarations (`property_name1: property_value1; property_name2: property_value2;
...`) and that carries the security type contract that its value will not cause
untrusted script execution (XSS) when evaluated as CSS in a browser.

SafeStyle's string representation can safely be:

-   Interpolated as the content of a quoted HTML style attribute. However, the
    SafeStyle string must be HTML-attribute-escaped before interpolation.
-   Interpolated as the content of a `{}`-wrapped block within a stylesheet. `<`
    characters in the SafeStyle string must be CSS-escaped before interpolation.
    The SafeStyle string is also guaranteed not to be able to introduce new
    properties or elide existing ones.
-   Interpolated as the content of a `{}`-wrapped block within an HTML `<style>`
    element. `<` characters in the SafeStyle string must be CSS-escaped before
    interpolation.
-   Assigned to the style property of a DOM node. The SafeStyle string should
    not be escaped before being assigned to the property.

In addition, values of this type are composable, i.e. for any two SafeStyle
values `style1` and `style2`, `style1.style() + style2.style()` is itself a
value that satisfies the SafeStyle type constraint.

In addition to ensuring that the SafeStyle itself does not contain unsafe CSS,
producers of SafeStyle must adhere to the following rules:

-   A SafeStyle may never contain literal angle brackets. Otherwise, it could be
    unsafe to place a SafeStyle into the contents of a `<style>` element where
    it can't be HTML escaped, see
    [Using character escapes in markup and CSS](http://www.w3.org/International/questions/qa-escapes).
    For example, if the SafeStyle containing `font: 'foo
    </style><script>evil</script>'` were interpolated within a `<style>` tag,
    this would then break out of the style context into HTML.
-   A SafeStyle value cannot end in a property value or property name context.
    For example, a value of `background:url(\"` or `font-` does not satisfy the
    SafeStyle contract. This rule is enforced to ensure composability:
    concatenating two incomplete strings that themselves do not contain unsafe
    CSS can result in an overall string that does. For example, if
    `javascript:evil())\"` is appended to `background:url(\"`, the resulting
    string may result in the execution of a malicious script.
-   A SafeStyle may, however, contain literal single or double quotes (for
    example, in the `content` property). Therefore, the entire style string must
    be escaped when used in a style attribute.

The following example values comply with this type's contract:

-   `width: 1em;`
-   `height:1em;`
-   `width: 1em;height: 1em;`
-   `background:url('http://url');`

In addition, the empty string is safe for use in a CSS attribute.

The following example values do NOT comply with this type's contract:

-   `background: red` --- missing a trailing semi-colon
-   `background:` --- missing a value and a trailing semi-colon
-   `1em` --- missing an attribute name, which provides context for the value

See also [http://www.w3.org/TR/css3-syntax/](http://www.w3.org/TR/css3-syntax/).

## SafeStyleSheet {#safestylesheet}

A SafeStyleSheet is a string-like object which represents a CSS style sheet and
that carries the security type contract that its value, as a string, will not
cause untrusted script execution (XSS) when evaluated as CSS in a browser.

A SafeStyleSheet's string representation can safely be interpolated as the
content of a style element within HTML. The SafeStyleSheet string should not be
escaped before interpolation.

Producers of SafeStyleSheet must ensure themselves that the SafeStyleSheet does
not contain unsafe script. Note in particular that `<` is dangerous, even when
inside CSS strings, and so should always be forbidden or CSS-escaped in user
controlled input. For example, if `</style><script>evil</script>` were
interpolated inside a CSS string, it would break out of the context of the
original style element and `evil` would execute. Also note that within an HTML
style (raw text) element, HTML character references such as `&lt;` are not
allowed. See
[Restrictions for contents of script elements](http://www.w3.org/TR/html5/scripting-1.html#restrictions-for-contents-of-script-elements).
(Similar considerations apply to the style element.)

## SafeUrl {#safeurl}

A SafeUrl is a string-like object that is safe to use where URLs are expected in
DOM APIs and HTML documents.

A SafeUrl is a string-like object that carries the security type contract that
its value as a string will not cause untrusted script execution when evaluated
as a hyperlink URL in a browser.

Values of this type are guaranteed to be safe to use in URL/hyperlink contexts,
such as assignment to URL-valued DOM properties, in the sense that the use will
not result in a Cross-Site-Scripting vulnerability. Similarly, SafeUrls can be
interpolated into the URL context of an HTML template (e.g., inside a `href`
attribute). However, appropriate HTML-escaping must still be applied.

Note that this type's contract does not imply any guarantees regarding the
resource the URL refers to. In particular, SafeUrls are not safe to use in a
context where the referred-to resource is interpreted as trusted code, e.g., as
the src of a script tag. For safely loading trusted resources, use the
TrustedResourceUrl type.

## TrustedResourceUrl {#trustedresourceurl}

A TrustedResourceUrl is a string-like object representing a URL referencing the
application’s own, trusted resources. It can be used to safely load scripts, CSS
and other sensitive resources without the risk of untrusted code execution. For
example, it is unsafe to insert a plain string in a

`<script src="..."></script>`

context since the URL may be originating from untrusted user input and the
script it is pointing to may thus be controlled by an attacker. It is, however,
safe to use a TrustedResourceUrl since its value is known to never have left
application control.

In order to ensure that an attacker cannot influence the TrustedResourceUrl
value, a TrustedResourceUrl can only be instantiated from compile-time constant
string literals, command-line flags (if really necessary; see https://go/totw/45) or a
combination of the two, but never from arbitrary string values potentially
representing untrusted user input.

Note that TrustedResourceUrls can also use absolute paths (starting with '/')
and relative paths. This allows the same binary to be used for different hosts
without hard-coding the hostname in a string literal or flag value.
