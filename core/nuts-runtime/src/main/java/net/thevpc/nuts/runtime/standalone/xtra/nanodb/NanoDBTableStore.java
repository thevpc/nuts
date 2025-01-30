package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.util.NStream;

import java.util.*;

public interface NanoDBTableStore<T> extends Iterable<T>, AutoCloseable {


    T get(long position);

    long add(T a);

    void flush();

    void close();

    NStream<T> stream();

    Iterable<T> items();

    Iterator<T> iterator();

    void updateIndices(T a, long objectId);

    NStream<T> findByIndex(String indexName, Object value);

    <T> NStream<T> findIndexValues(String indexName);

    long getFileLength();


}
