package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;
import java.util.function.Supplier;

public class NutsReservedOptionalValid<T> extends NutsReservedOptionalImpl<T> {
    private T value;

    public NutsReservedOptionalValid(T value) {
        this.value = value;
    }

    public T get(NutsSession session) {
        return value;
    }

    @Override
    public T get(Function<NutsSession, NutsMessage> message, NutsSession session) {
        return value;
    }

    @Override
    public T get(Supplier<NutsMessage> message) {
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
    public boolean isNotPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public NutsOptional<T> ifBlankNull() {
        T v = get();
        if (NutsBlankable.isBlank(v)) {
            return NutsOptional.ofEmpty((session) -> NutsMessage.ofPlain("blank value"));
        }
        return this;
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return (session) -> NutsMessage.ofPlain("element not found");
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public String toString() {
        return "ValidOptional@" + System.identityHashCode(this) + "=" + get();
    }

    @Override
    public NutsOptional<T> withDefault(T value) {
        return this;
    }

    @Override
    public NutsOptional<T> withoutDefault() {
        return this;
    }

    @Override
    public T orDefault() {
        return get();
    }

    @Override
    public NutsOptional<T> withDefault(Supplier<T> value) {
        return this;
    }
}
