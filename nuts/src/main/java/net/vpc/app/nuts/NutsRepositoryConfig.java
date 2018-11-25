/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class NutsRepositoryConfig implements Serializable {

    private static final long serialVersionUID = 1;
    private final Map<String, NutsRepositoryLocation> mirrors = new HashMap<>();
    private final Map<String, NutsUserConfig> users = new HashMap<>();
    private String id;
    private String type;
    private String location;
    private String componentsLocation = null;
    private String groups;
    private Properties env = new Properties();

    public NutsRepositoryConfig() {
    }

    public NutsRepositoryConfig(NutsRepositoryConfig other) {
        this.id = other.getId();
        this.location = other.getLocation();
        this.type = other.getType();
        this.groups = other.getGroups();
        this.componentsLocation = other.getComponentsLocation();
        this.env.putAll(other.getEnv());
        for (NutsRepositoryLocation mirror : other.getMirrors()) {
            addMirror(mirror);
        }
        for (NutsUserConfig secu : other.getUsers()) {
            setUser(secu);
        }
    }

    public NutsRepositoryConfig(String id, String location, String type) {
        this.id = id;
        this.location = location;
        this.type = type;
    }


    public String getComponentsLocation() {
        return componentsLocation;
    }

    public void setComponentsLocation(String componentsLocation) {
        this.componentsLocation = componentsLocation;
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = location;
    }


    public String getGroups() {
        return groups;
    }


    public void setGroups(String groups) {
        this.groups = groups;
    }


    public void removeMirror(String repositoryId) {
        mirrors.remove(repositoryId);
    }


    public void addMirror(NutsRepositoryLocation c) {
        if (c != null) {
            mirrors.put(c.getId(), c);
        }
    }


    public NutsRepositoryLocation getMirror(String id) {
        return mirrors.get(id);
    }


    public NutsRepositoryLocation[] getMirrors() {
        return mirrors.values().toArray(new NutsRepositoryLocation[0]);
    }


    public void setMirrors(NutsRepositoryLocation[] mirrors) {
        this.mirrors.clear();
        for (NutsRepositoryLocation mirror : mirrors) {
            addMirror(mirror);
        }
    }


    public String getEnv(String property, String defaultValue) {
        String o = getEnv().getProperty(property);
        if (NutsStringUtils.isEmpty(o)) {
            return defaultValue;
        }
        return o;
    }


    public void setUsers(NutsUserConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            users.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
    }


    public void setEnv(String property, String value) {
        if (NutsStringUtils.isEmpty(value)) {
            getEnv().remove(property);
        } else {
            getEnv().setProperty(property, value);
        }
    }


    public Properties getEnv() {
        return env;
    }


    public void setEnv(Properties env) {
        this.env = env;
    }


    public void removeUser(String securityId) {
        users.remove(securityId);
    }


    public void setUser(NutsUserConfig securityEntityConfig) {
        users.put(securityEntityConfig.getUser(), securityEntityConfig);
    }


    public NutsUserConfig getUser(String userId) {
        NutsUserConfig config = users.get(userId);
        if (config == null) {
            if (NutsConstants.USER_ADMIN.equals(userId) || NutsConstants.USER_ANONYMOUS.equals(userId)) {
                config = new NutsUserConfig(userId, null, null, null, null);
                users.put(userId, config);
            }
        }
        return config;
    }


    public NutsUserConfig[] getUsers() {
        return users.values().toArray(new NutsUserConfig[0]);
    }


    public void setSecurity(NutsUserConfig[] securityEntityConfigs) {
        this.users.clear();
        for (NutsUserConfig conf : securityEntityConfigs) {
            setUser(conf);
        }
    }


    public String toString() {
        return "NutsRepositoryConfig{" + "mirrors=" + mirrors + ", users=" + users + ", id=" + id + ", type=" + type + ", location=" + location + ", groups=" + groups + ", env=" + env + '}';
    }


    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.mirrors);
        hash = 83 * hash + Objects.hashCode(this.users);
        hash = 83 * hash + Objects.hashCode(this.id);
        hash = 83 * hash + Objects.hashCode(this.type);
        hash = 83 * hash + Objects.hashCode(this.location);
        hash = 83 * hash + Objects.hashCode(this.groups);
        hash = 83 * hash + Objects.hashCode(this.env);
        return hash;
    }


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
        if (!Objects.equals(this.id, other.id)) {
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
        if (!Objects.equals(this.mirrors, other.mirrors)) {
            return false;
        }
        if (!Objects.equals(this.users, other.users)) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        return true;
    }
}
