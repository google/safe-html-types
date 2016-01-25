// **** GENERATED CODE, DO NOT MODIFY ****
// This file was generated via preprocessing from input:
// javatests/com/google/common/html/types/TrustedResourceUrlBuilderTest.java.tpl
// ***************************************
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

/**
 * Unit tests for {@link TrustedResourceUrlBuilder}.
 */
@GwtCompatible
public class TrustedResourceUrlBuilderTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(TrustedResourceUrlBuilder.class);
  }

  public void testAppendString() {
    TrustedResourceUrlBuilder builder =
        new TrustedResourceUrlBuilder("http://www.google.com").append("/path");

    TrustedResourceUrl trustedResourceUrl = builder.build();
    assertEquals("http://www.google.com/path", trustedResourceUrl.getTrustedResourceUrlString());
  }
}
