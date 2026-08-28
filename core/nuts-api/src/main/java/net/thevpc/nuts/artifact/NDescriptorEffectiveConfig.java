package net.thevpc.nuts.artifact;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NUnexpectedException;

import java.util.Objects;

/**
 * NDescriptorEffectiveConfig class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NDescriptorEffectiveConfig implements Cloneable {
    private boolean ignoreCurrentEnvironment;

    /**
     * Checks if is ignore current environment.
     *
     * @return is ignore current environment result
     */
    public boolean isIgnoreCurrentEnvironment() {
        return ignoreCurrentEnvironment;
    }

    /**
     * Sets the ignore current environment.
     *
     * @param ignoreCurrentEnvironment ignore current environment
     * @return set ignore current environment result
     */
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
          /**
           * Return.
           *
           * @param super.clone( super.clone(
           */
            return (NDescriptorEffectiveConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s",getClass()),e);
        }
    }
}
