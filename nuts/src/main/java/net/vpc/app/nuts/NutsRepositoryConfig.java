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
import java.util.Properties;

public class NutsRepositoryConfig implements Serializable {

    private static final long serialVersionUID = 1;
    private final Map<String, NutsRepositoryLocation> mirrors = new HashMap<>();
    private final Map<String, NutsUserConfig> users = new HashMap<>();
    private String name;
    private String uuid;
    private String type;
    private String location;
    private String programsStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String libStoreLocation = null;
    private String logsStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private String groups;
    private Properties env = new Properties();

    public NutsRepositoryConfig() {
    }

    public NutsRepositoryConfig(NutsRepositoryConfig other) {
        this.name = other.getName();
        this.uuid = other.getUuid();
        this.location = other.getLocation();
        this.type = other.getType();
        this.groups = other.getGroups();
        this.programsStoreLocation = other.programsStoreLocation;
        this.configStoreLocation = other.configStoreLocation;
        this.varStoreLocation = other.varStoreLocation;
        this.libStoreLocation = other.libStoreLocation;
        this.logsStoreLocation = other.logsStoreLocation;
        this.logsStoreLocation = other.logsStoreLocation;
        this.tempStoreLocation = other.tempStoreLocation;
        this.cacheStoreLocation = other.cacheStoreLocation;
        this.storeLocationStrategy = other.storeLocationStrategy;
        this.env.putAll(other.getEnv());
        for (NutsRepositoryLocation mirror : other.getMirrors()) {
            addMirror(mirror);
        }
        for (NutsUserConfig secu : other.getUsers()) {
            setUser(secu);
        }
    }

    public NutsRepositoryConfig(String name, String location, String type) {
        this.name = name;
        this.location = location;
        this.type = type;
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

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public NutsRepositoryConfig setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;
        return this;
    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public NutsRepositoryConfig setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;
        return this;
    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public NutsRepositoryConfig setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;
        return this;
    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public NutsRepositoryConfig setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;
        return this;
    }

    public String getLogsStoreLocation() {
        return logsStoreLocation;
    }

    public NutsRepositoryConfig setLogsStoreLocation(String logsStoreLocation) {
        this.logsStoreLocation = logsStoreLocation;
        return this;
    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public NutsRepositoryConfig setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;
        return this;
    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public NutsRepositoryConfig setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;
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

    public Properties getEnv() {
        return env;
    }

    public NutsRepositoryConfig setEnv(Properties env) {
        this.env = env;
        return this;
    }

    public NutsRepositoryConfig removeMirror(String repositoryId) {
        mirrors.remove(repositoryId);
        return this;
    }


    public NutsRepositoryConfig addMirror(NutsRepositoryLocation c) {
        if (c != null) {
            mirrors.put(c.getName(), c);
        }
        return this;
    }


    public NutsRepositoryLocation getMirror(String id) {
        return mirrors.get(id);
    }


    public NutsRepositoryLocation[] getMirrors() {
        return mirrors.values().toArray(new NutsRepositoryLocation[0]);
    }


    public NutsRepositoryConfig setMirrors(NutsRepositoryLocation[] mirrors) {
        this.mirrors.clear();
        for (NutsRepositoryLocation mirror : mirrors) {
            addMirror(mirror);
        }
        return this;
    }


    public String getEnv(String property, String defaultValue) {
        String o = getEnv().getProperty(property);
        if (NutsUtils.isEmpty(o)) {
            return defaultValue;
        }
        return o;
    }


    public NutsRepositoryConfig setUsers(NutsUserConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            users.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
        return this;
    }


    public NutsRepositoryConfig setEnv(String property, String value) {
        if (NutsUtils.isEmpty(value)) {
            getEnv().remove(property);
        } else {
            getEnv().setProperty(property, value);
        }
        return this;
    }


    public NutsRepositoryConfig removeUser(String securityId) {
        users.remove(securityId);
        return this;
    }


    public NutsRepositoryConfig setUser(NutsUserConfig securityEntityConfig) {
        users.put(securityEntityConfig.getUser(), securityEntityConfig);
        return this;
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
        return "NutsRepositoryConfig{" + "mirrors=" + mirrors + ", users=" + users + ", name=" + name + ", uuid=" + uuid + ", type=" + type + ", location=" + location + ", groups=" + groups + ", env=" + env + '}';
    }
}
