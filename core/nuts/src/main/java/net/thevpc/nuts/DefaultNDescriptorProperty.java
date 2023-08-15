package net.thevpc.nuts;


import net.thevpc.nuts.util.NLiteral;

import java.util.Objects;

public class DefaultNDescriptorProperty implements NDescriptorProperty {
    private final String name;
    private final NLiteral value;
    private final NEnvCondition condition;

    public DefaultNDescriptorProperty(String name, NLiteral value, NEnvCondition condition) {
        this.name = name;
        this.value = value;
        this.condition = condition == null ? NEnvCondition.BLANK : condition.readOnly();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NLiteral getValue() {
        return value;
    }

    @Override
    public NEnvCondition getCondition() {
        return condition;
    }

    @Override
    public NDescriptorPropertyBuilder builder() {
        return new DefaultNDescriptorPropertyBuilder()
                .setName(getName())
                .setValue(getValue().asString().orNull())
                .setCondition(getCondition());
    }

    @Override
    public boolean isBlank() {
        if (!NBlankable.isBlank(name)) {
            return false;
        }
        if (!NBlankable.isBlank(value)) {
            return false;
        }
        return NBlankable.isBlank(condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, condition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNDescriptorProperty that = (DefaultNDescriptorProperty) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value) && Objects.equals(condition, that.condition);
    }

    @Override
    public String toString() {
        return name + "=" + value +
                (condition.isBlank() ? "" : (" when " + condition))
                ;
    }

    @Override
    public NDescriptorProperty readOnly() {
        return this;
    }
}
