package net.thevpc.nuts;

import java.util.Objects;

public class EffectiveNDescriptorConfig {
    private boolean filterCurrentEnvironment;

    public boolean isFilterCurrentEnvironment() {
        return filterCurrentEnvironment;
    }

    public EffectiveNDescriptorConfig setFilterCurrentEnvironment(boolean filterCurrentEnvironment) {
        this.filterCurrentEnvironment = filterCurrentEnvironment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EffectiveNDescriptorConfig that = (EffectiveNDescriptorConfig) o;
        return filterCurrentEnvironment == that.filterCurrentEnvironment;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(filterCurrentEnvironment);
    }

    @Override
    public String toString() {
        return "EffectiveNDescriptorConfig{" +
                "filterCurrentEnvironment=" + filterCurrentEnvironment +
                '}';
    }
}
