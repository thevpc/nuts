package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.spi.NutsStreams;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsIterable;
import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.util.NutsStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
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
    public <T> NutsStream<T> createStream(T[] str, Function<NutsSession, NutsElement> descr) {
        checkSession();
        String name=null;
        if (str == null) {
            return new NutsEmptyStream<T>(getSession(), name);
        }
        return createStream((Iterable<T>) Arrays.asList(str),descr);
    }

    @Override
    public <T> NutsStream<T> createStream(NutsIterable<T> str) {
        checkSession();
        String name=null;
        if (str == null) {
            return new NutsEmptyStream<T>(getSession(), name);
        }
        if (str instanceof List) {
            return new NutsListStream<T>(getSession(), name, (List<T>) str,e->str.describe(e));
        }
        if (str instanceof Collection) {
            return new NutsCollectionStream<T>(getSession(), name, (Collection<T>) str,e->str.describe(e));
        }
        return new NutsIterableStream<>(getSession(), name, str);
    }

    @Override
    public <T> NutsStream<T> createStream(Iterable<T> str, Function<NutsSession, NutsElement> descr) {
        checkSession();
        String name=null;
        if (str == null) {
            return new NutsEmptyStream<T>(getSession(), name);
        }
        if (str instanceof List) {
            return new NutsListStream<T>(getSession(), name, (List<T>) str,descr);
        }
        if (str instanceof Collection) {
            return new NutsCollectionStream<T>(getSession(), name, (Collection<T>) str,descr);
        }
        return new NutsIterableStream<>(getSession(), name, NutsIterable.of(str,descr));
    }

    @Override
    public <T> NutsStream<T> createStream(Iterator<T> str, Function<NutsSession, NutsElement> name) {
        return new NutsIteratorStream<T>(session, null,
                NutsIterator.of(str, name)
        );
    }
    @Override
    public <T> NutsStream<T> createStream(NutsIterator<T> str) {
        return new NutsIteratorStream<T>(session, null,str);
    }

    @Override
    public <T> NutsStream<T> createStream(Stream<T> str, Function<NutsSession, NutsElement> name) {
        checkSession();
        return new NutsJavaStream<>(getSession(), null, str, name);
    }

    @Override
    public <T> NutsStream<T> createEmptyStream() {
        return new NutsEmptyStream<T>(getSession(), null);
    }

    public void checkSession() {
        //should we ?
    }

    public NutsSession getSession() {
        return session;
    }
}
