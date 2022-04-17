package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.util.Optional;
import java.util.function.Function;

public interface NutsOptionalFactory extends NutsComponent {
    <T> NutsOptional<T> ofEmpty(NutsSession session, Function<NutsSession, NutsMessage> errorMessage);

    <T> NutsOptional<T> of(NutsSession session, T value);

    <T> NutsOptional<T> of(NutsSession session, T value, Function<NutsSession, NutsMessage> errorMessage);

    <T> NutsOptional<T> ofOptional(NutsSession session, Optional<T> optional, Function<NutsSession, NutsMessage> errorMessage);

    <T> NutsOptional<T> ofNull(NutsSession session);
}
