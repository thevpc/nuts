package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalStateException;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScoredValue;

public class FixedNScoredValue<T> implements NScoredValue<T> {
    T value;
    int score;
    public static final NScoredValue UNSUPPORTED = new FixedNScoredValue(null, NScorable.UNSUPPORTED_SCORE);

    public static <T> NScoredValue<T> ofUnsupported(Class<? extends T> implType, Class<T> apiType) {
        return UNSUPPORTED;
    }

    public FixedNScoredValue(T value, int score) {
        this.value = value;
        this.score = score;
    }

    @Override
    public Class<T> getApiType() {
        return null;
    }

    @Override
    public Class<? extends T> getImplType() {
        return null;
    }

    @Override
    public T value() {
        if (score <= 0) {
            throw new NIllegalStateException(NMsg.ofC("an unsupported value could not be instantiated"));
        }
        return value;
    }

    @Override
    public int score() {
        return score;
    }

}
