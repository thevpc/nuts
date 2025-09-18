package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.function.Supplier;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNConcurrentFactory implements NConcurrentFactory {
    private NRateLimitValueStore memoryStore = new NRateLimitValueStoreMemory();
    private NRateLimitValueFactory memoryFactory = new NRateLimitValueFactoryImpl(memoryStore);

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public <T> NRateLimitValueFactory rateLimitValueFactory() {
        return memoryFactory;
    }

    @Override
    public <T> NRateLimitValueFactory rateLimitValueFactory(NRateLimitValueStore store) {
        return store == null ? memoryFactory : new NRateLimitValueFactoryImpl(store);
    }

    @Override
    public <T> NCachedValue<T> cachedValue(Supplier<T> supplier) {
        return NCachedValueImpl.of(supplier);
    }

    @Override
    public <T> NStableValue<T> stableValue(Supplier<T> supplier) {
        return NStableValueImpl.of(supplier);
    }
}
