package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.NStreams;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@NComponentScope(NScopeType.SESSION)
public class DefaultNStreams implements NStreams {
    private final NSession session;

    public DefaultNStreams(NSession session) {
        this.session = session;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NSupported.DEFAULT_SUPPORT;
    }

    @Override
    public <T> NStream<T> createStream(T[] str, Function<NSession, NElement> descr) {
        checkSession();
        String name=null;
        if (str == null) {
            return new NEmptyStream<T>(getSession(), name);
        }
        return createStream((Iterable<T>) Arrays.asList(str),descr);
    }

    @Override
    public <T> NStream<T> createStream(NIterable<T> str) {
        checkSession();
        String name=null;
        if (str == null) {
            return new NEmptyStream<T>(getSession(), name);
        }
        if (str instanceof List) {
            return new NListStream<T>(getSession(), name, (List<T>) str, e->str.describe(e));
        }
        if (str instanceof Collection) {
            return new NCollectionStream<T>(getSession(), name, (Collection<T>) str, e->str.describe(e));
        }
        return new NIterableStream<>(getSession(), name, str);
    }

    @Override
    public <T> NStream<T> createStream(Iterable<T> str, Function<NSession, NElement> descr) {
        checkSession();
        String name=null;
        if (str == null) {
            return new NEmptyStream<T>(getSession(), name);
        }
        if (str instanceof List) {
            return new NListStream<T>(getSession(), name, (List<T>) str,descr);
        }
        if (str instanceof Collection) {
            return new NCollectionStream<T>(getSession(), name, (Collection<T>) str,descr);
        }
        return new NIterableStream<>(getSession(), name, NIterable.of(str,descr));
    }

    @Override
    public <T> NStream<T> createStream(Iterator<T> str, Function<NSession, NElement> name) {
        return new NIteratorStream<T>(session, null,
                NIterator.of(str, name)
        );
    }
    @Override
    public <T> NStream<T> createStream(NIterator<T> str) {
        return new NIteratorStream<T>(session, null,str);
    }

    @Override
    public <T> NStream<T> createStream(Stream<T> str, Function<NSession, NElement> name) {
        checkSession();
        return new NJavaStream<>(getSession(), null, str, name);
    }

    @Override
    public <T> NStream<T> createEmptyStream() {
        return new NEmptyStream<T>(getSession(), null);
    }

    public void checkSession() {
        //should we ?
    }

    public NSession getSession() {
        return session;
    }
}
