package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.util.NDetachedEmptyOptionalException;
import net.thevpc.nuts.util.NEmptyOptionalException;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.internal.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import net.thevpc.nuts.util.NOptionalType;

public class NReservedOptionalEmpty<T> extends NReservedOptionalThrowable<T> implements Cloneable {

    private Supplier<NMsg> message;

    public NReservedOptionalEmpty(Supplier<NMsg> message) {
        if (message == null) {
            message = NMsg::ofMissingValue;
        }
        this.message = message;
    }

    public NOptional<T> withMessage(Supplier<NMsg> message) {
        return new NReservedOptionalEmpty<>(message);
    }

    public NOptional<T> withMessage(NMsg message) {
        return new NReservedOptionalEmpty<T>(message == null ? (NMsg::ofMissingValue) : () -> message);
    }

    public NOptional<T> withName(NMsg name) {
        return new NReservedOptionalEmpty<T>(name == null ? (NMsg::ofMissingValue) : () -> NMsg.ofMissingValue(name));
    }

    @Override
    public NOptional<T> withName(String name) {
        return new NReservedOptionalEmpty<T>(name == null ? (NMsg::ofMissingValue) : () -> NMsg.ofMissingValue(name));
    }

    @Override
    public Optional<T> asOptional() {
        return Optional.empty();
    }


    @Override
    public T get() {
        throwError(message);
        //never reached!
        return null;
    }

    @Override
    public T get(Supplier<NMsg> message) {
        throwError(message);
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
            exception = exceptionFactory.createOptionalEmptyException(m);
        }
        if (exception == null) {
            exceptionFactory = NOptional.getDefaultExceptionFactory();
            if (exceptionFactory != null) {
                exception = exceptionFactory.createOptionalEmptyException(m);
            }
        }
        if (exception == null) {
            if (!NWorkspace.get().isPresent()) {
                exception = new NEmptyOptionalException(preferredMessage.get());
            } else {
                exception = new NDetachedEmptyOptionalException(preferredMessage.get());
            }
        }
        throw exception;
    }

    @Override
    public NElement describe() {
        return NElement.ofUpletBuilder("Optional")
                .add("evaluated", true)
                .add("empty", true)
                .add("error", false)
                .build()
                ;

    }

}
