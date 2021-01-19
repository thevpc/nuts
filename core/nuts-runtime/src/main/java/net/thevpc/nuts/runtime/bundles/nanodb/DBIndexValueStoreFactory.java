package net.thevpc.nuts.runtime.bundles.nanodb;

public interface DBIndexValueStoreFactory {
    DBIndexValueStore create(NanoDBIndex index,Object indexKey);

    <T> DBIndexValueStore load(NanoDBIndex<T> index, Object indexKey, long[] pos);

    <T> DBIndexValueStore loadExternal(NanoDBIndex<T> index, Object indexKey);
}
