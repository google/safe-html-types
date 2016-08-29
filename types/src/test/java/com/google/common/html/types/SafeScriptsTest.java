// **** GENERATED CODE, DO NOT MODIFY ****
// This file was generated via preprocessing from input:
// javatests/com/google/common/html/types/SafeScriptsTest.java.tpl
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
 * Unit tests for {@link SafeScripts}.
 */
@GwtCompatible
public class SafeScriptsTest extends TestCase {

  @GwtIncompatible("Assertions.assertClassIsNotExportable")
  public void testClassNotExportable() {
    assertClassIsNotExportable(SafeScripts.class);
  }

  public void testToAndFromProto() {
    final String script = "functionCall('hello');";
    SafeScript safeScript = SafeScripts.fromConstant(script);
    SafeScriptProto proto = SafeScripts.toProto(safeScript);
    assertEquals(script, SafeScripts.fromProto(proto).getSafeScriptString());
  }

  public void testFromConstant_allowEmptyString() {
    assertSame(SafeScript.EMPTY, SafeScripts.fromConstant(""));
  }
}
