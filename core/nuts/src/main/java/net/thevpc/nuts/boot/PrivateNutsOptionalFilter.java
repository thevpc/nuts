package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsNoSuchElementException;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;
import java.util.function.Predicate;

public class PrivateNutsOptionalFilter<T> extends PrivateNutsOptionalImpl<T> {
    private final Predicate<T> filter;
    private final PrivateNutsOptionalImpl<T> t;
    private final Function<NutsSession, NutsMessage> message;

    public PrivateNutsOptionalFilter(Predicate<T> filter, PrivateNutsOptionalImpl<T> t, Function<NutsSession, NutsMessage> message) {
        this.filter = filter;
        this.t = t;
        this.message = message;
    }

    @Override
    public T get(NutsMessage message,NutsSession session) {
        T v = this.t.get(message,session);
        if (filter.test(v)) {
            return v;
        }
        throw new NutsNoSuchElementException(session, buildMessage(session, this.message, message));
    }


    @Override
    public T get(NutsSession session) {
        T v = this.t.get(session);
        if (filter.test(v)) {
            return v;
        }
        throw new NutsNoSuchElementException(session, this.message.apply(session));
    }

    @Override
    public boolean isPresent() {
        return t.isPresent() && filter.test(t.get());
    }

    @Override
    public boolean isError() {
        return t.isError();
    }

    @Override
    public boolean isBlank() {
        return t.isBlank() || !isPresent();
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return t.getMessage();
    }
}
