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

import net.vpc.app.nuts.NutsSecurityEntityConfig;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vpc on 1/8/17.
 */
public class NutsSecurityEntityConfigImpl implements NutsSecurityEntityConfig {

    @JsonTransient
    private static transient final long serialVersionUID = 1;
    private String user;
    private String mappedUser;
    private String credentials;
    private Set<String> groups = new HashSet<>();
    private Set<String> rights = new HashSet<>();
    private long instanceSerialVersionUID = serialVersionUID;

    public NutsSecurityEntityConfigImpl() {
    }

    public NutsSecurityEntityConfigImpl(String user, String credentials, String[] groups, String[] rights) {
        setUser(user);
        setCredentials(credentials);
        setGroups(groups);
        setRights(rights);
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
    public String getMappedUser() {
        return mappedUser;
    }

    @Override
    public void setMappedUser(String mappedUser) {
        this.mappedUser = mappedUser;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    @Override
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public String[] getRights() {
        return rights.toArray(new String[rights.size()]);
    }

    @Override
    public void setRights(String[] rights) {
        this.rights = rights == null ? new HashSet() : new HashSet(Arrays.asList(rights));
    }

    @Override
    public void addGroup(String grp) {
        if (!CoreStringUtils.isEmpty(grp)) {
            groups.add(grp);
        }
    }

    @Override
    public void removeGroup(String grp) {
        if (!CoreStringUtils.isEmpty(grp)) {
            groups.remove(grp);
        }
    }

    @Override
    public void addRight(String right) {
        if (!CoreStringUtils.isEmpty(right)) {
            rights.add(right);
        }
    }

    @Override
    public void removeRight(String right) {
        if (!CoreStringUtils.isEmpty(right)) {
            rights.remove(right);
        }
    }

    @Override
    public String[] getGroups() {
        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public void setGroups(String[] groups) {
        this.groups = groups == null ? new HashSet() : new HashSet(Arrays.asList(groups));
    }

    @Override
    public boolean containsRight(String right) {
        return rights.contains(right);
    }
}
