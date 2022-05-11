package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

public class NutsReservedOptionalEmpty<T> extends NutsReservedOptionalThrowable<T> implements Cloneable {
    private Function<NutsSession, NutsMessage> message;

    public NutsReservedOptionalEmpty(Function<NutsSession, NutsMessage> message) {
        super(null);
        if (message == null) {
            message = (s) -> NutsMessage.ofPlain("missing value");
        }
        this.message = message;
    }

    @Override
    public T get(NutsSession session) {
        return get(this.message, session);
    }

    @Override
    public T get(Supplier<NutsMessage> message) {
        return get(s -> message.get(), null);
    }

    @Override
    public T get(Function<NutsSession, NutsMessage> message, NutsSession session) {
        if (message == null) {
            message = this.message;
        }
        NutsMessage m = prepareMessage(message.apply(session));
        if (session == null) {
            throw new NoSuchElementException(m.toString());
        } else {
            throw new NutsNoSuchElementException(session, m);
        }
    }

    @Override
    public Throwable getError() {
        return null;
    }


    @Override
    public NutsOptional<T> ifBlankNull() {
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

    @Override
    public String toString() {
        return "EmptyOptional@" + System.identityHashCode(this);
    }

    @Override
    protected NutsOptional<T> clone() {
        return super.clone();
    }
}
