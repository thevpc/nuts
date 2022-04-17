package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

abstract class NutsOptionalBase<T> extends NutsOptionalImpl<T> {
    private NutsSession session;

    public NutsOptionalBase(NutsSession session) {
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    public <V> NutsOptional<V> flatMap(Function<T, NutsOptional<V>> mapper) {
        return new FlatMapNutsOptional<>(getSession(), mapper, this);
    }

    public <V> NutsOptional<V> map(Function<T, V> mapper) {
        return new MapNutsOptional(getSession(),mapper, this);
    }

    public <V> NutsOptional<V> filter(NutsMessagedPredicate<T> p) {
        return new FilterNutsOptional(getSession(),p.filter(), this, p.message());
    }

    public <V> NutsOptional<V> filter(Predicate<T> filter, Function<NutsSession, NutsMessage> message) {
        return new FilterNutsOptional(getSession(),filter, this, message);
    }

    public NutsOptional<T> ifPresent(Consumer<T> t){
        if(isPresent()){
            t.accept(get());
        }
        return this;
    }

    @Override
    public NutsOptional<T> orElseGetOptional(Supplier<NutsOptional<T>> other) {
        if (isEmpty()) {
            return other.get();
        }
        return this;
    }

    @Override
    public T orElse(T other) {
        if (isEmpty()) {
            return other;
        }
        return get();
    }


    @Override
    public T orElseGet(Supplier<? extends T> other) {
        if (isEmpty()) {
            return other.get();
        }
        return get();
    }

    @Override
    public boolean isEmpty() {
        return !isPresent();
    }
}
