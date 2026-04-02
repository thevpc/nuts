package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;
import net.thevpc.nuts.util.NDecision;

import java.util.Map;
import java.util.Set;

public interface NClassDecisionFilter<T> {
    static <T> NClassDecisionFilter<T> of(Class<T> type) {
        return of(type, NDecision.ACCEPT);
    }

    static <T> NClassDecisionFilter<T> of(Class<T> type, NDecision defaultDecision) {
        return NCollectionsRPI.of().createClassDecisionFilter(type, defaultDecision);
    }

    NDecision defaultDecision();

    Class<T> keyType();

    Set<Class<? extends T>> keySet();

    Set<Map.Entry<Class<? extends T>, NDecision>> entries();

    NDecision get(Class<? extends T> t);

    NDecision getExact(Class<? extends T> t);

    void set(Class<? extends T> t, NDecision accept);

    void merge(NClassDecisionFilter<T> other);

    int size();

    boolean isEmpty();

    void unset(Class<? extends T> t);

    boolean accept(Class<? extends T> t);
}
