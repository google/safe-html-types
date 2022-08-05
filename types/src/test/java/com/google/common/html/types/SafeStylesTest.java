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
import junit.framework.TestCase;

/** Unit tests for {@link SafeStyles}. */
@GwtCompatible
public class SafeStylesTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeStyles.class);
  }

  public void testToAndFromProto() {
    final String style = "width: 1em;height: 1em;";
    SafeStyle safeStyle = SafeStyles.fromConstant(style);
    SafeStyleProto proto = SafeStyles.toProto(safeStyle);
    assertEquals(style, SafeStyles.fromProto(proto).getSafeStyleString());
  }

  public void testFromConstant_allowEmptyString() {
    assertSame(SafeStyle.EMPTY, SafeStyles.fromConstant(""));
  }

  public void testFromConstant_throwsOnForbiddenCharacters() {
    try {
      SafeStyles.fromConstant("width: x<;");
      fail("Should throw");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage(), expected.getMessage().contains("Forbidden characters"));
    }

    try {
      SafeStyles.fromConstant("width: x>;");
      fail("Should throw");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage(), expected.getMessage().contains("Forbidden characters"));
    }
  }

  public void testFromConstant_throwsIfNoFinalSemicolon() {
    try {
      SafeStyles.fromConstant("width: 1em");
      fail("Should throw");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testFromConstant_throwsIfNoColon() {
    try {
      SafeStyles.fromConstant("width= 1em;");
      fail("Should throw");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testConcat() {
    SafeStyle concatenated =
        SafeStyles.concat(
            SafeStyles.fromConstant("display: none;"), SafeStyles.fromConstant("width: 1em;"));
    assertEquals("display: none;width: 1em;", concatenated.getSafeStyleString());
  }

  public void testConcat_returnsEmptyStyle() {
    assertEquals(SafeStyle.EMPTY, SafeStyles.concat());
  }
}
