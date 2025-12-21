package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScoredValue;

public class FixedNScoredValue<T> implements NScoredValue<T> {
    T value;
    int score;
    public static final NScoredValue UNSUPPORTED=new FixedNScoredValue(null, NScorable.UNSUPPORTED_SCORE);
    public static <T> NScoredValue<T> UNSUPPORTED(){return UNSUPPORTED;};

    public FixedNScoredValue(T value, int score) {
        this.value = value;
        this.score = score;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public int score() {
        return score;
    }

}
