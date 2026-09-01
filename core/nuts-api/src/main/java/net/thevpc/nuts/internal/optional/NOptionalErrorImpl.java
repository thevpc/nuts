package net.thevpc.nuts.internal.optional;

import net.thevpc.nuts.util.*;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.internal.NApiUtilsRPI;
import net.thevpc.nuts.text.NMsg;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * NReservedOptionalError class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NOptionalErrorImpl<T> extends NOptionalThrowableImpl<T> implements Cloneable {

    private Throwable cause;

    /**
     * N reserved optional error.
     *
     * @param message message
     * @param cause cause
     * @return n reserved optional error result
     */
    public NOptionalErrorImpl(Supplier<NMsg> message, Throwable cause) {
      /**
       * Super.
       *
       * @param NMsg.ofInvalidValue(cause):message n msg.of invalid value(cause):message
       */
        super(message==null?() -> NMsg.ofInvalidValue(cause):message);
        this.cause = cause;
    }

    /**
     * With message.
     *
     * @param message message
     * @return with message result
     */
    public NOptional<T> withMessage(Supplier<NMsg> message) {
        return new NOptionalEmptyImpl<>(message);
    }

    /**
     * With message.
     *
     * @param message message
     * @return with message result
     */
    public NOptional<T> withMessage(NMsg message) {
        return new NOptionalEmptyImpl<>(message == null ? (() -> NMsg.ofInvalidValue(cause)) : () -> message);
    }

    /**
     * With name.
     *
     * @param name name
     * @return with name result
     */
    public NOptional<T> withName(NMsg name) {
        return new NOptionalEmptyImpl<>(name == null ? (() -> NMsg.ofInvalidValue(cause)) : () -> NMsg.ofInvalidValue(cause, name));
    }

    /**
     * With name.
     *
     * @param name name
     * @return with name result
     */
    public NOptional<T> withName(String name) {
        return new NOptionalEmptyImpl<>(name == null ? (() -> NMsg.ofInvalidValue(cause)) : () -> NMsg.ofInvalidValue(cause, name));
    }

    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        NAssert.requireNamedNonNull(mapper);
        return (NOptional<V>) this;
    }

    @Override
    public NOptionalType type() {
        return NOptionalType.ERROR;
    }

    @Override
    public Throwable getError() {
        return cause;
    }

    @Override
    public T get() {
      /**
       * Throw error.
       *
       * @param message() message()
       */
        throwError(message());
        return null;
    }

    @Override
    public T get(Supplier<NMsg> message) {
      /**
       * Throw error.
       *
       * @param message message
       */
        throwError(message);
        //never reached!
        return null;
    }

    @Override
    public NOptional<T> onBlankEmpty() {
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

    /**
     * Throw error.
     *
     * @param preferredMessage preferred message
     */
    protected void throwError(Supplier<NMsg> preferredMessage) {
        if (preferredMessage == null) {
            preferredMessage = message();
        }
        if (preferredMessage == null) {
            preferredMessage = NMsg::ofMissingValue;
        }
        Supplier<NMsg> finalMessage = preferredMessage;
        NMsg eMsg = NApiUtilsRPI.resolveValidErrorMessage(() -> finalMessage == null ? null : finalMessage.get());
        NMsg m = prepareMessage(eMsg);
        RuntimeException exception = null;
        NOptionalExceptionFactory exceptionFactory = getExceptionFactory();
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
            if (NWorkspace.get().isPresent()) {
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


    @Override
    public NElement describe() {
        return NElement.ofTupleBuilder("Optional")
                .add("evaluated", true)
                .add("empty", false)
                .add("error", true)
                .build()
                ;
    }
}
