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
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsRepositorySecurityManager {

    boolean isAllowed(String right);

    void checkAllowed(String right) throws SecurityException;

    void addUser(String user, String password, String... rights);

    void setUserRights(String user, String... rights);

    void addUserRights(String user, String... rights);

    void removeUserRights(String user, String... rights);

    void setUserRemoteIdentity(String user, String mappedIdentity);

    void setUserGroups(String user, String... groups);

    void addUserGroups(String user, String... groups);

    void removeUserGroups(String user, String... groups);

    void setUserCredentials(String login, String password, String oldPassword);

    NutsEffectiveUser[] findUsers();

    NutsEffectiveUser getEffectiveUser(String username);

    void setAuthenticationAgent(String authenticationAgent);

    NutsAuthenticationAgent getAuthenticationAgent();

}
