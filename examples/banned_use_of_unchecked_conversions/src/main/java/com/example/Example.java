package com.example;

import com.google.common.html.types.SafeHtml;
import com.google.common.html.types.SafeHtmlBuilder;
import com.google.common.html.types.SafeUrl;
import com.google.common.html.types.UncheckedConversions;

/**
 * <a href="https://github.com/google/safe-html-types/blob/master/doc/safehtml-unchecked.md">
 * Guidelines for Use of Unchecked Conversions</a> explains when to use the
 * {@code UncheckedConversions} APIs and what someone seeking or granting approval should do.
 * <p>
 * The <a href="https://github.com/google/safe-html-types/blob/master/examples/banned_use_of_unchecked_conversions/pom.xml">pom.xml</a> file
 * shows how the
 * <a href="http://mikesamuel.github.io/fences-maven-enforcer-rule/">fences rule</a>
 * can verify that all uses have been reviewed and guide developers towards
 * less easily misused APIs.
 */
public final class Example {

  /**
   * Not on any white-lists.
   */
  public static final class Unapproved {
    public static SafeUrl makeSafeUrl(String href) {
      // CAVEAT: href is not known to satisfy the type contract.
      // If href is "javascript:doEvil()" we could be in trouble.
      return UncheckedConversions.safeUrlFromStringKnownToSatisfyTypeContract(href);
    }
  }

  public static SafeHtml makeLink(String href) {
    SafeUrl safeHref = Unapproved.makeSafeUrl(href);

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
