package com.example;

import com.google.common.html.types.SafeHtmlBuilder;
import com.google.common.html.types.SafeHtmlProto;
import com.google.common.html.types.SafeHtmls;
import com.google.common.html.types.SafeUrlProto;
import com.google.common.html.types.SafeUrls;

import java.io.IOException;

/**
 * Safe string values can be serialized and deserialized as
 * <a href="https://developers.google.com/protocol-buffers/">protocol buffers</a>
 * but naive use of the protocol buffer builders can result in
 * values that do not obey the
 * <a href="https://github.com/google/safe-html-types/blob/master/doc/safehtml-types.md">type contract</a>.
 * <p>
 * The <a href="https://github.com/google/safe-html-types/blob/master/examples/banned_use_of_proto_setter/pom.xml">pom.xml</a> file
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
    public static SafeUrlProto makeSafeUrlProto(String href) {
      // CAVEAT: href is not known to satisfy the type contract.
      // If href is "javascript:doEvil()" we could be in trouble.
      return SafeUrlProto.newBuilder()
          // The hint is in the name.
          .setPrivateDoNotAccessOrElseSafeUrlWrappedValue(href)
          .build();
    }
  }

  public static SafeHtmlProto makeLink(String href) {
    SafeUrlProto safeHref = Unapproved.makeSafeUrlProto(href);

    // Use the builder to make the link.
    return SafeHtmls.toProto(
        new SafeHtmlBuilder("a")
        .setHref(SafeUrls.fromProto(safeHref))
        .build());
  }

  public static void main(String... argv) throws IOException {
    String href;
    if (argv.length == 0) {
      href = "http://example.com/";
    } else {
      href = argv[0];
    }
    SafeHtmlProto safeHtmlProto = makeLink(href);
    System.out.println("Serialized safe Link");
    safeHtmlProto.writeTo(System.out);
  }
}
