package net.thevpc.nuts.util;

import java.util.function.Supplier;

/**
 * Represents a four-valued logic used in composable predicates.
 * Values:
 * <ul>
 *   <li>TRUE: definitely true</li>
 *   <li>FALSE: definitely false</li>
 *   <li>UNKNOWN: unknown, cannot decide, known values dominate</li>
 *   <li>INDETERMINATE: unknown, can be either, propagates uncertainty</li>
 * </ul>
 */
public enum NTruth {
    /**
     * Represents logical truth.
     */
    TRUE,

    /**
     * Represents logical falsity.
     */
    FALSE,

    /**
     * Represents an indeterminate or unknown truth value.
     */
    UNKNOWN,

    /**
     * Represents unknown, can be either, propagates uncertainty
     */
    INDETERMINATE,

    ;

    // ----------------------------------------------------------------------
    // BASIC PREDICATES
    // ----------------------------------------------------------------------

    /**
     * Returns {@code true} if this value is {@link #TRUE}.
     */
    public boolean isTrue() {
        return this == TRUE;
    }

    /**
     * Returns {@code true} if this value is {@link #FALSE}.
     */
    public boolean isFalse() {
        return this == FALSE;
    }

    /**
     * Returns {@code true} if this value is {@link #UNKNOWN}.
     */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Returns {@code true} if this value is {@link #INDETERMINATE}.
     */
    public boolean isIndeterminate() {
        return this == INDETERMINATE;
    }

    // ----------------------------------------------------------------------
    // LOGICAL OPERATIONS
    // ----------------------------------------------------------------------



    /** Logical AND of this value with another, following four-valued logic. */
    public NTruth and(NTruth other) {
        if (this == FALSE || other == FALSE) {
            return (this == INDETERMINATE || other == INDETERMINATE) ? INDETERMINATE : FALSE;
        }
        if (this == TRUE && other == TRUE) return TRUE;
        if (this == INDETERMINATE || other == INDETERMINATE) return INDETERMINATE;
        return UNKNOWN; // both UNKNOWN or UNKNOWN + TRUE
    }

    /** Logical OR of this value with another, following four-valued logic. */
    public NTruth or(NTruth other) {
        if (this == TRUE || other == TRUE) return TRUE;
        if (this == FALSE && other == FALSE) return FALSE;
        if (this == INDETERMINATE || other == INDETERMINATE) return INDETERMINATE;
        return UNKNOWN; // FALSE + UNKNOWN or UNKNOWN + UNKNOWN
    }

    /** Logical NOT of this value. */
    public NTruth not() {
        switch (this) {
            case TRUE :return FALSE;
            case FALSE :return TRUE;
            case UNKNOWN :return UNKNOWN;           // cannot decide stays unknown
            case INDETERMINATE :return INDETERMINATE; // propagates uncertainty
        };
        return  UNKNOWN;
    }

    /**
     * Returns the logical exclusive OR (XOR) of this value and another.
     * UNKNOWN propagates.
     *
     * @param other the other value
     * @return the result of {@code this ⊕ other}
     */
    public NTruth xor(NTruth other) {
        if (this == INDETERMINATE || other == INDETERMINATE) {
            if (this == INDETERMINATE && other == INDETERMINATE) return UNKNOWN;
            return INDETERMINATE;
        }
        if (this == UNKNOWN || other == UNKNOWN) {
            if (this == UNKNOWN && other == UNKNOWN) return UNKNOWN;
            return INDETERMINATE;
        }
        // Both TRUE/FALSE
        return (this == other) ? FALSE : TRUE;
    }

    // ----------------------------------------------------------------------
    // N-ARY OPERATIONS (EAGER)
    // ----------------------------------------------------------------------

    /**
     * Returns the conjunction (AND) of all provided values.
     * Short-circuits on {@link #FALSE}.
     *
     * @param items iterable of truth values
     * @return aggregated AND value
     */
    public static NTruth andAll(Iterable<NTruth> items) {
        NTruth result = TRUE;
        for (NTruth v : items) {
            result = result.and(v);
            if (result == FALSE) break; // short-circuit
        }
        return result;
    }

    /**
     * Returns the disjunction (OR) of all provided values.
     * Short-circuits on {@link #TRUE}.
     *
     * @param items iterable of truth values
     * @return aggregated OR value
     */
    public static NTruth orAll(Iterable<NTruth> items) {
        NTruth result = FALSE;
        for (NTruth v : items) {
            result = result.or(v);
            if (result == TRUE) break; // short-circuit
        }
        return result;
    }

    // ----------------------------------------------------------------------
    // N-ARY OPERATIONS (LAZY)
    // ----------------------------------------------------------------------

    /**
     * Lazily evaluates the conjunction (AND) of supplied truth values.
     * Each supplier is invoked sequentially and may short-circuit.
     *
     * @param suppliers iterable of suppliers of truth values
     * @return aggregated AND value
     */
    public static NTruth andAllSuppliers(Iterable<? extends Supplier<NTruth>> suppliers) {
        NTruth result = TRUE;
        for (Supplier<NTruth> s : suppliers) {
            NTruth v = s.get();
            result = result.and(v);
            if (result == FALSE) break; // short-circuit
        }
        return result;
    }

    /**
     * Lazily evaluates the disjunction (OR) of supplied truth values.
     * Each supplier is invoked sequentially and may short-circuit.
     *
     * @param suppliers iterable of suppliers of truth values
     * @return aggregated OR value
     */
    public static NTruth orAllSuppliers(Iterable<? extends Supplier<NTruth>> suppliers) {
        NTruth result = FALSE;
        for (Supplier<NTruth> s : suppliers) {
            NTruth v = s.get();
            result = result.or(v);
            if (result == TRUE) break; // short-circuit
        }
        return result;
    }

    // ----------------------------------------------------------------------
    // CONVERSIONS
    // ----------------------------------------------------------------------

    /**
     * Converts a {@link Boolean} to a {@link NTruth}.
     * <ul>
     *     <li>{@code true → TRUE}</li>
     *     <li>{@code false → FALSE}</li>
     *     <li>{@code null → UNKNOWN}</li>
     * </ul>
     *
     * @param b input Boolean (may be {@code null})
     * @return corresponding {@link NTruth}
     */
    public static NTruth of(Boolean b) {
        return b == null ? UNKNOWN : (b ? TRUE : FALSE);
    }

    /**
     * Converts this {@link NTruth} to a {@link Boolean}.
     * Returns {@code null} for {@link #UNKNOWN}.
     *
     * @return corresponding {@link Boolean} or {@code null}
     */
    public Boolean toBoolean() {
        switch (this) {
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
        }
        return null;
    }
}
