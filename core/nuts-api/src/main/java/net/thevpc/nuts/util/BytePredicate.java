package net.thevpc.nuts.util;

import java.util.Objects;

/**
 * Represents a predicate (boolean-valued function) of one {@code byte}-valued argument.
 * <p>
 * This is the {@code byte}-specialized version of {@link java.util.function.Predicate}.
 * </p>
 */
@FunctionalInterface
public interface BytePredicate {
    /**
     * Evaluates this predicate on the given byte argument.
     *
     * @param c the input byte
     * @return {@code true} if the input matches the predicate, otherwise {@code false}
     */
    boolean test(byte c);

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of
     * this predicate and another. When evaluating the composed predicate, if this
     * predicate is {@code false}, then the {@code other} predicate is not evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate representing the short-circuiting logical AND
     * @throws NullPointerException if {@code other} is null
     */
    default BytePredicate and(BytePredicate other) {
        NAssert.requireNonNull(other, "other");
        return b -> test(b) && other.test(b);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate representing the logical negation of this predicate
     */
    default BytePredicate negate() {
        return b -> !test(b);
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
    default BytePredicate or(BytePredicate other) {
        NAssert.requireNonNull(other, "other");
        return b -> test(b) || other.test(b);
    }
}
