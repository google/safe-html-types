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

import com.google.common.annotations.GwtCompatible;
import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;

/** Unit tests for {@link SafeStyleSheet}. */
@GwtCompatible
public class SafeStyleSheetTest extends TestCase {

  public void testToString_returnsDebugString() {
    assertEquals(
        "SafeStyleSheet{P.special { color:red ; }}",
        SafeStyleSheets.fromConstant("P.special { color:red ; }").toString());
  }

  public void testEqualsAndHashCode() {
    new EqualsTester()
        .addEqualityGroup(SafeStyleSheet.EMPTY, SafeStyleSheets.fromConstant(""))
        .addEqualityGroup(
            SafeStyleSheets.fromConstant("P.special { color:red ; }"),
            SafeStyleSheets.fromConstant("P.special { color:red ; " + "}"))
        .addEqualityGroup(
            SafeStyleSheets.fromConstant("P.special { color:green ; }"),
            SafeStyleSheets.fromConstant("P.special { color:green ; }"))
        .testEquals();
  }
}
