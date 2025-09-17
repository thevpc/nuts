package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NRateLimitedValueFactoryImpl implements NRateLimitedValueFactory {
    private NRateLimitedValueStore store;
    private Map<String, Function<NRateLimitStrategyModel, NRateLimitStrategy>> strategies = new HashMap<>();

    public NRateLimitedValueFactoryImpl(NRateLimitedValueStore store) {
        this.store = store;
    }

    public NRateLimitStrategy createStrategy(NRateLimitStrategyModel model) {
        String strategyId = model.getId();
        Function<NRateLimitStrategyModel, NRateLimitStrategy> d = strategies.get(strategyId);
        if (d != null) {
            NRateLimitStrategy s = d.apply(model);
            if (s != null) {
                return s;
            }
        }
        switch (NNameFormat.LOWER_KEBAB_CASE.format(NStringUtils.trim(strategyId))) {
            case "bucket":{
                return new NRateLimitTokenBucketStrategy(model);
            }
            case "sliding-window":{
                return new NRateLimitSlidingWindowStrategy(model);
            }
            case "lecky-bucket":{
                return new NRateLimitLeakyBucketStrategy(model);
            }
            case "fixed-window":{
                return new NRateLimitFixedWindowStrategy(model);
            }
            default: {
                return new NRateLimitTokenBucketStrategy(model);
            }
        }
    }

    public NRateLimitedValueFactory defineStrategy(String name, Function<NRateLimitStrategyModel, NRateLimitStrategy> definition) {
        if (definition == null) {
            strategies.remove(name);
        } else {
            strategies.put(name, definition);
        }
    }

    @Override
    public NRateLimitedValueBuilder value(String id) {
        return new NRateLimitedValueBuilderImpl(id, this);
    }

    public NRateLimitedValueModel load(String id) {
        return store.load(id);
    }

    public void save(NRateLimitedValueModel nRateLimitedValue) {
        store.save(nRateLimitedValue);
    }
}
