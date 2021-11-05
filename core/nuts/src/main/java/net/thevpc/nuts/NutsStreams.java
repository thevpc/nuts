package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;

import java.util.Iterator;
import java.util.stream.Stream;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsStreams extends NutsComponent {
    static NutsStreams of(NutsSession session) {
        return session.extensions().createSupported(NutsStreams.class, true, session);
    }

    <T> NutsStream<T> create(T[] str, String name);

    <T> NutsStream<T> create(Iterable<T> str, String name);

    <T> NutsStream<T> create(Iterator<T> str, String name);

    <T> NutsStream<T> create(Stream<T> str, String name);
}
