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

import static com.google.common.html.types.LegacyConversions.riskilyAssumeSafeHtml;
import static com.google.common.html.types.LegacyConversions.riskilyAssumeSafeScript;
import static com.google.common.html.types.LegacyConversions.riskilyAssumeSafeStyle;
import static com.google.common.html.types.LegacyConversions.riskilyAssumeSafeStyleSheet;
import static com.google.common.html.types.LegacyConversions.riskilyAssumeSafeUrl;
import static com.google.common.html.types.LegacyConversions.riskilyAssumeTrustedResourceUrl;

import com.google.common.annotations.GwtCompatible;

import junit.framework.TestCase;

/**
 * Unit tests for {@link LegacyConversions}.
 */
@GwtCompatible
public class LegacyConversionsTest extends TestCase {

  public void testRiskilyAssumeSafeHtml() {
    String html = "<script>this is not valid SafeHtml";
    assertEquals(
        html,
        riskilyAssumeSafeHtml(html).getSafeHtmlString());
  }

  public void testRiskilyAssumeSafeScript() {
    String script = "invalid SafeScript";
    assertEquals(
        script,
        riskilyAssumeSafeScript(script).getSafeScriptString());
  }

  public void testRiskilyAssumeSafeStyle() {
    String style = "width:expression(this is not valid SafeStyle";
    assertEquals(
        style,
        riskilyAssumeSafeStyle(style).getSafeStyleString());
  }

  public void testRiskilyAssumeSafeStyleSheet() {
    String styleSheet = "selector { not a valid SafeStyleSheet";
    assertEquals(
        styleSheet,
        riskilyAssumeSafeStyleSheet(styleSheet).getSafeStyleSheetString());
  }

  public void testRiskilyAssumeSafeUrl() {
    String url = "data:this will not be sanitized";
    assertEquals(
        url,
        riskilyAssumeSafeUrl(url).getSafeUrlString());
  }

  public void testRiskilyAssumeTrustedResourceUrl() {
    String url = "data:this will not be sanitized";
    assertEquals(
        url,
        riskilyAssumeTrustedResourceUrl(url).getTrustedResourceUrlString());
  }
}
