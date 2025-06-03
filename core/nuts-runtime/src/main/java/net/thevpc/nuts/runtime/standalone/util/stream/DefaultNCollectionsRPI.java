package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.reserved.rpi.NCollectionsRPI;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.*;
import java.util.stream.Stream;

@NComponentScope(NScopeType.SESSION)
public class DefaultNCollectionsRPI implements NCollectionsRPI {

    public DefaultNCollectionsRPI() {
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public <T> NStream<T> arrayToStream(T[] str) {
        String name = null;
        if (str == null) {
            return new NStreamEmpty<T>(name);
        }
        return iterableToStream((Iterable<T>) Arrays.asList(str)).withDesc(() -> NElements.ofString("array"));
    }

    @Override
    public <T> NStream<T> iterableToStream(Iterable<T> str) {
        String name = null;
        if (str == null) {
            return new NStreamEmpty<T>(name);
        }
        if (str instanceof List) {
            return new NStreamFromList<T>(name, (List<T>) str);
        }
        if (str instanceof Collection) {
            return new NStreamFromCollection<T>(name, (Collection<T>) str);
        }

        return new NStreamFromNIterable<>(name, NIterable.of(str));
    }

    @Override
    public <T> NStream<T> iteratorToStream(Iterator<T> str) {
        return new NStreamFromNIterator<T>(null,
                NIterator.of(str)
        );
    }

    @Override
    public <T> NStream<T> toStream(Stream<T> str) {
        return new NStreamFromJavaStream<>(null, str);
    }

    @Override
    public <T> NStream<T> emptyStream() {
        return new NStreamEmpty<T>(null);
    }

    @Override
    public <T> NIterator<T> emptyIterator() {
        return NIteratorBuilder.emptyIterator();
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
        return new NIterableFromJavaIterable<>(o);
    }


    @Override
    public <T> NStream<T> optionalToStream(Optional<T> item) {
        if (item == null || !item.isPresent()) {
            return emptyStream();
        }
        return NStream.ofArray(item.get());
    }

    @Override
    public <T> NStream<T> optionalToStream(NOptional<T> item) {
        if (item == null || !item.isPresent() || item.isError()) {
            return emptyStream();
        }
        return NStream.ofArray(item.get());
    }

}
