package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStream;
import net.thevpc.nuts.spi.NutsStreams;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class DefaultNutsStreams implements NutsStreams {
    private final NutsSession session;

    public DefaultNutsStreams(NutsSession session) {
        this.session = session;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public <T> NutsStream<T> createStream(T[] str, String name) {
        checkSession();
        if (str == null) {
            return new NutsEmptyStream<T>(getSession(), name);
        }
        return new NutsListStream<T>(getSession(), name, Arrays.asList(str));
    }

    @Override
    public <T> NutsStream<T> createStream(Iterable<T> str, String name) {
        checkSession();
        if (str == null) {
            return new NutsEmptyStream<T>(getSession(), name);
        }
        if (str instanceof List) {
            return new NutsListStream<T>(getSession(), name, (List<T>) str);
        }
        if (str instanceof Collection) {
            return new NutsCollectionStream<T>(getSession(), name, (Collection<T>) str);
        }
        return new NutsIterableStream<>(getSession(), name, str);
    }

    @Override
    public <T> NutsStream<T> createStream(Iterator<T> str, String name) {
        return new NutsIteratorStream<T>(session, name, str);
    }

    @Override
    public <T> NutsStream<T> createStream(Stream<T> str, String name) {
        checkSession();
        return new NutsJavaStream<>(getSession(), name, str);
    }

    public void checkSession() {
        //should we ?
    }

    public NutsSession getSession() {
        return session;
    }
}
