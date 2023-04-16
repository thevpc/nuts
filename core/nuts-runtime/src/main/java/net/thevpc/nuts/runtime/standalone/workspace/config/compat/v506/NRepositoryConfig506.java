/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506;

import net.thevpc.nuts.NConfigItem;
import net.thevpc.nuts.NRepositoryRef;
import net.thevpc.nuts.NStoreStrategy;
import net.thevpc.nuts.NUserConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class NRepositoryConfig506 extends NConfigItem {

    private static final long serialVersionUID = 1;
    private String uuid;
    private String name;
    private String type;
    private String location;
    private String[] storeLocations = null;
    private NStoreStrategy storeLocationStrategy = null;
    private String groups;
    private Map<String,String> env;
    private List<NRepositoryRef> mirrors;
    private List<NUserConfig> users;
    private boolean indexEnabled;
    private String authenticationAgent;

    public NRepositoryConfig506() {
    }

    public String getName() {
        return name;
    }

    public NRepositoryConfig506 setName(String name) {
        this.name = name;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NRepositoryConfig506 setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getType() {
        return type;
    }

    public NRepositoryConfig506 setType(String type) {
        this.type = type;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NRepositoryConfig506 setLocation(String location) {
        this.location = location;
        return this;
    }

    public NStoreStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NRepositoryConfig506 setStoreLocationStrategy(NStoreStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NRepositoryConfig506 setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String,String> getEnv() {
        return env;
    }

    public NRepositoryConfig506 setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public List<NRepositoryRef> getMirrors() {
        return mirrors;
    }

    public NRepositoryConfig506 setMirrors(List<NRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public NRepositoryConfig506 setUsers(List<NUserConfig> users) {
        this.users = users;
        return this;
    }

    public List<NUserConfig> getUsers() {
        return users;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NRepositoryConfig506 setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NRepositoryConfig506 setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public String[] getStoreLocations() {
        return storeLocations;
    }

    public void setStoreLocations(String[] storeLocations) {
        this.storeLocations = storeLocations;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.uuid);
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Arrays.deepHashCode(this.storeLocations);
        hash = 53 * hash + Objects.hashCode(this.storeLocationStrategy);
        hash = 53 * hash + Objects.hashCode(this.groups);
        hash = 53 * hash + Objects.hashCode(this.env);
        hash = 53 * hash + Objects.hashCode(this.mirrors);
        hash = 53 * hash + Objects.hashCode(this.users);
        hash = 53 * hash + (this.indexEnabled ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.authenticationAgent);
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
        final NRepositoryConfig506 other = (NRepositoryConfig506) obj;
        if (this.indexEnabled != other.indexEnabled) {
            return false;
        }
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.groups, other.groups)) {
            return false;
        }
        if (!Objects.equals(this.authenticationAgent, other.authenticationAgent)) {
            return false;
        }
        if (!Arrays.deepEquals(this.storeLocations, other.storeLocations)) {
            return false;
        }
        if (this.storeLocationStrategy != other.storeLocationStrategy) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        if (!Objects.equals(this.mirrors, other.mirrors)) {
            return false;
        }
        if (!Objects.equals(this.users, other.users)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsRepositoryConfig507{" + ", uuid=" + uuid + ", name=" + name + ", type=" + type + ", location=" + location + ", storeLocations=" + (storeLocations==null?"null":Arrays.toString(storeLocations)) + ", storeLocationStrategy=" + storeLocationStrategy + ", groups=" + groups + ", env=" + env + ", mirrors=" + mirrors + ", users=" + users + ", indexEnabled=" + indexEnabled + ", authenticationAgent=" + authenticationAgent + '}';
    }

}
