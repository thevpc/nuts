package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsNoSuchElementException;
import net.thevpc.nuts.NutsSession;

import java.util.function.Function;
import java.util.function.Predicate;

class FilterNutsOptional<T> extends NutsOptionalBase<T> {
    private final Predicate<T> filter;
    private final NutsOptionalImpl<T> t;
    private final Function<NutsSession, NutsMessage> message;

    public FilterNutsOptional(NutsSession session, Predicate<T> filter, NutsOptionalImpl<T> t, Function<NutsSession, NutsMessage> message) {
        super(session);
        this.filter = filter;
        this.t = t;
        this.message = message;
    }

    @Override
    public T get(NutsMessage message) {
        T v = this.t.get(message);
        if (filter.test(v)) {
            return v;
        }
        throw new NutsNoSuchElementException(getSession(), buildMessage(getSession(), this.message, message));
    }


    @Override
    public T get() {
        T v = this.t.get();
        if (filter.test(v)) {
            return v;
        }
        throw new NutsNoSuchElementException(getSession(), this.message.apply(getSession()));
    }

    @Override
    public boolean isPresent() {
        return t.isPresent() && filter.test(t.get());
    }

    @Override
    protected Function<NutsSession, NutsMessage> getEmptyMessage() {
        return t.getEmptyMessage();
    }
}
