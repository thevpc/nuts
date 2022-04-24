package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

public class PrivateNutsOptionalMap<T, V> extends PrivateNutsOptionalImpl<V> {
    private final Function<T, V> mapper;
    private final PrivateNutsOptionalImpl<T> t;

    public PrivateNutsOptionalMap(Function<T, V> mapper, PrivateNutsOptionalImpl<T> t) {
        this.mapper = mapper;
        this.t = t;
    }

    @Override
    public V get(NutsMessage message,NutsSession session) {
        return mapper.apply(t.get(message,session));
    }

    @Override
    public V get(NutsSession session) {
        return mapper.apply(t.get(session));
    }

    @Override
    public boolean isEmpty() {
        if (isPresent()) {
            return false;
        }
        return t.isEmpty();
    }

    @Override
    public boolean isNotPresent() {
        return !isPresent();
    }

    @Override
    public boolean isPresent() {
        return t.isPresent();
    }

    @Override
    public boolean isError() {
        return t.isError();
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return t.getMessage();
    }
}
