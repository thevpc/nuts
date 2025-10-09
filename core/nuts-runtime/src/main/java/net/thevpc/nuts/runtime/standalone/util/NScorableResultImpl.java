package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScorableResult;

class NScorableResultImpl<T extends NScorable> implements NScorableResult<T> {
    T value;
    int score;
    NScorableContext context;

    public NScorableResultImpl(T value, int score, NScorableContext context) {
        this.value = value;
        this.score = score;
        this.context = context;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public int score() {
        return score;
    }

    @Override
    public NScorableContext context() {
        return context;
    }
}
