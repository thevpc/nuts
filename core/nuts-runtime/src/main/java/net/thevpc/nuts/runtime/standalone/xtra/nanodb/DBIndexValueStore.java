package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NutsSession;

import java.util.stream.LongStream;

public interface DBIndexValueStore {
    void add(long position, NutsSession session);

    void addAll(long[] positions,NutsSession session);

    LongStream stream(NutsSession session);

    boolean isMem();

    void flush(NutsSession session);
}
