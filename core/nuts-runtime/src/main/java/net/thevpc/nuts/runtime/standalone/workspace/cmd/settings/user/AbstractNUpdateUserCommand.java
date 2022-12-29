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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBaseRepo;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.*;

/**
 *
 * @author thevpc
 * @since 0.5.7
 */
public abstract class AbstractNUpdateUserCommand extends NWorkspaceCommandBaseRepo<NUpdateUserCommand> implements NUpdateUserCommand {

    protected String login;
    protected String remoteIdentity;
    protected boolean resetPermissions;
    protected boolean resetGroups;
    protected char[] credentials;
    protected char[] oldCredentials;
    protected char[] remoteCredentials;
    protected final Set<String> permissions = new HashSet<>();
    protected final Set<String> groups = new HashSet<>();
    protected final Set<String> rm_permissions = new HashSet<>();
    protected final Set<String> rm_groups = new HashSet<>();

    public AbstractNUpdateUserCommand(NSession ws) {
        super(ws, "update-user");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public char[] getCredentials() {
        return credentials;
    }

    @Override
    public AbstractNUpdateUserCommand credentials(char[] password) {
        return setCredentials(password);
    }

    @Override
    public AbstractNUpdateUserCommand setCredentials(char[] password) {
        this.credentials = password;
        return this;
    }

    @Override
    public char[] getOldCredentials() {
        return oldCredentials;
    }

    @Override
    public AbstractNUpdateUserCommand oldCredentials(char[] password) {
        return setOldCredentials(password);
    }

    @Override
    public AbstractNUpdateUserCommand setOldCredentials(char[] password) {
        this.oldCredentials = password;
        return this;
    }

    @Override
    public char[] getRemoteCredentials() {
        return remoteCredentials;
    }

    @Override
    public AbstractNUpdateUserCommand remoteCredentials(char[] password) {
        return setRemoteCredentials(password);
    }

    @Override
    public AbstractNUpdateUserCommand setRemoteCredentials(char[] password) {
        this.remoteCredentials = password;
        return this;
    }

    @Override
    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    @Override
    public AbstractNUpdateUserCommand remoteIdentity(String remoteIdentity) {
        return setRemoteIdentity(remoteIdentity);
    }

    @Override
    public AbstractNUpdateUserCommand setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand addPermissions(String... permissions) {
        if (permissions != null) {
            return addPermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand removePermissions(String... permissions) {
        if (permissions != null) {
            return removePermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoAddPermissions(String... permissions) {
        if (permissions != null) {
            return undoAddPermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoRemovePermissions(String... permissions) {
        if (permissions != null) {
            return undoRemovePermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand addPermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String right : permissions) {
                if (right != null) {
                    this.permissions.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand removePermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String right : permissions) {
                if (right != null) {
                    this.rm_permissions.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoAddPermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String right : permissions) {
                if (right != null) {
                    this.permissions.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoRemovePermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String right : permissions) {
                if (right != null) {
                    this.rm_permissions.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand addGroups(String... groups) {
        if (groups != null) {
            return addGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoAddGroups(String... groups) {
        if (groups != null) {
            return undoAddGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand removeGroups(String... groups) {
        if (groups != null) {
            return removePermissions(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoRemoveGroups(String... groups) {
        if (groups != null) {
            return undoRemoveGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand addGroup(String group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand removeGroup(String group) {
        if (group != null) {
            this.rm_groups.add(group);
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand addPermission(String permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoAddPermission(String permissions) {
        if (permissions != null) {
            this.permissions.remove(permissions);
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoAddGroup(String group) {
        if (group != null) {
            this.groups.remove(group);
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand removePermission(String permission) {
        if (permission != null) {
            this.rm_permissions.add(permission);
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand addGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.groups.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand removeGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.rm_groups.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoAddGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.groups.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNUpdateUserCommand undoRemoveGroups(Collection<String> groups) {
        if (groups != null) {
            for (String right : groups) {
                if (right != null) {
                    this.rm_groups.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public List<String> getAddPermissions() {
        return new ArrayList<>(permissions);
    }

    @Override
    public List<String> getAddGroups() {
        return new ArrayList<>(groups);
    }

    @Override
    public List<String> getRemovePermissions() {
        return new ArrayList<>(rm_permissions);
    }

    @Override
    public List<String> getRemoveGroups() {
        return new ArrayList<>(rm_groups);
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public NUpdateUserCommand setUsername(String login) {
        this.login = login;
        return this;
    }

    @Override
    public boolean isResetPermissions() {
        return resetPermissions;
    }

    @Override
    public NUpdateUserCommand resetPermissions() {
        return resetPermissions(true);
    }

    @Override
    public NUpdateUserCommand resetPermissions(boolean resetPermissions) {
        return setResetPermissions(resetPermissions);
    }

    @Override
    public NUpdateUserCommand setResetPermissions(boolean resetPermissions) {
        this.resetPermissions = resetPermissions;
        return this;
    }

    @Override
    public boolean isResetGroups() {
        return resetGroups;
    }

    @Override
    public NUpdateUserCommand resetGroups() {
        return resetGroups(true);
    }

    @Override
    public NUpdateUserCommand resetGroups(boolean resetGroups) {
        return setResetGroups(resetGroups);
    }

    @Override
    public NUpdateUserCommand setResetGroups(boolean resetGroups) {
        this.resetGroups = resetGroups;
        return this;
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        NArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
