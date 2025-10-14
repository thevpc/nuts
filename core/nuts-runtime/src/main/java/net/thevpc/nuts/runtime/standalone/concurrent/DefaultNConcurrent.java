package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.concurrent.NCallable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.IntFunction;
import java.util.function.Supplier;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNConcurrent implements NConcurrent {
    private final NRateLimitValueFactory memoryRateLimitValueFactory = new NRateLimitValueFactoryImpl(new NRateLimitValueStoreMemory(), null, null);
    private NRateLimitValueFactory rateLimitValueFactory;

    private final NSagaCallableFactory memorySagaFactory = new NSagaCallableFactoryImpl(new NSagaStoreMemory(), null);
    private NSagaCallableFactory sagaFactory;

    private final NCachedValueFactory memoryCachedValueFactory = new NCachedValueFactoryImpl(new NCachedValueStoreMemory());
    private NCachedValueFactory cachedValueFactory;
    private final NStableValueFactory memoryStableValueFactory = new NStableValueFactoryImpl(new NStableValueStoreMemory(), null);
    private NStableValueFactory stableValueFactory;

    private final NRetryCallFactory memoryRetryValueFactory = new NRetryCallFactoryImpl(new NRetryCallStoreMemory(), null);
    private NRetryCallFactory retryValueFactory;

    private final NCircuitBreakerCallFactory memoryCircuitBreakerCallFactory = new NCircuitBreakerCallFactoryImpl(new NCircuitBreakerCallStoreMemory(), null);
    private NCircuitBreakerCallFactory circuitBreakerCallFactory;

    private final NWorkBalancerFactory memoryWorkBalancerCallFactory = new NWorkBalancerFactoryImpl(new NWorkBalancerStoreMemory(), null, null);
    private NWorkBalancerFactory workBalancerCallFactory;

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
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
    public NStableValueFactory stableValueFactory() {
        return stableValueFactory == null ? defaultStableValueFactory() : stableValueFactory;
    }


    @Override
    public NRateLimitValueFactory defaultRateLimitValueFactory() {
        return memoryRateLimitValueFactory;
    }

    @Override
    public NSagaCallableFactory defaultSagaFactory() {
        return memorySagaFactory;
    }

    @Override
    public NSagaCallableFactory memorySagaFactory() {
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
    public NSagaCallableFactory sagaFactory() {
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
        return circuitBreakerCallFactory().of(id, callable);
    }

    @Override
    public NConcurrent setCircuitBreakerCallFactory(NCircuitBreakerCallFactory circuitBreakerCallFactory) {
        this.circuitBreakerCallFactory = circuitBreakerCallFactory;
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
    public NSagaCallableBuilder sagaCallBuilder() {
        return sagaFactory().ofBuilder();
    }

    @Override
    public NWorkBalancerFactory defaultWorkBalancerFactory() {
        return memoryWorkBalancerCallFactory;
    }

    @Override
    public NWorkBalancerFactory memoryWorkBalancerFactory() {
        return memoryWorkBalancerCallFactory;
    }

    @Override
    public NWorkBalancerFactory workBalancerFactory() {
        return workBalancerCallFactory == null ? defaultWorkBalancerFactory() : workBalancerCallFactory;
    }

    @Override
    public NConcurrent setWorkBalancerCallFactory(NWorkBalancerFactory workBalancerCallFactory) {
        this.workBalancerCallFactory = workBalancerCallFactory;
        return this;
    }

    @Override
    public NConcurrent sleep(NDuration durationMillis) throws NInterruptedException {
        return sleep(durationMillis == null ? 0 : durationMillis.getTimeAsMillis());
    }

    @Override
    public NConcurrent sleep(Duration durationMillis) throws NInterruptedException {
        return sleep(durationMillis == null ? 0 : durationMillis.toMillis());
    }

    @Override
    public NConcurrent sleep(long durationMillis) throws NInterruptedException {
        if (durationMillis > 0) {
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException e) {
                throw new NInterruptedException(e);
            }
        }
        return this;
    }

    @Override
    public NTaskSet taskSet() {
        return new NTaskSetImpl();
    }

    @Override
    public IntFunction<NDuration> retryFixedPeriod(NDuration period) {
        return retryFixedPeriods(period);
    }

    @Override
    public IntFunction<NDuration> retryFixedPeriods(NDuration... periods) {
        List<NDuration> all = new ArrayList<>();
        if (periods == null) {
            all.add(NDuration.ofMillis(0));
        } else {
            for (NDuration period : periods) {
                if (period != null) {
                    all.add(period);
                } else {
                    all.add(NDuration.ofMillis(0));
                }
            }
        }
        return new IntFunction<NDuration>() {
            @Override
            public NDuration apply(int i) {
                if (i < all.size()) {
                    return all.get(i);
                }
                return all.get(all.size() - 1);
            }
        };
    }

    @Override
    public IntFunction<NDuration> retryMultipliedPeriod(NDuration base, double multiplier) {
        if (base == null || base.isZero() || multiplier <= 0) {
            return retryFixedPeriod(NDuration.ofMillis(0));
        }
        return new IntFunction<NDuration>() {
            @Override
            public NDuration apply(int i) {
                return base.mul(multiplier * i);
            }
        };
    }
}
