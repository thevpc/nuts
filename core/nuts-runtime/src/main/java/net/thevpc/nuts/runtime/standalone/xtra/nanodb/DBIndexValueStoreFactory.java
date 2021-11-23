package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public interface DBIndexValueStoreFactory {
    DBIndexValueStore create(NanoDBIndex index,Object indexKey);

    <T> DBIndexValueStore load(NanoDBIndex<T> index, Object indexKey, long[] pos);

    <T> DBIndexValueStore loadExternal(NanoDBIndex<T> index, Object indexKey);
}
