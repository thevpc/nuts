package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class PrivateNutsOptionalEmpty<T> extends PrivateNutsOptionalImpl<T> {
    private Function<NutsSession, NutsMessage> message;
    public PrivateNutsOptionalEmpty(Function<NutsSession, NutsMessage> message) {
        if (message == null) {
            message = (s) -> NutsMessage.cstyle("missing value");
        }
        this.message = message;
    }

    @Override
    public T get(NutsSession session) {
        if (session == null) {
            throw new NoSuchElementException(message.apply(null).toString());
        } else {
            throw new NutsNoSuchElementException(session, message.apply(session));
        }
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public T get(NutsMessage message,NutsSession session) {
        throw new NutsNoSuchElementException(session,
                this.message.apply(session).concat(session, NutsMessage.plain(" : "), message)
        );
    }

    @Override
    public NutsOptional<T> nonBlank() {
        return this;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isNotPresent() {
        return true;
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return message;
    }

    @Override
    public boolean isBlank() {
        return true;
    }
}
