package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.*;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class NutsOptionalImpl<T> implements NutsOptional<T> {

    public static <T> NutsOptionalImpl<T> empty(NutsSession session, Function<NutsSession, NutsMessage> errorMessage) {
        return new EmptyNutsOptional<>(session,errorMessage);
    }

    public static <T> NutsOptionalImpl<T> of(NutsSession session, T value) {
        if (value == null) {
            throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("value could not be null"));
        }
        return new ValidNutsOptional<>(session, value);
    }

    public static <T> NutsOptionalImpl<T> of(NutsSession session, T value, Function<NutsSession,NutsMessage> errorMessage) {
        if (value == null) {
            return new EmptyNutsOptional<>(session,errorMessage);
        }
        return new ValidNutsOptional<>(session,value);
    }

    public static <T> NutsOptionalImpl<T> ofOptional(NutsSession session, Optional<T> optional, Function<NutsSession,NutsMessage> errorMessage) {
        if (optional.isPresent()) {
            return of(session,optional.get());
        }
        return empty(session,errorMessage);
    }

    public <V> NutsOptionalImpl<V> asEmpty() {
        return empty(getSession(),getEmptyMessage());
    }

    protected abstract Function<NutsSession,NutsMessage> getEmptyMessage();

    static NutsMessage buildMessage(NutsSession session,Function<NutsSession,NutsMessage> message0, NutsMessage message) {
        NutsMessage m = message0.apply(session);
        return m.concat(session,NutsMessage.plain(" : "),message);
    }


    protected abstract NutsSession getSession();
}
