// **** GENERATED CODE, DO NOT MODIFY ****
// This file was generated via preprocessing from input:
// javatests/com/google/common/html/types/SafeHtmlBuilderTest.java.tpl
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

import static com.google.common.html.types.testing.HtmlConversions.newSafeUrlForTest;
import static com.google.common.html.types.testing.assertions.Assertions.assertClassIsNotExportable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.util.ArrayList;
import junit.framework.TestCase;

/** Test case for {@link SafeHtmlBuilder}. */
@GwtCompatible
public class SafeHtmlBuilderTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeHtmlBuilder.class);
  }

  public void testSetIdWithPrefix() {
    assertSameHtml(
        "<div id=\"prefix-id\"></div>", new SafeHtmlBuilder("div").setIdWithPrefix("prefix", "id"));

    try {
      new SafeHtmlBuilder("div").setIdWithPrefix(" ", "id").build();
      fail("Should throw on empty string");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testOmitsEndTagForVoidElements() {
    assertSameHtml("<br>", new SafeHtmlBuilder("br"));
  }

  public void testOptionallyUsesSlashOnVoidElement() {
    assertSameHtml("<br/>", new SafeHtmlBuilder("br").useSlashOnVoid());
  }

  public void testIncludesEndTagForNonVoidElements() {
    assertSameHtml("<a></a>", new SafeHtmlBuilder("a"));
  }

  public void testUseSlashOnVoidHasNoEffectOnNonVoidElements() {
    assertSameHtml("<a></a>", new SafeHtmlBuilder("a").useSlashOnVoid());
  }

  public void testEscapesAttributeValues() {
    assertSameHtml("<a title=\"&quot;\"></a>", new SafeHtmlBuilder("a").setTitle("\""));
  }

  public void testAllowsEmptyAttributeValue() {
    assertSameHtml("<a title=\"\"></a>", new SafeHtmlBuilder("a").setTitle(""));
  }

  /**
   * Same string as used in {@link SafeHtmlsTest}, see documentation there.
   */
  public void testPreservesNonAsciiCharacters() {
    assertSameHtml(
      "<div>丄ê𐒖t</div>",
      new SafeHtmlBuilder("div")
          .escapeAndAppendContent("\u4E04\u00EA\uD801\uDC96t"));
  }

  public void testEscapesContent() {
    assertSameHtml("<a>&lt;&amp;</a>", new SafeHtmlBuilder("a")
        .escapeAndAppendContent("<")
        .escapeAndAppendContent("&"));
  }

  public void testAppendsContent() {
    SafeHtml br = new SafeHtmlBuilder("br").build();
    SafeHtml i = new SafeHtmlBuilder("i").build();
    ArrayList<SafeHtml> htmls = new ArrayList<SafeHtml>();
    htmls.add(br);
    htmls.add(i);

    // All 3 appendContent() variants.
    assertSameHtml(
        "<span>a<br><i></i><br><i></i><br><i></i>c</span>",
        new SafeHtmlBuilder("span")
            .escapeAndAppendContent("a")
            .appendContent(br, i)
            .appendContent(htmls)
            .appendContent(htmls.iterator())
            .escapeAndAppendContent("c"));
  }

  public void testDisallowsContentOnVoidElements() {
    SafeHtml html = new SafeHtmlBuilder("i").build();
    ArrayList<SafeHtml> htmls = new ArrayList<SafeHtml>();
    htmls.add(html);

    try {
      new SafeHtmlBuilder("br").appendContent(html);
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      new SafeHtmlBuilder("br").appendContent(htmls);
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      new SafeHtmlBuilder("br").appendContent(htmls.iterator());
      fail();
    } catch (IllegalStateException expected) {
    }

    try {
      new SafeHtmlBuilder("br").escapeAndAppendContent("content");
      fail();
    } catch (IllegalStateException expected) {
    }
  }

  public void testSupportsDataAttributes() {
    assertSameHtml("<abbr data-tooltip=\"a\"></abbr>", new SafeHtmlBuilder("abbr")
        .setDataAttribute("data-tooltip", "a"));
  }

  public void testDisallowsArbitraryDataAttribute() {
    try {
      new SafeHtmlBuilder("a").setDataAttribute("href", "");
      fail("Data attribute shouldn't allow setting arbitrary attributes.");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testAllowsResourceUrlInLinkedStylesheet() {
    TrustedResourceUrl url = TrustedResourceUrls.fromConstant("a");
    assertSameHtml(
        "<link rel=\"stylesheet\" href=\"a\">",
        new SafeHtmlBuilder("link").setRel("stylesheet").setHref(url));

    assertSameHtml(
        "<link href=\"a\" rel=\"stylesheet\">",
        new SafeHtmlBuilder("link").setHref(url).setRel("stylesheet"));
  }

  public void testAllowsResourceUrlInLinkedDangerousContexts() {
    TrustedResourceUrl url = TrustedResourceUrls.fromConstant("a");
    assertSameHtml(
        "<link rel=\"serviceworker\" href=\"a\">",
        new SafeHtmlBuilder("link").setRel("serviceworker").setHref(url));

    assertSameHtml(
        "<link rel=\"import\" href=\"a\">",
        new SafeHtmlBuilder("link").setRel("next").setHref(url).setRel("import"));
  }

  public void testAllowsSafeUrlInLinkedIcon() {
    assertSameHtml("<link rel=\"icon\" href=\"a\">", new SafeHtmlBuilder("link")
        .setRel("icon")
        .setHref(newSafeUrlForTest("a")));
  }

  public void testDisallowsSafeUrlInLinkedStylesheets() {
    // Test case-insensitive too.
    try {
      new SafeHtmlBuilder("link").setRel("Stylesheet").setHref(newSafeUrlForTest("a"));
      fail("Setting <link href> to SafeUrl with rel=\"stylesheet\" shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    // Test different order and matching anywhere.
    try {
      new SafeHtmlBuilder("link").setHref(newSafeUrlForTest("a")).setRel("xstylesheetx");
      fail("Setting <link href> to SafeUrl with rel=\"stylesheet\" shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

  }

  public void testDisallowsSafeUrlInOtherDangerousContexts() {
    try {
      new SafeHtmlBuilder("link").setRel("import").setHref(newSafeUrlForTest("a"));
      fail("Setting <link href> to SafeUrl with rel=\"import\" shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    try {
      new SafeHtmlBuilder("link").setRel("manifest").setHref(newSafeUrlForTest("a"));
      fail("Setting <link href> to SafeUrl with rel=\"manifest\" shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    try {
      new SafeHtmlBuilder("link").setRel("serviceworker").setHref(newSafeUrlForTest("a"));
      fail("Setting <link href> to SafeUrl with rel=\"serviceworker\" shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    // Test different order and rel change
    try {
      new SafeHtmlBuilder("link").setRel("next").setHref(newSafeUrlForTest("a")).setRel("import");
      fail("Setting <link href> to SafeUrl with rel=\"import\" shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    try {
      new SafeHtmlBuilder("link").setHref(newSafeUrlForTest("a")).setRel("import");
      fail("Setting <link href> to SafeUrl with rel=\"import\" shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testDisallowsInvalidTagNames() {
    try {
      new SafeHtmlBuilder("a href='a'");
      fail("Invalid tag name shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testDisallowsUnsafeTagNames() {
    try {
      new SafeHtmlBuilder("script");
      fail("<script> shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    try {
      new SafeHtmlBuilder("scRipt");
      fail("<scRipt> shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    try {
      new SafeHtmlBuilder("base");
      fail("<base> shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    try {
      new SafeHtmlBuilder("svg");
      fail("<svg> shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }

    try {
      new SafeHtmlBuilder("math");
      fail("<math> shouldn't be allowed.");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testSettingAttributeAgainIsAllowed() {
    assertSameHtml(
        "<a title=\"c\"></a>",
        new SafeHtmlBuilder("a").setTitle("a").setTitle("c"));
  }

  public void testDisallowsUsingNullValues() {
    try {
      new SafeHtmlBuilder("a").setTitle(null);
      fail("The null attribute value shouldn't be allowed.");
    } catch (NullPointerException expected) {
    }
  }

  public void testDisallowsSafeUrlInVideoSrcAttribute() {
    try {
      new SafeHtmlBuilder("video").setSrc(newSafeUrlForTest("b"));
      fail("<video src> should require TrustedResourceUrl.");
    } catch (IllegalArgumentException expected) {
    }
  }

  /**
   * Whitelists depend on element name being lower cased.
   */
  public void testDisallowsUpperCaseElementNames() {
    try {
      new SafeHtmlBuilder("dIv");
      fail("Element names with upper case should be disallowed");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testPreservesAttributesOrder() {
    assertSameHtml("<div class=\"a\" id=\"id1\"></div>",
        new SafeHtmlBuilder("div").setClass("a").setId("id1"));
    assertSameHtml("<div id=\"id2\" class=\"a\"></div>",
        new SafeHtmlBuilder("div").setId("id2").setClass("a"));
  }

  public void testAllowsNameTypeValueForInput() {
    assertSameHtml(
        "<input name=\"myName\" value=\"myValue\" type=\"hidden\">",
        new SafeHtmlBuilder("input")
            .setName("myName")
            .setValue("myValue")
            .setType("hidden"));
  }

  public void testDisallowsTypeAttributeForNonWhitelistedElement() {
    try {
      new SafeHtmlBuilder("img").setType("hidden");
      fail("<img> should not allow 'type' attribute");
    } catch (IllegalArgumentException expected) {
    }
  }


  private static void assertSameHtml(String expected, SafeHtmlBuilder builder) {
    assertEquals(expected, builder.build().getSafeHtmlString());
  }

}
