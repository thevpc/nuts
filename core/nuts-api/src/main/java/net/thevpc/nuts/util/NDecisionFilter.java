package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.Map;
import java.util.Set;

public interface NDecisionFilter<T> {
    static <T> NDecisionFilter<T> of(Class<T> type) {
        return of(type, null, null);
    }

    static <T> NDecisionFilter<T> of(Class<T> type, NDecisionConflict decisionConflict, NDecision defaultDecision) {
        return NCollectionsRPI.of().createDecisionFilter(type, decisionConflict, defaultDecision);
    }

    boolean accept(T t);

    NDecision defaultDecision();

    NDecisionConflict decisionConflict();

    Class<T> keyType();

    Set<Map.Entry<T, NDecision>> entries();

    NDecision get(T t);

    void set(T t, NDecision acceptDeny);

    void unset(T t);

    void merge(NDecisionFilter<T> other);

    boolean isEmpty();

    void clear();

    int size();
}
