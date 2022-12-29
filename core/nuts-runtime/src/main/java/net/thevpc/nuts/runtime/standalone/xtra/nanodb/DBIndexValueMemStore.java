package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

import java.io.File;
import java.util.Arrays;
import java.util.stream.LongStream;

public class DBIndexValueMemStore implements DBIndexValueStore {
    private long[] val;
    private File file;
    private final int max = 1000;
    private final NanoDBIndex index;
    private final Object indexKey;
    private DBIndexValueFileStore fallback;

    public DBIndexValueMemStore(NanoDBIndex index, Object indexKey, long[] val) {
        this.index = index;
        this.indexKey = indexKey;
        this.val = val;
    }

    @Override
    public void add(long position, NSession session) {
        if (fallback == null && val.length + 1 < max) {
            if (val.length == 0) {
                val = new long[]{position};
            } else {
                long[] t2 = new long[val.length + 1];
                System.arraycopy(val, 0, t2, 0, val.length);
                t2[val.length] = position;
                val = t2;
            }
        } else {
            if (fallback == null) {
                fallback = new DBIndexValueFileStore(index, indexKey);
                fallback.addAll(val, session);
                val = null;
            }
            fallback.add(position, session);
        }
    }


    @Override
    public void addAll(long[] position, NSession session) {
        if (fallback == null && val.length + position.length < max) {
            if (val.length == 0) {
                val = Arrays.copyOf(position, position.length);
            } else {
                long[] t2 = new long[val.length + position.length];
                System.arraycopy(val, 0, t2, 0, val.length);
                System.arraycopy(position, 0, t2, val.length, position.length);
                val = t2;
            }
        } else {
            if (fallback == null) {
                fallback = new DBIndexValueFileStore(index, indexKey);
                fallback.addAll(val, session);
                val = null;
            }
            fallback.addAll(position, session);
        }
    }

    @Override
    public LongStream stream(NSession session) {
        if (fallback == null) {
            return Arrays.stream(val);
        } else {
            return fallback.stream(session);
        }
    }

    @Override
    public boolean isMem() {
        return fallback == null;
    }

    public void flush(NSession session) {
        if (fallback != null) {
            fallback.flush(session);
        }
    }
}
