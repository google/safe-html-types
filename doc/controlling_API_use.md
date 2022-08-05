# Controlling API Use


## Overview

To prevent the introduction of specific classes of security vulnerabilities,
security team have developed
[inherently secure APIs](security_reviewers_guide.md#inherently_secure_apis)
whose use by design cannot result in such vulnerabilities. To accommodate
exceptional use cases that cannot be expressed in terms of the secure APIs,
[unconstrained, potentially insecure APIs](security_reviewers_guide.md#unchecked_conversions)
are available.

For this approach to be effective, all uses of such potentially insecure APIs
must be security reviewed by a human reviewer. In the security teams's
experience, it is insufficient to rely on documentation and developer education
alone to avoid the introduction of inappropriate API use. See the
[Appendix](#appendix_a) for some examples.

This document describes a number of patterns we have used to restrict and
control inappropriate use of such APIs.

## BUILD visibility

Often, an API can be structured such that the potentially insecure API is
factored out into a separate BUILD target.

The BUILD target exposing the potentially insecure API is BUILD visibility-
restricted, enforcing the requirement that at least the first use from a
specific package is security reviewed. This review can then ensure that the new
use adheres to [guidelines](safehtml-unchecked.md) for appropriate use.

A common use case requires that a type's public API only exposes builders and
factory methods whose implementations strongly guarantee that only
contract-compliant instances are created. For exceptional use cases that cannot
be expressed in terms of one of the "standard" builders and factory methods, we
provide an unconstrained "backdoor" factory method.

In contrast to the inherently secure builders, this backdoor factory method
essentially accepts as its argument(s) the internal state of the to-be-created
type instance, and hence fully relies on calling code to ensure the resulting
instance's type contract. The implementation of the backdoor method typically
calls a private constructor of the type, to which it has access through
language-level visibility mechanisms.

As an example, consider the implementation of the
[SafeHtml type](safehtml-types.md) in various languages.



###### C++

###### Go

###### Java

*   The [type's constructor][SafeHtml.SafeHtml(String)] is package-private, and
    as such visible to the type's builders and factory methods in the same
    package (e.g., [`SafeHtmls`][SafeHtmls.create].
*   The potentially unsafe "backdoor" API (e.g.,
    [`UncheckedConversions`][UncheckedConversions.safeHtmlFromStringKnownToSatisfyTypeContract])
    lives in the same package, and hence has access to the type's package
    private constructor.
*   However, the class exposing the backdoor API lives in a separate BUILD
    target which is BUILD visibility-restricted.

###### JavaScript

The approach for JS is essentially the same as Java, except that we add some
indirection to account for the less stringent static visibility checking in
JSCompiler. See
[`SafeHtml.createSafeHtmlSecurityPrivateDoNotAccessOrElse`][SafeHtml.createSafeHtmlSecurityPrivateDoNotAccessOrElse].

###### TypeScript

The approach for TS is essentially the same as JavaScript.

## Language-specific mechanisms

In some cases, restricting access via BUILD visibility is not possible. For
instance, if the method is in a public API, its callers often cannot be
refactored to the new BUILD target all at once. Language-specific static
analysis frameworks can provide a way to restrict access to individual methods
in an existing class or package.



###### C++

The C++ compiler currently does not support custom compiler
errors.

###### Go

### nogo-check

Go users can use https://github.com/bazelbuild/rules_go/blob/master/go/nogo.rst. Nogo can be used in the following scenarios:

*   Controlling the use of a package in the standard library.
*   Controlling the use of an individual method in a package.
*   In addition, Nogo can be used to enforce complex constraints on source code
    through custom checker plugins.

###### Java

### Error Prone-checked restrictions

In Java, we can use [Error Prone](https://github.com/google/error-prone) to restrict call sites
of specific methods.

#### @RestrictedApi

*   Mark the methods you want to prohibit with the
    [`RestrictedApi`](https://errorprone.info/api/latest/com/google/errorprone/annotations/RestrictedApi.html)
    annotation.

    In the annotation, you have to provide a short explanation why the API is
    restricted and a link to further background documentation. This should
    include which APIs you want developers to use instead and a point of
    contact.

*   Create a BUILD-visibility restricted allowlist annotation for existing
    callers

    Add this class to the `allowlistWithWarningAnnotations` parameter on the
    `@RestrictedApi` annotation. You can add this annotation to methods or
    classes surrounding existing callers. Error Prone will emit a warning, but
    allow compilation to succeed.

*   Optionally, add a separate allowlist annotation for uses which have been
    reviewed to the `allowlistAnnotations` attribute of `@RestrictedApi`. Call
    sites in classes or functions marked with this annotation will be allowed
    without a warning.

    It can be a useful to use separate annotations to distinguish uses that have
    actually been reviewed, from existing, unreviewed, potentially vulnerable
    legacy uses

*   If you cannot add annotations to call sites, you can instead allowlist based
    on the file path of the caller. Specify a regular expression matching files
    in which calls are allowed in the `allowedPath` parameter.

#### @RestrictedInheritance

Similarly, `@RestrictedInheritance` can be used to ensure that all
implementations of a given interface are security-reviewed.

#### Custom checkers

If adding an annotation is not possible, e.g. when restricting an API part of
the JDK, you can develop a custom Error Prone check to restrict calls.

### Guice bindings

https://github.com/google/guice/wiki/RestrictedBindingSource can be used to restrict which Guice modules
are allowed to provide a particular binding. This can be used to prevent
developers from providing insecure implementations for security-relevant
bindings.

###### JavaScript

###### TypeScript

###### Python

Python also does not support custom compiler errors.

## Appendix: Why documentation is insufficient {#appendix_a}

It's insufficient to rely on documentation to prevent inappropriate/incorrect
use of APIs. For example:

*   Google Web Toolkit includes a method,
    [`SafeHtmlUtils.fromTrustedString`][SafeHtmlUtils.fromTrustedString], whose
    uses can result in XSS vulnerabilities and hence need to be security
    reviewed. This requirement has been widely ignored in our codebase: A large
    number of uses (> 1000s) have been introduced, most of which are
    inappropriate, and some of which have resulted in actual security bugs
*   It appears to even be insufficient to use purposely cumbersone method names
    like `do_not_access_or_else...`. One would think that no reasonable person
    would inappropriately use those methods.
    *   We used this approach for intended-to-be-private fields in protocol
        buffers, but found that within weeks of their introduction, several uses
        had appeared in our codebase.
    *   Similarly, a GWT class whose name clearly specifies that it should only
        be used in generated code nevertheless has uses in our codebase.

[SafeHtml.SafeHtml(String)]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/SafeHtml.html#SafeHtml(String)
[SafeHtmls.create]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/SafeHtmls.html#create
[UncheckedConversions.safeHtmlFromStringKnownToSatisfyTypeContract]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/UncheckedConversions.html#safeHtmlFromStringKnownToSatisfyTypeContract
[SafeHtml.createSafeHtmlSecurityPrivateDoNotAccessOrElse]: https://github.com/google/closure-library/blob/98e9bc2e3034e5d37af8a024631c7f823d58c87a/closure/goog/html/safehtml.js#L615
[uncheckedconversions visibility]: https://github.com/google/closure-library/blob/98e9bc2e3034e5d37af8a024631c7f823d58c87a/closure/goog/html/uncheckedconversions.js#L28
[SafeHtmlUtils.fromTrustedString]: http://static.javadoc.io/com.google.gwt/gwt-user/2.7.0/com/google/gwt/safehtml/shared/SafeHtmlUtils.html#fromTrustedString%28java.lang.String%29
