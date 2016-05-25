package com.example.safehtmltypes;

import com.google.common.html.types.SafeHtml;

import org.owasp.html.Sanitizers;
import org.owasp.html.htmltypes.SafeHtmlMint;

/**
 * Demonstrates that a third-party library can use UncheckedConversions
 */
public final class Example {

  public static void main(String[] argv) {
    SafeHtmlMint mint = SafeHtmlMint.fromPolicyFactory(
        Sanitizers.FORMATTING.and(Sanitizers.BLOCKS));
    for (String arg : argv) {
      SafeHtml html = mint.sanitize(arg);
      System.out.println(html.getSafeHtmlString());
    }
  }
}
