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
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public final class NutsUserConfig implements Serializable {

    private static transient final long serialVersionUID = 1;
    private String user;
    private String credentials;
    private String[] groups;
    private String[] permissions;
    private String remoteIdentity;
    private String remoteCredentials;

    public NutsUserConfig() {
    }

    public NutsUserConfig(NutsUserConfig other) {
        this.user = other.getUser();
        this.credentials = other.getCredentials();
        this.remoteIdentity = other.getRemoteIdentity();
        this.remoteCredentials = other.getRemoteCredentials();
        this.groups=other.getGroups()==null?null:Arrays.copyOf(other.getGroups(),other.getGroups().length);
        this.permissions =other.getPermissions()==null?null:Arrays.copyOf(other.getPermissions(),other.getPermissions().length);
    }

    public NutsUserConfig(String user, String credentials, String[] groups, String[] permissions) {
        this.user = (user);
        this.credentials = (credentials);
        setGroups(groups);
        setPermissions(permissions);
    }

    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    public void setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
    }

    public String getRemoteCredentials() {
        return remoteCredentials;
    }

    public void setRemoteCredentials(String remoteCredentials) {
        this.remoteCredentials = remoteCredentials;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }
}
