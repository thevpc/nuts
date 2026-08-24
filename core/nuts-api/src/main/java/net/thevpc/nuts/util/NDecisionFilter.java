package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.Map;
import java.util.Set;

/**
 * NDecisionFilter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDecisionFilter<T> {
    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @return of result
     */
    static <T> NDecisionFilter<T> of(Class<T> type) {
        /**
         * Creates a new instance of of.
         *
         * @param type type
         * @param null null
         * @param null null
         * @return of result
         */
        return of(type, null, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @param decisionConflict decision conflict
     * @param defaultDecision default decision
     * @return of result
     */
    static <T> NDecisionFilter<T> of(Class<T> type, NDecisionConflict decisionConflict, NDecision defaultDecision) {
        return NUtilsRPI.of().createDecisionFilter(type, decisionConflict, defaultDecision);
    }

    /**
     * Accept.
     *
     * @param t t
     * @return accept result
     */
    boolean accept(T t);

    /**
     * Default decision.
     *
     * @return default decision result
     */
    NDecision defaultDecision();

    /**
     * Decision conflict.
     *
     * @return decision conflict result
     */
    NDecisionConflict decisionConflict();

    /**
     * Key type.
     *
     * @return key type result
     */
    Class<T> keyType();

    /**
     * Entries.
     *
     * @return entries result
     */
    Set<Map.Entry<T, NDecision>> entries();

    /**
     * Returns the get.
     *
     * @param t t
     * @return get result
     */
    NDecision get(T t);

    /**
     * Sets the set.
     *
     * @param t t
     * @param acceptDeny accept deny
     */
    void set(T t, NDecision acceptDeny);

    /**
     * Unset.
     *
     * @param t t
     */
    void unset(T t);

    /**
     * Merge.
     *
     * @param other other
     */
    void merge(NDecisionFilter<T> other);

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Clear.
     */
    void clear();

    /**
     * Size.
     *
     * @return size result
     */
    int size();
}
