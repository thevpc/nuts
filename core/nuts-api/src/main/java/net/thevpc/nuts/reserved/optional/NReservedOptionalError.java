package net.thevpc.nuts.reserved.optional;

import java.util.Objects;

import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NOptionalErrorException;

import java.util.function.Function;
import java.util.function.Supplier;
import net.thevpc.nuts.util.NOptionalType;

public class NReservedOptionalError<T> extends NReservedOptionalThrowable<T> implements Cloneable {

    private Supplier<NMsg> message;
    private Throwable error;

    public NReservedOptionalError(Supplier<NMsg> message, Throwable error) {
        if (message == null) {
            message = () -> NMsg.ofInvalidValue(error, null);
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
    public T get() {
        return get(this.message);
    }

    @Override
    public T get(Supplier<NMsg> message) {
        throwError(message, this.message);
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

    protected void throwError(Supplier<NMsg> message, Supplier<NMsg> message0) {
        if (message == null) {
            message = message0;
        }
        if (message == null) {
            message = () -> NMsg.ofMissingValue();
        }
        Supplier<NMsg> finalMessage = message;
        NMsg eMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> finalMessage == null ? null : finalMessage.get());
        NMsg m = prepareMessage(eMsg);
        throw new NOptionalErrorException(m);
    }
}
