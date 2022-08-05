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
import com.google.errorprone.annotations.CompileTimeConstant;
import junit.framework.TestCase;

/** Unit tests for {@link TrustedResourceUrlBuilder}. */
@GwtCompatible(emulated = true)
public class TrustedResourceUrlBuilderTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(TrustedResourceUrlBuilder.class);
  }

  public void testAppendString() {
    TrustedResourceUrlBuilder builder =
        new TrustedResourceUrlBuilder("https://www.google.com/").append("path");

    TrustedResourceUrl trustedResourceUrl = builder.build();
    assertEquals("https://www.google.com/path", trustedResourceUrl.getTrustedResourceUrlString());
  }

  public void testEncodeAndAppendHttps() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("https://google.com/");
    builder.appendEncoded("/%foo");
    builder.appendEncoded("/%foo");
    assertEquals(
        "https://google.com/%2F%25foo%2F%25foo", builder.build().getTrustedResourceUrlString());
  }

  public void testEncodeAndAppendFailHttp() {
    try {
      TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("http://google.com/");
      builder.appendEncoded("/%foo");
    } catch (IllegalArgumentException e) {
      // No assertThrows
    }
  }

  public void testEncodeAndAppendSchemaRelative() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("//google.com/");
    builder.appendEncoded("/%foo");
    assertEquals("//google.com/%2F%25foo", builder.build().getTrustedResourceUrlString());
  }

  public void testEncodeAndAppendPathRelative() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("/start?");
    builder.appendEncoded("/%foo");
    assertEquals("/start?%2F%25foo", builder.build().getTrustedResourceUrlString());
  }

  public void testEncodeInvalidChars() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("/q=");
    builder.appendEncoded("?redirect=evil.org");
    assertEquals("/q=%3Fredirect%3Devil.org", builder.build().getTrustedResourceUrlString());
  }

  public void testFailUnterminatedOrigin() {
    try {
      new TrustedResourceUrlBuilder("http://prefix.com");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // No assertThrows
    }
  }

  public void testFailScript() {
    try {
      // Not an origin
      new TrustedResourceUrlBuilder("script:foo");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // No assertThrows
    }
  }

  public void testValidFormat() {
    // With scheme.
    assertValidFormat("https://www.gOOgle.com/");
    // Scheme-relative.
    assertValidFormat("//www.google.com/");
    // Origin with hyphen and port.
    assertValidFormat("//ww-w.google.com:1000/path");
    // IPv6 origin.
    assertValidFormat("//[::1]/path");
    // Path-absolute.
    assertValidFormat("/path");
    assertValidFormat("/path/x");
    assertValidFormat("/path#x");
    assertValidFormat("/path?x");
    // Mixed case.
    assertValidFormat("https://www.google.cOm/pAth");
    assertValidFormat("about:blank#");
    assertValidFormat("about:blank#x");
    // Relative path.
    assertValidFormat("path/");
    assertValidFormat("path/a");
    assertValidFormat("../");
    assertValidFormat("../a");
    assertValidFormat("?a");
    assertValidFormat("path?a");
    assertValidFormat("path/?a");
    assertValidFormat("#a");
    assertValidFormat("path#a");
    assertValidFormat("path/#a");
  }

  private void assertValidFormat(@CompileTimeConstant String prefix) {
    assertEquals(
        prefix, new TrustedResourceUrlBuilder(prefix).build().getTrustedResourceUrlString());
  }

  public void testInvalidFormat() {
    // Invalid scheme.
    assertInvalidFormat("ftp://");
    // Missing origin.
    assertInvalidFormat("https:");
    assertInvalidFormat("https://");
    assertInvalidFormat("https:///"); // NOTYPO
    assertInvalidFormat("//");
    assertInvalidFormat("///");
    // Missing / after origin.
    assertInvalidFormat("https://google.com");
    // Invalid char in origin.
    assertInvalidFormat("https://www.google%.com/");
    assertInvalidFormat("https://www.google\\.com/");
    assertInvalidFormat("https://user:password@www.google.com/");
    // Two slashes, would allow origin to be set dynamically.
    assertInvalidFormat("//");
    // Two slashes. IE allowed (allows?) '\' instead of '/'.
    assertInvalidFormat("/\\");
    // Path.
    assertInvalidFormat(""); // Allows appending anything.
    assertInvalidFormat("/"); // Allows appending '/'.
    assertInvalidFormat("path"); // Allows appending ':'.
  }

  private void assertInvalidFormat(@CompileTimeConstant String prefix) {
    try {
      new TrustedResourceUrlBuilder(prefix);
      fail("Expected IllegalArgumentException.");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testAppendQueryFirstQueryParameter() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("/s");
    builder.appendQueryParam("q", " test*-._=");
    assertEquals("/s?q=+test*-._%3D", builder.build().getTrustedResourceUrlString());
  }

  public void testAppendQuerySecondQueryParameter() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("/s");
    builder.appendQueryParam(" *-._=test", "value");
    assertEquals("/s?+*-._%3Dtest=value", builder.build().getTrustedResourceUrlString());
  }

  public void testAppendQueryParameterEmptyQuery() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("/s?");
    builder.appendQueryParam("key", "value");
    assertEquals("/s?key=value", builder.build().getTrustedResourceUrlString());
  }

  public void testAppendQueryFragmentThrows() {
    try {
      // Not an origin
      new TrustedResourceUrlBuilder("/#foo").appendQueryParam("key", "val");
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // Expected. No assertThrows.
    }
  }

  public void testAppendQueryDoubleQuestionMarkWorks() {
    TrustedResourceUrlBuilder builder = new TrustedResourceUrlBuilder("/s??");
    builder.appendQueryParam("key", "value");
    // TODO(bangert): This is not valid URL formdata encoding, maybe we should handle this
    // differently?
    assertEquals("/s??&key=value", builder.build().getTrustedResourceUrlString());
  }

  public void testBuildersInSeries() {
    TrustedResourceUrlBuilder builder0 = new TrustedResourceUrlBuilder("/foo?bar=baz");
    TrustedResourceUrlBuilder builder1 = new TrustedResourceUrlBuilder(builder0.build());
    builder1.appendQueryParam("boo", "far");
    assertEquals("/foo?bar=baz&boo=far", builder1.build().getTrustedResourceUrlString());
  }
}
