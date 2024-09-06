package net.thevpc.nuts.reserved.optional;

import java.util.Objects;
import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NOptionalErrorException;

import java.util.function.Function;
import java.util.function.Supplier;
import net.thevpc.nuts.util.NOptionalType;
import static net.thevpc.nuts.util.NOptionalType.EMPTY;
import static net.thevpc.nuts.util.NOptionalType.ERROR;
import static net.thevpc.nuts.util.NOptionalType.PRESENT;

public class NReservedOptionalError<T> extends NReservedOptionalThrowable<T> implements Cloneable {

    private Function<NSession, NMsg> message;
    private Throwable error;

    public NReservedOptionalError(Function<NSession, NMsg> message, Throwable error) {
        if (message == null) {
            message = (s) -> NMsg.ofInvalidValue(error, null);
        }
        this.message = message;
        this.error = error;
    }

    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        Objects.requireNonNull(mapper);
        return NOptional.ofError(getMessage(), getError());
    }

    @Override
    public NOptionalType getType() {
        return NOptionalType.ERROR;
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
        throwError(message, session, this.message);
        //never reached!
        return null;
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

    protected void throwError(Function<NSession, NMsg> message, NSession session, Function<NSession, NMsg> message0) {
        if (session == null) {
            session = getSession();
        }
        if (message == null) {
            message = message0;
        }
        if (message == null) {
            message = s -> NMsg.ofMissingValue();
        }
        Function<NSession, NMsg> finalMessage = message;
        NSession finalSession = session;
        NMsg eMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> finalMessage == null ? null : finalMessage.apply(finalSession));
        NMsg m = prepareMessage(eMsg);
        if (session == null) {
            throw new NNoSessionOptionalErrorException(m);
        } else {
            throw new NOptionalErrorException(session, m);
        }
    }
}
