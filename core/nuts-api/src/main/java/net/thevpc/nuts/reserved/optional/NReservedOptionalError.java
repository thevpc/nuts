package net.thevpc.nuts.reserved.optional;

import java.util.Objects;

import net.thevpc.nuts.NDetachedErrorOptionalException;
import net.thevpc.nuts.NErrorOptionalException;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import net.thevpc.nuts.util.NOptionalType;

public class NReservedOptionalError<T> extends NReservedOptionalThrowable<T> implements Cloneable {

    private Supplier<NMsg> message;
    private Throwable cause;

    public NReservedOptionalError(Supplier<NMsg> message, Throwable cause) {
        if (message == null) {
            message = () -> NMsg.ofInvalidValue(cause);
        }
        this.message = message;
        this.cause = cause;
    }

    public NOptional<T> withMessage(Supplier<NMsg> message) {
        return new NReservedOptionalEmpty<>(message);
    }

    public NOptional<T> withMessage(NMsg message) {
        return new NReservedOptionalEmpty<>(message == null ? (() -> NMsg.ofInvalidValue(cause)) : () -> message);
    }

    public NOptional<T> withName(NMsg name) {
        return new NReservedOptionalEmpty<>(name == null ? (() -> NMsg.ofInvalidValue(cause)) : () -> NMsg.ofInvalidValue(cause, name));
    }

    public NOptional<T> withName(String name) {
        return new NReservedOptionalEmpty<>(name == null ? (() -> NMsg.ofInvalidValue(cause)) : () -> NMsg.ofInvalidValue(cause, name));
    }

    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        Objects.requireNonNull(mapper);
        return (NOptional<V>) this;
    }

    @Override
    public NOptionalType getType() {
        return NOptionalType.ERROR;
    }

    @Override
    public Throwable getError() {
        return cause;
    }

    @Override
    public T get() {
        throwError(message);
        return null;
    }

    @Override
    public T get(Supplier<NMsg> message) {
        throwError(message);
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
    public Supplier<NMsg> getMessage() {
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

    protected void throwError(Supplier<NMsg> preferredMessage) {
        if (preferredMessage == null) {
            preferredMessage = message;
        }
        if (preferredMessage == null) {
            preferredMessage = NMsg::ofMissingValue;
        }
        Supplier<NMsg> finalMessage = preferredMessage;
        NMsg eMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> finalMessage == null ? null : finalMessage.get());
        NMsg m = prepareMessage(eMsg);
        RuntimeException exception = null;
        ExceptionFactory exceptionFactory = getExceptionFactory();
        if (exceptionFactory != null) {
            exception = exceptionFactory.createOptionalErrorException(m, cause);
        }
        if (exception == null) {
            exceptionFactory = NOptional.getDefaultExceptionFactory();
            if (exceptionFactory != null) {
                exception = exceptionFactory.createOptionalErrorException(m, cause);
            }
        }
        if (exception == null) {
            if (!NWorkspace.get().isPresent()) {
                exception = new NErrorOptionalException(preferredMessage.get(), cause);
            } else {
                exception = new NDetachedErrorOptionalException(preferredMessage.get(), cause);
            }
        }
        throw exception;
    }

    @Override
    public Optional<T> asOptional() {
        return Optional.empty();
    }


}
