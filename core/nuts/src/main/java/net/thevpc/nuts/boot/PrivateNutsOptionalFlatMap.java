package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

public class PrivateNutsOptionalFlatMap<T, V> extends PrivateNutsOptionalImpl<V> {
    private final Function<T, NutsOptional<V>> mapper;
    private final NutsOptional<T> t;

    public PrivateNutsOptionalFlatMap(Function<T, NutsOptional<V>> mapper, NutsOptional<T> t) {
        this.mapper = mapper;
        this.t = t;
    }

    @Override
    public V get(NutsMessage message, NutsSession session) {
        T t = this.t.get(message, session);
        return mapper.apply(t).get(session);
    }

    @Override
    public V get(NutsSession session) {
        T t = this.t.get(session);
        return mapper.apply(t).get(session);
    }

    @Override
    public boolean isPresent() {
        return t.isPresent();
    }

    @Override
    public boolean isEmpty() {
        return t.isEmpty();
    }

    @Override
    public boolean isNotPresent() {
        return t.isNotPresent();
    }

    @Override
    public boolean isError() {
        return t.isError();
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return ((PrivateNutsOptionalImpl<T>) t).getMessage();
    }
}
