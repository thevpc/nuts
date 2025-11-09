package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;

public class NRateLimitValueFactoryImpl implements NRateLimitValueFactory {
    private NRateLimitValueStore store;
    private Map<String, NRateLimitStrategy> strategies = new HashMap<>();

    public NRateLimitValueFactoryImpl(NRateLimitValueStore store, Map<String, NRateLimitStrategy> strategies) {
        this.store = store;
        if (strategies != null) {
            for (Map.Entry<String, NRateLimitStrategy> e : strategies.entrySet()) {
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
        return new NRateLimitValueFactoryImpl(store, strategies);
    }

    @Override
    public NRateLimitValueStore getStore() {
        return store;
    }

    public NRateLimitRule createRule(NRateLimitRuleModel model) {
        String strategyId = model.getId();
        NRateLimitStrategy d = strategies.get(strategyId);
        if (d != null) {
            NRateLimitRule s = d.fromModel(model);
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

    public NRateLimitValueFactory defineStrategy(String name, NRateLimitStrategy definition) {
        Map<String, NRateLimitStrategy> strategies2 = new HashMap<>(strategies);
        if (definition == null) {
            strategies2.remove(name);
        } else {
            strategies2.put(name, definition);
        }
        return new NRateLimitValueFactoryImpl(store, strategies2);
    }

    @Override
    public NRateLimitValueBuilder ofBuilder(String id) {
        return new NRateLimitValueBuilderImpl(id, this);
    }

    public NRateLimitValueModel load(String id) {
        return store.load(id);
    }

    public void save(NRateLimitValueModel rateLimitValue) {
        store.save(rateLimitValue);
    }
}
