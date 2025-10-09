package net.thevpc.nuts.util;

import java.util.Objects;

/**
 * Represents a predicate (boolean-valued function) of one {@code char}-valued argument.
 * This is the primitive type specialization of {@link java.util.function.Predicate}
 * for {@code char}.
 */
@FunctionalInterface
public interface CharPredicate {
    /**
     * Evaluates this predicate on the given argument.
     *
     * @param c the input argument
     * @return {@code true} if the input argument matches the predicate,
     *         otherwise {@code false}
     */
    boolean test(char c);

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of
     * this predicate and another. When evaluating the composed predicate, if this
     * predicate is {@code false}, then the {@code other} predicate is not evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate representing the short-circuiting logical AND
     * @throws NullPointerException if {@code other} is null
     */
    default CharPredicate and(CharPredicate other) {
        NAssert.requireNonNull(other, "other");
        return c -> test(c) && other.test(c);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate representing the logical negation of this predicate
     */
    default CharPredicate negate() {
        return c -> !test(c);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of
     * this predicate and another. When evaluating the composed predicate, if this
     * predicate is {@code true}, then the {@code other} predicate is not evaluated.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate representing the short-circuiting logical OR
     * @throws NullPointerException if {@code other} is null
     */
    default CharPredicate or(CharPredicate other) {
        NAssert.requireNonNull(other, "other");
        return c -> test(c) || other.test(c);
    }
}
