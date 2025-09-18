package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NRateLimitValueFactoryImpl implements NRateLimitValueFactory {
    private NRateLimitValueStore store;
    private Map<String, Function<NRateLimitRuleModel, NRateLimitRule>> strategies = new HashMap<>();

    public NRateLimitValueFactoryImpl(NRateLimitValueStore store) {
        this.store = store;
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
            case "bucket":{
                return new NRateLimitBucketRule(model);
            }
            case "sliding-window":{
                return new NRateLimitSlidingWindowRule(model);
            }
            case "lecky-bucket":{
                return new NRateLimitLeakyBucketRule(model);
            }
            case "fixed-window":{
                return new NRateLimitFixedWindowRule(model);
            }
            default: {
                return new NRateLimitBucketRule(model);
            }
        }
    }

    public NRateLimitValueFactory defineStrategy(String name, Function<NRateLimitRuleModel, NRateLimitRule> definition) {
        if (definition == null) {
            strategies.remove(name);
        } else {
            strategies.put(name, definition);
        }
        return this;
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
