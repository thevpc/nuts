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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsRepositoryConfig;
import net.vpc.app.nuts.NutsSecurityEntityConfig;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import net.vpc.app.nuts.NutsRepositoryLocation;

/**
 * Created by vpc on 1/8/17.
 */
public class NutsRepositoryConfigImpl implements NutsRepositoryConfig {

    private static final long serialVersionUID = 1;
    private final Map<String, NutsRepositoryLocation> mirrors = new HashMap<>();
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
    public void setGroups(String groups) {
        this.groups = groups;
    }

    @Override
    public void removeMirror(String repositoryId) {
        mirrors.remove(repositoryId);
    }

    @Override
    public void addMirror(NutsRepositoryLocation c) {
        if (c != null) {
            mirrors.put(c.getId(), c);
        }
    }

    @Override
    public NutsRepositoryLocation getMirror(String id) {
        return mirrors.get(id);
    }

    @Override
    public NutsRepositoryLocation[] getMirrors() {
        return mirrors.values().toArray(new NutsRepositoryLocation[mirrors.size()]);
    }

    @Override
    public void setMirrors(NutsRepositoryLocation[] mirrors) {
        this.mirrors.clear();
        for (NutsRepositoryLocation mirror : mirrors) {
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

    @Override
    public String toString() {
        return "NutsRepositoryConfig{" + "mirrors=" + mirrors + ", security=" + security + ", id=" + id + ", type=" + type + ", location=" + location + ", groups=" + groups + ", env=" + env + ", instanceSerialVersionUID=" + instanceSerialVersionUID + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.mirrors);
        hash = 83 * hash + Objects.hashCode(this.security);
        hash = 83 * hash + Objects.hashCode(this.id);
        hash = 83 * hash + Objects.hashCode(this.type);
        hash = 83 * hash + Objects.hashCode(this.location);
        hash = 83 * hash + Objects.hashCode(this.groups);
        hash = 83 * hash + Objects.hashCode(this.env);
        hash = 83 * hash + (int) (this.instanceSerialVersionUID ^ (this.instanceSerialVersionUID >>> 32));
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
        final NutsRepositoryConfigImpl other = (NutsRepositoryConfigImpl) obj;
        if (this.instanceSerialVersionUID != other.instanceSerialVersionUID) {
            return false;
        }
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
        if (!Objects.equals(this.security, other.security)) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        return true;
    }
    
}
