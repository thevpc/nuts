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
    public NTaskSet taskSet() {
        return new NTaskSetImpl();
    }

    @Override
    public void lockFactory(NLockFactory lockFactory) {
        NWorkspaceExt.of().getModel().lockFactory = lockFactory;
    }

    @Override
    public NLockFactory memoryLockFactory() {
        return NWorkspaceExt.of().getModel().memoryLockFactory;
    }

    @Override
    public NLockFactory defaultLockFactory() {
        return memoryLockFactory();
    }

    @Override
    public NLockFactory lockFactory() {
        return NWorkspaceExt.of().getModel().lockFactory == null ? defaultLockFactory() : NWorkspaceExt.of().getModel().lockFactory;
    }

    @Override
    public void cachedValueFactory(NCachedValueFactory cachedValueFactory) {
        NWorkspaceExt.of().getModel().cachedValueFactory = cachedValueFactory;
    }

    @Override
    public NCachedValueFactory memoryCachedValueFactory() {
        return NWorkspaceExt.of().getModel().memoryCachedValueFactory;
    }

    @Override
    public NCachedValueFactory defaultCachedValueFactory() {
        return memoryCachedValueFactory();
    }

    @Override
    public NCachedValueFactory cachedValueFactory() {
        return NWorkspaceExt.of().getModel().cachedValueFactory == null ? defaultCachedValueFactory() : NWorkspaceExt.of().getModel().cachedValueFactory;
    }


    @Override
    public void retryCallFactory(NRetryCallFactory retryCallFactory) {
        NWorkspaceExt.of().getModel().retryValueFactory = retryCallFactory;
    }

    @Override
    public NRetryCallFactory memoryRetryCallFactory() {
        return NWorkspaceExt.of().getModel().memoryRetryValueFactory;
    }

    @Override
    public NRetryCallFactory defaultRetryCallFactory() {
        return memoryRetryCallFactory();
    }

    @Override
    public NRetryCallFactory retryCallFactory() {
        return NWorkspaceExt.of().getModel().retryValueFactory == null ? defaultRetryCallFactory() : NWorkspaceExt.of().getModel().retryValueFactory;
    }


    @Override
    public void onceValueFactory(NOnceValueFactory onceValueFactory) {
        NWorkspaceExt.of().getModel().onceValueFactory = onceValueFactory;
    }

    @Override
    public NOnceValueFactory memoryOnceValueFactory() {
        return NWorkspaceExt.of().getModel().memoryOnceValueFactory;
    }

    @Override
    public NOnceValueFactory defaultOnceValueFactory() {
        return memoryOnceValueFactory();
    }

    @Override
    public NOnceValueFactory onceValueFactory() {
        return NWorkspaceExt.of().getModel().onceValueFactory == null ? defaultOnceValueFactory() : NWorkspaceExt.of().getModel().onceValueFactory;
    }


    @Override
    public NRateLimitValueFactory defaultRateLimitValueFactory() {
        return NWorkspaceExt.of().getModel().memoryRateLimitValueFactory;
    }

    @Override
    public NSagaCallableFactory defaultSagaFactory() {
        return NWorkspaceExt.of().getModel().memorySagaFactory;
    }

    @Override
    public NSagaCallableFactory memorySagaFactory() {
        return NWorkspaceExt.of().getModel().memorySagaFactory;
    }

    @Override
    public NRateLimitValueFactory memoryRateLimitValueFactory() {
        return NWorkspaceExt.of().getModel().memoryRateLimitValueFactory;
    }

    @Override
    public void rateLimitValueFactory(NRateLimitValueFactory factory) {
        NWorkspaceExt.of().getModel().rateLimitValueFactory = factory;
    }

    @Override
    public NRateLimitValueFactory rateLimitValueFactory() {
        return NWorkspaceExt.of().getModel().rateLimitValueFactory == null ? defaultRateLimitValueFactory() : NWorkspaceExt.of().getModel().rateLimitValueFactory;
    }


    @Override
    public NSagaCallableFactory sagaFactory() {
        return NWorkspaceExt.of().getModel().sagaFactory == null ? defaultSagaFactory() : NWorkspaceExt.of().getModel().sagaFactory;
    }

    @Override
    public void sagaFactory(NSagaCallableFactory factory) {
        NWorkspaceExt.of().getModel().sagaFactory = factory;
    }


    @Override
    public <T> NCachedValue<T> cachedValue(Supplier<T> supplier) {
        return cachedValueFactory().of(supplier);
    }

    @Override
    public <T> NOnceValue<T> onceValue(Supplier<T> supplier) {
        return onceValueFactory().of(supplier);
    }

    @Override
    public <T> NCachedValue<T> cachedValue(String id, Supplier<T> supplier) {
        return cachedValueFactory().of(id, supplier);
    }

    @Override
    public <T> NOnceValue<T> onceValue(String id, Supplier<T> supplier) {
        return onceValueFactory().of(id, supplier);
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
    public void circuitBreakerCallFactory(NCircuitBreakerCallFactory circuitBreakerCallFactory) {
        NWorkspaceExt.of().getModel().circuitBreakerCallFactory = circuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory memoryCircuitBreakerCallFactory() {
        return NWorkspaceExt.of().getModel().memoryCircuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory defaultCircuitBreakerCallFactory() {
        return NWorkspaceExt.of().getModel().memoryCircuitBreakerCallFactory;
    }

    @Override
    public NCircuitBreakerCallFactory circuitBreakerCallFactory() {
        return NWorkspaceExt.of().getModel().circuitBreakerCallFactory == null ? defaultCircuitBreakerCallFactory() : NWorkspaceExt.of().getModel().circuitBreakerCallFactory;
    }

    @Override
    public NBulkheadCallFactory defaultBulkheadCallFactory() {
        return NWorkspaceExt.of().getModel().memoryBulkheadCallFactory;
    }

    @Override
    public NBulkheadCallFactory memoryBulkheadCallFactory() {
        return NWorkspaceExt.of().getModel().memoryBulkheadCallFactory;
    }

    @Override
    public NBulkheadCallFactory bulkheadCallFactory() {
        return NWorkspaceExt.of().getModel().bulkheadCallFactory == null ? defaultBulkheadCallFactory() : NWorkspaceExt.of().getModel().bulkheadCallFactory;
    }

    @Override
    public void bulkheadCallFactory(NBulkheadCallFactory bulkheadCallFactory) {
        NWorkspaceExt.of().getModel().bulkheadCallFactory = bulkheadCallFactory;
    }

    @Override
    public NSagaCallableBuilder sagaCallBuilder() {
        return sagaFactory().ofBuilder();
    }

    @Override
    public NSagaCallableBuilder sagaCallBuilder(String id) {
        return sagaFactory().ofBuilder(id);
    }

    @Override
    public NWorkBalancerFactory defaultWorkBalancerFactory() {
        return NWorkspaceExt.of().getModel().memoryWorkBalancerCallFactory;
    }

    @Override
    public NWorkBalancerFactory memoryWorkBalancerFactory() {
        return NWorkspaceExt.of().getModel().memoryWorkBalancerCallFactory;
    }

    @Override
    public NWorkBalancerFactory workBalancerFactory() {
        return NWorkspaceExt.of().getModel().workBalancerCallFactory == null ? defaultWorkBalancerFactory() : NWorkspaceExt.of().getModel().workBalancerCallFactory;
    }

    @Override
    public void workBalancerCallFactory(NWorkBalancerFactory workBalancerCallFactory) {
        NWorkspaceExt.of().getModel().workBalancerCallFactory = workBalancerCallFactory;
    }

    public NRetryPeriodFunction retryExponentialPeriod(NDuration base, double multiplier) {
        if (base == null || base.isZero() || multiplier <= 0) {
            return retryFixedPeriods(NDuration.ofMillis(0));
        }
        return new NRetryPeriodFunction() {
            @Override
            public NDuration apply(int iteration) {
                return base.mul(Math.pow(multiplier, iteration));
            }
        };
    }

    @Override
    public NRetryPeriodFunction retryFixedPeriods(NDuration... periods) {
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
    public NRetryPeriodFunction retryMultipliedPeriod(NDuration base, double multiplier) {
        if (base == null || base.isZero() || multiplier <= 0) {
            return retryFixedPeriods(NDuration.ofMillis(0));
        }
        return new NRetryPeriodFunction() {
            @Override
            public NDuration apply(int i) {
                return base.mul(multiplier * i);
            }
        };
    }


    @Override
    public ExecutorService executorService() {
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
