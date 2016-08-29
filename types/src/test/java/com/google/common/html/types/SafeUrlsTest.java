// **** GENERATED CODE, DO NOT MODIFY ****
// This file was generated via preprocessing from input:
// javatests/com/google/common/html/types/SafeUrlsTest.java.tpl
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

import static com.google.common.html.types.testing.assertions.Assertions.assertClassIsNotExportable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.html.types.testing.HtmlConversions;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link SafeUrls}.
 */
@GwtCompatible
public class SafeUrlsTest extends TestCase {

  /** A URL which, unless trusted, gets transformed into an innocuous string by SafeUrl. */
  private static final String SAFE_URL = "javascript:irrelevant";
  /**
    * A URL which, even if untrusted, will not get transformed into an innocuous string by
    * SafeUrl.
    */
  private static final String POTENTIALLY_UNSAFE_URL = "https://www.google.com/";

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
    // Whitelisted schemes.
    assertSanitizedAsSafeUrl("http://example.com/");
    assertSanitizedAsSafeUrl("https://example.com");
    assertSanitizedAsSafeUrl("mailto:foo@example.com");
    assertSanitizedAsSafeUrl("ftp://example.com");
    assertSanitizedAsSafeUrl("ftp://username@example.com");
    assertSanitizedAsSafeUrl("ftp://username:password@example.com");
    // Scheme is case-insensitive
    assertSanitizedAsSafeUrl("HTtp://example.com/");
    // Different URL components go through.
    assertSanitizedAsSafeUrl("https://example.com/path?foo=bar#baz");
    // Scheme-less URL with authority.
    assertSanitizedAsSafeUrl("//example.com/path");
    // Absolute path with no authority.
    assertSanitizedAsSafeUrl("/path");
    assertSanitizedAsSafeUrl("/path?foo=bar#baz");
    // Relative path.
    assertSanitizedAsSafeUrl("path");
    assertSanitizedAsSafeUrl("path?foo=bar#baz");
    assertSanitizedAsSafeUrl("p//ath");
    assertSanitizedAsSafeUrl("p//ath?foo=bar#baz");
    // Restricted characters ('&', ':', \') after [/?#].
    assertSanitizedAsSafeUrl("/&");
    assertSanitizedAsSafeUrl("?:");

    // Non-whitelisted schemes.
    assertSanitizedAsUnsafeUrl("javascript:evil();");
    assertSanitizedAsUnsafeUrl("javascript:evil();//\nhttp://good.com/");
    assertSanitizedAsUnsafeUrl("data:blah");
    // Not whitelisted by default.
    assertSanitizedAsUnsafeUrl("tel:+1234567890");
    // Restricted characters before [/?#].
    assertSanitizedAsUnsafeUrl("&");
    assertSanitizedAsUnsafeUrl(":");
    // '\' is not treated like '/': no restricted characters allowed after it.
    assertSanitizedAsUnsafeUrl("\\:");
    // Regex anchored to the left: doesn't match on "/:".
    assertSanitizedAsUnsafeUrl(":/:");
    // Regex multiline not enabled: first line would match but second one wouldn't.
    assertSanitizedAsUnsafeUrl("path\n:");
  }

  private static void assertSanitizedAsSafeUrl(String url) {
    SafeUrl safeUrl = SafeUrls.sanitize(url);
    assertEquals(url, safeUrl.getSafeUrlString());

    assertEquals(url, SafeUrls.sanitizeAsString(url, "irrelevant"));
  }

  private static void assertSanitizedAsUnsafeUrl(String url) {
    SafeUrl safeUrl = SafeUrls.sanitize(url);
    assertEquals(SafeUrl.INNOCUOUS, safeUrl);

    assertEquals("about:invalid#id", SafeUrls.sanitizeAsString(url, "id"));
  }

  public void testSanitize_customSchemes() {
    Set<CustomSafeUrlScheme> schemes = new HashSet<CustomSafeUrlScheme>();
    schemes.add(CustomSafeUrlScheme.TEL);

    // Default schemes still permitted.
    String url = "http://example.com/";
    SafeUrl safeUrl = SafeUrls.sanitize(url, schemes);
    assertEquals(url, safeUrl.getSafeUrlString());

    // Non-whitelisted schemes still forbidden.
    url = "javascript:evil();";
    assertEquals(SafeUrl.INNOCUOUS, SafeUrls.sanitize(url));

    // 'tel' now allowed.
    url = "tel:+1234567890";
    safeUrl = SafeUrls.sanitize(url, schemes);
    assertEquals(url, safeUrl.getSafeUrlString());
  }

  public void testHtmlDataUrl() {
    SafeHtml html = HtmlConversions.newSafeHtmlForTest("<h1>Hello World!!?!</h1>..");
    SafeUrl dataUrl = SafeUrls.createHtmlDataUrl(html);
    assertEquals(
        "data:text/html;charset=UTF-8;base64,PGgxPkhlbGxvIFdvcmxkISE/ITwvaDE+Li4=",
        dataUrl.getSafeUrlString());
  }
}
