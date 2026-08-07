package net.thevpc.nuts.collections;

import net.thevpc.nuts.pipeline.NIterator;
import net.thevpc.nuts.pipeline.NStream;

public interface NChunkedStore<T> extends AutoCloseable {
    void flush();

    void add(T content);

    boolean isEmpty();

    long size();

    NIterator<T> iterator();

    NStream<T> stream();

    void close();
}
