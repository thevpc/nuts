/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Security
 * @since 0.5.5
 */
public interface NUpdateUserCommand extends NWorkspaceCommand {
    static NUpdateUserCommand of(NSession session) {
        return NExtensions.of(session).createSupported(NUpdateUserCommand.class);
    }

    NUpdateUserCommand removeGroup(String group);

    NUpdateUserCommand addGroup(String group);

    NUpdateUserCommand undoAddGroup(String group);

    NUpdateUserCommand addGroups(String... groups);

    NUpdateUserCommand undoAddGroups(String... groups);

    NUpdateUserCommand addGroups(Collection<String> groups);

    NUpdateUserCommand undoAddGroups(Collection<String> groups);

    NUpdateUserCommand removePermission(String permission);

    NUpdateUserCommand addPermission(String permission);

    NUpdateUserCommand undoAddPermission(String permissions);

    NUpdateUserCommand addPermissions(String... permissions);

    NUpdateUserCommand undoAddPermissions(String... permissions);

    NUpdateUserCommand addPermissions(Collection<String> permissions);

    NUpdateUserCommand undoAddPermissions(Collection<String> permissions);

    NUpdateUserCommand removeGroups(String... groups);

    NUpdateUserCommand undoRemoveGroups(String... groups);

    NUpdateUserCommand removeGroups(Collection<String> groups);

    NUpdateUserCommand undoRemoveGroups(Collection<String> groups);

    NUpdateUserCommand removePermissions(String... permissions);

    NUpdateUserCommand undoRemovePermissions(String... permissions);

    NUpdateUserCommand removePermissions(Collection<String> permissions);

    NUpdateUserCommand undoRemovePermissions(Collection<String> permissions);

    NUpdateUserCommand credentials(char[] password);

    NUpdateUserCommand oldCredentials(char[] password);

    NUpdateUserCommand remoteIdentity(String remoteIdentity);

    String getUsername();

    NUpdateUserCommand setUsername(String login);

    boolean isResetPermissions();

    NUpdateUserCommand setResetPermissions(boolean resetPermissions);

    NUpdateUserCommand resetPermissions();

    NUpdateUserCommand resetPermissions(boolean resetPermissions);

    boolean isResetGroups();

    NUpdateUserCommand setResetGroups(boolean resetGroups);

    NUpdateUserCommand resetGroups();

    NUpdateUserCommand resetGroups(boolean resetGroups);

    NUpdateUserCommand remoteCredentials(char[] password);

    char[] getRemoteCredentials();

    NUpdateUserCommand setRemoteCredentials(char[] password);

    List<String> getAddGroups();

    List<String> getRemoveGroups();

    char[] getCredentials();

    NUpdateUserCommand setCredentials(char[] password);

    char[] getOldCredentials();

    NUpdateUserCommand setOldCredentials(char[] oldCredentials);

    String getRemoteIdentity();

    NUpdateUserCommand setRemoteIdentity(String remoteIdentity);

    List<String> getAddPermissions();

    List<String> getRemovePermissions();

    NRepository getRepository();

    NUpdateUserCommand setRepository(NRepository repository);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NUpdateUserCommand setSession(NSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateUserCommand copySession();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NUpdateUserCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateUserCommand run();
}
