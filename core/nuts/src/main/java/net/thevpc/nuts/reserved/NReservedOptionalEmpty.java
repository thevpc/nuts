package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

public class NReservedOptionalEmpty<T> extends NReservedOptionalThrowable<T> implements Cloneable {
    private Function<NSession, NMsg> message;

    public NReservedOptionalEmpty(Function<NSession, NMsg> message) {
        super(null);
        if (message == null) {
            message = (s) -> NMsg.ofMissingValue();
        }
        this.message = message;
    }

    @Override
    public T get(NSession session) {
        return get(this.message, session);
    }

    @Override
    public T get(Supplier<NMsg> message) {
        return get(s -> message.get(), null);
    }

    @Override
    public T get(Function<NSession, NMsg> message, NSession session) {
        if(session==null){
            session=getSession();
        }
        if (message == null) {
            message = this.message;
        }
        NMsg m = prepareMessage(message.apply(session));
        if (session == null) {
            throw new NoSuchElementException(m.toString());
        } else {
            throw new NNoSuchElementException(session, m);
        }
    }

    @Override
    public Throwable getError() {
        return null;
    }


    @Override
    public NOptional<T> ifBlankEmpty() {
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
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isNotPresent() {
        return true;
    }

    @Override
    public Function<NSession, NMsg> getMessage() {
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
    protected NOptional<T> clone() {
        return super.clone();
    }
}
