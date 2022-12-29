/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 * Descriptor Property Builder
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.8.3
 */
public class DefaultNDescriptorPropertyBuilder implements NDescriptorPropertyBuilder {

    private static final long serialVersionUID = 1L;

    private String name;
    private NValue value = NValue.of(null);
    private NEnvConditionBuilder condition;

    public DefaultNDescriptorPropertyBuilder() {
        this.condition = new DefaultNEnvConditionBuilder();
    }

    public DefaultNDescriptorPropertyBuilder(NDescriptorProperty other) {
        this.condition = new DefaultNEnvConditionBuilder();
        setAll(other);
    }

    public String getName() {
        return name;
    }

    public NValue getValue() {
        return value;
    }

    @Override
    public NDescriptorPropertyBuilder setCondition(NEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }

    public NEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NDescriptorPropertyBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NDescriptorPropertyBuilder setValue(String value) {
        this.value = NValue.of(value);
        return this;
    }

    @Override
    public NDescriptorPropertyBuilder setAll(NDescriptorProperty value) {
        if (value == null) {
            this.setName(null);
            this.setValue(null);
            this.setCondition(null);
        } else {
            this.setName(value.getName());
            this.setValue(value.getValue().asString().orNull());
            this.setCondition(value.getCondition());
        }
        return this;
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

    public NDescriptorPropertyBuilder builder() {
        return new DefaultNDescriptorPropertyBuilder(this);
    }

    @Override
    public NDescriptorProperty readOnly() {
        return new DefaultNDescriptorProperty(getName(), getValue(), getCondition().readOnly());
    }

    @Override
    public NDescriptorProperty build() {
        return readOnly();
    }

    @Override
    public NDescriptorPropertyBuilder copy() {
        return builder();
    }
}
