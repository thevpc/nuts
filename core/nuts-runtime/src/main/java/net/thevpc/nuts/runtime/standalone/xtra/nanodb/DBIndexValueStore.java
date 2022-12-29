package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

import java.util.stream.LongStream;

public interface DBIndexValueStore {
    void add(long position, NSession session);

    void addAll(long[] positions, NSession session);

    LongStream stream(NSession session);

    boolean isMem();

    void flush(NSession session);
}
