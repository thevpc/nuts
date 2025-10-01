package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NRateLimitValueFactoryImpl implements NRateLimitValueFactory {
    private NRateLimitValueStore store;
    private NBeanContainer beanContainer;
    private Map<String, Function<NRateLimitRuleModel, NRateLimitRule>> strategies = new HashMap<>();

    public NRateLimitValueFactoryImpl(NRateLimitValueStore store, NBeanContainer beanContainer, Map<String, Function<NRateLimitRuleModel, NRateLimitRule>> strategies) {
        this.store = store;
        this.beanContainer = beanContainer;
        if (strategies != null) {
            for (Map.Entry<String, Function<NRateLimitRuleModel, NRateLimitRule>> e : strategies.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    strategies.put(e.getKey(), e.getValue());
                }
            }
            this.strategies.putAll(strategies);
        }
    }


    @Override
    public NRateLimitValueFactory withStore(NRateLimitValueStore store) {
        if (store == this.store) {
            return this;
        }
        return new NRateLimitValueFactoryImpl(store, beanContainer, strategies);
    }

    @Override
    public NRateLimitValueFactory withBeanContainer(NBeanContainer beanContainer) {
        if (beanContainer == this.beanContainer) {
            return this;
        }
        return new NRateLimitValueFactoryImpl(store, beanContainer, strategies);
    }

    @Override
    public NBeanContainer getBeanContainer() {
        return beanContainer;
    }

    @Override
    public NRateLimitValueStore getStore() {
        return store;
    }

    public NRateLimitRule createRule(NRateLimitRuleModel model) {
        String strategyId = model.getId();
        Function<NRateLimitRuleModel, NRateLimitRule> d = strategies.get(strategyId);
        if (d != null) {
            NRateLimitRule s = d.apply(model);
            if (s != null) {
                return s;
            }
        }
        switch (NNameFormat.LOWER_KEBAB_CASE.format(NStringUtils.trim(strategyId))) {
            case "bucket": {
                return new NRateLimitBucketRule(model);
            }
            case "sliding-window": {
                return new NRateLimitSlidingWindowRule(model);
            }
            case "lecky-bucket": {
                return new NRateLimitLeakyBucketRule(model);
            }
            case "fixed-window": {
                return new NRateLimitFixedWindowRule(model);
            }
            default: {
                return new NRateLimitBucketRule(model);
            }
        }
    }

    public NRateLimitValueFactory defineStrategy(String name, Function<NRateLimitRuleModel, NRateLimitRule> definition) {
        Map<String, Function<NRateLimitRuleModel, NRateLimitRule>> strategies2 = new HashMap<>(strategies);
        if (definition == null) {
            strategies2.remove(name);
        } else {
            strategies2.put(name, definition);
        }
        return new NRateLimitValueFactoryImpl(store, beanContainer, strategies2);
    }

    @Override
    public NRateLimitValueBuilder valueBuilder(String id) {
        return new NRateLimitValueBuilderImpl(id, this);
    }

    public NRateLimitValueModel load(String id) {
        return store.load(id);
    }

    public void save(NRateLimitValueModel rateLimitValue) {
        store.save(rateLimitValue);
    }
}
