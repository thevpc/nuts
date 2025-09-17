package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NMsg;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

class NRateLimitedValueImpl implements NRateLimitedValue {
    private NRateLimitedValueFactoryImpl factory;
    private String id;

    public NRateLimitedValueImpl(NRateLimitedValueModel data, NRateLimitedValueFactoryImpl factory) {
        this.id =data.getId();
        this.factory = factory;
    }

    public NRateLimitedValueModel model() {
        return factory.load(id);
    }

    public synchronized NRateLimitedValueResult take() {
        return take(1);
    }

    @Override
    public synchronized NRateLimitedValueResult take(int count) {
        Instant lastAccess = Instant.now();
        NRateLimitedValueModel model = model();
        NRateLimitStrategyModel[] constraints = model.getConstraints();
        NRateLimitStrategy[] constraints2 = new NRateLimitStrategy[constraints.length];

        for (int i = 0; i < constraints.length; i++) {
            NRateLimitStrategyModel cc = constraints[i];
            NRateLimitStrategy c=factory.createStrategy(cc);
            constraints2[i] = c;
            if (!c.tryConsume(count)) {
                return new NRateLimitedValueResultImpl(false, NMsg.ofC("failed to allocate %s %s", id, c));
            }
        }

        factory.save(new NRateLimitedValueModel(
                id,lastAccess,
                Arrays.stream(constraints2).map(NRateLimitStrategy::toModel).toArray(NRateLimitStrategyModel[]::new)
        ));
        return new NRateLimitedValueResultImpl(true, null);
    }

    @Override
    public NRateLimitedValueResult take(Runnable runnable) {
        return take(1, runnable);
    }

    @Override
    public NRateLimitedValueResult take(int count, Runnable runnable) {
        return take(count).onSuccess(runnable);
    }

    Instant lastAccess() {
        return factory.load(id).getLastAccess();
    }

//    public void updateModel(NRateLimitedValueModel newModel) {
//        this.uuid = newModel.getId();
//        this.lastAccess = newModel.getLastAccess();
//        this.constraints = Arrays.stream(newModel.getConstraints()).map(NRateLimitedValueConstraintRuntime::new).toArray(NRateLimitedValueConstraintRuntime[]::new);
//    }

    @Override
    public NRateLimitedValueResult claim(int count) {
        return claim(count, (Duration) null);
    }

    @Override
    public NRateLimitedValueResult claim(int count, Duration timeout) {
        long start = System.currentTimeMillis();
        long maxWaitTimeMillis = (timeout != null) ? timeout.toMillis() : -1;
        long deadline = (timeout != null) ? start + timeout.toMillis() : Long.MAX_VALUE;
        while (true) {
            NRateLimitedValueResult take = null;
            NRateLimitedValueModel model = model();
            Instant lastAccess = Instant.now();
            long shouldWaitForMs = 0;
            NRateLimitStrategyModel[] constraints = model.getConstraints();
            NRateLimitStrategy[] constraints2 = new NRateLimitStrategy[constraints.length];
            for (int i = 0; i < constraints.length; i++) {
                NRateLimitStrategyModel cc = constraints[i];
                NRateLimitStrategy c=factory.createStrategy(cc);
                constraints2[i] = c;
                if (!c.tryConsume(count)) {
                    if (take == null) {
                        take = new NRateLimitedValueResultImpl(false, NMsg.ofC("failed to allocate %s %s", id, c));
                    }
                    shouldWaitForMs = Math.max(shouldWaitForMs, c.nextAvailableMillis(count));
                }
            }
            if (take == null) {
                factory.save(new NRateLimitedValueModel(
                        id,lastAccess,
                        Arrays.stream(constraints2).map(NRateLimitStrategy::toModel).toArray(NRateLimitStrategyModel[]::new)
                ));
                take = new NRateLimitedValueResultImpl(true, null);
            }else{
                factory.save(new NRateLimitedValueModel(
                        id,lastAccess,
                        Arrays.stream(constraints2).map(NRateLimitStrategy::toModel).toArray(NRateLimitStrategyModel[]::new)
                ));
            }
            if (take.success()) {
                return take;
            }
            if (shouldWaitForMs <= 0) {
                shouldWaitForMs = 10;
            }
            if (maxWaitTimeMillis > 0) {
                long now = System.currentTimeMillis();
                if (now >= deadline) {
                    return new NRateLimitedValueResultImpl(false, NMsg.ofC("failed to claim %s after %s", id, maxWaitTimeMillis));
                }
            }
            try {
                Thread.sleep(shouldWaitForMs);
            } catch (InterruptedException e) {
                return new NRateLimitedValueResultImpl(false, NMsg.ofC("Interrupted while waiting for token %s after %s", id, maxWaitTimeMillis));
            }
        }
    }

    @Override
    public NRateLimitedValueResult claim(Runnable runnable) {
        return claim(1).onSuccess(runnable);
    }

    @Override
    public NRateLimitedValueResult claim(int count, Runnable runnable) {
        return claim(count).onSuccess(runnable);
    }

    @Override
    public NRateLimitedValueResult claim(Duration timeout, Runnable runnable) {
        return claim(1, timeout).onSuccess(runnable);
    }

    @Override
    public NRateLimitedValueResult claim(int count, Duration timeout, Runnable runnable) {
        return claim(count, timeout).onSuccess(runnable);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRateLimitedValueImpl that = (NRateLimitedValueImpl) o;
        return Objects.equals(factory, that.factory) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factory, id);
    }


}
