package net.thevpc.nuts.runtime.bundles.nanodb;

public class DBIndexValueStoreDefaultFactory implements DBIndexValueStoreFactory{
    @Override
    public DBIndexValueStore create(NanoDBIndex index, Object indexKey) {
        return new DBIndexValueMemStore(index, indexKey, new long[0]);
    }

    @Override
    public <T> DBIndexValueStore load(NanoDBIndex<T> index, Object indexKey, long[] pos) {
        return new DBIndexValueMemStore(index, indexKey, new long[0]);
    }

    @Override
    public <T> DBIndexValueStore loadExternal(NanoDBIndex<T> index, Object indexKey) {
        return new DBIndexValueFileStore(index,indexKey);
    }
}
