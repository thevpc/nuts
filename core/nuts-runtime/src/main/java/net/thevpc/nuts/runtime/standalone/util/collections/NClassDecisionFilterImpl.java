package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.reflect.NClassDecisionFilter;
import net.thevpc.nuts.reflect.NClassMap;
import net.thevpc.nuts.util.NDecision;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class NClassDecisionFilterImpl<T> implements NClassDecisionFilter<T> {
    private final Class<T> keyType;
    private final NClassMap<T, NDecision> decisions;
    private final NDecision defaultDecision;


    public NClassDecisionFilterImpl(Class<T> base, NDecision defaultDecision) {
        this.keyType = base;
        this.decisions = NClassMap.of(base, NDecision.class);
        this.defaultDecision = defaultDecision == null ? NDecision.ABSTAIN : defaultDecision;
    }

    public NDecision defaultDecision() {
        return defaultDecision;
    }

    public Class<T> keyType() {
        return keyType;
    }

    public void merge(NClassDecisionFilter<T> other) {
        for (Map.Entry<Class<? extends T>, NDecision> entry : other.entries()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    public Set<Class<? extends T>> keySet() {
        return Collections.unmodifiableSet(decisions.keySet());
    }

    @Override
    public Set<Map.Entry<Class<? extends T>, NDecision>> entries() {
        return Collections.unmodifiableSet(decisions.entrySet());
    }

    public void set(Class<? extends T> t, NDecision decision) {
        if (decision == null || decision == NDecision.ABSTAIN) {
            decisions.remove(t);
        } else {
            decisions.put(t, decision);
        }
    }

    @Override
    public int size() {
        return decisions.size();
    }


    @Override
    public boolean isEmpty() {
        return decisions.isEmpty();
    }

    @Override
    public void unset(Class<? extends T> t) {
        decisions.remove(t);
    }

    public NDecision get(Class<? extends T> t) {
        NDecision b = decisions.get(t);
        if (b == null) {
            return NDecision.ABSTAIN;
        }
        return b;
    }

    public NDecision getExact(Class<? extends T> t) {
        NDecision b = decisions.getExact(t);
        if (b == null) {
            return NDecision.ABSTAIN;
        }
        return b;
    }

    @Override
    public boolean accept(Class<? extends T> t) {
        NDecision res = decisions.get(t);
        if (res == NDecision.ACCEPT) return true;
        if (res == NDecision.DENY) return false;
        return defaultDecision == NDecision.ACCEPT;
    }
}
