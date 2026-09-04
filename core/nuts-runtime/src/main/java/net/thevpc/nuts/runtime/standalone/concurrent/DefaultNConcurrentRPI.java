package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.internal.rpi.NConcurrentRPI;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.time.NDuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class DefaultNConcurrentRPI implements NConcurrentRPI {

    @Override
    public NTaskSet createTaskSet() {
        return new NTaskSetImpl();
    }

    @Override
    public void setCurrentLockFactory(NLockFactory lockFactory) {
        NWorkspaceExt.of().getModel().lockFactory = lockFactory;
    }

    @Override
    public NLockFactory getMemoryLockFactory() {
        return NWorkspaceExt.of().getModel().memoryLockFactory;
    }

    @Override
    public NLockFactory getDefaultLockFactory() {
        return getMemoryLockFactory();
    }

    @Override
    public NLockFactory getCurrentLockFactory() {
        return NWorkspaceExt.of().getModel().lockFactory == null ? getDefaultLockFactory() : NWorkspaceExt.of().getModel().lockFactory;
    }

    @Override
    public void setCachedValueFactory(NCachedValueFactory cachedValueFactory) {
        NWorkspaceExt.of().getModel().cachedValueFactory = cachedValueFactory;
    }

    @Override
    public NCachedValueFactory getMemoryCachedValueFactory() {
        return NWorkspaceExt.of().getModel().memoryCachedValueFactory;
    }

    @Override
    public NCachedValueFactory getDefaultCachedValueFactory() {
        return getMemoryCachedValueFactory();
    }

    @Override
    public NCachedValueFactory getCachedValueFactory() {
        return NWorkspaceExt.of().getModel().cachedValueFactory == null ? getDefaultCachedValueFactory() : NWorkspaceExt.of().getModel().cachedValueFactory;
    }


    @Override
    public void setRetryCallFactory(NRetryCallFactory retryCallFactory) {
        NWorkspaceExt.of().getModel().retryValueFactory = retryCallFactory;
    }

    @Override
    public NRetryCallFactory getMemoryRetryCallFactory() {
        return NWorkspaceExt.of().getModel().memoryRetryValueFactory;
    }

    @Override
    public NRetryCallFactory getDefaultRetryCallFactory() {
        return getMemoryRetryCallFactory();
    }

    @Override
    public NRetryCallFactory getRetryCallFactory() {
        return NWorkspaceExt.of().getModel().retryValueFactory == null ? getDefaultRetryCallFactory() : NWorkspaceExt.of().getModel().retryValueFactory;
    }


    @Override
    public void setOnceValueFactory(NOnceValueFactory onceValueFactory) {
        NWorkspaceExt.of().getModel().onceValueFactory = onceValueFactory;
    }

    @Override
    public NOnceValueFactory getMemoryOnceValueFactory() {
        return NWorkspaceExt.of().getModel().memoryOnceValueFactory;
    }

    @Override
    public NOnceValueFactory getDefaultOnceValueFactory() {
        return getMemoryOnceValueFactory();
    }

    @Override
    public NOnceValueFactory getOnceValueFactory() {
        return NWorkspaceExt.of().getModel().onceValueFactory == null ? getDefaultOnceValueFactory() : NWorkspaceExt.of().getModel().onceValueFactory;
    }


    @Override
    public NRateLimitValueFactory getDefaultRateLimitValueFactory() {
        return NWorkspaceExt.of().getModel().memoryRateLimitValueFactory;
    }

    @Override
    public NSagaCallableFactory getDefaultSagaFactory() {
        return NWorkspaceExt.of().getModel().memorySagaFactory;
    }

    @Override
    public NSagaCallableFactory memorySagaFactory() {
        return NWorkspaceExt.of().getModel().memorySagaFactory;
    }

    @Override
    public NRateLimitValueFactory getMemoryRateLimitValueFactory() {
        return NWorkspaceExt.of().getModel().memoryRateLimitValueFactory;
    }

    @Override
    public void setRateLimitValueFactory(NRateLimitValueFactory factory) {
        NWorkspaceExt.of().getModel().rateLimitValueFactory = factory;
    }

    @Override
    public NRateLimitValueFactory getRateLimitValueFactory() {
        return NWorkspaceExt.of().getModel().rateLimitValueFactory == null ? getDefaultRateLimitValueFactory() : NWorkspaceExt.of().getModel().rateLimitValueFactory;
    }


    @Override
    public NSagaCallableFactory getSagaFactory() {
        return NWorkspaceExt.of().getModel().sagaFactory == null ? getDefaultSagaFactory() : NWorkspaceExt.of().getModel().sagaFactory;
    }

    @Override
    public void setSagaFactory(NSagaCallableFactory factory) {
        NWorkspaceExt.of().getModel().sagaFactory = factory;
    }


    @Override
    public <T> NCachedValue<T> cachedValue(Supplier<T> supplier) {
        return getCachedValueFactory().of(supplier);
    }

    @Override
    public <T> NOnceValue<T> createOnceValue(Supplier<T> supplier) {
        return getOnceValueFactory().of(supplier);
    }

    @Override
    public <T> NCachedValue<T> cachedValue(String id, Supplier<T> supplier) {
        return getCachedValueFactory().of(id, supplier);
    }

    @Override
    public <T> NOnceValue<T> createOnceValue(String id, Supplier<T> supplier) {
        return getOnceValueFactory().of(id, supplier);
    }

    @Override
    public <T> NRetryCall<T> createRetryCall(String id, NCallable<T> callable) {
        return getRetryCallFactory().of(id, callable);
    }

    @Override
    public <T> NRetryCall<T> createRetryCall(NCallable<T> callable) {
        return getRetryCallFactory().of(callable);
    }

    @Override
    public <T> NCircuitBreakerCall<T> createCircuitBreakerCall(NCallable<T> callable) {
        return getCircuitBreakerCallFactory().of(callable);
    }

    @Override
    public <T> NCircuitBreakerCall<T> createCircuitBreakerCall(String id, NCallable<T> callable) {
        return getCircuitBreakerCallFactory().of(id, callable);
    }

    @Override
    public void setCircuitBreakerCallFactory(NCircuitBreakerCallFactory circuitBreakerCallFactory) {
        NWorkspaceExt.of().getModel().circuitBreakerCallFactory = circuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory getMemoryCircuitBreakerCallFactory() {
        return NWorkspaceExt.of().getModel().memoryCircuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory getDefaultCircuitBreakerCallFactory() {
        return NWorkspaceExt.of().getModel().memoryCircuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory getCircuitBreakerCallFactory() {
        return NWorkspaceExt.of().getModel().circuitBreakerCallFactory == null ? getDefaultCircuitBreakerCallFactory() : NWorkspaceExt.of().getModel().circuitBreakerCallFactory;
    }

    @Override
    public NBulkheadCallFactory getDefaultBulkheadCallFactory() {
        return NWorkspaceExt.of().getModel().memoryBulkheadCallFactory;
    }

    @Override
    public NBulkheadCallFactory getMemoryBulkheadCallFactory() {
        return NWorkspaceExt.of().getModel().memoryBulkheadCallFactory;
    }

    @Override
    public NBulkheadCallFactory getBulkheadCallFactory() {
        return NWorkspaceExt.of().getModel().bulkheadCallFactory == null ? getDefaultBulkheadCallFactory() : NWorkspaceExt.of().getModel().bulkheadCallFactory;
    }

    @Override
    public void setBulkheadCallFactory(NBulkheadCallFactory bulkheadCallFactory) {
        NWorkspaceExt.of().getModel().bulkheadCallFactory = bulkheadCallFactory;
    }

    @Override
    public NSagaCallableBuilder sagaCallBuilder() {
        return getSagaFactory().ofBuilder();
    }

    @Override
    public NSagaCallableBuilder sagaCallBuilder(String id) {
        return getSagaFactory().ofBuilder(id);
    }

    @Override
    public NWorkBalancerFactory getDefaultWorkBalancerFactory() {
        return NWorkspaceExt.of().getModel().memoryWorkBalancerCallFactory;
    }

    @Override
    public NWorkBalancerFactory getMemoryWorkBalancerFactory() {
        return NWorkspaceExt.of().getModel().memoryWorkBalancerCallFactory;
    }

    @Override
    public NWorkBalancerFactory getWorkBalancerFactory() {
        return NWorkspaceExt.of().getModel().workBalancerCallFactory == null ? getDefaultWorkBalancerFactory() : NWorkspaceExt.of().getModel().workBalancerCallFactory;
    }

    @Override
    public void setWorkBalancerCallFactory(NWorkBalancerFactory workBalancerCallFactory) {
        NWorkspaceExt.of().getModel().workBalancerCallFactory = workBalancerCallFactory;
    }

    public NRetryPeriodFunction createRetryExponentialPeriod(NDuration base, double multiplier) {
        if (base == null || base.isZero() || multiplier <= 0) {
            return createRetryFixedPeriods(NDuration.ofMillis(0));
        }
        return new NRetryPeriodFunction() {
            @Override
            public NDuration apply(int iteration) {
                return base.mul(Math.pow(multiplier, iteration));
            }
        };
    }

    @Override
    public NRetryPeriodFunction createRetryFixedPeriods(NDuration... periods) {
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
        return new NRetryPeriodFunction() {
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
    public NRetryPeriodFunction createRetryMultipliedPeriod(NDuration base, double multiplier) {
        if (base == null || base.isZero() || multiplier <= 0) {
            return createRetryFixedPeriods(NDuration.ofMillis(0));
        }
        return new NRetryPeriodFunction() {
            @Override
            public NDuration apply(int i) {
                return base.mul(multiplier * i);
            }
        };
    }


    @Override
    public ExecutorService getExecutorService() {
        return NWorkspaceExt.of().getModel().configModel.executorService();
    }


    @Override
    public void sleep(NDuration durationMillis) throws NInterruptedException {
        sleep(durationMillis == null ? 0 : durationMillis.toMillis());
    }

    @Override
    public void sleep(Duration durationMillis) throws NInterruptedException {
        sleep(durationMillis == null ? 0 : durationMillis.toMillis());
    }

    @Override
    public void sleep(long durationMillis) throws NInterruptedException {
        if (durationMillis > 0) {
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException e) {
                throw new NInterruptedException(e);
            }
        }
    }

}
