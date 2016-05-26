#!/bin/bash

set -e

# This should be run from the project directory.
[ -f pom.xml ]

if [ -z "$MVN" ]; then
  export MVN="$(which mvn)"
fi
[ -n "$MVN" ]

# Older versions of javadoc do not recognize the -Xdoclint flag used in the POM.
export SKIP_JAVADOC="$(echo -- "$TRAVIS_JDK_VERSION" | grep -q openjdk6 && echo -n true || echo -n false)"

# Make sure that the tests run, and we can produce the 3 jars needed
# by maven central
$MVN clean verify \
    org.apache.maven.plugins:maven-javadoc-plugin:jar \
    org.apache.maven.plugins:maven-source-plugin:jar \
    install \
    -Dmaven.javadoc.skip="$SKIP_JAVADOC" \
    -B -V

function fail() {
  echo -- "$*"
  exit -1
}

function require_in_log() {
  grep -q -- "$*" mvn-log.txt \
  || fail "expected '$*' in log of $(basename "$PWD")"
}

function expect_broken_build() {
  ($MVN -B clean verify >& mvn-log.txt) \
  && fail "$(basename "$PWD") should not build"

  require_in_log 'BUILD FAILURE'
}

function expect_successful_build() {
  ($MVN -B clean verify >& mvn-log.txt) \
  || fail "$(basename "$PWD") should build"

  require_in_log 'BUILD SUCCESS'
}

# Run the examples and check their outputs.
pushd examples/banned_use_of_from_constant
  expect_broken_build
  require_in_log \
      'Non-compile-time constant expression passed to parameter with @CompileTimeConstant type annotation.'
popd

pushd examples/banned_use_of_unchecked_conversions
  expect_broken_build
  require_in_log '1 access policy violation'
  # Violation location
  require_in_log \
      'com.example.safehtmltypes:banned_use_of_unchecked_conversions:jar:1.0.0-SNAPSHOT : Example.java : L28'
  # Violation text
  require_in_log \
      'com.google.common.html.types.UncheckedConversions.safeUrlFromStringKnownToSatisfyTypeContract() cannot be accessed from com.example.Example$Unapproved'
  # Rationale
  require_in_log \
      'UncheckedConversions should be used rarely and all uses must'
  # Addendum
  require_in_log 'security@example.com | http://docs/code-quality'
popd

pushd examples/banned_use_of_proto_setter
  expect_broken_build
  require_in_log '1 access policy violation'
  require_in_log \
      'com.google.common.html.types.SafeUrlProto.Builder.setPrivateDoNotAccessOrElseSafeUrlWrappedValue() cannot be accessed from com.example.Example$Unapproved'
  require_in_log \
      'Improper use of SafeUrlProto$Builder can result in values that do'
  require_in_log \
      'not obey their type contracts.  Instead use SafeUrls.'
popd

pushd examples/third_party_library
  expect_successful_build
  require_in_log 'No access policy violations'
popd
