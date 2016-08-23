// **** GENERATED CODE, DO NOT MODIFY ****
// This file was generated via preprocessing from input:
// java/com/google/common/html/types/SafeHtmlBuilder.java.tpl
// Please make changes to that file and run
// java/com/google/common/html/types/gen_srcs.sh
// to regenerate the .java files.
// ***************************************
/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.html.types;

import static com.google.common.html.types.BuilderUtils.coerceToInterchangeValid;
import static com.google.common.html.types.BuilderUtils.escapeHtmlInternal;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CompileTimeConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Generated;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Builder for HTML elements which conform to the {@link SafeHtml} contract. Supports setting
 * element name, individual attributes and content.
 *
 * <p>While this builder disallows some invalid HTML constructs (for example, void elements with
 * content) it does not guarantee well-formed HTML (for example, attribute values are not strictly
 * enforced if they pose no security risk). A large number of runtime failures are possible and it
 * is therefore recommended to thoroughly unit test code using this builder.
 */
@Generated(value = "//java/com/google/common/html/types:gen_srcs.sh")
@GwtCompatible
@NotThreadSafe
public final class SafeHtmlBuilder {
  // Keep these regular expressions compatible across Java and JavaScript native implementations.
  // They are uncompiled because we couldn't depend on java.util.regex.Pattern or
  // com.google.gwt.regexp.shared.RegExp
  private static final String VALID_ELEMENT_NAMES_REGEXP = "[a-z0-9-]+";
  private static final String VALID_DATA_ATTRIBUTES_REGEXP = "data-[a-zA-Z-]+";

  private static final Set<String> UNSUPPORTED_ELEMENTS =
      createUnmodifiableSet(
          "applet",
          "base",
          "embed",
          "iframe",
          "math",
          "meta",
          "object",
          "script",
          "style",
          "svg",
          "template");

  private static final Set<String> VOID_ELEMENTS =
      createUnmodifiableSet(
          "area", "br", "col", "hr", "img", "input", "keygen", "link", "param", "source", "track",
          "wbr");

  private final String elementName;
  /** We use LinkedHashMap to maintain attribute insertion order. */
  private final Map<String, String> attributes = new LinkedHashMap<String, String>();

  private final List<SafeHtml> contents = new ArrayList<SafeHtml>();

  private boolean useSlashOnVoid = false;

  private enum AttributeContract {
    SAFE_URL,
    TRUSTED_RESOURCE_URL
  }

  /** Contract of the value currently assigned to the {@code href} attribute. */
  private AttributeContract hrefValueContract = AttributeContract.TRUSTED_RESOURCE_URL;

  /**
   * Creates a builder for the given {@code elementName}, which must consist only of lowercase
   * letters, digits and {@code -}.
   *
   * <p>If {@code elementName} is not a void element then the string representation of the builder
   * is {@code <elementName[optional attributes]>[optional content]</elementName>}. If {@code
   * elementName} is a void element then the string representation is {@code <elementName[optional
   * attributes]>}. Contents between the element's start and end tag can be set via, for example,
   * {@code appendContent()}.
   *
   * <p>{@code embed}, {@code object}, {@code script}, {@code style}, {@code template} are not
   * supported because their content has special semantics, and they can result the execution of
   * code not under application control. Some of these have dedicated creation methods.
   *
   * @throws IllegalArgumentException if {@code elementName} contains invalid characters or is not
   *     supported
   * @see http://whatwg.org/html/syntax.html#void-elements
   */
  public SafeHtmlBuilder(@CompileTimeConstant final String elementName) {
    if (elementName == null) {
      throw new NullPointerException();
    }
    if (!elementName.matches(VALID_ELEMENT_NAMES_REGEXP)) {
      throw new IllegalArgumentException(
          "Invalid element name \""
              + elementName
              + "\". "
              + "Only lowercase letters, numbers and '-' allowed.");
    }
    if (UNSUPPORTED_ELEMENTS.contains(elementName)) {
      throw new IllegalArgumentException("Element \"" + elementName + "\" is not supported.");
    }
    this.elementName = elementName;
  }

  /**
   * Causes the builder to use a slash on the tag of a void element, emitting e.g. {@code <br/>}
   * instead of the default {@code <br>}. Slashes are required if rendering XHTML and optional in
   * HTML 5.
   *
   * <p>This setting has no effect for non-void elements.
   *
   * @see http://www.w3.org/TR/html5/syntax.html#start-tags
   */
  public SafeHtmlBuilder useSlashOnVoid() {
    useSlashOnVoid = true;
    return this;
  }

  /** Sets the {@code alt} attribute for this element. */
  public SafeHtmlBuilder setAlt(String value) {
    return setAttribute("alt", value);
  }

  /** Sets the {@code class} attribute for this element. */
  public SafeHtmlBuilder setClass(String value) {
    return setAttribute("class", value);
  }

  /** Sets the {@code color} attribute for this element. */
  public SafeHtmlBuilder setColor(String value) {
    return setAttribute("color", value);
  }

  /** Values that can be passed to {@link #setDir(DirValue)}. */
  public enum DirValue {

    /** Value of {@code auto}. */
    AUTO("auto"),
    /** Value of {@code ltr}. */
    LTR("ltr"),
    /** Value of {@code rtl}. */
    RTL("rtl");

    private final String value;

    private DirValue(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  /** Sets the {@code dir} attribute for this element. */
  public SafeHtmlBuilder setDir(DirValue value) {
    return setAttribute("dir", value.toString());
  }

  /** These elements are whitelisted to use href with a SafeUrl value. */
  private static final Set<String> HREF_SAFE_URL_ELEMENT_WHITELIST =
      createUnmodifiableSet("a", "area");
  /**
   * On {@code link} elements, the {@code href} attribute may be set to {@code SafeUrl} values only
   * for these values of the {@code rel} attribute.
   */
  private static final Set<String> LINK_HREF_SAFE_URL_REL_WHITELIST =
      createUnmodifiableSet(
          "alternate",
          "author",
          "bookmark",
          "canonical",
          "cite",
          "help",
          "icon",
          "license",
          "next",
          "prefetch",
          "prerender",
          "prev",
          "search",
          "subresource");

  /**
   * Sets the {@code href} attribute for this element.
   *
   * <p>The attribute {@code href} with a {@code SafeUrl} value is allowed on these elements:
   *
   * <ul>
   * <li>{@code a}
   * <li>{@code area}
   * <li>{@code link}
   * </ul>
   *
   * <p>On {@code link} elements, {@code href} may only be set to a SafeUrl value if {@code rel} is
   * one of the following values:
   *
   * <ul>
   * <li>{@code alternate}
   * <li>{@code author}
   * <li>{@code bookmark}
   * <li>{@code canonical}
   * <li>{@code cite}
   * <li>{@code help}
   * <li>{@code icon}
   * <li>{@code license}
   * <li>{@code next}
   * <li>{@code prefetch}
   * <li>{@code prerender}
   * <li>{@code prev}
   * <li>{@code search}
   * <li>{@code subresource}
   * </ul>
   *
   * @throws IllegalArgumentException if the {@code href} attribute with a {@code SafeUrl} value is
   *     not allowed on this element
   * @throws IllegalArgumentException if this a {@code link} element and the value of {@code rel}
   *     does not allow the SafeUrl contract on {@code href}
   */
  public SafeHtmlBuilder setHref(SafeUrl value) {
    if (!HREF_SAFE_URL_ELEMENT_WHITELIST.contains(elementName) && !elementName.equals("link")) {
      throw new IllegalArgumentException(
          "Attribute \"href\" with a SafeUrl value can only be used "
              + "by one of the following elements: "
              + HREF_SAFE_URL_ELEMENT_WHITELIST);
    }
    if (elementName.equals("link")) {
      checkLinkDependentAttributes(attributes.get("rel"), AttributeContract.SAFE_URL);
    }
    hrefValueContract = AttributeContract.SAFE_URL;
    return setAttribute("href", value.getSafeUrlString());
  }

  /** Sets the {@code href} attribute for this element. */
  public SafeHtmlBuilder setHref(TrustedResourceUrl value) {
    hrefValueContract = AttributeContract.TRUSTED_RESOURCE_URL;
    return setAttribute("href", value.getTrustedResourceUrlString());
  }

  /** Sets the {@code id} attribute for this element. */
  public SafeHtmlBuilder setId(@CompileTimeConstant final String value) {
    return setAttribute("id", value);
  }

  /** Sets the {@code name} attribute for this element. */
  public SafeHtmlBuilder setName(String value) {
    return setAttribute("name", value);
  }

  /** Sets the {@code placeholder} attribute for this element. */
  public SafeHtmlBuilder setPlaceholder(String value) {
    return setAttribute("placeholder", value);
  }

  /**
   * Sets the {@code rel} attribute for this element.
   *
   * <p>If this is a {@code link}} element, and {@code href} has been set from a {@link SafeUrl},
   * then {@code value} has to be an allowed value. See {@link #setHref(SafeHtml)}.
   *
   * @throws IllegalArgumentException if this is a {@code link} element and this value of {@code
   *     rel} is not allowed
   */
  public SafeHtmlBuilder setRel(String value) {
    checkLinkDependentAttributes(value, hrefValueContract);
    return setAttribute("rel", value);
  }

  /** These elements are whitelisted to use src with a SafeUrl value. */
  private static final Set<String> SRC_SAFE_URL_ELEMENT_WHITELIST =
      createUnmodifiableSet("img", "input");

  /**
   * Sets the {@code src} attribute for this element.
   *
   * <p>The attribute {@code src} with a {@code SafeUrl} value is allowed on these elements:
   *
   * <ul>
   * <li>{@code img}
   * <li>{@code input}
   * </ul>
   *
   * @throws IllegalArgumentException if the {@code src} attribute with a {@code SafeUrl} value is
   *     not allowed on this element
   */
  public SafeHtmlBuilder setSrc(SafeUrl value) {
    if (!SRC_SAFE_URL_ELEMENT_WHITELIST.contains(elementName)) {
      throw new IllegalArgumentException(
          "Attribute \"src\" with a SafeUrl value can only be used "
              + "by one of the following elements: "
              + SRC_SAFE_URL_ELEMENT_WHITELIST);
    }
    return setAttribute("src", value.getSafeUrlString());
  }

  /** Sets the {@code src} attribute for this element. */
  public SafeHtmlBuilder setSrc(TrustedResourceUrl value) {
    return setAttribute("src", value.getTrustedResourceUrlString());
  }

  /** Sets the {@code style} attribute for this element. */
  public SafeHtmlBuilder setStyle(SafeStyle value) {
    return setAttribute("style", value.getSafeStyleString());
  }

  /** Values that can be passed to {@link #setTarget(TargetValue)}. */
  public enum TargetValue {

    /** Value of {@code _blank}. */
    BLANK("_blank"),
    /** Value of {@code _self}. */
    SELF("_self");

    private final String value;

    private TargetValue(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  /** Sets the {@code target} attribute for this element. */
  public SafeHtmlBuilder setTarget(TargetValue value) {
    return setAttribute("target", value.toString());
  }

  /** Sets the {@code title} attribute for this element. */
  public SafeHtmlBuilder setTitle(String value) {
    return setAttribute("title", value);
  }

  /** These elements are whitelisted to use type with a String value. */
  private static final Set<String> TYPE_STRING_ELEMENT_WHITELIST =
      createUnmodifiableSet("button", "command", "input", "li", "link", "ol");

  /**
   * Sets the {@code type} attribute for this element.
   *
   * <p>The attribute {@code type} with a {@code String} value is allowed on these elements:
   *
   * <ul>
   * <li>{@code button}
   * <li>{@code command}
   * <li>{@code input}
   * <li>{@code li}
   * <li>{@code link}
   * <li>{@code ol}
   * </ul>
   *
   * @throws IllegalArgumentException if the {@code type} attribute with a {@code String} value is
   *     not allowed on this element
   */
  public SafeHtmlBuilder setType(String value) {
    if (!TYPE_STRING_ELEMENT_WHITELIST.contains(elementName)) {
      throw new IllegalArgumentException(
          "Attribute \"type\" with a String value can only be used "
              + "by one of the following elements: "
              + TYPE_STRING_ELEMENT_WHITELIST);
    }
    return setAttribute("type", value);
  }

  /** Sets the {@code value} attribute for this element. */
  public SafeHtmlBuilder setValue(String value) {
    return setAttribute("value", value);
  }

  /**
   * Sets a custom data attribute, {@code name}, to {@code value} for this element. {@code value}
   * must consist only of letters and {@code -}.
   *
   * @param name including the "data-" prefix, e.g. "data-tooltip"
   * @throws IllegalArgumentException if the attribute name isn't valid
   * @see
   *     http://www.w3.org/TR/html5/dom.html#embedding-custom-non-visible-data-with-the-data-*-attributes
   */
  public SafeHtmlBuilder setDataAttribute(@CompileTimeConstant final String name, String value) {
    if (!name.matches(VALID_DATA_ATTRIBUTES_REGEXP)) {
      throw new IllegalArgumentException(
          "Invalid data attribute name \""
              + name
              + "\"."
              + "Name must start with \"data-\" and be followed by letters and '-'.");
    }
    return setAttribute(name, value);
  }

  /**
   * Sets the {@code id} attribute for this element, as the concatenation of a {@link
   * CompileTimeConstant} {@code prefix} and a {@code value}.
   *
   * @throws IllegalArgumentException if {@code prefix} is an empty string
   */
  public SafeHtmlBuilder setIdWithPrefix(@CompileTimeConstant final String prefix, String value) {
    if (prefix.trim().length() == 0) {
      throw new IllegalArgumentException("Prefix cannot be empty string");
    }
    return setAttribute("id", prefix + "-" + value);
  }

  /**
   * Appends the given {@code htmls} as this element's content, in sequence.
   *
   * @throws IllegalStateException if this builder represents a void element
   */
  public SafeHtmlBuilder appendContent(SafeHtml... htmls) {
    checkNotVoidElement();
    Collections.addAll(contents, htmls);
    return this;
  }

  /**
   * Appends the given {@code htmls} as this element's content, in the sequence the Iterable returns
   * them.
   *
   * @throws IllegalStateException if this builder represents a void element
   */
  public SafeHtmlBuilder appendContent(Iterable<SafeHtml> htmls) {
    checkNotVoidElement();
    for (SafeHtml html : htmls) {
      contents.add(html);
    }
    return this;
  }

  /**
   * Appends the given {@code htmls} as this element's content, in the sequence the Iterator returns
   * them.
   *
   * @throws IllegalStateException if this builder represents a void element
   */
  public SafeHtmlBuilder appendContent(Iterator<SafeHtml> htmls) {
    checkNotVoidElement();
    while (htmls.hasNext()) {
      contents.add(htmls.next());
    }
    return this;
  }

  /**
   * Checks that this combination of rel value and href contract is safe.
   *
   * @param relValue is the value of rel or null if rel isn't present.
   * @throws IllegalArgumentException if this value and contract combination is not allowed.
   */
  private static void checkLinkDependentAttributes(
      @Nullable String relValue, AttributeContract hrefValueContract) {

    if (hrefValueContract.equals(AttributeContract.SAFE_URL)
        && relValue != null
        && !LINK_HREF_SAFE_URL_REL_WHITELIST.contains(relValue.toLowerCase(Locale.ENGLISH))) {
      throw new IllegalArgumentException(
          "SafeUrl values for the href attribute are not allowed on <link rel="
              + relValue
              + ">. Did you intend to use a TrustedResourceUrl?");
    }
  }

  private void checkNotVoidElement() {
    if (VOID_ELEMENTS.contains(elementName)) {
      throw new IllegalStateException(
          "Element \"" + elementName + "\" is a void element and so cannot have content.");
    }
  }

  /**
   * HTML-escapes and appends {@code text} to this element's content.
   *
   * @throws IllegalStateException if this builder represents a void element
   */
  public SafeHtmlBuilder escapeAndAppendContent(String text) {
    // htmlEscape() unicode coerces in non-portable version.
    return appendContent(SafeHtmls.htmlEscape(text));
  }

  public SafeHtml build() {
    StringBuilder sb = new StringBuilder("<" + elementName);
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      sb.append(" " + entry.getKey() + "=\"" + escapeHtmlInternal(entry.getValue()) + "\"");
    }

    boolean isVoid = VOID_ELEMENTS.contains(elementName);
    if (isVoid && useSlashOnVoid) {
      sb.append("/");
    }
    sb.append(">");
    if (!isVoid) {
      for (SafeHtml content : contents) {
        sb.append(content.getSafeHtmlString());
      }
      sb.append("</" + elementName + ">");
    }
    return SafeHtmls.create(sb.toString());
  }

  private static final Set<String> createUnmodifiableSet(String... strings) {
    HashSet<String> set = new HashSet<String>();
    for (String string : strings) {
      set.add(string);
    }
    return Collections.unmodifiableSet(set);
  }

  private SafeHtmlBuilder setAttribute(@CompileTimeConstant final String name, String value) {
    if (value == null) {
      throw new NullPointerException("setAttribute requires a non-null value.");
    }
    attributes.put(name, coerceToInterchangeValid(value));
    return this;
  }
}
