package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsOptionalFactory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NutsOptional<T> {

    static <T> NutsOptional<T> ofEmpty(NutsSession session, Function<NutsSession, NutsMessage> errorMessage) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsOptionalFactory.class, true, null)
                .ofEmpty(session,errorMessage);
    }

    static <T> NutsOptional<T> ofNull(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsOptionalFactory.class, true, null)
                .ofNull(session);
    }

    static <T> NutsOptional<T> of(NutsSession session, T value) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsOptionalFactory.class, true, null)
                .of(session,value);
    }

    static <T> NutsOptional<T> of(NutsSession session, T value, Function<NutsSession, NutsMessage> errorMessage) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsOptionalFactory.class, true, null)
                .of(session,value,errorMessage);
    }

    static <T> NutsOptional<T> ofOptional(NutsSession session, Optional<T> optional, Function<NutsSession, NutsMessage> errorMessage) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsOptionalFactory.class, true, null)
                .ofOptional(session,optional,errorMessage);
    }

    <V> NutsOptional<V> flatMap(Function<T, NutsOptional<V>> mapper);

    <V> NutsOptional<V> map(Function<T, V> mapper);

    <V> NutsOptional<V> filter(NutsMessagedPredicate<T> p);

    <V> NutsOptional<V> filter(Predicate<T> filter, Function<NutsSession, NutsMessage> message);

    NutsOptional<T> ifPresent(Consumer<T> t);

    <V> NutsOptional<V> asEmpty();

    T get(NutsMessage message);

    T get();

    T orElse(T other);

    NutsOptional<T> orElseGetOptional(Supplier<NutsOptional<T>> other);

    T orElseGet(Supplier<? extends T> other);

    boolean isEmpty();

    boolean isPresent();


    interface NutsMessagedPredicate<T> {
        Predicate<T> filter();

        Function<NutsSession, NutsMessage> message();
    }
}
