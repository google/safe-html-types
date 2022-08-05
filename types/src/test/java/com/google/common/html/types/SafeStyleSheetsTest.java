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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/** Unit tests for {@link SafeStyleSheets}. */
@GwtCompatible
public class SafeStyleSheetsTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeStyleSheets.class);
  }

  public void testToAndFromProto() {
    final String styleSheet = "P.special { color:red ; }";
    SafeStyleSheet safeStyleSheet = SafeStyleSheets.fromConstant(styleSheet);
    SafeStyleSheetProto proto = SafeStyleSheets.toProto(safeStyleSheet);
    assertEquals(styleSheet, SafeStyleSheets.fromProto(proto).getSafeStyleSheetString());
  }

  public void testFromConstant_allowEmptyString() {
    assertSame(SafeStyleSheet.EMPTY, SafeStyleSheets.fromConstant(""));
  }

  public void testFromConstant_throwsOnLessThanCharacter() {
    try {
      SafeStyleSheets.fromConstant("x<x");
      fail("Should throw");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage(), expected.getMessage().contains("Forbidden '<' character"));
    }
  }

  public void testConcats() {
    assertEquals("", SafeStyleSheets.concat().getSafeStyleSheetString());

    assertEquals(
        "ab", SafeStyleSheets.concat(SafeStyleSheets.fromConstant("ab")).getSafeStyleSheetString());

    assertEquals(
        "ab",
        SafeStyleSheets.concat(SafeStyleSheets.fromConstant("a"), SafeStyleSheets.fromConstant("b"))
            .getSafeStyleSheetString());

    List<SafeStyleSheet> stylesheets = new ArrayList<>();
    stylesheets.add(SafeStyleSheets.fromConstant("a"));
    stylesheets.add(SafeStyleSheets.fromConstant("b"));
    assertEquals("ab", SafeStyleSheets.concat(stylesheets).getSafeStyleSheetString());
  }

  @GwtIncompatible("SafeStyleSheets.fromResource")
  public void testFromResource() throws Exception {
    SafeStyleSheet styleSheet =
        SafeStyleSheets.fromResource(
            "com/google/common/html/types/resources/style.css", Charset.forName("UTF-8"));

    assertEquals("P.special { color:red ; }\n", styleSheet.getSafeStyleSheetString());
  }

  @GwtIncompatible("SafeStyleSheets.fromResource")
  public void testFromResourceContext() throws Exception {
    SafeStyleSheet styleSheet =
        SafeStyleSheets.fromResource(
            SafeStyleSheetsTest.class, "resources/style.css", Charset.forName("UTF-8"));
    assertEquals("P.special { color:red ; }\n", styleSheet.getSafeStyleSheetString());
  }
}
