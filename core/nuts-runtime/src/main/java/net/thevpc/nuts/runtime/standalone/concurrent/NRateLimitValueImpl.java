package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

class NRateLimitValueImpl implements NRateLimitValue {
    private NRateLimitValueFactoryImpl factory;
    private String id;

    public NRateLimitValueImpl(NRateLimitValueModel data, NRateLimitValueFactoryImpl factory) {
        this.id = data.getId();
        this.factory = factory;
    }

    public NRateLimitValueModel model() {
        return factory.load(id);
    }

    public synchronized NRateLimitValueResult take() {
        return take(1);
    }

    @Override
    public synchronized NRateLimitValueResult take(int count) {
        Instant lastAccess = Instant.now();
        NRateLimitValueModel model = model();
        NRateLimitRuleModel[] ruleModels = model.getRules();
        NRateLimitRule[] rules = new NRateLimitRule[ruleModels.length];

        for (int i = 0; i < ruleModels.length; i++) {
            NRateLimitRuleModel ruleModel = ruleModels[i];
            NRateLimitRule rule = factory.createRule(ruleModel);
            rules[i] = rule;
            if (!rule.tryConsume(count)) {
                return new NRateLimitValueResultImpl(false, null,NMsg.ofC("rate limit exceeded (%s) for %s", ruleModel.getId(), id));
            }
        }

        factory.save(new NRateLimitValueModel(
                id, lastAccess == null ? 0 : lastAccess.getEpochSecond(),
                Arrays.stream(rules).map(NRateLimitRule::toModel).toArray(NRateLimitRuleModel[]::new)
        ));
        return new NRateLimitValueResultImpl(true, null,null);
    }

    @Override
    public NRateLimitValueResult takeAndRun(Runnable runnable) {
        return takeAndRun(1, runnable);
    }

    @Override
    public NRateLimitValueResult takeAndRun(int count, Runnable runnable) {
        return take(count).onSuccess(runnable);
    }

    @Override
    public <T> NRateLimitValueResult takeAndCall(int count, NCallable<T> callable) {
        return take(count).onSuccessCall(callable);
    }

    @Override
    public <T> NRateLimitValueResult takeAndCall(NCallable<T> callable) {
        return take().onSuccessCall(callable);
    }



    @Override
    public NRateLimitValueResult claim(int count) {
        return claim(count, (Duration) null);
    }

    @Override
    public NRateLimitValueResult claim(int count, Duration timeout) {
        long start = System.currentTimeMillis();
        long maxWaitTimeMillis = (timeout != null) ? timeout.toMillis() : -1;
        long deadline = (timeout != null) ? start + timeout.toMillis() : Long.MAX_VALUE;
        while (true) {
            NRateLimitValueResult take = null;
            NRateLimitValueModel model = model();
            Instant lastAccess = Instant.now();
            long shouldWaitForMs = 0;
            NRateLimitRuleModel[] ruleModels = model.getRules();
            NRateLimitRule[] rules = new NRateLimitRule[ruleModels.length];
            NRateLimitRuleModel faultyRuleModel = null;
            for (int i = 0; i < ruleModels.length; i++) {
                NRateLimitRuleModel ruleModel = ruleModels[i];
                NRateLimitRule rule = factory.createRule(ruleModel);
                rules[i] = rule;
                if (!rule.tryConsume(count)) {
                    if (take == null) {
                        faultyRuleModel = ruleModel;
                        take = new NRateLimitValueResultImpl(false, null,NMsg.ofC("rate limit exceeded (%s) for %s", ruleModel.getId(), id));
                    }
                    shouldWaitForMs = Math.max(shouldWaitForMs, rule.nextAvailableMillis(count));
                }
            }
            factory.save(new NRateLimitValueModel(
                    id, lastAccess == null ? 0 : lastAccess.getEpochSecond(),
                    Arrays.stream(rules).map(NRateLimitRule::toModel).toArray(NRateLimitRuleModel[]::new)
            ));
            if (take == null) {
                take = new NRateLimitValueResultImpl(true, null,null);
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
                    if (faultyRuleModel != null) {
                        return new NRateLimitValueResultImpl(false, null,NMsg.ofC("rate limit exceeded (%s) for %s after %s ms", faultyRuleModel.getId(), id, maxWaitTimeMillis));
                    } else {
                        //should never happen
                        return new NRateLimitValueResultImpl(false, null,NMsg.ofC("rate limit exceeded for %s after %s ms", id, maxWaitTimeMillis));
                    }
                }
            }
            try {
                Thread.sleep(shouldWaitForMs);
            } catch (InterruptedException e) {
                if (faultyRuleModel != null) {
                    return new NRateLimitValueResultImpl(false, null,NMsg.ofC("rate limit exceeded (%s) for %s after %s ms", faultyRuleModel.getId(), id, maxWaitTimeMillis));
                } else {
                    //should never happen
                    return new NRateLimitValueResultImpl(false, null,NMsg.ofC("rate limit exceeded for %s after %s ms", id, maxWaitTimeMillis));
                }
            }
        }
    }

    @Override
    public NRateLimitValueResult claimAndRun(Runnable runnable) {
        return claim(1).onSuccess(runnable);
    }

    @Override
    public <T> NRateLimitValueResult claimAndCall(int count, NCallable<T> callable) {
        return claim(count).onSuccessCall(callable);
    }

    @Override
    public <T> NRateLimitValueResult claimAndCall(Duration timeout, NCallable<T> callable) {
        return claim(1,timeout).onSuccessCall(callable);
    }

    @Override
    public <T> NRateLimitValueResult claimAndCall(int count, Duration timeout, NCallable<T> callable) {
        return claim(count,timeout).onSuccessCall(callable);
    }

    @Override
    public NRateLimitValueResult claimAndRun(int count, Runnable runnable) {
        return claim(count).onSuccess(runnable);
    }

    @Override
    public NRateLimitValueResult claimAndRun(Duration timeout, Runnable runnable) {
        return claim(1, timeout).onSuccess(runnable);
    }

    @Override
    public NRateLimitValueResult claimAndRun(int count, Duration timeout, Runnable runnable) {
        return claim(count, timeout).onSuccess(runnable);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRateLimitValueImpl that = (NRateLimitValueImpl) o;
        return Objects.equals(factory, that.factory) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factory, id);
    }


}
