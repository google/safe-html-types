# Banned Use of `fromConstant`

The [Error Prone checker](https://github.com/google/error-prone) checks that
values passed to `@CompileTimeConstant` method parameters are, in fact,
compile time constants like `"foo"`, not values that are not obviously
under the code author's control.

When a developer writes

```java
SafeUrls.fromConstant("http://example.com/")
```

we trust that value to the same degree we trust the author.

The same is not true of arbitrary expressions.

```java
SafeUrls.fromConstant(x)
```


Try building the project by running

```bash
.../safe-html-types/examples/banned_use_of_from_constant$ mvn
[INFO] Compiling 1 source file to .../safe-html-types/examples/banned_use_of_from_constant/target/classes
.../safe-html-types/examples/banned_use_of_from_constant/src/main/java/com/example/Example.java:14: error: [CompileTimeConstant] Non-compile-time constant expression passed to parameter with @CompileTimeConstant type annotation.
    safeHref = SafeUrls.fromConstant(href);  // href is not a constant!!!
                                     ^
1 error
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
```

The error indicates that `fromConstant` was called with a value that is not
a [compile time constant](http://static.javadoc.io/com.google.errorprone/error_prone_annotations/2.0.8/com/google/errorprone/annotations/CompileTimeConstant.html).

Try editing the code to use
[`SafeUrls.sanitize`](http://www.javadoc.io/doc/com.google.common.html.types/types/1.0.0)
instead which checks at run-time, that `href` is safe.

Then rebuild and `mvn package` should work this time.
