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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

import java.util.*;

/**
 *
 * @author vpc
 * @since 0.5.4
 * @category Config
 */
public class NutsRepositoryConfig extends NutsConfigItem {

    private static final long serialVersionUID = 1;
    private String uuid;
    private String name;
    private String type;
    private String location;
    private Map<String, String> storeLocations = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private String groups;
    private Map<String,String> env;
    private List<NutsRepositoryRef> mirrors;
    private List<NutsUserConfig> users;
    private boolean indexEnabled;
    private String authenticationAgent;

    public NutsRepositoryConfig() {
    }

    public String getName() {
        return name;
    }

    public NutsRepositoryConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsRepositoryConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getType() {
        return type;
    }

    public NutsRepositoryConfig setType(String type) {
        this.type = type;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsRepositoryConfig setLocation(String location) {
        this.location = location;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsRepositoryConfig setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NutsRepositoryConfig setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String,String> getEnv() {
        return env;
    }

    public NutsRepositoryConfig setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public List<NutsRepositoryRef> getMirrors() {
        return mirrors;
    }

    public NutsRepositoryConfig setMirrors(List<NutsRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public NutsRepositoryConfig setUsers(List<NutsUserConfig> users) {
        this.users = users;
        return this;
    }

    public List<NutsUserConfig> getUsers() {
        return users;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NutsRepositoryConfig setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NutsRepositoryConfig setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }

    public NutsRepositoryConfig setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.uuid);
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.storeLocations);
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
        final NutsRepositoryConfig other = (NutsRepositoryConfig) obj;
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
        if (!Objects.equals(this.storeLocations, other.storeLocations)) {
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
        return "NutsRepositoryConfig{" + ", uuid=" + uuid + ", name=" + name + ", type=" + type + ", location=" + location + ", storeLocations=" + (storeLocations==null?"null":storeLocations.toString()) + ", storeLocationStrategy=" + storeLocationStrategy + ", groups=" + groups + ", env=" + env + ", mirrors=" + mirrors + ", users=" + users + ", indexEnabled=" + indexEnabled + ", authenticationAgent=" + authenticationAgent + '}';
    }

}
