package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import java.util.stream.LongStream;

public interface DBIndexValueStore {
    void add(long position);

    void addAll(long[] positions);

    LongStream stream();

    boolean isMem();

    void flush();
}
