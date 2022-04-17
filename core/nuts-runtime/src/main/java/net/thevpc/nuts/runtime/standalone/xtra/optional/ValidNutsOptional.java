package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

class ValidNutsOptional<T> extends NutsOptionalBase<T> {
    private T value;

    public ValidNutsOptional(NutsSession session, T value) {
        super(session);
        this.value = value;
    }

    public T get() {
        return value;
    }

    @Override
    public T get(NutsMessage message) {
        return value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    protected Function<NutsSession, NutsMessage> getEmptyMessage() {
        return (session) -> NutsMessage.cstyle("element not found");
    }
}
