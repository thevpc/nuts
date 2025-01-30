package net.thevpc.nuts.runtime.standalone.xtra.nanodb.mem;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.DBIndexValueStore;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.DBIndexValueStoreFactory;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBAbstractIndex;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBSerializer;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NanoDBDefaultIndexInMem<T> extends NanoDBAbstractIndex<T> {
    public static final String NANODB_INDEX_0_8_1 = "nanodb-index-0.8.1";
    private Map<T, DBIndexValueStore> index = new HashMap<>();
    private DBIndexValueStoreFactory storeFactory;
    private Class<T> keyType;
    private NWorkspace workspace;

    public NanoDBDefaultIndexInMem(NWorkspace workspace, Class<T> keyType, NanoDBSerializer<T> ser, DBIndexValueStoreFactory storeFactory, Map<T, DBIndexValueStore> index) {
        super(ser);
        this.keyType = keyType;
        this.index = index;
        this.storeFactory = storeFactory;
        this.workspace = workspace;
    }


    @Override
    public void load() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void put(T s, long position) {
        DBIndexValueStore store = index.get(s);
        if (store == null) {
            store = storeFactory.create(this, s);
            index.put(s, store);
        }
        store.add(position);
    }

    @Override
    public LongStream get(T s) {
        DBIndexValueStore store = index.get(s);
        return store == null ? Arrays.stream(new long[0]) : store.stream();
    }

    @Override
    public void clear() {
        index.clear();
    }

    @Override
    public Stream<T> findAll() {
        return index.keySet().stream();
    }

}
