package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

class MapNutsOptional<T, V> extends NutsOptionalBase<V> {
    private final Function<T, V> mapper;
    private final NutsOptionalImpl<T> t;

    public MapNutsOptional(NutsSession session, Function<T, V> mapper, NutsOptionalImpl<T> t) {
        super(session);
        this.mapper = mapper;
        this.t = t;
    }

    @Override
    public V get(NutsMessage message) {
        return mapper.apply(t.get(message));
    }

    @Override
    public V get() {
        return mapper.apply(t.get());
    }

    @Override
    public boolean isPresent() {
        return t.isPresent();
    }

    @Override
    protected Function<NutsSession, NutsMessage> getEmptyMessage() {
        return t.getEmptyMessage();
    }
}
