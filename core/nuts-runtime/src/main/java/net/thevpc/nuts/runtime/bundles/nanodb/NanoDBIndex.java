package net.thevpc.nuts.runtime.bundles.nanodb;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface NanoDBIndex<K> {
    void put(K k,long position);

    LongStream get(K k);

    void flush() ;

    void load();

    void clear();

    Stream<K> findAll();
}
