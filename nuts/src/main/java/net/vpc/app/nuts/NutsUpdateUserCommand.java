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
 *
 * @author vpc
 * @since 0.5.5
 */
public interface NutsUpdateUserCommand extends NutsWorkspaceCommand {

    NutsUpdateUserCommand removeGroup(String group);

    NutsUpdateUserCommand addGroup(String group);

    NutsUpdateUserCommand undoAddGroup(String group);

    NutsUpdateUserCommand addGroups(String... groups);

    NutsUpdateUserCommand undoAddGroups(String... groups);

    NutsUpdateUserCommand addGroups(Collection<String> groups);

    NutsUpdateUserCommand undoAddGroups(Collection<String> groups);

    NutsUpdateUserCommand removePermission(String permission);

    NutsUpdateUserCommand addPermission(String permission);

    NutsUpdateUserCommand undoAddPermission(String permissions);

    NutsUpdateUserCommand addPermissions(String... permissions);

    NutsUpdateUserCommand undoAddPermissions(String... permissions);

    NutsUpdateUserCommand addPermissions(Collection<String> permissions);

    NutsUpdateUserCommand undoAddPermissions(Collection<String> permissions);

    NutsUpdateUserCommand removeGroups(String... groups);

    NutsUpdateUserCommand undoRemoveGroups(String... groups);

    NutsUpdateUserCommand removeGroups(Collection<String> groups);

    NutsUpdateUserCommand undoRemoveGroups(Collection<String> groups);

    NutsUpdateUserCommand removePermissions(String... permissions);

    NutsUpdateUserCommand undoRemovePermissions(String... permissions);

    NutsUpdateUserCommand removePermissions(Collection<String> permissions);

    NutsUpdateUserCommand undoRemovePermissions(Collection<String> permissions);

    NutsUpdateUserCommand credentials(char[] password);

    NutsUpdateUserCommand setCredentials(char[] password);

    NutsUpdateUserCommand oldCredentials(char[] password);

    NutsUpdateUserCommand setOldCredentials(char[] oldCredentials);

    NutsUpdateUserCommand remoteIdentity(String remoteIdentity);

    NutsUpdateUserCommand setRemoteIdentity(String remoteIdentity);

    String getLogin();

    NutsUpdateUserCommand login(String login);

    NutsUpdateUserCommand setLogin(String login);

    boolean isResetPermissions();

    NutsUpdateUserCommand resetPermissions();

    NutsUpdateUserCommand resetPermissions(boolean resetPermissions);

    NutsUpdateUserCommand setResetPermissions(boolean resetPermissions);

    boolean isResetGroups();

    NutsUpdateUserCommand resetGroups();

    NutsUpdateUserCommand resetGroups(boolean resetGroups);

    NutsUpdateUserCommand setResetGroups(boolean resetGroups);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsUpdateUserCommand session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsUpdateUserCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsUpdateUserCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsUpdateUserCommand run();

    NutsUpdateUserCommand setRemoteCredentials(char[] password);

    NutsUpdateUserCommand remoteCredentials(char[] password);

    char[] getRemoteCredentials();

    String[] getAddGroups();

    String[] getRemoveGroups();

    char[] getCredentials();

    char[] getOldCredentials();

    String getRemoteIdentity();

    String[] getAddPermissions();

    String[] getRemovePermissions();

}
