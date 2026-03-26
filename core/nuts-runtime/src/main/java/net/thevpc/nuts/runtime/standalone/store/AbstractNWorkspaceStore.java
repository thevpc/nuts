package net.thevpc.nuts.runtime.standalone.store;

import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.util.NAssert;

import java.util.function.Supplier;

public abstract class AbstractNWorkspaceStore implements NWorkspaceStore{

    @Override
    public <T> T supplyWithCache(NStoreKey k, Class<T> type, Supplier<T> supplier) {
        NAssert.requireNamedTrue(k.type() == NStoreType.CACHE, "cache");
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
