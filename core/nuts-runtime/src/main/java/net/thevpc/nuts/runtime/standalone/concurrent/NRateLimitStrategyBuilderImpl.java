package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitDefaultStrategy;
import net.thevpc.nuts.concurrent.NRateLimitedValueBuilder;
import net.thevpc.nuts.concurrent.NRateLimitStrategyBuilder;

import java.time.Duration;
import java.time.Instant;

class NRateLimitStrategyBuilderImpl implements NRateLimitStrategyBuilder {
    private final NRateLimitedValueBuilderImpl defaultNLimitedValueBuilder;
    private String id;
    private String strategy;
    private int max;
    private Duration duration;
    private Instant startDate;
    public NRateLimitStrategyBuilderImpl(String id, NRateLimitedValueBuilderImpl defaultNLimitedValueBuilder) {
        this.id = id;
        this.defaultNLimitedValueBuilder = defaultNLimitedValueBuilder;
    }


    public Instant getStartDate() {
        return startDate;
    }

    public String getStrategy() {
        return strategy;
    }

    @Override
    public NRateLimitStrategyBuilder withMax(int max) {
        this.max=max;
        return this;
    }

    @Override
    public NRateLimitStrategyBuilder withDuration(Duration duration) {
        this.duration=duration;
        return this;
    }

    @Override
    public NRateLimitStrategyBuilder withStrategy(String strategy) {
        this.strategy=strategy;
        return this;
    }

    @Override
    public NRateLimitStrategyBuilder withStartDate(Instant startDate) {
        return this;
    }

    @Override
    public NRateLimitStrategyBuilder withStrategy(NRateLimitDefaultStrategy strategy) {
        return null;
    }

    public String getId() {
        return id;
    }

    public int getMax() {
        return max;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public NRateLimitedValueBuilder end() {
        defaultNLimitedValueBuilder.constraints.add(this);
        if (this == defaultNLimitedValueBuilder.lastConstraint) {
            defaultNLimitedValueBuilder.lastConstraint = null;
        }
        return defaultNLimitedValueBuilder;
    }
}
