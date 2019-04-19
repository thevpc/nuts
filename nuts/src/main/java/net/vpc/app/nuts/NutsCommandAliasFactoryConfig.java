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

/**
 * 
 * @author vpc
 * @since 0.5.4
 */
public class NutsCommandAliasFactoryConfig {
    private String factoryId;
    private String factoryType;
    private int priority;
    private Map<String,String> parameters;

    public String getFactoryId() {
        return factoryId;
    }

    public NutsCommandAliasFactoryConfig setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public String getFactoryType() {
        return factoryType;
    }

    public NutsCommandAliasFactoryConfig setFactoryType(String factoryType) {
        this.factoryType = factoryType;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public NutsCommandAliasFactoryConfig setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public NutsCommandAliasFactoryConfig setPriority(int priority) {
        this.priority = priority;
        return this;
    }
}
