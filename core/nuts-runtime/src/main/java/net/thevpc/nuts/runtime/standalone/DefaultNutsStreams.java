package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStream;
import net.thevpc.nuts.NutsStreams;
import net.thevpc.nuts.runtime.standalone.util.*;
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
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public <T> NutsStream<T> create(T[] str, String name) {
        checkSession();
        if (str == null) {
            return new NutsEmptyStream<T>(getSession(), name);
        }
        return new NutsListStream<T>(getSession(), name, Arrays.asList(str));
    }

    @Override
    public <T> NutsStream<T> create(Iterable<T> str, String name) {
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
    public <T> NutsStream<T> create(Iterator<T> str, String name) {
        return new NutsIteratorStream<T>(session, name, str);
    }

    @Override
    public <T> NutsStream<T> create(Stream<T> str, String name) {
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
