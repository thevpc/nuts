package net.thevpc.nuts;

import java.util.Objects;

public class NDescriptorEffectiveConfig implements Cloneable {
    private boolean ignoreCurrentEnvironment;

    public boolean isIgnoreCurrentEnvironment() {
        return ignoreCurrentEnvironment;
    }

    public NDescriptorEffectiveConfig setIgnoreCurrentEnvironment(boolean ignoreCurrentEnvironment) {
        this.ignoreCurrentEnvironment = ignoreCurrentEnvironment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NDescriptorEffectiveConfig that = (NDescriptorEffectiveConfig) o;
        return ignoreCurrentEnvironment == that.ignoreCurrentEnvironment;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ignoreCurrentEnvironment);
    }

    @Override
    public String toString() {
        return "EffectiveNDescriptorConfig{" +
                "ignoreCurrentEnvironment=" + ignoreCurrentEnvironment +
                '}';
    }

    @Override
    public NDescriptorEffectiveConfig clone() {
        try {
            return (NDescriptorEffectiveConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
