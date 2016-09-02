// **** GENERATED CODE, DO NOT MODIFY ****
// This file was generated via preprocessing from input:
// javatests/com/google/common/html/types/SafeHtmlsTest.java.tpl
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

import static com.google.common.html.types.testing.HtmlConversions.newSafeHtmlForTest;
import static com.google.common.html.types.testing.assertions.Assertions.assertClassIsNotExportable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Unit tests for {@link SafeHtmls}.
 */
@GwtCompatible
public class SafeHtmlsTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeHtmls.class);
  }

  public void testFromScript() {
    SafeHtml html = SafeHtmls.fromScript(SafeScripts.fromConstant("test('<hello>');"));
    assertEquals(
        "<script type=\"text/javascript\">test('<hello>');</script>",
        html.getSafeHtmlString());
  }

  public void testFromScriptUrl() {
    SafeHtml html = SafeHtmls.fromScriptUrl(
        TrustedResourceUrls.fromConstant("https://example.com/&<\"'script.js"));
    assertEquals(
        "<script type=\"text/javascript\" "
            + "src=\"https://example.com/&amp;&lt;&quot;&#39;script.js\"></script>",
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

    assertEquals("&lt;3",
        SafeHtmls.concat(SafeHtmls.htmlEscape("<"), SafeHtmls.htmlEscape("3")).getSafeHtmlString());

    List<SafeHtml> htmls = new ArrayList<>();
    htmls.add(SafeHtmls.htmlEscape("<"));
    htmls.add(SafeHtmls.htmlEscape("3"));
    assertEquals("&lt;3", SafeHtmls.concat(htmls).getSafeHtmlString());
  }

  public void testComment() {
    assertEquals("<!--&lt;script&gt;-->", SafeHtmls.comment("<script>").getSafeHtmlString());
  }

  /**
   * Test that string with 4 Unicode characters is preserved correctly though escaping. The runes
   * in the string "丄ê𐒖t" have, in order, code points:
   * <ol>
   * <li>U+4E04. E4 B8 84 in UTF-8 and 4E04 in UTF-16.
   * <li>U+00EA. C3 AA in UTF-8 and 00EA in UTF-16.
   * <li>U+10496. F0 90 92 96 in UTF-8 and D801 DC96 in UTF-16. String.length() == 2 but
   *     String.codePointCount() == 1.
   * <li>U+0074. 74 in UTF-8 and ASCII. 0074 in UTF-16.
   * </ol>
   * <p>
   * This covers 1, 2, 3 and 4 byte UTF-8 encodings. Since Java internally represents strings as
   * UTF-16, one code point which requires a different number of bytes in UTF-8 and UTF-16 is used.
   * Since Java's String.length() counts UTF-16 code units and not Unicode code points, one
   * character requires two UTF-16 code units.
   */
  public void testPreservesNonAsciiCharacters() {
    SafeHtml safeHtml = SafeHtmls.htmlEscape("\u4E04\u00EA\uD801\uDC96t");
    assertEquals("丄ê𐒖t", safeHtml.getSafeHtmlString());
  }
}
