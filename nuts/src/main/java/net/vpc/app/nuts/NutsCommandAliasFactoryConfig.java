/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Map;
import java.util.Objects;

/**
 * Command Alias Factory Definition Config
 *
 * @author vpc
 * @since 0.5.4
 * @category SPI Base
 */
public class NutsCommandAliasFactoryConfig extends NutsConfigItem {
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
     * @return Factory id (unique identifier in the workspace)
     */
    public String getFactoryId() {
        return factoryId;
    }

    /**
     * Factory id (unique identifier in the workspace)
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasFactoryConfig setFactoryId(String value) {
        this.factoryId = value;
        return this;
    }

    /**
     * Factory Type
     * @return Factory Type
     */
    public String getFactoryType() {
        return factoryType;
    }

    /**
     * Factory Type
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasFactoryConfig setFactoryType(String value) {
        this.factoryType = value;
        return this;
    }

    /**
     * factory parameters
     * @return factory parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * factory parameters
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasFactoryConfig setParameters(Map<String, String> value) {
        this.parameters = value;
        return this;
    }

    /**
     * priority (the higher the better)
     * @return priority (the higher the better)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * priority (the higher the better)
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasFactoryConfig setPriority(int value) {
        this.priority = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsCommandAliasFactoryConfig that = (NutsCommandAliasFactoryConfig) o;
        return priority == that.priority &&
                Objects.equals(factoryId, that.factoryId) &&
                Objects.equals(factoryType, that.factoryType) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factoryId, factoryType, priority, parameters);
    }

    @Override
    public String toString() {
        return "NutsCommandAliasFactoryConfig{" +
                "factoryId='" + factoryId + '\'' +
                ", factoryType='" + factoryType + '\'' +
                ", priority=" + priority +
                ", parameters=" + parameters +
                '}';
    }
}
