package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class PrivateNutsOptionalImpl<T> implements NutsOptional<T> {

    public PrivateNutsOptionalImpl() {
    }

    public <V> PrivateNutsOptionalImpl<V> asEmpty() {
        return new PrivateNutsOptionalEmpty<>(getMessage(), false);
    }

    public T get() {
        if(isPresent()){
            return get(null);
        }
        throw new NoSuchElementException("empty value");
    }

    static NutsMessage buildMessage(NutsSession session, Function<NutsSession, NutsMessage> message0, NutsMessage message) {
        NutsMessage m = message0.apply(session);
        return m.concat(session, NutsMessage.plain(" : "), message);
    }
    public <V> NutsOptional<V> flatMap(Function<T, NutsOptional<V>> mapper) {
        return new PrivateNutsOptionalFlatMap<>(mapper, this);
    }

    public <V> NutsOptional<V> map(Function<T, V> mapper) {
        return new PrivateNutsOptionalMap(mapper, this);
    }

    public <V> NutsOptional<V> filter(NutsMessagedPredicate<T> p) {
        return new PrivateNutsOptionalFilter(p.filter(), this, p.message());
    }

    public <V> NutsOptional<V> filter(Predicate<T> filter, Function<NutsSession, NutsMessage> message) {
        return new PrivateNutsOptionalFilter(filter, this, message);
    }

    public NutsOptional<T> ifPresent(Consumer<T> t){
        if(isPresent()){
            t.accept(get(null));
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
        return get(null);
    }


    @Override
    public T orElseGet(Supplier<? extends T> other) {
        if (isEmpty()) {
            return other.get();
        }
        return get(null);
    }

    @Override
    public boolean isEmpty() {
        return !isPresent();
    }

    @Override
    public NutsOptional<T> nonBlank() {
        if(isBlank()){
            return asEmpty();
        }
        return this;
    }

}
