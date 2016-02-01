package com.example;

import com.google.common.html.types.SafeHtml;
import com.google.common.html.types.SafeHtmlBuilder;
import com.google.common.html.types.SafeUrls;

/**
 * This is an example of how the Safe HTML types, in conjunction with
 * the ErrorProne source code checker can be used to guide developers
 * away from practices that lead to security bugs.
 */
public final class Example {
  public static SafeHtml makeLink(String href) {
    SafeUrl safeHref;

    safeHref = SafeUrls.fromConstant(href);  // href is not a constant!!!
    // If href is "javascript:doEvil()" we could be in trouble.

    // Instead, sanitize the URL.
//  safeHref = SafeUrls.sanitize(href);

    // Use the builder to make the link.
    return new SafeHtmlBuilder("a")
      .setHref(safeHref)
      .build();
  }

  public static void main(String... argv) {
    String href;
    if (argv.length == 0) {
      href = "http://example.com/";
    } else {
      href = argv[0];
    }
    SafeHtml safeHtml = makeLink(href);
    System.out.println("Safe Link:" + safeHtml.getSafeHtmlString());
  }
}
