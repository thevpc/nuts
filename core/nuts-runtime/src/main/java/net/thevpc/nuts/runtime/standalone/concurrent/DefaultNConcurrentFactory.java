package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.function.Supplier;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNConcurrentFactory implements NConcurrentFactory {
    private NRateLimitedValueStore memoryStore = new NRateLimitedValueStoreMemory();
    private NRateLimitedValueFactory memoryFactory = new NRateLimitedValueFactoryImpl(memoryStore);

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public <T> NRateLimitedValueFactory rateLimitedValueFactory() {
        return memoryFactory;
    }

    @Override
    public <T> NRateLimitedValueFactory rateLimitedValueFactory(NRateLimitedValueStore store) {
        return store == null ? memoryFactory : new NRateLimitedValueFactoryImpl(store);
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
