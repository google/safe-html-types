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

import static com.google.common.html.types.testing.HtmlConversions.newSafeHtmlForTest;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableList;
import com.google.common.html.types.SpliceableSafeHtml.Segment;
import com.google.common.html.types.SpliceableSafeHtml.Segment.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

/** Unit tests for {@link SpliceableSafeHtml}. */
@GwtCompatible
public class SpliceableSafeHtmlTest extends TestCase {

  public void testSegment_placeholderLabel() {
    Segment segment = Segment.fromPlaceholderLabel("foo");
    assertEquals(Type.PLACEHOLDER_LABEL, segment.getType());
    assertEquals("foo", segment.getPlaceholderLabel());
    assertNull(segment.getSafeHtml());
  }

  public void testSegment_safeHtml() {
    Segment segment = Segment.fromSafeHtml(newSafeHtmlForTest("bar"));
    assertEquals(Type.SAFE_HTML, segment.getType());
    assertEquals("bar", segment.getSafeHtml().getSafeHtmlString());
    assertNull(segment.getPlaceholderLabel());
  }

  public void testToAndFromTemplateProto() {
    List<Segment> segments =
        ImmutableList.of(
            Segment.fromSafeHtml(newSafeHtmlForTest("<div>")),
            Segment.fromPlaceholderLabel("foo"),
            Segment.fromSafeHtml(newSafeHtmlForTest("</div>")));
    SpliceableSafeHtml expected = new SpliceableSafeHtml(segments);
    SpliceableSafeHtml actual = SpliceableSafeHtml.fromProto(expected.toProto());
    assertEquals("<div><!--foo--></div>", actual.toString());
  }

  public void testFromSafeHtml() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(newSafeHtmlForTest("<div><!--foo--></div>"));
    assertEquals("<div><!--foo--></div>", spliceableSafeHtml.toString());
  }

  public void testGetPlaceholderLabels_noLabels() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(Collections.<Segment>emptyList());
    assertEquals(0, spliceableSafeHtml.getPlaceholderLabels().size());
  }

  public void testGetPlaceholderLabels_multipleLabels() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(Segment.fromPlaceholderLabel("a"), Segment.fromPlaceholderLabel("b")));
    Set<String> labels = spliceableSafeHtml.getPlaceholderLabels();
    assertEquals(2, labels.size());
    assertTrue(labels.contains("a"));
    assertTrue(labels.contains("b"));
  }

  public void testGetPlaceholderLabels_duplicateLabels() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(Segment.fromPlaceholderLabel("a"), Segment.fromPlaceholderLabel("a")));
    Set<String> labels = spliceableSafeHtml.getPlaceholderLabels();
    assertEquals(1, labels.size());
    assertTrue(labels.contains("a"));
  }

  public void testToString() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<b>Hello ")),
                Segment.fromPlaceholderLabel("type"),
                Segment.fromSafeHtml(newSafeHtmlForTest(" World</b>"))));
    assertEquals("<b>Hello <!--type--> World</b>", spliceableSafeHtml.toString());
  }

  public void testGetSafeHtml_NoPlaceholdersOneStatic() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(Segment.fromSafeHtml(newSafeHtmlForTest("<b>Hello World</b>"))));
    assertEquals("<b>Hello World</b>", spliceableSafeHtml.getSafeHtml().getSafeHtmlString());
  }

  public void testGetSafeHtml_NoPlaceholdersMultipleStatic() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<b>Hello ")),
                Segment.fromSafeHtml(newSafeHtmlForTest("World</b>"))));
    assertEquals("<b>Hello World</b>", spliceableSafeHtml.getSafeHtml().getSafeHtmlString());
  }

  public void testGetSafeHtml_WithPlaceholder() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<b>Hello ")),
                Segment.fromPlaceholderLabel("type"),
                Segment.fromSafeHtml(newSafeHtmlForTest("World</b>"))));
    try {
      spliceableSafeHtml.getSafeHtml();
      fail("Calling getSafeHtml with placeholders should throw an IllegalArgumentException.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  public void testGetSafeHtmlIgnoringPlaceholders() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<b>Hello ")),
                Segment.fromPlaceholderLabel("type"),
                Segment.fromSafeHtml(newSafeHtmlForTest("World</b>"))));
    assertEquals(
        "<b>Hello World</b>",
        spliceableSafeHtml.getSafeHtmlIgnoringPlaceholders().getSafeHtmlString());
  }

  public void testSpliceOne() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<b>Hello ")),
                Segment.fromPlaceholderLabel("type"),
                Segment.fromSafeHtml(newSafeHtmlForTest(" World</b>"))));
    SafeHtml spliced = spliceableSafeHtml.spliceOne("type", newSafeHtmlForTest("Wonderful"));
    assertEquals("<b>Hello Wonderful World</b>", spliced.getSafeHtmlString());
  }

  public void testSpliceAll() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<div>")),
                Segment.fromPlaceholderLabel("before"),
                Segment.fromSafeHtml(newSafeHtmlForTest("<span>")),
                Segment.fromPlaceholderLabel("in"),
                Segment.fromSafeHtml(newSafeHtmlForTest("</span>")),
                Segment.fromPlaceholderLabel("after"),
                Segment.fromSafeHtml(newSafeHtmlForTest("</div>"))));
    Map<String, SafeHtml> assignments = new HashMap<>();
    assignments.put("before", newSafeHtmlForTest("AAA"));
    assignments.put("in", newSafeHtmlForTest("BBB"));
    assignments.put("after", newSafeHtmlForTest("CCC"));
    SafeHtml spliced = spliceableSafeHtml.spliceAll(assignments);
    assertEquals("<div>AAA<span>BBB</span>CCC</div>", spliced.getSafeHtmlString());
  }

  public void testSpliceAll_MissingAssignment() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(Segment.fromPlaceholderLabel("a"), Segment.fromPlaceholderLabel("b")));
    Map<String, SafeHtml> assignments = new HashMap<>();
    assignments.put("a", newSafeHtmlForTest("1"));
    try {
      spliceableSafeHtml.spliceAll(assignments);
      fail("Calling render with an assignment missing should throw an IllegalArgumentException.");
    } catch (IllegalArgumentException e) {
      // Pass.
    }
  }

  public void testSpliceSome() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<div>")),
                Segment.fromPlaceholderLabel("before"),
                Segment.fromSafeHtml(newSafeHtmlForTest("<span>")),
                Segment.fromPlaceholderLabel("in"),
                Segment.fromSafeHtml(newSafeHtmlForTest("</span>")),
                Segment.fromPlaceholderLabel("after"),
                Segment.fromSafeHtml(newSafeHtmlForTest("</div>"))));
    Map<String, SafeHtml> assignments = new HashMap<>();
    assignments.put("before", newSafeHtmlForTest("AAA"));
    assignments.put("in", newSafeHtmlForTest("BBB"));
    SafeHtml spliced = spliceableSafeHtml.spliceSome(assignments);
    assertEquals("<div>AAA<span>BBB</span></div>", spliced.getSafeHtmlString());
  }

  public void testSpliceSomePreservingPlaceholders() {
    SpliceableSafeHtml spliceableSafeHtml =
        new SpliceableSafeHtml(
            ImmutableList.of(
                Segment.fromSafeHtml(newSafeHtmlForTest("<div>")),
                Segment.fromPlaceholderLabel("before"),
                Segment.fromSafeHtml(newSafeHtmlForTest("<span>")),
                Segment.fromPlaceholderLabel("in"),
                Segment.fromSafeHtml(newSafeHtmlForTest("</span>")),
                Segment.fromPlaceholderLabel("after"),
                Segment.fromSafeHtml(newSafeHtmlForTest("</div>"))));
    Map<String, SafeHtml> assignments = new HashMap<>();
    assignments.put("before", newSafeHtmlForTest("AAA"));
    assignments.put("in", newSafeHtmlForTest("BBB"));
    SpliceableSafeHtml spliced = spliceableSafeHtml.spliceSomePreservingPlaceholders(assignments);
    assertEquals("<div>AAA<span>BBB</span><!--after--></div>", spliced.toString());
  }
}
