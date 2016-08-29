// **** GENERATED CODE, DO NOT MODIFY ****
// This file was generated via preprocessing from input:
// java/com/google/common/html/types/SafeHtmls.java.tpl
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

import static com.google.common.html.types.BuilderUtils.coerceToInterchangeValid;
import static com.google.common.html.types.BuilderUtils.escapeHtmlInternal;

import com.google.common.annotations.GwtCompatible;

/**
 * Protocol conversions, builders and factory methods for {@link SafeHtml}.
 */
@GwtCompatible
public final class SafeHtmls {

  /**
   * Deserializes a SafeHtmlProto into a SafeHtml instance.
   *
   * <p>Protocol-message forms are intended to be opaque. The fields of the protocol message should
   * be considered encapsulated and are not intended for direct inspection or manipulation. Protocol
   * message forms of this type should be produced by {@link #toProto(SafeHtml)} or its
   * equivalent in other implementation languages.
   *
   * <p><b>Important:</b> It is unsafe to invoke this method on a protocol message that has been
   * received from an entity outside the application's trust domain. Data coming from the browser
   * is outside the application's trust domain.
   */
  public static SafeHtml fromProto(SafeHtmlProto proto) {
    return create(proto.getPrivateDoNotAccessOrElseSafeHtmlWrappedValue());
  }

  /**
   * Wraps a SafeScript inside a &lt;script type="text/javascript"&gt; tag.
   */
  public static SafeHtml fromScript(SafeScript script) {
    return create("<script type=\"text/javascript\">" + script.getSafeScriptString() + "</script>");
  }

  /**
   * Creates a &lt;script type="text/javascript" src="<i>url</i>"&gt;&lt;script&gt; where the
   * {@code src} attribute points to the given {@code trustedResourceUrl}.
   */
  public static SafeHtml fromScriptUrl(TrustedResourceUrl trustedResourceUrl) {
    String escapedUrl = htmlEscapeInternal(trustedResourceUrl.getTrustedResourceUrlString());
    return create("<script type=\"text/javascript\" src=\"" + escapedUrl + "\"></script>"); 
  }

  /**
   * Serializes a SafeHtml into its opaque protocol message representation.
   *
   * <p>Protocol message forms of this type are intended to be opaque. The fields of the returned
   * protocol message should be considered encapsulated and are not intended for direct inspection
   * or manipulation. Protocol messages can be converted back into a SafeHtml using
   * {@link #fromProto(SafeHtmlProto)}.
   */
  public static SafeHtmlProto toProto(SafeHtml safeHtml) {
    return SafeHtmlProto.newBuilder().setPrivateDoNotAccessOrElseSafeHtmlWrappedValue(
        safeHtml.getSafeHtmlString()).build();
  }

  /**
   * Converts, by HTML-escaping, an arbitrary string into a contract-compliant {@link SafeHtml}.
   */
  public static SafeHtml htmlEscape(String text) {
    return create(htmlEscapeInternal(text));
  }

  /**
   * Converts an arbitrary string into an HTML comment by HTML-escaping the contents and embedding
   * the result between HTML comment markers.
   *
   * <p>Escaping is needed because Internet Explorer supports conditional comments and so may render
   * HTML markup within comments.
   */
   public static SafeHtml comment(String text) {
     return create("<!--" + htmlEscapeInternal(text) + "-->");
   }


  /**
   * Creates a new SafeHtml which contains, in order, the string representations of the given
   * {@code htmls}.
   */
  public static SafeHtml concat(SafeHtml... htmls) {
    int concatLength = 0;
    for (int i = 0; i < htmls.length; i++) {
      concatLength += htmls[i].getSafeHtmlString().length();
    }

    StringBuilder result = new StringBuilder(concatLength);
    for (int i = 0; i < htmls.length; i++) {
      result.append(htmls[i].getSafeHtmlString());
    }
    return create(result.toString());
  }

  // Default visibility for use by SafeHtmlBuilder.
  static SafeHtml create(String html) {
    return new SafeHtml(html);
  }

  private static String htmlEscapeInternal(String text) {
    return escapeHtmlInternal(coerceToInterchangeValid(text));
  }

  private SafeHtmls() { }
}

