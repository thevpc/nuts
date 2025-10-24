package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitValueBuilder;
import net.thevpc.nuts.concurrent.NRateLimitRuleBuilder;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;

class NRateLimitRuleBuilderImpl implements NRateLimitRuleBuilder {
    private final NRateLimitValueBuilderImpl defaultNLimitedValueBuilder;
    private String id;
    private String strategy;
    private int max;
    private NDuration duration;
    private Instant startDate;
    public NRateLimitRuleBuilderImpl(String id, NRateLimitValueBuilderImpl defaultNLimitedValueBuilder) {
        this.id = id;
        this.defaultNLimitedValueBuilder = defaultNLimitedValueBuilder;
    }

    public NRateLimitRuleBuilderImpl setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public String getStrategy() {
        return strategy;
    }

    @Override
    public NRateLimitRuleBuilder withCapacity(int capacity) {
        this.max= capacity;
        return this;
    }

    @Override
    public NRateLimitRuleBuilder withDuration(NDuration duration) {
        this.duration=duration;
        return this;
    }

    @Override
    public NRateLimitRuleBuilder withStrategy(String strategy) {
        this.strategy= NStringUtils.trimToNull(strategy);
        return this;
    }

    @Override
    public NRateLimitRuleBuilder withStartDate(Instant startDate) {
        return this;
    }

    @Override
    public NRateLimitRuleBuilder withStrategy(NRateLimitDefaultStrategy strategy) {
        this.strategy=strategy==null?null:strategy.id();
        return this;
    }

    public String getId() {
        return id;
    }

    public int getMax() {
        return max;
    }

    public NDuration getDuration() {
        return duration;
    }

    @Override
    public NRateLimitValueBuilder end() {
        defaultNLimitedValueBuilder.rules.add(this);
        if (this == defaultNLimitedValueBuilder.lastRule) {
            defaultNLimitedValueBuilder.lastRule = null;
        }
        return defaultNLimitedValueBuilder;
    }
}
