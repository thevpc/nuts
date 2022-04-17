package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsNoSuchElementException;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

class EmptyNutsOptional<T> extends NutsOptionalBase<T> {
    private Function<NutsSession, NutsMessage> message;

    public EmptyNutsOptional(NutsSession session, Function<NutsSession, NutsMessage> message) {
        super(session);
        if (message == null) {
            message = (s) -> NutsMessage.cstyle("missing value");
        }
        this.message = message;
    }

    @Override
    public T get() {
        throw new NutsNoSuchElementException(getSession(), message.apply(getSession()));
    }

    @Override
    public T get(NutsMessage message) {
        throw new NutsNoSuchElementException(getSession(),
                this.message.apply(getSession()).concat(getSession(), NutsMessage.plain(" : "), message)
        );
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    protected Function<NutsSession, NutsMessage> getEmptyMessage() {
        return message;
    }
}
