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

import static com.google.common.html.types.testing.HtmlConversions.newSafeStyleForTest;

import com.google.common.annotations.GwtCompatible;
import com.google.common.testing.EqualsTester;

import junit.framework.TestCase;

/**
 * Unit tests for {@link SafeStyle} and its factory methods.
 */
@GwtCompatible
public class SafeStyleTest extends TestCase {

  // TODO(mlourenco): Remove usage newSafeStyleForTest once we have a GWT
  // version of builders.

  public void testToString_returnsDebugString() {
    assertEquals(
        "SafeStyle{width: 1em;}",
        newSafeStyleForTest("width: 1em;").toString());
  }

  public void testEqualsAndHashCode() {
    new EqualsTester()
        .addEqualityGroup(
            newSafeStyleForTest("width: 1em;"),
            newSafeStyleForTest("width: 1em;"))
        .addEqualityGroup(
            newSafeStyleForTest("height: 1em;"),
            newSafeStyleForTest("height: 1em;"))
        .addEqualityGroup(
            newSafeStyleForTest("width: 2em;"),
            newSafeStyleForTest("width: 2em;"))
        .testEquals();
  }
}
