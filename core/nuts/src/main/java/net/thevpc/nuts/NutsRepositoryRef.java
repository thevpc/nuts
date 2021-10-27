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

import java.util.Objects;

/**
 *
 * @author thevpc
 * @since 0.5.4
 * @app.category Base
 */
public class NutsRepositoryRef extends NutsConfigItem {

    private static final long serialVersionUID = 2;

    private String name;
    private String location;
    private boolean enabled = true;
    private boolean failSafe = false;
    private int deployWeight;

    public NutsRepositoryRef() {
    }

    public NutsRepositoryRef(NutsRepositoryRef other) {
        this.name = other.getName();
        this.location = other.getLocation();
        this.enabled = other.isEnabled();
        this.failSafe = other.isEnabled();
        this.deployWeight = other.getDeployWeight();
    }

    public NutsRepositoryRef(String name, String location, int deployPriority, boolean enabled) {
        this.name = name;
        this.location = location;
        this.deployWeight = deployPriority;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NutsRepositoryRef setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsRepositoryRef setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsRepositoryRef setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isFailSafe() {
        return failSafe;
    }

    public NutsRepositoryRef setFailSafe(boolean failSafe) {
        this.failSafe = failSafe;
        return this;
    }

    public NutsRepositoryRef copy() {
        return new NutsRepositoryRef(this);
    }

    public int getDeployWeight() {
        return deployWeight;
    }

    public NutsRepositoryRef setDeployWeight(int deployPriority) {
        this.deployWeight = deployPriority;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.location);
        hash = 79 * hash + this.deployWeight;
        hash = 79 * hash + (this.enabled ? 1 : 0);
        hash = 79 * hash + (this.failSafe ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsRepositoryRef other = (NutsRepositoryRef) obj;
        if (this.enabled != other.enabled) {
            return false;
        }
        if (this.failSafe != other.failSafe) {
            return false;
        }
        if (this.deployWeight != other.deployWeight) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.location, other.location);
    }

    @Override
    public String toString() {
        return "NutsRepositoryRef{" + "name=" + name + ", location=" + location + ", enabled=" + enabled + ", failSafe=" + failSafe + ", deployPriority=" + deployWeight + '}';
    }
}
