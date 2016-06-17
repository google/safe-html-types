#!/bin/bash

set -e

export PROJECT_BUILD_OUTPUTDIRECTORY="$1"

if ! [ -d "$PROJECT_BUILD_OUTPUTDIRECTORY" ]; then
    echo "Usage $0 target/classes"
    exit 1
fi

export TYPES_PACKAGE="$PROJECT_BUILD_OUTPUTDIRECTORY/com/google/common/html/types"
export META_DIR="$PROJECT_BUILD_OUTPUTDIRECTORY/META-INF"

mkdir -p "$META_DIR"

echo "PROJECT_BUILD_OUTPUTDIRECTORY=$PROJECT_BUILD_OUTPUTDIRECTORY"
echo "META_DIR=$META_DIR"
echo "TYPES_PACKAGE=$TYPES_PACKAGE"

export JAVAP="$(which javap)"
if [ -z "$JAVAP" ]; then
  export JAVAP="$JAVA_HOME/bin/javap"
fi

(
    echo "
<!-- See https://github.com/mikesamuel/fences-maven-enforcer-rule/blob/master/src/site/markdown/configuration.md -->
<configuration>
  <package>
    <name>com.google.common.html.types</name>
    <class>
      <name>UncheckedConversions</name>
      <distrusts>*</distrusts>
      <trusts>com.google.common.html.types.UncheckedConversions</trusts>
      <trusts>com.google.common.html.types.testing.HtmlConversions</trusts>
      <rationale>
        UncheckedConversions should be used rarely and all uses must
        be thoroughly checked.  See https://goo.gl/1JTTHd
      </rationale>
    </class>
    <package>
      <name>testing</name>
      <distrusts>*</distrusts>
      <trusts>com.google.common.html.types.testing</trusts>
      <rationale>
        \${fences.api} sould only be used from testing code.
        Make sure it's included in &lt;scope&gt;test&lt;/scope&gt;.
      </rationale>
    </package>"

    for f in "$TYPES_PACKAGE/"*Proto.class "$TYPES_PACKAGE/"*'Proto$Builder.class'
    do
        export CLASSNAME="$(basename "$f" ".class")"
        export METHODS="$(
          $JAVAP -classpath "$PROJECT_BUILD_OUTPUTDIRECTORY" \
                "com/google/common/html/types/$CLASSNAME" \
              | grep DoNotAccessOrElse \
              | perl -ne 'print "$1\n" if m/public .*? ((?!get|has)\w*DoNotAccessOrElse\w*)\(/;'
        )"

        if [ -n "$METHODS" ]; then

            export SIMPLE_TYPE="$(basename "$(basename "$CLASSNAME" '$Builder')" Proto)"

            export RATIONALE="
        Improper use of $CLASSNAME can result in values that do
        not obey their type contracts.  Instead use ""$SIMPLE_TYPE""s.
        https://github.com/google/safe-html-types
        "

            echo "    <class>
      <name>$CLASSNAME</name>"

            for m in $METHODS; do
                echo "      <method>
        <name>$m</name>
        <trusts>com.google.common.html.types</trusts>
        <distrusts>*</distrusts>
        <rationale>$RATIONALE</rationale>
      </method>"
            done

            echo "    </class>"
        fi
    done
echo "  </package>
</configuration>"

) > "$META_DIR/fences.xml"
