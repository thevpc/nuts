package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reserved.rpi.NCollectionsRPI;
import net.thevpc.nuts.lib.common.iter.NIteratorEmpty;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NIterable;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.lib.common.iter.NIteratorBaseFromJavaIterator;
import net.thevpc.nuts.lib.common.iter.NIterableFromJavaIterable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@NComponentScope(NScopeType.SESSION)
public class DefaultNCollectionsRPI implements NCollectionsRPI {
    private final NSession session;

    public DefaultNCollectionsRPI(NSession session) {
        this.session = session;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public <T> NStream<T> arrayToStream(T[] str) {
        checkSession();
        String name = null;
        if (str == null) {
            return new NStreamEmpty<T>(name);
        }
        return iterableToStream((Iterable<T>) Arrays.asList(str)).withDesc(() -> NElements.of().ofString("array"));
    }

    @Override
    public <T> NStream<T> iterableToStream(Iterable<T> str) {
        checkSession();
        String name = null;
        if (str == null) {
            return new NStreamEmpty<T>(name);
        }
        if (str instanceof List) {
            return new NStreamFromList<T>(getSession(), name, (List<T>) str);
        }
        if (str instanceof Collection) {
            return new NStreamFromCollection<T>(getSession(), name, (Collection<T>) str);
        }

        return new NStreamFromNIterable<>(getSession(), name, NIterable.of(str));
    }

    @Override
    public <T> NStream<T> iteratorToStream(Iterator<T> str) {
        return new NStreamFromNIterator<T>(null,
                NIterator.of(str)
        );
    }

    @Override
    public <T> NStream<T> toStream(Stream<T> str) {
        checkSession();
        return new NStreamFromJavaStream<>(getSession(), null, str);
    }

    @Override
    public <T> NStream<T> emptyStream() {
        return new NStreamEmpty<T>(null);
    }

    @Override
    public <T> NIterator<T> emptyIterator() {
        return new NIteratorEmpty<>();
    }

    @Override
    public <T> NIterator<T> toIterator(Iterator<T> str) {
        if (str == null) {
            return null;
        }
        if (str instanceof NIterator<?>) {
            return (NIterator<T>) str;
        }
        return new NIteratorBaseFromJavaIterator<>(str);
    }

    @Override
    public <T> NIterable<T> toIterable(Iterable<T> o) {
        if (o == null) {
            return null;
        }
        if (o instanceof NIterable) {
            return (NIterable<T>) o;
        }
        return new NIterableFromJavaIterable<>(o,session);
    }

    public void checkSession() {
        //should we ?
    }

    public NSession getSession() {
        return session;
    }
}
