package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.internal.rpi.NCollectionsRPI;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;

import java.util.*;
import java.util.stream.Stream;

@NComponentScope(NScopeType.SESSION)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNCollectionsRPI implements NCollectionsRPI {

    public DefaultNCollectionsRPI() {
    }

    @Override
    public <T> NStream<T> arrayToStream(T[] str) {
        String name = null;
        if (str == null) {
            return NStreamBase.ofEmpty(name);
        }
        return iterableToStream((Iterable<T>) Arrays.asList(str)).withDescription(() -> NElement.ofString("array"));
    }

    @Override
    public <T> NStream<T> iterableToStream(Iterable<T> str) {
        String name = null;
        if (str == null) {
            return NStreamBase.ofEmpty(name);
        }
        if (str instanceof Collection) {
            return NStreamBase.ofCollection(name, (Collection<T>) str);
        }

        return NStreamBase.ofIterable(name, NIterable.of(str));
    }

    @Override
    public <T> NStream<T> iteratorToStream(Iterator<T> str) {
        return NStreamBase.ofIterator(null,
                NIterator.of(str)
        );
    }

    @Override
    public <T> NStream<T> toStream(Stream<T> str) {
        return NStreamBase.ofJavaStream(null, str);
    }

    @Override
    public <T> NStream<T> emptyStream() {
        return NStreamBase.ofEmpty(null);
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
