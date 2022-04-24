package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.NoSuchElementException;
import java.util.function.Function;

public class PrivateNutsOptionalEmpty<T> extends PrivateNutsOptionalImpl<T> {
    private Function<NutsSession, NutsMessage> message;
    private boolean error;

    public PrivateNutsOptionalEmpty(Function<NutsSession, NutsMessage> message, boolean error) {
        if (message == null) {
            message = (s) -> NutsMessage.cstyle("missing value");
        }
        this.message = message;
        this.error = error;
    }

    @Override
    public T get(NutsSession session) {
        if(error){
            if (session == null) {
                throw new NoSuchElementException(message.apply(null).toString());
            } else {
                throw new NutsOptionalErrorException(session, message.apply(session));
            }
        }else {
            if (session == null) {
                throw new NoSuchElementException(message.apply(null).toString());
            } else {
                throw new NutsNoSuchElementException(session, message.apply(session));
            }
        }
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
        return error;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return !error;
    }

    @Override
    public boolean isNotPresent() {
        return true;
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return message;
    }
}
