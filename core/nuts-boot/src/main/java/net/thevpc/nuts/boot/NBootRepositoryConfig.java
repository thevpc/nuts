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


import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NBootRepositoryConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = 1;
    private String name;
    private NBootRepositoryLocation location;
    private Map<String, String> storeLocations = null;
    private String storeStrategy = null;
    private String groups;
    private Map<String, String> env;
    private String authenticationAgent;
    private String[] tags;

    public NBootRepositoryConfig() {
    }

    public NBootRepositoryConfig copy() {
        return clone();
    }

    @Override
    protected NBootRepositoryConfig clone() {
        try {
            NBootRepositoryConfig o = (NBootRepositoryConfig) super.clone();
            if (o.location != null) {
                o.location = o.location.copy();
            }
            if (o.storeLocations != null) {
                o.storeLocations = new LinkedHashMap<>(storeLocations);
            }
            if (o.env != null) {
                o.env = new LinkedHashMap<>(o.env);
            }
            if (o.tags != null) {
                o.tags = Arrays.copyOf(tags, tags.length);
            }
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getTags() {
        return tags;
    }

    public NBootRepositoryConfig setTags(String[] tags) {
        this.tags = tags;
        return this;
    }

    public String getName() {
        return name;
    }

    public NBootRepositoryConfig setName(String name) {
        this.name = name;
        return this;
    }

    public NBootRepositoryLocation getLocation() {
        return location;
    }

    public NBootRepositoryConfig setLocation(NBootRepositoryLocation location) {
        this.location = location;
        return this;
    }

    public String getStoreStrategy() {
        return storeStrategy;
    }

    public NBootRepositoryConfig setStoreStrategy(String storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NBootRepositoryConfig setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public NBootRepositoryConfig setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NBootRepositoryConfig setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }

    public NBootRepositoryConfig setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.storeLocations);
        hash = 53 * hash + Objects.hashCode(this.storeStrategy);
        hash = 53 * hash + Objects.hashCode(this.groups);
        hash = 53 * hash + Objects.hashCode(this.env);
        hash = 53 * hash + Objects.hashCode(this.authenticationAgent);
        hash = 53 * hash + Arrays.hashCode(this.tags);
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
        final NBootRepositoryConfig other = (NBootRepositoryConfig) obj;
        if (!Objects.equals(this.name, other.name)) {
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
        if (!Objects.equals(this.storeLocations, other.storeLocations)) {
            return false;
        }
        if (!NBootUtils.sameEnum(this.storeStrategy, other.storeStrategy)) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        if (!Arrays.equals(this.tags, other.tags)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsRepositoryConfig{"
                + ", name=" + name
                + ", location=" + location + ", storeLocations=" + (storeLocations == null ? "null" : storeLocations.toString()) + ", storeStrategy=" + storeStrategy + ", groups=" + groups + ", env=" + env
                + ", authenticationAgent=" + authenticationAgent
                + ", tags=" + (tags == null ? "[]" : Arrays.toString(tags))
                + '}';
    }

}
