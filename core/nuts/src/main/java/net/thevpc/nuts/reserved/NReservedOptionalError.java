package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;

import java.util.function.Function;
import java.util.function.Supplier;

public class NReservedOptionalError<T> extends NReservedOptionalThrowable<T> implements Cloneable {
    private Function<NSession, NMsg> message;
    private Throwable error;

    public NReservedOptionalError(Function<NSession, NMsg> message, Throwable error) {
        super(null);
        if (message == null) {
            message = (s) -> NMsg.ofInvalidValue(error,null);
        }
        this.message = message;
        this.error = error;
    }

    @Override
    public Throwable getError() {
        return error;
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
            throw new NNoSessionOptionalErrorException(m);
        } else {
            throw new NOptionalErrorException(session, m);
        }
    }

    @Override
    public NOptional<T> ifBlankEmpty() {
        return this;
    }

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
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
        return false;
    }

    @Override
    public String toString() {
        return "ErrorOptional@" + System.identityHashCode(this);
    }

    @Override
    protected NOptional<T> clone() {
        return super.clone();
    }
}
