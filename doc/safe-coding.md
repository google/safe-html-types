# Enforcing safe coding


The safe coding style ensures that code is free of vulnerabilities in a
particular class. It requires:

1.  Comprehensive and correct use of
    [inherently safe APIs](safe-apis.md) (such as
    strict HTML templates and `goog.dom.safe` wrapper APIs).
1.  Complete avoidance of the underlying raw, vulnerability-prone APIs (such as
    `.innerHTML` assignment).

Adherence to this safe, "vulnerability-proof" coding style is statically
enforced. In some cases, this is accomplished through the implementation
language's type system, in other cases through the use of custom static checkers
such as [Error Prone](https://github.com/google/error-prone).

## Reviewed exceptions of unsafe API usage {#exceptions}

Inherently safe APIs are designed to satisfy the vast majority of use cases. By
necessity, safe APIs sacrifice flexibility for safety. To accommodate use cases
that cannot be expressed using these safe APIs, we provide unconstrained,
low-level alternatives.

> Important: Usage of such unconstrained APIs carries a risk of vulnerabilities,
> and hence must be security reviewed, and designed to be effectively
> reviewable, according to the guidelines in
> [Safe HTML unchecked conversions](safehtml-unchecked.md).
>
> To track the usage of such unconstrained APIs and to ensure security reviews
> take place, access to them is typically restricted through BUILD visibility
> (if you'd like to know about the details, see
> [Controlling API use](controlling_API_use.md)).
