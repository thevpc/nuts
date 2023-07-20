package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;

import java.util.function.Function;
import java.util.function.Supplier;

public class NReservedOptionalValid<T> extends NReservedOptionalImpl<T> {
    private T value;

    public NReservedOptionalValid(T value) {
        this.value = value;
    }

    public T get(NSession session) {
        return value;
    }

    @Override
    public T get(Function<NSession, NMsg> message, NSession session) {
        return value;
    }

    @Override
    public T get(Supplier<NMsg> message) {
        return value;
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public NOptional<T> ifBlankEmpty() {
        if (isBlank()) {
            return NOptional.ofEmpty((session) -> NMsg.ofMissingValue());
        }
        return this;
    }

    @Override
    public Function<NSession, NMsg> getMessage() {
        return (session) -> NMsg.ofMissingValue();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(value);
    }

    @Override
    public String toString() {
        return "Optional@" + System.identityHashCode(this) + "=" + get();
    }

    @Override
    public NOptional<T> withDefault(T value) {
        return this;
    }

    @Override
    public NOptional<T> withoutDefault() {
        return this;
    }

    @Override
    public T orDefault() {
        return get();
    }

    @Override
    public NOptional<T> withDefault(Supplier<T> value) {
        return this;
    }

    @Override
    public boolean isNull() {
        return value==null;
    }

}
