package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import net.thevpc.nuts.util.NOptionalType;

public class NReservedOptionalEmpty<T> extends NReservedOptionalThrowable<T> implements Cloneable {

    private Supplier<NMsg> message;

    public NReservedOptionalEmpty(Supplier<NMsg> message) {
        if (message == null) {
            message = () -> NMsg.ofMissingValue();
        }
        this.message = message;
    }

    @Override
    public T get() {
        throwError(message, this.message);
        //never reached!
        return null;
    }

    @Override
    public T get(Supplier<NMsg> message) {
        throwError(message, this.message);
        //never reached!
        return null;
    }

    public <V> NOptional<V> then(Function<T, V> mapper) {
        Objects.requireNonNull(mapper);
        return NOptional.ofEmpty(getMessage());
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public NOptionalType getType() {
        return NOptionalType.EMPTY;
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
    public Supplier<NMsg> getMessage() {
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

    protected void throwError(Supplier<NMsg> message, Supplier<NMsg> message0) {
        if (message == null) {
            message = message0;
        }
        if (message == null) {
            message = NMsg::ofMissingValue;
        }
        Supplier<NMsg> finalMessage = message;
        NMsg eMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> finalMessage == null ? null : finalMessage.get());
        NMsg m = prepareMessage(eMsg);
        RuntimeException exception = null;
        ExceptionFactory exceptionFactory = getExceptionFactory();
        if (exceptionFactory != null) {
            exception = exceptionFactory.createException(m, null);
        }
        if (exception == null) {
            exception = new NNoSuchElementException(m);
        }
        throw exception;
    }


}
