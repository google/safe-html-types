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
import com.google.common.primitives.Chars;
import com.google.errorprone.annotations.CompileTimeConstant;
import junit.framework.TestCase;

/** Unit tests for {@link SafeStyleBuilder}. */
@GwtCompatible
public class SafeStyleBuilderTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeStyleBuilder.class);
  }

  public void testEmpty() {
    SafeStyle style = new SafeStyleBuilder().build();
    assertEquals("", style.getSafeStyleString());
  }

  public void testConstantDisallowsUnsafeCharacters() {
    assertConstantNotAllowed("<");
    assertConstantNotAllowed(">");
    assertConstantNotAllowed("'");
    assertConstantNotAllowed("/*");
    assertConstantNotAllowed("//");
    assertConstantNotAllowed("*/");
    SafeStyle style = new SafeStyleBuilder().backgroundImageAppendConstant("/").build();
    assertEquals("background-image:/;", style.getSafeStyleString());
  }

  private static void assertConstantNotAllowed(@CompileTimeConstant final String constant) {
    try {
      new SafeStyleBuilder().backgroundImageAppendConstant(constant);
      fail("Should throw when using constant: " + constant);
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testAllowsEnumProperty() {
    SafeStyle style = new SafeStyleBuilder().backgroundAttachmentAppend("aZ").build();
    assertEquals("background-attachment:aZ;", style.getSafeStyleString());
  }

  public void testSanitizesEnumProperty() {
    assertbackgroundAttachmentAppendSanitized("");
    assertbackgroundAttachmentAppendSanitized(" ");
    assertbackgroundAttachmentAppendSanitized("\t");
    assertbackgroundAttachmentAppendSanitized("1");
  }

  private static void assertbackgroundAttachmentAppendSanitized(String value) {
    SafeStyle style = new SafeStyleBuilder().backgroundAttachmentAppend(value).build();
    assertEquals("background-attachment:zJSafeHtmlzinvalid;", style.getSafeStyleString());
  }

  public void testSanitizesUrl() {
    SafeStyle style = new SafeStyleBuilder().backgroundImageAppendUrl("javaScript:evil").build();
    assertEquals(
        "background-image:url(" + SafeUrl.INNOCUOUS_STRING + ");", style.getSafeStyleString());

    style = new SafeStyleBuilder().backgroundImageAppendUrl("http://google.com;a,b(c%d").build();
    assertEquals(
        "background-image:url(http://google.com%3Ba%2Cb%28c%d);", style.getSafeStyleString());

    // Invalid lead/high surrogate.
    style = new SafeStyleBuilder().backgroundImageAppendUrl("http://google.com/\udc00").build();
    assertEquals("background-image:url(zJSafeHtmlzinvalid);", style.getSafeStyleString());

    // Invalid trail/low surrogate.
    style =
        new SafeStyleBuilder().backgroundImageAppendUrl("http://google.com/\ud800\ud800").build();
    assertEquals("background-image:url(zJSafeHtmlzinvalid);", style.getSafeStyleString());
  }

  public void testAllowsRegularProperty() {
    // Test boundaries.
    SafeStyle style = new SafeStyleBuilder().backgroundSizeAppend("09azAZ").build();
    assertEquals("background-size:09azAZ;", style.getSafeStyleString());

    style = new SafeStyleBuilder().backgroundSizeAppend("%#! foo +4 *d /a").build();
    assertEquals("background-size:%#! foo +4 *d /a;", style.getSafeStyleString());
  }

  public void testSanitizesRegularProperty() {
    // Test boundaries.
    char[] boundaryChars = {
        /* '/' is allowed */
      ':', '@', '[', '`', '{'
    };
    for (Character c : Chars.asList(boundaryChars)) {
      assertBackgroundSizeStringSanitized(c.toString());
    }

    // Empty strings.
    assertBackgroundSizeStringSanitized("");
    assertBackgroundSizeStringSanitized(" ");
    assertBackgroundSizeStringSanitized("\t");
    // Unlike jslayout, comma is not allowed.
    assertBackgroundSizeStringSanitized(",");
    // Disallowed characters.
    assertBackgroundSizeStringSanitized(";");
    // Function call characters.
    assertBackgroundSizeStringSanitized("(");
    assertBackgroundSizeStringSanitized(")");
    assertBackgroundSizeStringSanitized("calc()");
    // Newlines.
    assertBackgroundSizeStringSanitized("\n");
    assertBackgroundSizeStringSanitized("\r");
    // Comments.
    assertBackgroundSizeStringSanitized("foo /*");
    assertBackgroundSizeStringSanitized("foo //foo");
    // Escape sequences.
    assertBackgroundSizeStringSanitized("\\ff ");
  }

  private static void assertBackgroundSizeStringSanitized(String value) {
    SafeStyle style = new SafeStyleBuilder().backgroundSizeAppend(value).build();
    assertEquals("background-size:zJSafeHtmlzinvalid;", style.getSafeStyleString());
  }

  public void testSetterVarArgsForEnumValue() {
    SafeStyle style =
        new SafeStyleBuilder().backgroundAttachmentAppend("", "fixed", "local", "").build();
    assertEquals(
        "background-attachment:zJSafeHtmlzinvalid,fixed,local,zJSafeHtmlzinvalid;",
        style.getSafeStyleString());
  }

  public void testSetterVarArgsForRegularValue() {
    SafeStyle style =
        new SafeStyleBuilder().backgroundSizeAppend("", "3em 25%", "auto", "").build();
    assertEquals(
        "background-size:zJSafeHtmlzinvalid,3em 25%,auto,zJSafeHtmlzinvalid;",
        style.getSafeStyleString());
  }

  public void testSanitizesFontFamily() {
    SafeStyle style =
        new SafeStyleBuilder()
            .fontFamilyAppend(
                "Times New Roman", "sans-serif", "inherit", "", " ", "\t", "\"Evil", "\n")
            .build();
    assertEquals(
        "font-family:\"Times New Roman\",sans-serif,inherit,zJSafeHtmlzinvalid,"
            + "zJSafeHtmlzinvalid,zJSafeHtmlzinvalid,zJSafeHtmlzinvalid,zJSafeHtmlzinvalid;",
        style.getSafeStyleString());
  }

  public void testMultipleProperties() {
    SafeStyle style =
        new SafeStyleBuilder()
            .backgroundImageAppendConstant("img1")
            .backgroundImageAppendConstant("img2")
            .backgroundSizeAppend("backgroundSize")
            .backgroundAttachmentAppend("fixed")
            .build();
    assertEquals(
        "background-image:img1,img2;background-size:backgroundSize;background-attachment:fixed;",
        style.getSafeStyleString());
  }

  public void testLinearGradient() {
    SafeStyle style =
        new SafeStyleBuilder()
            .backgroundLinearGradient("135deg", "#aabbcc", "#abcdef 60%", "#123456")
            .build();
    assertEquals(
        "background:linear-gradient(135deg,#aabbcc,#abcdef 60%,#123456);",
        style.getSafeStyleString());
  }

  public void testLinearGradientAllSupportedColorFormats() {
    SafeStyle style =
        new SafeStyleBuilder()
            .backgroundLinearGradient(
                "135deg",
                "rgba(100, 0, 50, 1.0) 0",
                "rgba(222,1,0,1.0)10%",
                "rgb(50,50,0)     ",
                "hsla(360, 54%, 41%, 1.0) 40%",
                "hsl(267, 13%, 99%)     ",
                "red",
                "orange 80%",
                "#ff00ff ",
                "#00ffff 100%")
            .build();
    assertEquals(
        "background:linear-gradient(135deg,"
            + "rgba(100, 0, 50, 1.0) 0,rgba(222,1,0,1.0)10%,rgb(50,50,0)     ,"
            + "hsla(360, 54%, 41%, 1.0) 40%,hsl(267, 13%, 99%)     ,"
            + "red,orange 80%,"
            + "#ff00ff ,#00ffff 100%);",
        style.getSafeStyleString());
  }

  public void testSanitizesIllegalFunctionCall() {
    assertLinearGradientStringSanitized("expression(100,0,50,1.0) 0");
  }

  public void testSanitizesIllegalRGBAFunctionCall() {
    assertLinearGradientStringSanitized("rgba;(100,0,50,1.0) 0");
  }

  public void testSanitizesNestedFunctionCall() {
    assertLinearGradientStringSanitized("rgba(expression(),0,50,1.0) 0");
  }

  public void testSanitizesUnbalancedParentheses() {
    assertLinearGradientStringSanitized("expression(100,0,50,1.0 0");
  }

  public void testSanitizesNoSpaceBeforeLastParameter() {
    assertLinearGradientStringSanitized("}body{font:\"Comic Sans MS\"}c{color:rgb(0,0,0)");
  }

  public void testSanitizesNewlineSeparator() {
    assertLinearGradientStringSanitized("rgba(100,0,50,1.0)\n0");
  }

  public void testSanitizesOrphanedOpenParenthesis() {
    assertLinearGradientStringSanitized("expression(");
  }

  public void testSanitizesOrphanedOpenParenthesisWithAlert() {
    assertLinearGradientStringSanitized("expression(alert(1//");
  }

  public void testSanitizesOrphanedOpenParenthesisAllowedFunction() {
    assertLinearGradientStringSanitized("rgba(");
  }

  private static void assertLinearGradientStringSanitized(String value) {
    SafeStyle style = new SafeStyleBuilder().backgroundLinearGradient("135deg", value).build();
    assertEquals(
        "background:linear-gradient(135deg,zJSafeHtmlzinvalid);", style.getSafeStyleString());
  }

  public void testColorRgba() {
    SafeStyle style =
        new SafeStyleBuilder()
            .color("rgba(100, 0, 50, .40)")
            .backgroundColor("rgba(102,24,213,.24)")
            .borderColor("rgba(24,0,255,1.0)")
            .build();
    assertEquals(
        "color:rgba(100, 0, 50, .40);background-color:rgba(102,24,213,.24);"
            + "border-color:rgba(24,0,255,1.0);",
        style.getSafeStyleString());
  }

  public void testColorRgb() {
    SafeStyle style =
        new SafeStyleBuilder()
            .color("rgb(255,52 ,0)")
            .backgroundColor("rgb(123, 0,25)")
            .borderColor("rgb(42, 1, 200)")
            .build();
    assertEquals(
        "color:rgb(255,52 ,0);background-color:rgb(123, 0,25);border-color:rgb(42, 1, 200);",
        style.getSafeStyleString());
  }

  public void testColorHsla() {
    SafeStyle style =
        new SafeStyleBuilder()
            .color("hsla(360, 54%, 41%, 1.0)")
            .backgroundColor("hsla(360,54%,41%,1.0)")
            .borderColor("hsla(360, 54%, 41%, 1.0) ")
            .build();
    assertEquals(
        "color:hsla(360, 54%, 41%, 1.0);background-color:hsla(360,54%,41%,1.0);"
            + "border-color:hsla(360, 54%, 41%, 1.0) ;",
        style.getSafeStyleString());
  }

  public void testColorHsl() {
    SafeStyle style =
        new SafeStyleBuilder()
            .color("hsl(267, 13%, 99%)")
            .backgroundColor("hsl(267,13%,99%)")
            .borderColor("hsl(267, 13%, 99%) ")
            .build();
    assertEquals(
        "color:hsl(267, 13%, 99%);background-color:hsl(267,13%,99%);"
            + "border-color:hsl(267, 13%, 99%) ;",
        style.getSafeStyleString());
  }

  public void testColorRgbUntrimmed() {
    SafeStyle style = new SafeStyleBuilder().color(" rgb(130,255,0)").build();
    assertEquals("color: rgb(130,255,0);", style.getSafeStyleString());
  }

  public void testSanitizesColorRgbCloseParamNotLast() {
    assertColorStringSanitized("rgb(130,52,0) 0");
  }

  public void testSanitizesIllegalRgbMissingCloseParam() {
    assertColorStringSanitized("rgb(130,52,23");
  }

  private static void assertColorStringSanitized(String value) {
    SafeStyle style = new SafeStyleBuilder().color(value).build();
    assertEquals("color:zJSafeHtmlzinvalid;", style.getSafeStyleString());
  }
}
