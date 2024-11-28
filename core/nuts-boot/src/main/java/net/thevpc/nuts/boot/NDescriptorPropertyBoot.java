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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

/**
 * Descriptor Property Builder
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.8.3
 */
public class NDescriptorPropertyBoot {

    private static final long serialVersionUID = 1L;

    private String name;
    private String value = null;
    private NEnvConditionBoot condition;

    public NDescriptorPropertyBoot() {
        this.condition = new NEnvConditionBoot();
    }

    public NDescriptorPropertyBoot(NDescriptorPropertyBoot other) {
        this.condition = new NEnvConditionBoot();
        setAll(other);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }


    public NDescriptorPropertyBoot setCondition(NEnvConditionBoot condition) {
        this.condition.setAll(condition);
        return this;
    }

    public NEnvConditionBoot getCondition() {
        return condition;
    }


    public NDescriptorPropertyBoot setName(String name) {
        this.name = name;
        return this;
    }


    public NDescriptorPropertyBoot setValue(String value) {
        this.value = value;
        return this;
    }


    public NDescriptorPropertyBoot setAll(NDescriptorPropertyBoot value) {
        if (value == null) {
            this.setName(null);
            this.setValue(null);
            this.setCondition(null);
        } else {
            this.setName(value.getName());
            this.setValue(value.getValue());
            this.setCondition(value.getCondition());
        }
        return this;
    }

    public boolean isBlank() {
        if (!NStringUtilsBoot.isBlank(name)) {
            return false;
        }
        if (!NStringUtilsBoot.isBlank(value)) {
            return false;
        }
        return condition == null || condition.isBlank();
    }

    public NDescriptorPropertyBoot copy() {
        return new NDescriptorPropertyBoot(this);
    }

}
