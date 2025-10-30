package net.thevpc.nuts.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A three-valued predicate returning an {@link NTruth} instead of a boolean.
 * <p>
 * This is a generalization of {@link Predicate} suitable for situations where
 * a logical condition may be indeterminate.
 *
 * @param <T> the argument type
 * @since 0.8.x
 */
@FunctionalInterface
public interface NTruthPredicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return the truth value result
     */
    NTruth test(T t);

    // ---------------------------------------------------------------------
    // Static combinators
    // ---------------------------------------------------------------------

    /** Negate this predicate. */
    default NTruthPredicate<T> negate() {
        return t -> this.test(t).not();
    }

    /** Logical AND of this predicate with another. */
    default NTruthPredicate<T> and(NTruthPredicate<T> other) {
        return t -> this.test(t).and(other.test(t));
    }

    /** Logical OR of this predicate with another. */
    default NTruthPredicate<T> or(NTruthPredicate<T> other) {
        return t -> this.test(t).or(other.test(t));
    }

    /** Logical XOR of this predicate with another. */
    default NTruthPredicate<T> xor(NTruthPredicate<T> other) {
        return t -> this.test(t).xor(other.test(t));
    }

    /** Compose multiple predicates using AND. */
    static <T> NTruthPredicate<T> andAll(Iterable<NTruthPredicate<T>> predicates) {
        return t -> {
            NTruth result = NTruth.TRUE;
            for (NTruthPredicate<T> p : predicates) {
                result = result.and(p.test(t));
                if (result == NTruth.FALSE) break; // short-circuit
            }
            return result;
        };
    }

    /** Compose multiple predicates using OR. */
    static <T> NTruthPredicate<T> orAll(Iterable<NTruthPredicate<T>> predicates) {
        return t -> {
            NTruth result = NTruth.FALSE;
            for (NTruthPredicate<T> p : predicates) {
                result = result.or(p.test(t));
                if (result == NTruth.TRUE) break; // short-circuit
            }
            return result;
        };
    }

    /** Compose multiple suppliers of predicates using AND. */
    static <T> NTruthPredicate<T> andAllSuppliers(Iterable<? extends Supplier<NTruthPredicate<T>>> suppliers) {
        return t -> {
            NTruth result = NTruth.TRUE;
            for (Supplier<NTruthPredicate<T>> s : suppliers) {
                result = result.and(s.get().test(t));
                if (result == NTruth.FALSE) break;
            }
            return result;
        };
    }

    /** Compose multiple suppliers of predicates using OR. */
    static <T> NTruthPredicate<T> orAllSuppliers(Iterable<? extends Supplier<NTruthPredicate<T>>> suppliers) {
        return t -> {
            NTruth result = NTruth.FALSE;
            for (Supplier<NTruthPredicate<T>> s : suppliers) {
                result = result.or(s.get().test(t));
                if (result == NTruth.TRUE) break;
            }
            return result;
        };
    }

    /** Wrap a standard boolean predicate into an NTruthPredicate (TRUE/FALSE). */
    static <T> NTruthPredicate<T> of(Predicate<T> predicate) {
        return t -> predicate.test(t) ? NTruth.TRUE : NTruth.FALSE;
    }

    /** Constant predicate returning a fixed NTruth value. */
    static <T> NTruthPredicate<T> constant(NTruth value) {
        return t -> value;
    }
}
