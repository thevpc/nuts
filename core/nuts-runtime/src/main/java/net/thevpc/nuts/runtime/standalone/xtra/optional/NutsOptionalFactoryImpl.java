package net.thevpc.nuts.runtime.standalone.xtra.optional;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsOptionalFactory;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.Optional;
import java.util.function.Function;

public class NutsOptionalFactoryImpl implements NutsOptionalFactory {

    public <T> NutsOptional<T> ofEmpty(NutsSession session, Function<NutsSession, NutsMessage> errorMessage) {
        return new EmptyNutsOptional<>(session,errorMessage);
    }

    public <T> NutsOptional<T> of(NutsSession session, T value) {
        if (value == null) {
            throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("value could not be null"));
        }
        return new ValidNutsOptional<>(session, value);
    }

    public <T> NutsOptional<T> of(NutsSession session, T value, Function<NutsSession,NutsMessage> errorMessage) {
        if (value == null) {
            return new EmptyNutsOptional<>(session,errorMessage);
        }
        return new ValidNutsOptional<>(session,value);
    }

    @Override
    public <T> NutsOptional<T> ofNull(NutsSession session) {
        return new ValidNutsOptional<>(session,null);
    }

    public <T> NutsOptional<T> ofOptional(NutsSession session, Optional<T> optional, Function<NutsSession,NutsMessage> errorMessage) {
        if (optional.isPresent()) {
            return of(session,optional.get());
        }
        return ofEmpty(session,errorMessage);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
