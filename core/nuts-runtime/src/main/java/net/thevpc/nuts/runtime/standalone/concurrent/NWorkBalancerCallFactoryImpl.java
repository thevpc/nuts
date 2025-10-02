package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NWorkBalancerCallFactoryImpl implements NWorkBalancerCallFactory {
    private NWorkBalancerCallStore store;
    private NBeanContainer beanContainer;
    private Map<String, NWorkBalancerStrategy> strategies = new HashMap<>();

    public NWorkBalancerCallFactoryImpl(NWorkBalancerCallStore store, NBeanContainer beanContainer, Map<String, NWorkBalancerStrategy> strategies) {
        this.store = store;
        this.beanContainer = beanContainer;
        if (strategies != null) {
            for (Map.Entry<String, NWorkBalancerStrategy> e : strategies.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    strategies.put(e.getKey(), e.getValue());
                }
            }
            this.strategies.putAll(strategies);
        }
    }


    @Override
    public NWorkBalancerCallFactory withStore(NWorkBalancerCallStore store) {
        if (store == this.store) {
            return this;
        }
        return new NWorkBalancerCallFactoryImpl(store, beanContainer, strategies);
    }

    @Override
    public NWorkBalancerCallFactory withBeanContainer(NBeanContainer beanContainer) {
        if (beanContainer == this.beanContainer) {
            return this;
        }
        return new NWorkBalancerCallFactoryImpl(store, beanContainer, strategies);
    }

    @Override
    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public NWorkBalancerCallStore getStore() {
        return store;
    }

    public NWorkBalancerStrategy createStrategy(String strategyId) {
        NWorkBalancerStrategy d = strategies.get(strategyId);
        if (d != null) {
            return d;
        }
        switch (NWorkBalancerDefaultStrategy.parse(strategyId).orElse(NWorkBalancerDefaultStrategy.ROUND_ROBIN)) {
            case ROUND_ROBIN:
                return new RoundRobin();
            case LEAST_LOAD:
                return new LeastLoad();
            case POWER_OF_TWO_CHOICES:
                return new PowerOfTwoLoad();
        }
        return new RoundRobin();
    }

    public NWorkBalancerCallFactory defineStrategy(String name, NWorkBalancerStrategy definition) {
        Map<String, NWorkBalancerStrategy> strategies2 = new HashMap<>(strategies);
        if (definition == null) {
            strategies2.remove(name);
        } else {
            strategies2.put(name, definition);
        }
        return new NWorkBalancerCallFactoryImpl(store, beanContainer, strategies2);
    }

    @Override
    public <T> NWorkBalancerCallBuilder<T> ofBuilder(String id) {
        return new NWorkBalancerCallBuilderImpl(id, this);
    }

    private static class LeastLoad implements NWorkBalancerStrategy {
        private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

        @Override
        public void onStartCall(NWorkBalancerStrategyEvent event) {

        }

        @Override
        public void onEndCall(NWorkBalancerStrategyEvent event) {

        }

        @Override
        public String selectWorker(NWorkBalancerStrategyContext context) {
            List<String> workers = context.workers();
            return workers.stream()
                    .min(Comparator.comparingDouble(workerId -> resolveWorkerLoad(workerId, context)))
                    .orElse(workers.get(0));
        }

        private double resolveWorkerLoad(String workerId, NWorkBalancerStrategyContext context) {
            NWorkBalancerWorkerLoad w = context.getWorkerLoad(workerId).orNull();
            if (w != null) {
                Float f = w.hostLoadFactor().orElse(0.0f);
                if (f.isNaN()) {
                    return 0.0;
                }
                return f;
            }
            return 0.0;
        }
    }


    private static class PowerOfTwoLoad implements NWorkBalancerStrategy {
        private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

        @Override
        public void onStartCall(NWorkBalancerStrategyEvent event) {

        }

        @Override
        public void onEndCall(NWorkBalancerStrategyEvent event) {

        }

        @Override
        public String selectWorker(NWorkBalancerStrategyContext context) {
            List<String> workers = context.workers();
            // POWER_OF_TWO_CHOICES: pick 2 random workers and choose the least loaded
            Random r = new Random();
            String w1 = workers.get(r.nextInt(workers.size()));
            String w2 = workers.get(r.nextInt(workers.size()));
            return resolveWorkerLoad(w1, context) <= resolveWorkerLoad(w2, context) ? w1 : w2;
        }

        private double resolveWorkerLoad(String workerId, NWorkBalancerStrategyContext context) {
            NWorkBalancerWorkerLoad w = context.getWorkerLoad(workerId).orNull();
            if (w != null) {
                Float f = w.hostLoadFactor().orElse(0.0f);
                if (f.isNaN()) {
                    return 0.0;
                }
                return f;
            }
            return 0.0;
        }
    }

    private static class RoundRobin implements NWorkBalancerStrategy {
        private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

        @Override
        public void onStartCall(NWorkBalancerStrategyEvent event) {

        }

        @Override
        public void onEndCall(NWorkBalancerStrategyEvent event) {

        }

        @Override
        public String selectWorker(NWorkBalancerStrategyContext context) {
            List<String> workers = context.workers();
            int index = roundRobinCounter.getAndIncrement() % workers.size();
            return workers.get(index);
        }
    }
}
