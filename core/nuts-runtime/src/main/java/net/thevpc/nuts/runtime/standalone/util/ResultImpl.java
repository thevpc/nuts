package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.spi.NScorable;
import net.thevpc.nuts.spi.NScorableContext;

class ResultImpl<T extends NScorable> implements NScorable.Result<T> {
    T value;
    int score;
    NScorableContext context;

    public ResultImpl(T value, int score, NScorableContext context) {
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
