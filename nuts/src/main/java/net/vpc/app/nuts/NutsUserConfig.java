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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class NutsUserConfig implements Serializable {

    private static transient final long serialVersionUID = 1;
    private String user;
    private String mappedUser;
    private String authenticationAgent;
    private String credentials;
    private Set<String> groups = new HashSet<>();
    private Set<String> rights = new HashSet<>();

    public NutsUserConfig() {
    }

    public NutsUserConfig(NutsUserConfig other) {
        this.user = other.getUser();
        this.mappedUser = other.getMappedUser();
        this.credentials = other.getCredentials();
        this.authenticationAgent = other.getAuthenticationAgent();
        this.groups.addAll(Arrays.asList(other.getGroups()));
        this.rights.addAll(Arrays.asList(other.getRights()));
    }


    public NutsUserConfig(String user, String credentials, String authenticationAgent, String[] groups, String[] rights) {
        this.user = (user);
        this.credentials = (credentials);
        this.authenticationAgent = authenticationAgent;
        setGroups(groups);
        setRights(rights);
    }


    public String getMappedUser() {
        return mappedUser;
    }


    public void setMappedUser(String mappedUser) {
        this.mappedUser = mappedUser;
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


    public String[] getRights() {
        return rights.toArray(new String[0]);
    }


    public void setRights(String[] rights) {
        this.rights = rights == null ? new HashSet() : new HashSet(Arrays.asList(rights));
    }


    public void addGroup(String grp) {
        groups.add(grp);
    }


    public void removeGroup(String grp) {
        groups.remove(grp);
    }


    public void addRight(String right) {
            rights.add(right);
    }


    public void removeRight(String right) {
        if (!NutsStringUtils.isEmpty(right)) {
            rights.remove(right);
        }
    }


    public String[] getGroups() {
        return groups.toArray(new String[0]);
    }


    public void setGroups(String[] groups) {
        this.groups = groups == null ? new HashSet() : new HashSet(Arrays.asList(groups));
    }


    public boolean containsRight(String right) {
        return rights.contains(right);
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NutsUserConfig setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }
}
