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

import java.util.Map;
import java.util.Objects;

/**
 * Command Alias Factory Definition Config
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public class NutsCommandFactoryConfig extends NutsConfigItem {
    private static final long serialVersionUID = 1;

    /**
     * Factory id (unique identifier in the workspace)
     */
    private String factoryId;

    /**
     * Factory Type
     */
    private String factoryType;

    /**
     * priority (the higher the better)
     */
    private int priority;

    /**
     * factory parameters
     */
    private Map<String, String> parameters;

    /**
     * Factory id (unique identifier in the workspace)
     *
     * @return Factory id (unique identifier in the workspace)
     */
    public String getFactoryId() {
        return factoryId;
    }

    /**
     * Factory id (unique identifier in the workspace)
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandFactoryConfig setFactoryId(String value) {
        this.factoryId = value;
        return this;
    }

    /**
     * Factory Type
     *
     * @return Factory Type
     */
    public String getFactoryType() {
        return factoryType;
    }

    /**
     * Factory Type
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandFactoryConfig setFactoryType(String value) {
        this.factoryType = value;
        return this;
    }

    /**
     * factory parameters
     *
     * @return factory parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * factory parameters
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandFactoryConfig setParameters(Map<String, String> value) {
        this.parameters = value;
        return this;
    }

    /**
     * priority (the higher the better)
     *
     * @return priority (the higher the better)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * priority (the higher the better)
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandFactoryConfig setPriority(int value) {
        this.priority = value;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(factoryId, factoryType, priority, parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsCommandFactoryConfig that = (NutsCommandFactoryConfig) o;
        return priority == that.priority &&
                Objects.equals(factoryId, that.factoryId) &&
                Objects.equals(factoryType, that.factoryType) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public String toString() {
        return "NutsCommandFactoryConfig{" +
                "factoryId='" + factoryId + '\'' +
                ", factoryType='" + factoryType + '\'' +
                ", priority=" + priority +
                ", parameters=" + parameters +
                '}';
    }
}
