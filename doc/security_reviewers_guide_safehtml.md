# The Security Reviewer's Guide to safe HTML APIs and Strict Templating

This document serves as a guide to security reviewers of applications that use
safe HTML APIs and/or Strict Contextually Autoescaping Template Systems to
address the risk of XSS vulnerabilities.

These APIs and template system auto-escape modes have two primary goals:

1.  To drastically reduce the likelihood that application developers write code
    that has XSS vulnerabilities, by providing APIs and framework features that
    are inherently secure with respect to XSS.
2.  To drastically reduce the amount of code that needs to be read/reviewed by a
    security reviewer in order to assess an application's code base for XSS.

For background regarding general patterns behind such vulnerability-resistant
(aka "Hardened", for lack of a better term) APIs, see [The Security Reviewer's
Guide to Hardened APIs](security_reviewers_guide.md).


## A brief introduction to safe HTML types and strict templates

This approach to the prevention of XSS is based on the collection of several
concepts, which are briefly introduced in this section.  For a more in-depth
description of this approach and its benefits, refer to the following references:

* [Securing the TangledWeb](http://research.google.com/pubs/archive/42934.pdf),
  CACM 57.9 (2014) --- Externally published article on this approach.


### Strictly contextually autoescaping template systems

A large fraction of XSS bugs arise from template-rendered HTML markup, where a
template substitution applies no, or an incorrect, escaping or sanitization
operation on the interpolated data.  Typically, this manifests itself in
auto-escaping that is suppressed via a template directive (e.g., `<div>{$content
|noAutoescape}</div>`), or incorrect escaping in a template that does not apply
contextual autoescaping (`<a href="{$untrustedUrl}">...`).

A strictly contextually auto-escaping template system,

1.  applies contextual escaping and sanitization to all template substitutions /
    variable bindings, and
2.  completely disallows the use of any template directives that modify or
    disable the automatically inferred, appropriate escaping/sanitization.

The goal of strict contextual escaping is that a template simply cannot result
in XSS vulnerabilities that are due to bugs in the template (there are some
caveats for some implementations however), *nor* due to bugs in code that
supplies data to the template (again, with some caveats, detailed in this
document).

Hence, it is (modulo said caveats) unnecessary to read any template files to
determine if an app has XSS bugs.

### Safe HTML types {#safehtml_types_intro}

As described above, strict templates do not permit data to be inserted into the
template without escaping -- the template *always* escapes.  However, in some
(typically rare) situations (e.g, server-side sanitized HTML mail message
interpolated into gmail frontend template), it is necessary to suppress the
default escaping behavior.

To accommodate this use case, we introduce [security-contract
types](security_reviewers_guide.md#security_contract_types) with properties
relevant to XSS. For example, the `SafeHtml` type essentially carries the
contract that its values have been constructed, sanitized, or otherwise
processed to ensure that no XSS results when this value is rendered as HTML
(e.g, interpolated in PC_DATA context into a template).

Strict templates recognize types such as SafeHtml, SafeUrl, etc, and suppress
automatic escaping and sanitization *iff* a value of such a type is used in a
matching context.

The safe HTML types have implementations in the major languages, and common
libraries to create contract-compliant values. Note also that rendering a strict
contextual template results in a value that satisfies the SafeHtml type
contract.

Most builders for such types have been designed to ensure the type contract at
an API level. However, the types support
[unchecked conversions](security_reviewers_guide.md#unchecked_conversions) to
support unusual/exceptional use cases; their use must be hand-reviewed. The
unchecked conversions are [BUILD-visibility-restricted](http://bazel.build/docs/be/common-definitions.html#common.visibility)
(modulo limitations of the language's deps strictness), and in most cases this
review should already have happened.

### XSS Sinks: DOM API wrappers and UI toolkits

Many DOM APIs are injection prone, and some of them, e.g., `.innerHTML` and
`document.location.href`, are common sources of XSS bugs.

We provide wrapper libraries that expose safe variants (which typically either
accept a plain string that is always runtime-sanitized, or an appropriate
SafeHtml type).  There are compile-time checks to prevent use of the
underlying, injection-prone API.

It is common for libraries and toolkits (e.g, Closure, or GWT) to leak outwards
the injection-prone-ness of the underlying web platform API.  E.g., many
methods/widgets accept plain-string-typed values that are forwarded to a
`.innerHTML` assignment, which makes that widget's API itself injection-prone.

To address this concern, we have refactored libraries to expose safe API
alternatives (again, typically consuming an appropriate SafeHtml type).

The main purpose of this approach is to largely eliminate the use of injection
prone APIs (both web-platform-level and unsafe, legacy framework/library APIs)
from application code: Application code should only be calling
[inherently-secure APIs](security_reviewers_guide.md#inherently_secure_apis)
(which in turn often provide their own safety based on parameter's type
contracts), whose uses don't need to be reviewed.


## Review checklists

The following sections provide detailed checklists on what to look for when
reviewing an app (that uses strict templating and safe HTML types) for XSS.

These checklists are detailed, and have a lot of checks, but each check is
quick and shouldn't result in much work.[^automation]

[^automation]: In addition, many of the checklist items should be automatable.
    Hint hint.

*   Most of the checklist items are of the form "verify that their BUILD rules
    are configured right", or "find all call sites of method
    `FooBar#thingyThing(String)` and review the surrounding code".  The former
    should be quick, and the latter should, in a typical app, come up with an
    empty or very small set of call sites.

*   In most cases, actual code review should require *local reasoning* only.
    I.e., you should not have to backwards-trace control-and-dataflow fan-in
    into callers of the code you're reviewing; it should be possible to
    determine if a call-site of a potentially-dangerous API is actually safe
    only by looking at the surrounding code.  If this is not the case, the code
    is not conforming to [unchecked Safe HTML](http://github.com/google/safe-html-types/blob/master/doc/safehtml-unchecked.md) guidelines

In a time-constrained review, you may not be able to exhaustively check all
items. However, a project that uses safe HTML APIs and strict templates
reasonably comprehensively and largely correctly will still be much less prone
to XSS.

It's worth noting that a fully completed detailed review will result in a high
confidence that covered classes of XSS are absent from the app (caveat: there
are some classes of XSS we haven't been able to address).  In other words,
you're reviewing to establish the absence of the bug, rather than to find some
of the instances of it. Furthermore, there will be high confidence that bugs
*will remain absent* in future code iterations (barring significant developer
mistakes, such as disabling static checks, or introducing un-reviewed,
additional uses of unchecked conversions in an already-whitelisted package).

## Strict contextual autoescaping in Closure Templates

### References

*    [Developer
     Docs](https://developers.google.com/closure/templates/docs/security#strict)

### Review checklist

#### Is strict mode used comprehensively?

There should be no `{namespace}` or `{template}` declarations with an
`autoescape` attribute whose value is `deprecated-noncontextual` (indiscriminate
HTML escaping) or `deprecated-contextual` (contextual escaping, but not strict),
in any of the `.soy` files in the transitive closure of the apps's dependencies.

*    Strict mode is the Closure Templates default, i.e. an explicit `autoescape="strict"` is
     not required (there are likely still lots of them, and presubmits to
     enforce them, from before the switch in the default).
*    Many projects have presubmit rules to prevent addition of new templates
     with non-strict autoescape modes
     This may be useful in large projects that are not fully migrated, to
     prevent regressions due to cut-and-paste.
*    Note that strict mode is "viral", i.e. strict templates can't call
     non-strict ones. However, apps may depend on libraries that have their own
     .soy files, and which use non-strict mode.
*    The JS runtime has a global `@define` flag
     [`goog.soy.REQUIRE_STRICT_AUTOESCAPE`][REQUIRE_STRICT_AUTOESCAPE],
     to globally disable rendering of any non-strict Closure Templates template.  This is
     rarely used, but useful to check for (in the jscompiler flags); extra
     points if a project uses it.
*    There should be a test that compiles all of the project's Closure Templates sources at
     once
     and that reliably reports certain errors.
 
#### Unchecked conversions to SanitizedContent?

Closure Templates has its own equivalent of `SafeHtml` types called `SanitizedContent`. It
comes with its own unchecked conversion functions that create type instances
from plain strings, and their call sites must be reviewed similarly to the call
sites of [SafeHtml unchecked conversions](#review_unchecked_conversions):

*    [com/google/template/soy/data/UnsafeSanitizedContentOrdainer.java][UnsafeSanitizedContentOrdainer]
*    [`soydata.VERY_UNSAFE.ordainSanitizedHtml`][soydata.VERY_UNSAFE.ordainSanitizedHtml]
     (and similar methods for Uris, etc, in the same file).

Note that the Java API is BUILD-visibility restricted (with a fairly large
grandfathered whitelist), the JS one unfortunately is not.

Use of unchecked conversions must comply with the
[Safe HTML unchecked guidelines](safehtml-unchecked.md) (with the goal that uses
of the unchecked conversions can be reviewed based on local reasoning); if not
raise an issue with the project.

Note that common safe HTML types are convertible to SanitizedContent (via
[`soydata.SanitizedHtml.from`][soydata.SanitizedHtml.from]),
so unchecked conversions into the common safe HTML types need to be
[reviewed as well](#review_unchecked_conversions).


### Caveats and Limitations

There are a number of areas where Closure Templates currently falls short of the promise to
completely prevent template-based XSS. It may or may not be practical to review
for these.

#### Script/Style sourced from untrusted source

Closure Templates has no concept equivalent to the [`TrustedResourceUrl` type](#safehtml_types).
I.e. it is possible for template authors to write,

```html
{template .foo}
  <script src="{$scriptUrl}"></script>>
{/template}
```
Closure Templates will sanitize `$scriptUrl` in the same way as a hyperlink (i.e., require a
whitelisted scheme). However, it cannot ensure that the URL actually  points to
a trustworthy resource.

There are plans to introduce a TrustedResourceUrl concept in Closure Templates.

#### DOM XSS in in-line script
For example,

```html
{template .foo}
  <script>
    // ... 
    messageElem.innerHTML = '{$message}';
  </script>
{/template}
```

Closure Templates infers that javascript-string-literal escaping is necessary for the
substitution of `{$message}`.  However, it does not know that the resulting
value will be used as HTML, i.e. no HTML-entity-escaping is done beforehand.

The same concern applies to in-line event handler attributes.

Ideally, an app will use very little inline script -- all script should be
defined in js-compiler-compiled source, where it is subject to [conformance
checks](#closure_safehtml).

In addition, JS is complicated.  There are probably (hopefully pathological) JS
snippets that confuse the contextual escaper.


#### Incorrect/permissive inference/handling of certain attributes

Closure Templates does not always infer that certain contexts have special meaning,
and hence require special sanitization.

*   `<meta http-equiv="refresh" content="0;URL='{$untrusted}'">`
*   "custom" attributes, like jsaction.
*   Concatenated URLs: `<a href="javascript:{$untrusted}"></a>`
*   Substitutions are allowed in element names `<h${level}>`.  Presumably
    `<{$untrusted}>` could cause all sorts of problems.
*   SanitizedContent of kind="attributes" is quite likely problematic.


#### Safe HTML types caveats

Any [caveats related to safe HTML types](#safehtml_caveats) apply here as well.





## Safe HTML types {#safehtml_types}

Safe HTML types are a collection of types, with implementations in C++, Java, JS
(Go, Python TBD), as well as protobuf, that represent string-like values that
are known to satisfy a HTML-related security contract.  For example, type
`SafeHtml` represents strings that are, by construction or provenance, known to
be safe to use in a HTML context (i.e., assigned to `.innerHTML`, or
interpolated into a HTML document in "inner HTML" (`PC_DATA`) context.

A brief, and simplified, summary of each type:

*    `SafeHtml`: String that is safe to use in HTML context in DOM APIs and HTML
     documents.
*    `SafeScript`: JavaScript code that is safe to use as the content of an HTML
     script element.
*    `SafeStyle`: Sequence of CSS declarations safe to use in style context in
     an HTML document or in DOM APIs.
*    `SafeStyleSheet`: A complete CSS style sheet, safe to use in style context
     in an HTML document or DOM APIs.
*    `SafeUrl`: String that is safe to use in URL contexts in DOM APIs and HTML
     documents, where the URL context does not refer to a resource that loads
     code.
*    `TrustedResourceUrl`: String that is safe to use in all URL contexts in DOM
     APIs and HTML documents; even where the referred-to resource is interpreted
     as code, e.g., as the src of a script element.


### Security notes

*   Public (i.e, not visibility-restricted) APIs that produce values of these
    types are generally designed to uphold the type contract without the need
    for human review. For example, uses of
    [`c.g.common.html.types.SafeHtmlBuilder`][SafeHtmlBuilder]
    don't need to be manually reviewed.

    There are however exceptions to this rule, which are detailed below.
*   There are framework-specific equivalents to these general-purpose types, for
    Closure Templates
    ([`SanitizedContent`][SanitizedContent])
    and GWT
    ([`c.g.gwt.safehtml.shared.SafeHtml`][c.g.gwt.safehtml.shared.SafeHtml]
    etc).
    There are conversion functions between these and the common SafeHtml
    types.

### Review Checklist {#safehtml_types_review}

#### Review unchecked conversions

To support use cases where existing, general purpose APIs for creating values of
safe HTML types (such as, `SafeHtmlBuilder`) are not sufficient, there are
unchecked conversions that create a type instance from an arbitrary,
unconstrained string.

All uses of such unchecked conversions must be security reviewed.  To make
meaningful review feasible, all uses should comply with the
[Safe HTML unchecked guidelines](safehtml-unchecked.md) (the primary goal of the
guidelines is to ensure security reviews are possible based on local reasoning
about the code surrounding the use of the unchecked conversions).


A typical app should have few, if any, uses of unchecked conversions in
application-specific code; there are existing uses
in common libraries/frameworks



### Caveats and limitations {#safehtml_caveats}

#### `TrustedResourceUrl` builders {#trustedresourceurl_caveats}

The `TrustedResourceUrl` type has a [builder][TrustedResourceUrlBuilder]
that allows a `TrustedResourceUrl` to be constructed as the concatenation of
known-trustworthy strings (compile-time constants and flags).  This does not
actually ensure the type's contract, namely that the resource the URL *points
to* is trustworthy (i.e., we're using the fact that the URLs value is
application-controlled as an (approximate) proxy for the trustworthiness of the
resource the URL points to).

In principle, nothing prevents a programmer from writing,
```java
  TrustedResourceUrl jsUrl =
      TrustedResourceUrlBuilder("http://evil.org/evil.js").build();
```

However, the API does ensure that the value of the URL constructed reflects
programmer intent.


#### Subversion of type encapsulation

Safe HTML types are implemented as simple wrappers for string, i.e., classes
with a single string-typed field. As such one can create type instances that
violate their contract through use of reflection or casts. This sort of thing
should never happen in our codebase

#### Protobuf encapsulation, reflection and deserialization

Analogous to their language-specific equivalents, protobuf representations of
these types are simple messages with a single string-typed field.  However,
proto fields are inherently public.  To prevent ad-hoc assignment to these
intended-to-be-private message fields, we gave them purposely unwieldy names:

```proto
message SafeHtmlProto {
  // IMPORTANT: Never set or read this field, even from tests, it is private.
  // ...
  optional string private_do_not_access_or_else_safe_html_wrapped_value = 2
      [ctype=CORD, (datapol.semantic_type) = ST_CONTENT_DEPENDENT];
}
```


Protobufs support reflection, and application code might reflectively modify a
SafeHtmlProto's "internal" field.

Values that violate their type contract (and hence could cause vulnerabilities)
can result from deserializing protobufs whose serialized form originated from an
untrusted source (e.g., sent from an external client to a server), or have been
mucked with intentionally by the programmer.  We unfortunately currently don't
have any practical way to detect/prevent this.


## Safe Web Platform and Closure APIs {#closure_safehtml}

There are a (large-ish) number of DOM APIs that are injection-prone (i.e.
consume strings that will be parsed and interpreted in some context, and if
supplied with attacker-controlled values, can result in vulnerabilities).

Furthermore, the Closure library (and commonly used libraries built on top of
it) contains many APIs that forward strings to such injection-prone web platform
APIs, and hence are themselves injection-prone. 

To address this, we have:

1.  Created safe wrapper APIs for injection-prone APIs.  These wrappers either
    run-time sanitize their argument (if possible) or consume an appropriate
    security-contract type (`SafeHtml`, `SafeUrl`, etc).
2.  Created [JSConformance](http://github.com/google/closure-compiler/wiki/JS-Conformance-Framework) rules that disallow use of the
    underlying injection-prone platform APIs, except in white-listed usages
    (such as, inside their corresponding safe wrapper).
3.  Refactored Closure to add safe alternatives to injection-prone Closure
    APIs.
4.  Created conformance rules that disallow use of the injection-prone legacy
    APIs.  In addition, there is a global `@define` flag that disables such APIs
    (causing a runtime exception if they're called on a code path not flagged
    by the static conformance check). See
    [legacyconversions.js][legacyconversions.js].



### Review Checklist

#### Safe HTML types and unchecked conversions

This approach heavily relies on safe HTML types.  In particular, use of
unchecked conversions must be reviewed.  See [safe HTML types](#safehtml_types).

#### Jscompiler flags and conformance configs

TODO

*   conformance configs

*   should have strict type checking etc

*   @define for legacy conversions

*   where to look (js_binary, MSS options class)


#### Custom whitelists


### Caveats and Limitations

#### Limitations of static conformance checking

JavaScript is harder to analyze than other languages with existing tools
so checking is not bulletproof.

#### WIP

Work on this aspect is still in progress.  As of 04/2015, Closure has been
largely refactored to be free of arbitrary `.innerHTML` assignments, and any
external APIs that flow into `.innerHTML` have been augmented with safe
alternatives.  However, there's a rather large long tail of injection-prone APIs
that still needs to be worked on.

#### Safe HTML types caveats

Any [caveats related to safe HTML types](#safehtml_caveats) apply here as well.



[REQUIRE_STRICT_AUTOESCAPE]: https://github.com/google/closure-library/blob/831f9eb5c8c739e51315c2e2743ea9c623eae2d8/closure/goog/soy/soy.js#L31-L40
[SafeHtmlBuilder]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/SafeHtmlBuilder.html
[TrustedResourceUrlBuilder]: https://static.javadoc.io/com.google.common.html.types/types/0.0/com/google/common/html/types/TrustedResourceUrlBuilder.html
[UnsafeSanitizedContentOrdainer]: https://github.com/google/closure-templates/blob/master/java/src/com/google/template/soy/data/UnsafeSanitizedContentOrdainer.java
[soydata.VERY_UNSAFE.ordainSanitizedHtml]: https://github.com/google/closure-templates/blob/6655c629607116862f4862d705ecfccdedd7a790/javascript/soyutils_usegoog.js#L453-L467
[soydata.SanitizedHtml.from]: https://github.com/google/closure-templates/blob/6655c629607116862f4862d705ecfccdedd7a790/javascript/soyutils_usegoog.js#L166-L191
[SanitizedContent]: https://github.com/google/closure-templates/blob/master/java/src/com/google/template/soy/data/SanitizedContent.java
[c.g.gwt.safehtml.shared.SafeHtml]: https://github.com/gwtproject/gwt/blob/c9c32256f6ffb8ca69f50693a06b91bc25331fef/user/src/com/google/gwt/safehtml/shared/SafeHtml.java
[legacyconversions.js]: https://github.com/google/closure-library/blob/master/closure/goog/html/legacyconversions.js



