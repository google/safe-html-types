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

import static com.google.common.html.types.testing.assertions.Assertions.assertClassIsNotExportable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.html.types.testing.HtmlConversions;
import junit.framework.TestCase;

/** Unit tests for {@link SafeUrls}. */
@GwtCompatible(emulated = true)
public class SafeUrlsTest extends TestCase {

  /** A URL which, unless trusted, gets transformed into an innocuous string by SafeUrl. */
  private static final String SAFE_URL = "javascript:irrelevant";
  /**
   * A URL which, even if untrusted, will not get transformed into an innocuous string by SafeUrl.
   */
  private static final String POTENTIALLY_UNSAFE_URL = "https://www.google.com/";

  private static final String CUSTOM_INNOCUOUS_URL = "javascript:void(0);";

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeUrls.class);
  }

  public void testToAndFromProto() {
    final String url = "https://www.google.com/";
    SafeUrl safeUrl = SafeUrls.fromConstant(url);
    SafeUrlProto proto = SafeUrls.toProto(safeUrl);
    assertEquals(url, SafeUrls.fromProto(proto).getSafeUrlString());
  }

  public void testFromProto_doesNotSanitize() {
    SafeUrl safeUrl = SafeUrls.fromConstant(SAFE_URL);
    SafeUrlProto proto = SafeUrls.toProto(safeUrl);
    assertEquals(SAFE_URL, SafeUrls.fromProto(proto).getSafeUrlString());

    safeUrl = SafeUrls.fromConstant(POTENTIALLY_UNSAFE_URL);
    proto = SafeUrls.toProto(safeUrl);
    assertEquals(POTENTIALLY_UNSAFE_URL, SafeUrls.fromProto(proto).getSafeUrlString());
  }

  public void testFromConstant_doesNotSanitize() {
    SafeUrl safeUrl = SafeUrls.fromConstant(SAFE_URL);
    assertEquals(SAFE_URL, safeUrl.getSafeUrlString());

    safeUrl = SafeUrls.fromConstant(POTENTIALLY_UNSAFE_URL);
    assertEquals(POTENTIALLY_UNSAFE_URL, safeUrl.getSafeUrlString());
  }

  public void testSanitize_validatesUrl() {
    for (SafeUrlsTestVectors.Vector v : SafeUrlsTestVectors.VECTORS) {
      assertEquals(
          v.expected(), SafeUrls.sanitize(v.input(), v.customSchemes()).getSafeUrlString());
      // sanitizeAsString assumes no custom schemes.
      if (v.customSchemes().isEmpty()) {
        String expected = v.safe() ? v.expected() : "about:invalid#irrelevant";
        assertEquals(expected, SafeUrls.sanitizeAsString(v.input(), "irrelevant"));
      }
    }
  }

  public void testSanitize_customInnocuous() {
    assertFalse(SafeUrl.INNOCUOUS_STRING.equals(CUSTOM_INNOCUOUS_URL));

    for (SafeUrlsTestVectors.Vector v : SafeUrlsTestVectors.VECTORS) {
      String sanitized =
          SafeUrls.sanitize(
                  v.input(), v.customSchemes(), SafeUrls.fromConstant(CUSTOM_INNOCUOUS_URL))
              .getSafeUrlString();
      assertEquals(v.safe() ? v.expected() : CUSTOM_INNOCUOUS_URL, sanitized);

      if (v.customSchemes().isEmpty()) {
        sanitized =
            SafeUrls.sanitize(v.input(), SafeUrls.fromConstant(CUSTOM_INNOCUOUS_URL))
                .getSafeUrlString();
        assertEquals(v.safe() ? v.expected() : CUSTOM_INNOCUOUS_URL, sanitized);
      }
    }
  }

  public void testHtmlDataUrl() {
    SafeHtml html =
        HtmlConversions.newSafeHtmlForTest(
            "<h1>Hello World!!?!</h1><br />"
                + "<span>4 is &gt; 1 + 2, and 2 * 0.5 &lt; 3, and & is &amp; in HTML.</span><br />"
                + "<a href=\"https://google.com/search?q=test#random%20suff\">Google Search</a>"
                + "<p>And some non-ascii text: 日本語は面白いね。</p>");
    SafeUrl dataUrl = SafeUrls.createHtmlDataUrl(html);
    assertEquals(
        "data:text/html;charset=UTF-8,%3Ch1%3EHello%20World!!%3F!%3C%2Fh1%3E%3Cbr%20%2F%3E%3C"
            + "span%3E4%20is%20&gt;%201%20+%202,%20and%202%20*%200.5%20&lt;%203,%20and%20&%20is%20"
            + "&amp;%20in%20HTML.%3C%2Fspan%3E%3Cbr%20%2F%3E%3Ca%20href=%22https:%2F%2Fgoogle.com"
            + "%2Fsearch%3Fq=test%23random%2520suff%22%3EGoogle%20Search%3C%2Fa%3E%3Cp%3EAnd"
            + "%20some%20non-ascii%20text:%20%E6%97%A5%E6%9C%AC%E8%AA%9E%E3%81%AF%E9%9D%A2%E7"
            + "%99%BD%E3%81%84%E3%81%AD%E3%80%82%3C%2Fp%3E",
        dataUrl.getSafeUrlString());
  }

  public void testHtmlDataUrlBase64() {
    SafeHtml html = HtmlConversions.newSafeHtmlForTest("<h1>Hello World!!?!</h1>..");
    SafeUrl dataUrl = SafeUrls.createHtmlDataUrlBase64(html);
    assertEquals(
        "data:text/html;charset=UTF-8;base64,PGgxPkhlbGxvIFdvcmxkISE/ITwvaDE+Li4=",
        dataUrl.getSafeUrlString());
  }
}
