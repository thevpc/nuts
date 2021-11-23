package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NutsSession;

import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface NanoDBIndex<K> {
    void put(K k, long position, NutsSession session);

    LongStream get(K k, NutsSession session);

    void flush(NutsSession session) ;

    void load(NutsSession session);

    void clear(NutsSession session);

    Stream<K> findAll(NutsSession session);
}
