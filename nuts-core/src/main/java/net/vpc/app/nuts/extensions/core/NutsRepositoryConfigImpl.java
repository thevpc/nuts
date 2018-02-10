/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsRepositoryConfig;
import net.vpc.app.nuts.NutsSecurityEntityConfig;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by vpc on 1/8/17.
 */
public class NutsRepositoryConfigImpl implements NutsRepositoryConfig {

    private static final long serialVersionUID = 1;
    private final Map<String, NutsRepositoryConfig> mirrors = new HashMap<>();
    private final Map<String, NutsSecurityEntityConfig> security = new HashMap<>();
    private String id;
    private String type;
    private String location;
    private String groups;
    private Properties env = new Properties();
    private long instanceSerialVersionUID = serialVersionUID;

    public NutsRepositoryConfigImpl() {
    }

    public NutsRepositoryConfigImpl(String id, String location, String type) {
        this.id = id;
        this.location = location;
        this.type = type;
    }

    @Override
    public long getInstanceSerialVersionUID() {
        return instanceSerialVersionUID;
    }

    @Override
    public void setInstanceSerialVersionUID(long instanceSerialVersionUID) {
        this.instanceSerialVersionUID = instanceSerialVersionUID;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getGroups() {
        return groups;
    }

    @Override
    public NutsRepositoryConfig setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    @Override
    public void removeMirror(String repositoryId) {
        mirrors.remove(repositoryId);
    }

    @Override
    public void addMirror(NutsRepositoryConfig c) {
        if (c != null) {
            mirrors.put(c.getId(), c);
        }
    }

    @Override
    public NutsRepositoryConfig getMirror(String id) {
        return mirrors.get(id);
    }

    @Override
    public NutsRepositoryConfig[] getMirrors() {
        return mirrors.values().toArray(new NutsRepositoryConfig[mirrors.size()]);
    }

    @Override
    public void setMirrors(NutsRepositoryConfig[] mirrors) {
        this.mirrors.clear();
        for (NutsRepositoryConfig mirror : mirrors) {
            addMirror(mirror);
        }
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        String o = getEnv().getProperty(property);
        if (CoreStringUtils.isEmpty(o)) {
            return defaultValue;
        }
        return o;
    }

    @Override
    public void setSecurity(NutsSecurityEntityConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            security.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
    }

    @Override
    public void setEnv(String property, String value) {
        if (CoreStringUtils.isEmpty(value)) {
            getEnv().remove(property);
        } else {
            getEnv().setProperty(property, value);
        }
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public void setEnv(Properties env) {
        this.env = env;
    }

    @Override
    public void removeSecurity(String securityId) {
        security.remove(securityId);
    }

    @Override
    public void addSecurity(NutsSecurityEntityConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            security.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
    }

    @Override
    public NutsSecurityEntityConfig getSecurity(String id) {
        NutsSecurityEntityConfig config = security.get(id);
        if (config == null) {
            if (NutsConstants.USER_ADMIN.equals(id) || NutsConstants.USER_ANONYMOUS.equals(id)) {
                config = new NutsSecurityEntityConfigImpl(id, null, null, null);
                security.put(id, config);
            }
        }
        return config;
    }

    @Override
    public NutsSecurityEntityConfig[] getSecurity() {
        return security.values().toArray(new NutsSecurityEntityConfig[security.size()]);
    }

    @Override
    public void setSecurity(NutsSecurityEntityConfig[] securityEntityConfigs) {
        this.security.clear();
        for (NutsSecurityEntityConfig conf : securityEntityConfigs) {
            addSecurity(conf);
        }
    }
}
