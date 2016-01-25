# Guidelines for Use of Unchecked Conversions of Security Contract Types


**Audience note:** This document is for developers who think they need to use
unchecked conversions, and for security engineers who are reviewing new uses of
such conversions.


## Overview
Security contract types, such as `SafeHtml`, let us secure APIs against common
vulnerabilities such as [XSS](http://www.google.com/about/appsecurity/learning/xss/) and [SQL injection]
(http:https://www.owasp.org/index.php/SQL_Injection). When used correctly, they help Google's security
team make sure our applications work on behalf of our users, or at worst, fail
safe; but misuse makes it next to impossible for reviewers to check program
safety.

The APIs and their related security contract types are designed such that in a
typical application, the vast majority of instances of such types can be
created using inherently safe APIs (uses of which do not need to be manually
security reviewed). For example,
[F1][F1] uses a SQL Builder interface
to create SQL queries that are guaranteed to
not be subject to SQL injection vulnerabilities. Similarly,
[safe HTML types](safehtml-types.md)
come with various inherently safe builders and factory methods, as does
[GWT's SafeHtml type](http:http://www.gwtproject.org/doc/latest/DevGuideSecuritySafeHtml.html).

There are however scenarios that cannot be expressed using inherently safe
APIs, such as available builders of security contract types. To accommodate
such use cases, the inherently safe API is accompanied by an API that consumes
an arbitrary string without any validation; typically this is embodied as a
factory method that performs an *unchecked conversion* from an arbitrary plain
string to an instance of the desired security contract type.

Obviously, such unchecked conversions must only be used if it can be
established through careful security code review that for all possible program
states, the value supplied to the conversion does in fact satisfy the type
contract, or it is otherwise known from context that this use cannot result in
security vulnerabilities.

The primary goal of our efforts to create inherently safe APIs is to reduce the
amount of code that could potentially harbor security bugs and hence needs
manual security review. It also allows us to perform reviews of code that does
need manual review with a high degree of confidence. With that in mind:

* **Unchecked conversions should be used rarely, and only if strictly necessary;
that is, if inherently safe APIs cannot be used.**
* **Use of unchecked conversions should be structured such that it is readily
apparent to a security reviewer that the use is indeed safe, and such that it
is unlikely that future code modifications result in later introduction of
security bugs.**

The remainder of this documents presents guidelines for appropriate use of
unchecked conversions, along with specific examples.

## Guidelines

Generally, we BUILD visibility-restrict access to unchecked
conversion APIs, and require review and approval before new call sites are
whitelisted. This review ensures adherence to these guidelines, and
establishes correctness from a security perspective.

### Use unchecked conversions only if strictly necessary

To make manual review tractable, the number of call sites of unchecked
conversions (each of which needs manual code review) must be kept as low as
possible.  Therefore, unchecked conversions must not be used if the use case
can be expressed in terms of an available inherently safe API.

For example, the following uses of
[`SafeHtmlUtils.fromTrustedString`][SafeHtmlUtils.fromTrustedString]
(GWT's unchecked conversion from string to GWT's [`SafeHtml`][SafeHtml]
type) are inappropriate in this sense:

```java
private static final SafeHtml POSITIVE_INFINITY_SIGN =
  SafeHtmlUtils.fromTrustedString(
    "<span style=\"font-size:130%;\">\u221e</span>");
```

Here, [`SafeHtmlUtils.fromSafeConstant`][SafeHtmlUtils.fromSafeConstant]
should be used; in this context, it does exactly the same thing, but is
accompanied by a static check that verifies that it is supplied with safe,
compile-time-constant HTML markup, and hence does not need manual security
review.

Another example:

```java
builder.append(SafeHtmlUtils.fromTrustedString("<span "
  + "class=\'promo-image\' " + "title=\' "
  + candidateType.getTitle() + "\' id=\'gwt-debug-"
  + candidateType.getTitle() + "\'>"));
```

This code should be rewritten using a
[`SafeHtmlTemplates`]
(http://www.gwtproject.org/doc/latest/DevGuideSecuritySafeHtml.html#SafeHtmlTemplates)
interface (which arguably is also more readable).  Note that this code may
harbor an XSS if `candidateType.getTitle()` is user/attacker-controlled.

### Use unchecked conversions only within inherently safe APIs

Unchecked conversions should be used only within self-contained,
special-purpose libraries or packages that themselves expose an inherently safe
API:

* It must be possible to ascertain that an unchecked conversion used in such a
library/package is guaranteed to produce type-contract-compliant values solely
based on inspection of the library (and in some cases code the library depends
on). The safety of such unchecked conversions **must not** hinge on code that
depends on the library (i.e., the library's clients' code). This guideline
allows for one-time review of the code surrounding the unchecked
conversion, and avoids the need for (unscalable and difficult to
track/ensure) manual code review of all callers of the library.

* Unchecked conversions must only be used in libraries/packages dedicated to a
specific purpose. In particular, they must not be used throughout application
code. This guideline ensures that uses of unchecked conversions are limited as
much as possible. In our codebase, unchecked conversions are generally provided
by separate, BUILD visibility-restricted targets. The targets whitelist is
intended to be kept as narrow as possible to discourage introduction of
inappropriate uses of unchecked conversions.

* Unchecked conversions may also be used in code that is generated by a
security-reviewed code generator designed to only generate code that upholds
type contracts.  Generated code should use a special-purpose conversion
function separate from unchecked conversions intended to be used in
hand-written code.

#### The good: Examples of appropriate uses of unchecked conversions

* TODO: good public examples

#### The bad: (Hypothetical) examples of inappropriate use

**Uses that expose a not-inherently safe API**

For example, this would be an inappropriate use of the unchecked conversion to
the `SafeHtml` type:

```java
public final class HtmlUtils {
  /**
   * Creates a <div> element styled with the provided class, and the provided
   * HTML content.  Important:  The HTML content must be safe to render.
   */
  public SafeHtml createDiv(String contentHtml, String class) {
    return UncheckedConversions.safeHtmlFromStringKnownToSatisfyTypeContract(
      "<div class=\" + class + "\">" + contentHtml + "</div>");
  }
}
```

This (public) method does not provide an inherently safe API, and it cannot be
demonstrated by inspection of this class alone that the expression passed to
the unchecked conversion always evaluates to a value that satisfies the
`SafeHtml` type contract.  It is irrelevant that the method's
documentation states that `contentHtml` must be safe to render; we
have ample experience that requirements in documentation are often not heeded.
To ensure absence of security bugs, each use of this method would have to be
manually security reviewed, defeating the purpose of using
`SafeHtml` types in the first place.

As noted above, many uses of GWT's
[`SafeHtmlUtils.fromTrustedString`][SafeHtmlUtils.fromTrustedString]
unchecked conversion are inappropriate, both because they could be expressed
using an inherently safe API such as `SafeHtmlTemplates`, and because they are
not contained in dedicated packages but rather dispersed across application
code.

### Unchecked conversions in legacy code and code under refactoring

During ongoing refactorings to adopt use of security contract types in an
existing codebase, it is often convenient or necessary to use unchecked
conversions.

For example, if a web frontend's HTML rendering is converted to the use of a
[strict autoescaping template system]
(http:strict-template-systems.md), code
that supplies HTML markup to the template system must be changed to provide it
in the form of a security contract type, such as
`c.g.common.html.types.SafeHtml` for jslayout, or `SanitizedContent`
for Closure Templates. If the value supplied originates from "far away", it may not be
feasible to immediately thread through the SafeHtml type to the value's origin.
In this case, it may be necessary to temporarily use unchecked conversions that
are not considered appropriate with respect to these guidelines.  Please
refactor away such temporary uses, ideally before launch, and get a security
review for any unchecked conversions that remain.


### In tests, use a test-only unchecked conversion

Unchecked conversions may be used in tests where necessary or convenient (since
this cannot result in vulnerabilities in production code). However, to
distinguish test and non-test use, we typically provide a for-test-only
unchecked conversion in a [`testonly=1`](https://bazel.io/docs/be/common-definitions.html#common.testonly) target,
e.g.,
[`c.g.c.html.types.testing.HtmlConversions.newSafeHtmlForTest`][HtmlConversions.newSafeHtmlForTest]
and [`goog.html.testing.newSafeHtmlForTest`]
(https://github.com/google/closure-library/blob/c271d80cc69c926994f42c275ff7b9169c4c459a/closure/goog/html/testing.js#L38).

Note that even `testonly` unchecked conversions should only be used only if an
inherently safe API is not convenient.


[F1]: http://research.google.com/pubs/pub38125.html

[HtmlConversions.newSafeHtmlForTest]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/testing/HtmlConversions.html#newSafeHtmlForTest
[SafeHtmlUtils.fromTrustedString]: http://static.javadoc.io/com.google.gwt/gwt-user/2.7.0/com/google/gwt/safehtml/shared/SafeHtmlUtils.html#fromTrustedString%28java.lang.String%29
[SafeHtml]: http://static.javadoc.io/com.google.gwt/gwt-user/2.7.0/com/google/gwt/safehtml/shared/SafeHtml.html
[SafeHtmlUtils.fromSafeConstant]: http://static.javadoc.io/com.google.gwt/gwt-user/2.7.0/com/google/gwt/safehtml/shared/SafeHtmlUtils.html#fromSafeConstant%28java.lang.String%29


