package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NCallable;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNConcurrent implements NConcurrent {
    private final NRateLimitValueFactory memoryRateLimitValueFactory = new NRateLimitValueFactoryImpl(new NRateLimitValueStoreMemory(), null, null);
    private NRateLimitValueFactory rateLimitValueFactory;

    private final NSagaCallFactory memorySagaFactory = new NSagaCallFactoryImpl(new NSagaCallStoreMemory(), null);
    private NSagaCallFactory sagaFactory;

    private final NCachedValueFactory memoryCachedValueFactory = new NCachedValueFactoryImpl(new NCachedValueStoreMemory());
    private NCachedValueFactory cachedValueFactory;
    private final NStableValueFactory memoryStableValueFactory = new NStableValueFactoryImpl(new NStableValueStoreMemory(), null);
    private NStableValueFactory stableValueFactory;

    private final NRetryCallFactory memoryRetryValueFactory = new NRetryCallFactoryImpl(new NRetryCallStoreMemory(), null);
    private NRetryCallFactory retryValueFactory;

    private final NCircuitBreakerCallFactory memoryCircuitBreakerCallFactory = new NCircuitBreakerCallFactoryImpl(new NCircuitBreakerCallStoreMemory(), null);
    private NCircuitBreakerCallFactory circuitBreakerCallFactory;

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NConcurrent setCachedValueFactory(NCachedValueFactory cachedValueFactory) {
        this.cachedValueFactory = cachedValueFactory;
        return this;
    }

    @Override
    public NCachedValueFactory memoryCachedValueFactory() {
        return memoryCachedValueFactory;
    }

    @Override
    public NCachedValueFactory defaultCachedValueFactory() {
        return memoryCachedValueFactory();
    }

    @Override
    public NCachedValueFactory cachedValueFactory(NCachedValueStore store) {
        return store == null ? defaultCachedValueFactory() : new NCachedValueFactoryImpl(store);
    }

    @Override
    public NCachedValueFactory cachedValueFactory() {
        return cachedValueFactory == null ? defaultCachedValueFactory() : cachedValueFactory;
    }


    @Override
    public NConcurrent setRetryCallFactory(NRetryCallFactory retryCallFactory) {
        this.retryValueFactory = retryCallFactory;
        return this;
    }

    @Override
    public NRetryCallFactory memoryRetryCallFactory() {
        return memoryRetryValueFactory;
    }

    @Override
    public NRetryCallFactory defaultRetryCallFactory() {
        return memoryRetryCallFactory();
    }

    @Override
    public NRetryCallFactory retryCallFactory() {
        return retryValueFactory == null ? defaultRetryCallFactory() : retryValueFactory;
    }


    @Override
    public NConcurrent setStableValueFactory(NStableValueFactory StableValueFactory) {
        this.stableValueFactory = StableValueFactory;
        return this;
    }

    @Override
    public NStableValueFactory memoryStableValueFactory() {
        return memoryStableValueFactory;
    }

    @Override
    public NStableValueFactory defaultStableValueFactory() {
        return memoryStableValueFactory();
    }

    @Override
    public NStableValueFactory stableValueFactory(NStableValueStore store) {
        return store == null ? defaultStableValueFactory() : new NStableValueFactoryImpl(store, null);
    }

    @Override
    public NStableValueFactory stableValueFactory() {
        return stableValueFactory == null ? defaultStableValueFactory() : stableValueFactory;
    }


    @Override
    public NRateLimitValueFactory defaultRateLimitValueFactory() {
        return memoryRateLimitValueFactory;
    }

    @Override
    public NSagaCallFactory defaultSagaFactory() {
        return memorySagaFactory;
    }

    @Override
    public NSagaCallFactory memorySagaFactory() {
        return memorySagaFactory;
    }

    @Override
    public NRateLimitValueFactory memoryRateLimitValueFactory() {
        return memoryRateLimitValueFactory;
    }

    @Override
    public NConcurrent setRateLimitValueFactory(NRateLimitValueFactory factory) {
        this.rateLimitValueFactory = factory;
        return this;
    }

    @Override
    public NRateLimitValueFactory rateLimitValueFactory() {
        return rateLimitValueFactory == null ? defaultRateLimitValueFactory() : rateLimitValueFactory;
    }


    @Override
    public NRateLimitValueFactory rateLimitValueFactory(NRateLimitValueStore store) {
        return store == null ? defaultRateLimitValueFactory() : new NRateLimitValueFactoryImpl(store, null, null);
    }

    @Override
    public NSagaCallFactory sagaFactory() {
        return sagaFactory == null ? defaultSagaFactory() : sagaFactory;
    }


    @Override
    public <T> NCachedValue<T> cachedValue(Supplier<T> supplier) {
        return cachedValueFactory().of(supplier);
    }

    @Override
    public <T> NStableValue<T> stableValue(Supplier<T> supplier) {
        return stableValueFactory().of(supplier);
    }

    @Override
    public <T> NCachedValue<T> cachedValue(String id, Supplier<T> supplier) {
        return cachedValueFactory().of(id, supplier);
    }

    @Override
    public <T> NStableValue<T> stableValue(String id, Supplier<T> supplier) {
        return stableValueFactory().of(id, supplier);
    }

    @Override
    public <T> NRetryCall<T> retryCall(String id, NCallable<T> callable) {
        return retryCallFactory().of(id, callable);
    }

    @Override
    public <T> NRetryCall<T> retryCall(NCallable<T> callable) {
        return retryCallFactory().of(callable);
    }

    @Override
    public <T> NCircuitBreakerCall<T> circuitBreakerCall(NCallable<T> callable) {
        return circuitBreakerCallFactory().of(callable);
    }

    @Override
    public <T> NCircuitBreakerCall<T> circuitBreakerCall(String id, NCallable<T> callable) {
        return circuitBreakerCallFactory().of(id,callable);
    }

    @Override
    public NConcurrent setCircuitBreakerCallFactory(NCircuitBreakerCallFactory circuitBreakerCallFactory) {
        this.circuitBreakerCallFactory=circuitBreakerCallFactory;
        return this;
    }

    @Override
    public NCircuitBreakerCallFactory memoryCircuitBreakerCallFactory() {
        return memoryCircuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory defaultCircuitBreakerCallFactory() {
        return memoryCircuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory circuitBreakerCallFactory() {
        return circuitBreakerCallFactory == null ? defaultCircuitBreakerCallFactory() : circuitBreakerCallFactory;
    }

    @Override
    public ExecutorService executorService() {
        return NWorkspaceExt.of().getModel().configModel.executorService();
    }

    @Override
    public NSagaBuilder saga() {
        return sagaFactory().of();
    }
}
