# Safe HTML Types Overview



The "safe HTML types" are simple wrappers for strings, and represent values
that are known -- by construction or via escaping or sanitization -- to be safe
to use in various HTML contexts and with various DOM APIs. For example, values
of type SafeHtml can be used in "inner HTML" (PC_DATA) contexts in an HTML
template, or assigned to node.innerHTML, without causing untrusted script
execution; similarly, values of type TrustedResourceUrl can be used as the src
attribute of a script element, and so forth.

Implementations exist in multiple languages. Instances of the types can
be converted to/from protocol message format, and thus propagated across
languages and systems.

Consumers of these types include template systems like
[Closure Templates](https://developers.google.com/closure/templates/docs/security), and
[Closure DOM APIs][Closure DOM APIs].
End-to-end type safety from certain classes of vulnerabilities, principally
[cross-site scripting (XSS)](http://www.google.com/about/appsecurity/learning/xss/), is the goal.


## Types

A brief, and simplified, summary of each type:

* **SafeHtml**: String that is safe to use in HTML contexts in DOM APIs and
HTML documents.
* **SafeScript**: JavaScript code that is safe to use as the content of an
HTML script element.
* **SafeStyle**: Sequence of CSS declarations safe to use in style contexts
in an HTML document or in DOM APIs.
* **SafeStyleSheet**: A complete CSS style sheet, safe to use in style
contexts in an HTML document or DOM APIs.
* **SafeUrl**: String that is safe to use in URL contexts in DOM APIs and
HTML documents, where the URL context does not refer to a resource that loads
code.
* **TrustedResourceUrl**: String that is safe to use in all URL contexts in
DOM APIs and HTML documents; even where the referred-to resource is interpreted
as code, e.g., as the
[src](https://developer.mozilla.org/en/docs/Web/HTML/Element/script#attr-src)
of a [script](https://developer.mozilla.org/en/docs/Web/HTML/Element/script)
element.

## Type creation

The following C++ example creates a TrustedResourceUrl from a compile-time
constant:

```cpp
TrustedResourceUrl url = TrustedResourceUrl::FromConstant(
    "http://www.google-analytics.com/analytics.js");
```

The compiler will enforce that `FromConstant()` is only called with a compile-
time constant. Since the constant has to be under programmer control,
TrustedResourceUrl could then be used as a URL to load JavaScript in one of our
pages. (If the programmer passed "//nongoogledomain.com/script.js" as a
constant we'd still be in trouble. The important distinction is that this API
is guaranteed to reflect programmer intent.)

The following JavaScript example creates a SafeUrl from a string of unknown
provenance:

```js
var safeUrl = goog.html.SafeUrl.sanitize(untrustedUrl);
```

`sanitize()` will check at runtime that the string represents a URL scheme
which cannot execute JavaScript or launch an external application; so, for
example, `javascript:` would not be allowed. The SafeUrl could then be used as
a URL from which to display an image (as the src attribute in an img element);
but not as a URL from which to load code, unlike a TrustedResourceUrl.

The following Java example creates a SafeHtml representing an `a` element, from
a variety of values, while using a mix of runtime and compile-time checks.

```java
SafeHtml safeHtml = new SafeHtmlBuilder("a")
    .setSrc(SafeUrl.sanitize(untrustedUrl))
    .setStyle(SafeStyle.fromConstant("color:red;"))
    .setClass("user-link")
    .build();
```


## Protocol buffer conversion

In order to maintain end-to-end type-safety, the protocol message
representation of the types is opaque; its fields are never to be set or read
directly. There is an API to convert to and from protocol buffer. For example:


```cpp
// C++ backend. Construct a URL referring to trusted content and convert to
// protocol buffer.
TrustedResourceUrl trusted_url = TrustedResourceUrl::FromConstant(
    "https://ssl.gstatic.com/app.js");
// Serialize to a protocol buffer.
TrustedResourceUrlProto proto_to_send = trusted_url.ToProto();
```

```java
// Java frontend. Read the URL from the protocol buffer; the type contract for
// URL received from backend guarantees that it refers to a trusted resource.
TrustedResourceUrl url = TrustedResourceUrls.fromProto(received_proto);
```

## Unchecked conversion from string {#unchecked}

The public API of these types is expected to be sufficiently flexible for the
majority of cases where an instance needs to be created. However, some
scenarios where we'd like to create instances of these types cannot reasonably
be supported with a generic API. An example of such a scenario is transforming
the SafeHtml emitted by a sanitizer.

For such cases it is possible to use an unchecked conversion from string to
a safe HTML type. Use of unchecked conversions should go through a security
review and access to the conversion functions is generally controlled through
BUILD visibility. It is important that uses of unchecked conversions are
designed such that a security reviewer can readily verify that converted values
indeed satisfy the claimed type contract.

If you think you need to use an unchecked conversion, please read the
[safe HTML unchecked conversions guidelines](safehtml-unchecked.md).


## Use in tests

In case the API of the types is impossible to use in a test, or just too
inconvenient, then you can use conversion functions which take a string and
return one of the safe HTML types. These are exported via a
`testonly` BUILD target and have names like
[`newSafeHtmlForTest`][HtmlConversions.newSafeHtmlForTest].

However, we recommend you avoid using these, as the real API ensures that the
produced values hold on to their contract and thus can avoid bugs creeping into
code.





[HtmlConversions.newSafeHtmlForTest]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/testing/HtmlConversions.java#newSafeHtmlForTest
[Closure DOM APIs]: https://github.com/google/closure-library/blob/master/closure/goog/dom/safe.js



