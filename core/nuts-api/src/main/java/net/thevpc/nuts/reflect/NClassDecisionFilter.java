package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NDecision;

import java.util.Map;
import java.util.Set;

/**
 * NClassDecisionFilter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NClassDecisionFilter<T> {
    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @return of result
     */
    static <T> NClassDecisionFilter<T> of(Class<T> type) {
        /**
         * Creates a new instance of of.
         *
         * @param type type
         * @param NDecision.ACCEPT n decision.accept
         * @return of result
         */
        return of(type, NDecision.ACCEPT);
    }

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @param defaultDecision default decision
     * @return of result
     */
    static <T> NClassDecisionFilter<T> of(Class<T> type, NDecision defaultDecision) {
        return NUtilsRPI.of().createClassDecisionFilter(type, defaultDecision);
    }

    /**
     * Default decision.
     *
     * @return default decision result
     */
    NDecision defaultDecision();

    /**
     * Key type.
     *
     * @return key type result
     */
    Class<T> keyType();

    /**
     * Key set.
     *
     * @return key set result
     */
    Set<Class<? extends T>> keySet();

    /**
     * Entries.
     *
     * @return entries result
     */
    Set<Map.Entry<Class<? extends T>, NDecision>> entries();

    /**
     * Returns the get.
     *
     * @param t t
     * @return get result
     */
    NDecision get(Class<? extends T> t);

    /**
     * Returns the exact.
     *
     * @param t t
     * @return get exact result
     */
    NDecision getExact(Class<? extends T> t);

    /**
     * Sets the set.
     *
     * @param t t
     * @param accept accept
     */
    void set(Class<? extends T> t, NDecision accept);

    /**
     * Merge.
     *
     * @param other other
     */
    void merge(NClassDecisionFilter<T> other);

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Unset.
     *
     * @param t t
     */
    void unset(Class<? extends T> t);

    /**
     * Accept.
     *
     * @param t t
     * @return accept result
     */
    boolean accept(Class<? extends T> t);
}
