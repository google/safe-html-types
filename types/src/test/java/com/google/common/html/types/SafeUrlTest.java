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

import static com.google.common.html.types.testing.HtmlConversions.newSafeUrlForTest;

import com.google.common.annotations.GwtCompatible;
import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;

/** Unit tests for {@link SafeUrl}. */
@GwtCompatible
public class SafeUrlTest extends TestCase {

  // TODO(mlourenco): Remove usage newSafeUrlForTest once we have a GWT
  // version of builders.

  public void testToString_returnsDebugString() {
    assertEquals("SafeUrl{url}", newSafeUrlForTest("url").toString());
  }

  public void testEqualsAndHashCode() {
    new EqualsTester()
        .addEqualityGroup(newSafeUrlForTest("url1"), newSafeUrlForTest("url1"))
        .addEqualityGroup(newSafeUrlForTest("url2"), newSafeUrlForTest("url2"))
        .testEquals();
  }
}
