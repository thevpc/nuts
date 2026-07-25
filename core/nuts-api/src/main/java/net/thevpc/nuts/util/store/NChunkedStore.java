package net.thevpc.nuts.util.store;

import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

public interface NChunkedStore<T> extends AutoCloseable {
    void flush();

    void add(T content);

    boolean isEmpty();

    long size();

    NIterator<T> iterator();

    NStream<T> stream();

    void close();
}
