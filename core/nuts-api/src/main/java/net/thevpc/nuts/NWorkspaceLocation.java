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
package net.thevpc.nuts;

import java.io.Serializable;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * date 2019-03-02
 * @app.category Config
 * @since 0.5.4
 */
public class NWorkspaceLocation implements Serializable {

    private static final long serialVersionUID = 1;

    private String uuid;
    private String name;
    private String location;
    private boolean enabled = true;

    public NWorkspaceLocation() {
    }

    public NWorkspaceLocation(NWorkspaceLocation other) {
        this.name = other.uuid;
        this.name = other.getName();
        this.location = other.getLocation();
        this.enabled = other.isEnabled();
        this.uuid = other.getUuid();
    }

    public NWorkspaceLocation(String uuid, String name, String location) {
        this.uuid = uuid;
        this.name = name;
        this.location = location;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NWorkspaceLocation setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getName() {
        return name;
    }

    public NWorkspaceLocation setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NWorkspaceLocation setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NWorkspaceLocation setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NWorkspaceLocation that = (NWorkspaceLocation) o;

        return location != null ? location.equals(that.location) : that.location == null;

    }

    @Override
    public String toString() {
        return "NutsWorkspaceLocation{"
                + "uuid='" + uuid + '\''
                + ", name='" + name + '\''
                + ", location='" + location + '\''
                + ", enabled=" + enabled
                + '}';
    }

    public NWorkspaceLocation copy() {
        return new NWorkspaceLocation(this);
    }
}
