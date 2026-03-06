package net.thevpc.nuts.util;

public interface NChunkedStore<T> extends AutoCloseable {
    void flush();

    void add(T content);

    boolean isEmpty();

    long size();

    NIterator<T> iterator();

    NStream<T> stream();

    void close();
}
