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

import static com.google.common.html.types.UncheckedConversions.safeHtmlFromStringKnownToSatisfyTypeContract;
import static com.google.common.html.types.UncheckedConversions.safeScriptFromStringKnownToSatisfyTypeContract;
import static com.google.common.html.types.UncheckedConversions.safeStyleFromStringKnownToSatisfyTypeContract;
import static com.google.common.html.types.UncheckedConversions.safeStyleSheetFromStringKnownToSatisfyTypeContract;
import static com.google.common.html.types.UncheckedConversions.safeUrlFromStringKnownToSatisfyTypeContract;
import static com.google.common.html.types.UncheckedConversions.trustedResourceUrlFromStringKnownToSatisfyTypeContract;
import static com.google.common.html.types.testing.assertions.Assertions.assertClassIsNotExportable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;

import junit.framework.TestCase;

/**
 * Unit tests for {@link UncheckedConversions}.
 */
@GwtCompatible
public class UncheckedConversionsTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testNotExportable() {
    assertClassIsNotExportable(UncheckedConversions.class);
  }

  public void testSafeHtmlFromStringKnownToSatisfyTypeContract() {
    String html = "<script>this is not valid SafeHtml";
    assertEquals(
        html,
        safeHtmlFromStringKnownToSatisfyTypeContract(html).getSafeHtmlString());
  }

  public void testSafeScriptFromStringKnownToSatisfyTypeContract() {
    String script = "invalid SafeScript";
    assertEquals(
        script,
        safeScriptFromStringKnownToSatisfyTypeContract(script).getSafeScriptString());
  }

  public void testSafeStyleFromStringKnownToSatisfyTypeContract() {
    String style = "width:expression(this is not valid SafeStyle";
    assertEquals(
        style,
        safeStyleFromStringKnownToSatisfyTypeContract(style).getSafeStyleString());
  }

  public void testSafeStyleSheetFromStringKnownToSatisfyTypeContract() {
    String styleSheet = "selector { not a valid SafeStyleSheet";
    assertEquals(
        styleSheet,
        safeStyleSheetFromStringKnownToSatisfyTypeContract(styleSheet).getSafeStyleSheetString());
  }

  public void testSafeUrlFromStringKnownToSatisfyTypeContract() {
    String url = "data:this will not be sanitized";
    assertEquals(
        url,
        safeUrlFromStringKnownToSatisfyTypeContract(url).getSafeUrlString());
  }

  public void testTrustedResourceUrlFromStringKnownToSatisfyTypeContract() {
    String url = "data:this will not be sanitized";
    assertEquals(
        url,
        trustedResourceUrlFromStringKnownToSatisfyTypeContract(url).getTrustedResourceUrlString());
  }
}
