package net.thevpc.nuts.runtime.standalone.store;

import net.thevpc.nuts.NLocationKey;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.util.NAssert;

import java.util.function.Supplier;

public abstract class AbstractNWorkspaceStore implements NWorkspaceStore{

    @Override
    public <T> T supplyWithCache(NLocationKey k, Class<T> type, Supplier<T> supplier) {
        NAssert.requireTrue(k.getStoreType() == NStoreType.CACHE, "cache");
        T t = null;
        try {
            t = (T) loadLocationKey(k, type);
            if (t != null) {
                return t;
            }
        } catch (Exception e) {
            //
        }
        t = supplier.get();
        if (t != null) {
            saveLocationKey(k, t);
        }
        return t;
    }
}
