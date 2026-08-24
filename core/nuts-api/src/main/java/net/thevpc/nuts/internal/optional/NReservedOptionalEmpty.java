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
 * NReservedOptionalEmpty class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NReservedOptionalEmpty<T> extends NReservedOptionalThrowable<T> implements Cloneable {


    /**
     * N reserved optional empty.
     *
     * @param message message
     * @return n reserved optional empty result
     */
    public NReservedOptionalEmpty(Supplier<NMsg> message) {
      /**
       * Super.
       *
       * @param message message
       */
        super(message);
    }

    /**
     * With message.
     *
     * @param message message
     * @return with message result
     */
    public NOptional<T> withMessage(Supplier<NMsg> message) {
        return new NReservedOptionalEmpty<>(message);
    }

    /**
     * With message.
     *
     * @param message message
     * @return with message result
     */
    public NOptional<T> withMessage(NMsg message) {
        return new NReservedOptionalEmpty<T>(message == null ? (NMsg::ofMissingValue) : () -> message);
    }

    /**
     * With name.
     *
     * @param name name
     * @return with name result
     */
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
      /**
       * Throw error.
       *
       * @param message() message()
       */
        throwError(message());
        //never reached!
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

    /**
     * Then.
     *
     * @param mapper mapper
     * @return then result
     */
    public <V> NOptional<V> then(Function<T, V> mapper) {
        NAssert.requireNamedNonNull(mapper);
        return NOptional.ofEmpty(message());
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public NOptionalType type() {
        return NOptionalType.EMPTY;
    }

    @Override
    public NOptional<T> onBlankEmpty() {
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
            exception = exceptionFactory.createOptionalEmptyException(m);
        }
        if (exception == null) {
            exceptionFactory = NOptional.getDefaultExceptionFactory();
            if (exceptionFactory != null) {
                exception = exceptionFactory.createOptionalEmptyException(m);
            }
        }
        if (exception == null) {
            if (NWorkspace.get().isPresent()) {
                exception = new NEmptyOptionalException(preferredMessage.get());
            } else {
                exception = new NDetachedEmptyOptionalException(preferredMessage.get());
            }
        }
        throw exception;
    }

    @Override
    public NElement describe() {
        return NElement.ofTupleBuilder("Optional")
                .add("evaluated", true)
                .add("empty", true)
                .add("error", false)
                .build()
                ;

    }

}
