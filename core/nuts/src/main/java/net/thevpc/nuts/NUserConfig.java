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
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedLangUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public final class NUserConfig extends NConfigItem {

    private static final long serialVersionUID = 2;
    private String user;
    private String credentials;
    private List<String> groups;
    private List<String> permissions;
    private String remoteIdentity;
    private String remoteCredentials;

    public NUserConfig() {
    }

    public NUserConfig(NUserConfig other) {
        this.user = other.getUser();
        this.credentials = other.getCredentials();
        this.remoteIdentity = other.getRemoteIdentity();
        this.remoteCredentials = other.getRemoteCredentials();
        setGroups(other.getGroups());
        setPermissions(other.getPermissions());
    }

    public NUserConfig(String user, String credentials, List<String> groups, List<String> permissions) {
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

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = NReservedLangUtils.nonNullList(permissions);
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = NReservedLangUtils.nonNullList(groups);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(user, credentials, remoteIdentity, remoteCredentials,groups,permissions);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NUserConfig that = (NUserConfig) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(credentials, that.credentials) &&
                Objects.equals(groups, that.groups) &&
                Objects.equals(permissions, that.permissions) &&
                Objects.equals(remoteIdentity, that.remoteIdentity) &&
                Objects.equals(remoteCredentials, that.remoteCredentials);
    }

    @Override
    public String toString() {
        return "NutsUserConfig{" +
                "user='" + user + '\'' +
                ", credentials='" + credentials + '\'' +
                ", groups=" + groups +
                ", permissions=" + permissions +
                ", remoteIdentity='" + remoteIdentity + '\'' +
                ", remoteCredentials='" + remoteCredentials + '\'' +
                '}';
    }
}
