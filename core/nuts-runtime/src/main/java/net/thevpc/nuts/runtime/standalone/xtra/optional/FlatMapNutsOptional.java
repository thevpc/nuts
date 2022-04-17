package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

class FlatMapNutsOptional<T, V> extends NutsOptionalBase<V> {
    private final Function<T, NutsOptional<V>> mapper;
    private final NutsOptional<T> t;

    public FlatMapNutsOptional(NutsSession session, Function<T, NutsOptional<V>> mapper, NutsOptional<T> t) {
        super(session);
        this.mapper = mapper;
        this.t = t;
    }

    @Override
    public V get(NutsMessage message) {
        T t = this.t.get(message);
        return mapper.apply(t).get();
    }

    @Override
    public V get() {
        T t = this.t.get();
        return mapper.apply(t).get();
    }

    @Override
    public boolean isPresent() {
        return t.isPresent();
    }

    @Override
    protected Function<NutsSession, NutsMessage> getEmptyMessage() {
        return ((NutsOptionalImpl<T>)t).getEmptyMessage();
    }
}
