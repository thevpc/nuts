package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface NanoDBIndex<K> {
    void put(K k, long position, NSession session);

    LongStream get(K k, NSession session);

    void flush(NSession session) ;

    void load(NSession session);

    void clear(NSession session);

    Stream<K> findAll(NSession session);
}
