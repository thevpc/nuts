package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;

import java.util.*;

public class NWorkBalancerFactoryImpl implements NWorkBalancerFactory {
    private NWorkBalancerStore store;
    private NBeanContainer beanContainer;
    private Map<String, NWorkBalancerStrategy> strategies = new HashMap<>();

    public NWorkBalancerFactoryImpl(NWorkBalancerStore store, NBeanContainer beanContainer, Map<String, NWorkBalancerStrategy> strategies) {
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
    public NWorkBalancerFactory withStore(NWorkBalancerStore store) {
        if (store == this.store) {
            return this;
        }
        return new NWorkBalancerFactoryImpl(store, beanContainer, strategies);
    }

    @Override
    public NWorkBalancerFactory withBeanContainer(NBeanContainer beanContainer) {
        if (beanContainer == this.beanContainer) {
            return this;
        }
        return new NWorkBalancerFactoryImpl(store, beanContainer, strategies);
    }

    @Override
    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public NWorkBalancerStore getStore() {
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

    public NWorkBalancerFactory defineStrategy(String name, NWorkBalancerStrategy definition) {
        Map<String, NWorkBalancerStrategy> strategies2 = new HashMap<>(strategies);
        if (definition == null) {
            strategies2.remove(name);
        } else {
            strategies2.put(name, definition);
        }
        return new NWorkBalancerFactoryImpl(store, beanContainer, strategies2);
    }

    @Override
    public <T> NWorkBalancerBuilder<T> ofBuilder(String id) {
        return new NWorkBalancerBuilderImpl(id, this);
    }


}
