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

import java.util.Collection;

/**
 * Command class for adding users to workspaces and repositories.
 * All Command classes have a 'run' method to perform the operation.
 * @see NutsWorkspaceSecurityManager#addUser(java.lang.String) 
 * @see NutsRepositorySecurityManager#addUser(java.lang.String) 
 * 
 * @author vpc
 * @since 0.5.5
 */
public interface NutsAddUserCommand extends NutsWorkspaceCommand {

    NutsAddUserCommand addGroup(String group);

    NutsAddUserCommand addGroups(String... groups);

    NutsAddUserCommand addGroups(Collection<String> groups);

    NutsAddUserCommand addRight(String right);

    NutsAddUserCommand addRights(String... rights);

    NutsAddUserCommand addRights(Collection<String> rights);

    String[] getGroups();

    String getLogin();

    char[] getCredentials();

    String getRemoteIdentity();

    String[] getRights();

    NutsAddUserCommand group(String group);

    NutsAddUserCommand groups(String... groups);

    NutsAddUserCommand groups(Collection<String> groups);

    NutsAddUserCommand removeGroups(String... groups);

    NutsAddUserCommand removeGroups(Collection<String> groups);

    NutsAddUserCommand removeRights(String... rights);

    NutsAddUserCommand removeRights(Collection<String> rights);

    NutsAddUserCommand right(String right);

    NutsAddUserCommand rights(String... rights);

    NutsAddUserCommand rights(Collection<String> rights);

    NutsAddUserCommand login(String login);

    NutsAddUserCommand setLogin(String login);

    NutsAddUserCommand credentials(char[] password);

    NutsAddUserCommand setCredentials(char[] password);

    NutsAddUserCommand remoteIdentity(String remoteIdentity);

    NutsAddUserCommand setRemoteIdentity(String remoteIdentity);

    //
    // NutsWorkspaceCommand overridden methods
    //    

    @Override
    NutsAddUserCommand session(NutsSession session);

    @Override
    NutsAddUserCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments.
     * This is an override of the {@link NutsConfigurable#configure(java.lang.String...)}
     * to help return a more specific return type;
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsAddUserCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     * @return {@code this} instance
     */
    @Override
    NutsAddUserCommand run();

    char[] getRemoteCredentials();

    NutsAddUserCommand remoteCredentials(char[] password);

    NutsAddUserCommand setRemoteCredentials(char[] password);
}
