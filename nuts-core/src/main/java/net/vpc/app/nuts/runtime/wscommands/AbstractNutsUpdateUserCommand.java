/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.wscommands;

import net.vpc.app.nuts.*;

import java.util.*;

/**
 *
 * @author vpc
 * @since 0.5.7
 */
public abstract class AbstractNutsUpdateUserCommand extends NutsWorkspaceCommandBase<NutsUpdateUserCommand> implements NutsUpdateUserCommand {

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
    protected NutsRepository repo;

    public AbstractNutsUpdateUserCommand(NutsWorkspace ws) {
        super(ws, "update-user");
    }

    public AbstractNutsUpdateUserCommand(NutsRepository repo) {
        super(repo.getWorkspace(), "update-user");
        this.repo = repo;
    }

    @Override
    public char[] getCredentials() {
        return credentials;
    }

    @Override
    public AbstractNutsUpdateUserCommand credentials(char[] password) {
        return setCredentials(password);
    }

    @Override
    public AbstractNutsUpdateUserCommand setCredentials(char[] password) {
        this.credentials = password;
        return this;
    }

    @Override
    public char[] getOldCredentials() {
        return oldCredentials;
    }

    @Override
    public AbstractNutsUpdateUserCommand oldCredentials(char[] password) {
        return setOldCredentials(password);
    }

    @Override
    public AbstractNutsUpdateUserCommand setOldCredentials(char[] password) {
        this.oldCredentials = password;
        return this;
    }

    @Override
    public char[] getRemoteCredentials() {
        return remoteCredentials;
    }

    @Override
    public AbstractNutsUpdateUserCommand remoteCredentials(char[] password) {
        return setRemoteCredentials(password);
    }

    @Override
    public AbstractNutsUpdateUserCommand setRemoteCredentials(char[] password) {
        this.remoteCredentials = password;
        return this;
    }

    @Override
    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    @Override
    public AbstractNutsUpdateUserCommand remoteIdentity(String remoteIdentity) {
        return setRemoteIdentity(remoteIdentity);
    }

    @Override
    public AbstractNutsUpdateUserCommand setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand addPermissions(String... permissions) {
        if (permissions != null) {
            return addPermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand removePermissions(String... permissions) {
        if (permissions != null) {
            return removePermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand undoAddPermissions(String... permissions) {
        if (permissions != null) {
            return undoAddPermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand undoRemovePermissions(String... permissions) {
        if (permissions != null) {
            return undoRemovePermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand addPermissions(Collection<String> permissions) {
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
    public AbstractNutsUpdateUserCommand removePermissions(Collection<String> permissions) {
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
    public AbstractNutsUpdateUserCommand undoAddPermissions(Collection<String> permissions) {
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
    public AbstractNutsUpdateUserCommand undoRemovePermissions(Collection<String> permissions) {
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
    public AbstractNutsUpdateUserCommand addGroups(String... groups) {
        if (groups != null) {
            return addGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand undoAddGroups(String... groups) {
        if (groups != null) {
            return undoAddGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand removeGroups(String... groups) {
        if (groups != null) {
            return removePermissions(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand undoRemoveGroups(String... groups) {
        if (groups != null) {
            return undoRemoveGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand addGroup(String group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand removeGroup(String group) {
        if (group != null) {
            this.rm_groups.add(group);
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand addPermission(String permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand undoAddPermission(String permissions) {
        if (permissions != null) {
            this.permissions.remove(permissions);
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand undoAddGroup(String group) {
        if (group != null) {
            this.groups.remove(group);
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand removePermission(String permission) {
        if (permission != null) {
            this.rm_permissions.add(permission);
        }
        return this;
    }

    @Override
    public AbstractNutsUpdateUserCommand addGroups(Collection<String> groups) {
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
    public AbstractNutsUpdateUserCommand removeGroups(Collection<String> groups) {
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
    public AbstractNutsUpdateUserCommand undoAddGroups(Collection<String> groups) {
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
    public AbstractNutsUpdateUserCommand undoRemoveGroups(Collection<String> groups) {
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
    public String[] getAddPermissions() {
        return permissions.toArray(new String[0]);
    }

    @Override
    public String[] getAddGroups() {
        return groups.toArray(new String[0]);
    }

    @Override
    public String[] getRemovePermissions() {
        return rm_permissions.toArray(new String[0]);
    }

    @Override
    public String[] getRemoveGroups() {
        return rm_groups.toArray(new String[0]);
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public NutsUpdateUserCommand username(String login) {
        return setUsername(login);
    }

    @Override
    public NutsUpdateUserCommand setUsername(String login) {
        this.login = login;
        return this;
    }

    @Override
    public boolean isResetPermissions() {
        return resetPermissions;
    }

    @Override
    public NutsUpdateUserCommand resetPermissions() {
        return resetPermissions(true);
    }

    @Override
    public NutsUpdateUserCommand resetPermissions(boolean resetPermissions) {
        return setResetPermissions(resetPermissions);
    }

    @Override
    public NutsUpdateUserCommand setResetPermissions(boolean resetPermissions) {
        this.resetPermissions = resetPermissions;
        return this;
    }

    @Override
    public boolean isResetGroups() {
        return resetGroups;
    }

    @Override
    public NutsUpdateUserCommand resetGroups() {
        return resetGroups(true);
    }

    @Override
    public NutsUpdateUserCommand resetGroups(boolean resetGroups) {
        return setResetGroups(resetGroups);
    }

    @Override
    public NutsUpdateUserCommand setResetGroups(boolean resetGroups) {
        this.resetGroups = resetGroups;
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
