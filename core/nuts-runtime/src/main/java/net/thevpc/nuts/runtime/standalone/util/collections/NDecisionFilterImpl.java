package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NAssert;import net.thevpc.nuts.util.NDecision;
import net.thevpc.nuts.util.NDecisionFilter;
import net.thevpc.nuts.util.NDecisionConflict;

import java.util.*;

public class NDecisionFilterImpl<T> implements NDecisionFilter<T> {
    private final Map<T, NDecision> decisions = new HashMap<>();
    private boolean[] rulesCache = null;
    private final Class<T> keyType;
    private final NDecisionConflict decisionConflict;
    private final NDecision defaultDecision;

    public NDecisionFilterImpl(Class<T> keyType, NDecisionConflict decisionConflict, NDecision defaultDecision) {
        this.keyType = NAssert.requireNamedNonNull(keyType,"keyType");
        this.decisionConflict = decisionConflict == null ? NDecisionConflict.DENY_WINS : decisionConflict;
        this.defaultDecision = defaultDecision == null ? NDecision.ABSTAIN : defaultDecision;
    }

    @Override
    public NDecision defaultDecision() {
        return defaultDecision;
    }

    @Override
    public NDecisionConflict decisionConflict() {
        return decisionConflict;
    }

    @Override
    public Class<T> keyType() {
        return keyType;
    }


    @Override
    public void merge(NDecisionFilter<T> other) {
        if (other != null) {
            for (Map.Entry<T, NDecision> e : other.entries()) {
                set(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public Set<Map.Entry<T, NDecision>> entries() {
        return Collections.unmodifiableSet(decisions.entrySet());
    }

    @Override
    public NDecision get(T t) {
        NDecision i = decisions.get(t);
        return i == null ? NDecision.ABSTAIN : i;
    }


    @Override
    public void unset(T t) {
        decisions.remove(t);
        this.rulesCache = null;
    }

    @Override
    public boolean isEmpty() {
        return decisions.isEmpty();
    }

    @Override
    public void clear() {
        decisions.clear();
        this.rulesCache = null;
    }

    @Override
    public int size() {
        return decisions.size();
    }

    public void set(T t, NDecision decision) {
        if (decision == null || decision == NDecision.ABSTAIN) {
            decisions.remove(t);
        } else {
            decisions.put(t, decision);
        }
        this.rulesCache = null;
    }

    private boolean[] ensureCache() {
        if (rulesCache != null) {
            return rulesCache;
        }
        boolean[] result = new boolean[2];
        for (NDecision value : decisions.values()) {
            if (value == NDecision.ACCEPT) {
                result[0] = true;
                if (result[1]) {
                    break;
                }
            } else if (value == NDecision.DENY) {
                result[1] = true;
                if (result[0]) {
                    break;
                }
            }
        }
        this.rulesCache = result;
        return this.rulesCache;
    }

    public boolean accept(T t) {
        NDecision u = decisions.get(t);
        if (u != null) {
            return u == NDecision.ACCEPT;
        }
        boolean[] v = ensureCache();
        boolean hasAcceptRules = v[0];
        boolean hasDenyRules = v[1];
        if (hasAcceptRules && hasDenyRules) {
            switch (decisionConflict) {
                case DENY_WINS:
                    return false;
                case ACCEPT_WINS:
                    return true;
            }
        }
        if (hasAcceptRules) return false;
        if (hasDenyRules) return true;
        return defaultDecision == NDecision.ACCEPT;
    }
}
