package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;

public class PrivateNutsOptionalValid<T> extends PrivateNutsOptionalImpl<T> {
    private T value;

    public PrivateNutsOptionalValid(T value) {
        this.value = value;
    }

    public T get(NutsSession session) {
        return value;
    }

    @Override
    public T get(NutsMessage message, NutsSession session) {
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

    public NutsOptional<T> nonBlank() {
        T v = get();
        if (NutsBlankable.isBlank(v)) {
            return NutsOptional.ofEmpty((session) -> NutsMessage.cstyle("blank value"));
        }
        return this;
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return (session) -> NutsMessage.cstyle("element not found");
    }

    @Override
    public boolean isBlank() {
        return false;
    }
}
