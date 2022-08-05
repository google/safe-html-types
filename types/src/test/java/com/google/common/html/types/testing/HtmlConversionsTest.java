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

package com.google.common.html.types.testing;

import com.google.common.annotations.GwtCompatible;
import com.google.common.html.types.SafeHtml;
import com.google.common.html.types.SafeHtmlBuilder;
import com.google.common.html.types.SafeHtmlProto;
import com.google.common.html.types.SafeHtmls;
import com.google.common.html.types.SafeScriptProto;
import com.google.common.html.types.SafeScripts;
import com.google.common.html.types.SafeStyleProto;
import com.google.common.html.types.SafeStyleSheetProto;
import com.google.common.html.types.SafeStyleSheets;
import com.google.common.html.types.SafeStyles;
import com.google.common.html.types.SafeUrlProto;
import com.google.common.html.types.SafeUrls;
import com.google.common.html.types.TrustedResourceUrlProto;
import com.google.common.html.types.TrustedResourceUrls;
import junit.framework.TestCase;

/** Unit tests for {@link HtmlConversions}. */
@GwtCompatible
public class HtmlConversionsTest extends TestCase {

  public void testNewSafeHtmlProtoForTest() {
    SafeHtmlProto proto = HtmlConversions.newSafeHtmlProtoForTest("<div>hello</div>");
    SafeHtml html = new SafeHtmlBuilder("div").escapeAndAppendContent("hello").build();
    assertEquals(html, SafeHtmls.fromProto(proto));
  }

  public void testNewSafeScriptProtoForTest() {
    final String scriptString = "functionCall('hello');";
    SafeScriptProto proto = HtmlConversions.newSafeScriptProtoForTest(scriptString);
    assertEquals(SafeScripts.fromConstant(scriptString), SafeScripts.fromProto(proto));
  }

  public void testNewSafeStyleProtoForTest() {
    final String style = "width: 1em;";
    SafeStyleProto proto = HtmlConversions.newSafeStyleProtoForTest(style);
    assertEquals(SafeStyles.fromConstant(style), SafeStyles.fromProto(proto));
  }

  public void testNewSafeStyleSheetProtoForTest() {
    final String styleSheet = "P.special { color:red ; }";
    SafeStyleSheetProto proto = HtmlConversions.newSafeStyleSheetProtoForTest(styleSheet);
    assertEquals(SafeStyleSheets.fromConstant(styleSheet), SafeStyleSheets.fromProto(proto));
  }

  public void testNewSafeUrlProtoForTest() {
    String url = "https://www.google.com";
    SafeUrlProto proto = HtmlConversions.newSafeUrlProtoForTest(url);
    assertEquals(SafeUrls.sanitize(url), SafeUrls.fromProto(proto));
  }

  public void testNewTrustedResourceUrlProtoForTest() {
    final String url = "https://www.google.com";
    TrustedResourceUrlProto proto = HtmlConversions.newTrustedResourceUrlProtoForTest(url);
    assertEquals(TrustedResourceUrls.fromConstant(url), TrustedResourceUrls.fromProto(proto));
  }
}
