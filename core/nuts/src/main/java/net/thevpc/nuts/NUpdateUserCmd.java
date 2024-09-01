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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.ext.NExtensions;

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Security
 * @since 0.5.5
 */
public interface NUpdateUserCmd extends NWorkspaceCmd {
    static NUpdateUserCmd of(NSession session) {
        return NExtensions.of(session).createComponent(NUpdateUserCmd.class).get();
    }

    NUpdateUserCmd removeGroup(String group);

    NUpdateUserCmd addGroup(String group);

    NUpdateUserCmd undoAddGroup(String group);

    NUpdateUserCmd addGroups(String... groups);

    NUpdateUserCmd undoAddGroups(String... groups);

    NUpdateUserCmd addGroups(Collection<String> groups);

    NUpdateUserCmd undoAddGroups(Collection<String> groups);

    NUpdateUserCmd removePermission(String permission);

    NUpdateUserCmd addPermission(String permission);

    NUpdateUserCmd undoAddPermission(String permissions);

    NUpdateUserCmd addPermissions(String... permissions);

    NUpdateUserCmd undoAddPermissions(String... permissions);

    NUpdateUserCmd addPermissions(Collection<String> permissions);

    NUpdateUserCmd undoAddPermissions(Collection<String> permissions);

    NUpdateUserCmd removeGroups(String... groups);

    NUpdateUserCmd undoRemoveGroups(String... groups);

    NUpdateUserCmd removeGroups(Collection<String> groups);

    NUpdateUserCmd undoRemoveGroups(Collection<String> groups);

    NUpdateUserCmd removePermissions(String... permissions);

    NUpdateUserCmd undoRemovePermissions(String... permissions);

    NUpdateUserCmd removePermissions(Collection<String> permissions);

    NUpdateUserCmd undoRemovePermissions(Collection<String> permissions);

    NUpdateUserCmd credentials(char[] password);

    NUpdateUserCmd oldCredentials(char[] password);

    NUpdateUserCmd remoteIdentity(String remoteIdentity);

    String getUsername();

    NUpdateUserCmd setUsername(String login);

    boolean isResetPermissions();

    NUpdateUserCmd setResetPermissions(boolean resetPermissions);

    NUpdateUserCmd resetPermissions();

    NUpdateUserCmd resetPermissions(boolean resetPermissions);

    boolean isResetGroups();

    NUpdateUserCmd setResetGroups(boolean resetGroups);

    NUpdateUserCmd resetGroups();

    NUpdateUserCmd resetGroups(boolean resetGroups);

    NUpdateUserCmd remoteCredentials(char[] password);

    char[] getRemoteCredentials();

    NUpdateUserCmd setRemoteCredentials(char[] password);

    List<String> getAddGroups();

    List<String> getRemoveGroups();

    char[] getCredentials();

    NUpdateUserCmd setCredentials(char[] password);

    char[] getOldCredentials();

    NUpdateUserCmd setOldCredentials(char[] oldCredentials);

    String getRemoteIdentity();

    NUpdateUserCmd setRemoteIdentity(String remoteIdentity);

    List<String> getAddPermissions();

    List<String> getRemovePermissions();

    NRepository getRepository();

    NUpdateUserCmd setRepository(NRepository repository);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NUpdateUserCmd setSession(NSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateUserCmd copySession();

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
    NUpdateUserCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateUserCmd run();
}
