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

import static com.google.common.html.types.testing.HtmlConversions.newSafeHtmlForTest;
import static com.google.common.html.types.testing.assertions.Assertions.assertClassIsNotExportable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/** Unit tests for {@link SafeHtmls}. */
@GwtCompatible
public class SafeHtmlsTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeHtmls.class);
  }

  public void testCreateElement() {
    assertEquals("<br>", SafeHtmls.createElement("br").getSafeHtmlString());
    List<SafeHtml> htmls = new ArrayList<>();
    htmls.add(SafeHtmls.htmlEscape("test"));
    assertEquals("<b>test</b>", SafeHtmls.createElement("b", htmls).getSafeHtmlString());
    assertEquals("<b>test</b>", SafeHtmls.createElement("b", "test").getSafeHtmlString());
    assertEquals(
        "<b>&lt;</b>", SafeHtmls.createElement("b", SafeHtmls.htmlEscape("<")).getSafeHtmlString());
    assertEquals(
        "<b>&lt;&gt;</b>",
        SafeHtmls.createElement("b", SafeHtmls.htmlEscape("<"), SafeHtmls.htmlEscape(">"))
            .getSafeHtmlString());
  }

  @GwtIncompatible("SafeHtmls.fromResource")
  public void testFromResource() throws Exception {
    SafeHtml safeHtml = SafeHtmls.fromResource("com/google/common/html/types/resources/html.html");
    assertEquals("<title>Test</title>\n", safeHtml.getSafeHtmlString());
  }

  @GwtIncompatible("SafeHtmls.fromResource")
  public void testFromResourceContext() throws Exception {
    SafeHtml safeHtml = SafeHtmls.fromResource(SafeHtmlsTest.class, "resources/html.html");
    assertEquals("<title>Test</title>\n", safeHtml.getSafeHtmlString());
  }

  public void testFromScript() {
    SafeHtml html = SafeHtmls.fromScript(SafeScripts.fromConstant("test('<hello>');"));
    assertEquals(
        "<script type=\"text/javascript\">test('<hello>');</script>", html.getSafeHtmlString());
  }

  public void testFromScriptForTypeApplicationLdJson() {
    SafeHtml html =
        SafeHtmls.fromScriptForTypeApplicationLdJson(SafeScripts.fromConstant("{\"a\": 123}"));
    assertEquals(
        "<script type=\"application/ld+json\">{\"a\": 123}</script>", html.getSafeHtmlString());
  }

  public void testFromScriptWithCspNonce() {
    SafeHtml html =
        SafeHtmls.fromScriptWithCspNonce(
            SafeScripts.fromConstant("test('<hello>');"), "QswwJvFgUzpcN+HRUB9gDIueLB8");
    assertEquals(
        "<script type=\"text/javascript\" "
            + "nonce=\"QswwJvFgUzpcN+HRUB9gDIueLB8\">test('<hello>');</script>",
        html.getSafeHtmlString());
  }

  public void testFromScriptUrl() {
    SafeHtml html =
        SafeHtmls.fromScriptUrl(
            TrustedResourceUrls.fromConstant("https://example.com/&<\"'script.js"));
    assertEquals(
        "<script type=\"text/javascript\" "
            + "src=\"https://example.com/&amp;&lt;&quot;&#39;script.js\"></script>",
        html.getSafeHtmlString());
  }

  public void testFromScriptUrlDeferred() {
    SafeHtml html =
        SafeHtmls.fromScriptUrlDeferred(
            TrustedResourceUrls.fromConstant("https://example.com/&<\"'script.js"));
    assertEquals(
        "<script defer type=\"text/javascript\" "
            + "src=\"https://example.com/&amp;&lt;&quot;&#39;script.js\"></script>",
        html.getSafeHtmlString());
  }

  public void testFromScriptUrlWithCspNonce() {
    SafeHtml html =
        SafeHtmls.fromScriptUrlWithCspNonce(
            TrustedResourceUrls.fromConstant("https://example.com/&<\"'script.js"),
            "QswwJvFgUzpcN+HRUB9gDIueLB8");
    assertEquals(
        "<script type=\"text/javascript\" "
            + "nonce=\"QswwJvFgUzpcN+HRUB9gDIueLB8\" "
            + "src=\"https://example.com/&amp;&lt;&quot;&#39;script.js\"></script>",
        html.getSafeHtmlString());
  }

  public void testFromScriptUrlWithCspNonceDeferred() {
    SafeHtml html =
        SafeHtmls.fromScriptUrlWithCspNonceDeferred(
            TrustedResourceUrls.fromConstant("https://example.com/&<\"'script.js"),
            "QswwJvFgUzpcN+HRUB9gDIueLB8");
    assertEquals(
        "<script defer type=\"text/javascript\" "
            + "nonce=\"QswwJvFgUzpcN+HRUB9gDIueLB8\" "
            + "src=\"https://example.com/&amp;&lt;&quot;&#39;script.js\"></script>",
        html.getSafeHtmlString());
  }

  public void testFromStyleSheet() {
    SafeHtml html =
        SafeHtmls.fromStyleSheet(SafeStyleSheets.fromConstant(".title { color: #000000; };"));
    assertEquals(
        "<style type=\"text/css\">.title { color: #000000; };</style>", html.getSafeHtmlString());
  }

  public void testFromStyleSheetIllegal() {
    SafeStyleSheet unsafeStyle =
        UncheckedConversions.safeStyleSheetFromStringKnownToSatisfyTypeContract(
            ".title { color: #000000; };</style><script>alert('malicious script');</script>");
    try {
      SafeHtmls.fromStyleSheet(unsafeStyle);
      fail("Should throw an AssertionError if style contains \"<\" or \">\"");
    } catch (IllegalArgumentException e) {
      assertNotNull(e);
    }
  }

  public void testFromStyleUrl() {
    SafeHtml html =
        SafeHtmls.fromStyleUrl(
            TrustedResourceUrls.fromConstant("https://example.com/&<\"'style.css"));
    assertEquals(
        "<link rel=\"stylesheet\" href=\"https://example.com/&amp;&lt;&quot;&#39;style.css\">",
        html.getSafeHtmlString());
  }

  public void testToAndFromProto() {
    String html = "<b>Hello World</b>";
    SafeHtml safeHtml = newSafeHtmlForTest(html);
    SafeHtmlProto proto = SafeHtmls.toProto(safeHtml);
    assertEquals(html, SafeHtmls.fromProto(proto).getSafeHtmlString());
  }

  public void testConcats() {
    assertEquals("", SafeHtmls.concat().getSafeHtmlString());

    assertEquals("ab", SafeHtmls.concat(SafeHtmls.htmlEscape("ab")).getSafeHtmlString());

    assertEquals(
        "&lt;3",
        SafeHtmls.concat(SafeHtmls.htmlEscape("<"), SafeHtmls.htmlEscape("3")).getSafeHtmlString());

    List<SafeHtml> htmls = new ArrayList<>();
    htmls.add(SafeHtmls.htmlEscape("<"));
    htmls.add(SafeHtmls.htmlEscape("3"));
    assertEquals("&lt;3", SafeHtmls.concat(htmls).getSafeHtmlString());
  }

  public void testHtmlEscapePreservingNewlines() {
    assertEquals(
        "a<br>&lt;3<br>", SafeHtmls.htmlEscapePreservingNewlines("a\n<3\r\n").getSafeHtmlString());
  }

  public void testHtmlEscapePreservingWhitespace() {
    assertEquals("a<br>c", SafeHtmls.htmlEscapePreservingWhitespace("a\nc").getSafeHtmlString());
    assertEquals("&lt;<br>", SafeHtmls.htmlEscapePreservingWhitespace("<\n").getSafeHtmlString());
    assertEquals("<br>", SafeHtmls.htmlEscapePreservingWhitespace("\r\n").getSafeHtmlString());
    assertEquals("<br>", SafeHtmls.htmlEscapePreservingWhitespace("\r").getSafeHtmlString());
    assertEquals("", SafeHtmls.htmlEscapePreservingWhitespace("").getSafeHtmlString());

    assertEquals("a &#160;b", SafeHtmls.htmlEscapePreservingWhitespace("a  b").getSafeHtmlString());
    assertEquals(
        "a<br>&#160;b", SafeHtmls.htmlEscapePreservingWhitespace("a\n b").getSafeHtmlString());
    assertEquals(
        "&#160;a<br>b", SafeHtmls.htmlEscapePreservingWhitespace(" a\nb").getSafeHtmlString());
    assertEquals(
        "a<span style=\"white-space:pre\">\t\t</span>&#160;b",
        SafeHtmls.htmlEscapePreservingWhitespace("a\t\t b").getSafeHtmlString());
  }

  public void testComment() {
    assertEquals("<!--&lt;script&gt;-->", SafeHtmls.comment("<script>").getSafeHtmlString());
  }

  public void testHtml5Doctype() {
    assertEquals("<!DOCTYPE html>", SafeHtmls.html5Doctype().getSafeHtmlString());
  }

  /**
   * Test that string with 4 Unicode characters is preserved correctly though escaping. The runes in
   * the string "丄ê𐒖t" have, in order, code points:
   *
   * <ol>
   *   <li>U+4E04. E4 B8 84 in UTF-8 and 4E04 in UTF-16.
   *   <li>U+00EA. C3 AA in UTF-8 and 00EA in UTF-16.
   *   <li>U+10496. F0 90 92 96 in UTF-8 and D801 DC96 in UTF-16. String.length() == 2 but
   *       String.codePointCount() == 1.
   *   <li>U+0074. 74 in UTF-8 and ASCII. 0074 in UTF-16.
   * </ol>
   *
   * <p>This covers 1, 2, 3 and 4 byte UTF-8 encodings. Since Java internally represents strings as
   * UTF-16, one code point which requires a different number of bytes in UTF-8 and UTF-16 is used.
   * Since Java's String.length() counts UTF-16 code units and not Unicode code points, one
   * character requires two UTF-16 code units.
   */
  public void testPreservesNonAsciiCharacters() {
    SafeHtml safeHtml = SafeHtmls.htmlEscape("\u4E04\u00EA\uD801\uDC96t");
    assertEquals("丄ê𐒖t", safeHtml.getSafeHtmlString());
  }
}
