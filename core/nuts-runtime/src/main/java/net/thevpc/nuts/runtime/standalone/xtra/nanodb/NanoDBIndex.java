package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface NanoDBIndex<K> extends java.io.Closeable {
    void put(K k, long position);

    LongStream get(K k);

    void flush() ;

    void load();

    void clear();

    Stream<K> findAll();

    @Override
    void close() throws java.io.IOException;
}
