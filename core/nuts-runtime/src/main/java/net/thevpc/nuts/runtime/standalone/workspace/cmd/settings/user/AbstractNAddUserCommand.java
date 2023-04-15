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
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBaseRepo;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.*;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public abstract class AbstractNAddUserCommand extends NWorkspaceCommandBaseRepo<NAddUserCommand> implements NAddUserCommand {

    protected String login;
    protected String remoteIdentity;
    protected char[] remoteCredentials;
    protected char[] password;
    protected final Set<String> permissions = new HashSet<>();
    protected final Set<String> groups = new HashSet<>();

    public AbstractNAddUserCommand(NSession session) {
        super(session, "add-user");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String getUsername() {
        return login;
    }


    @Override
    public AbstractNAddUserCommand setUsername(String username) {
        this.login = username;
        return this;
    }

    @Override
    public char[] getCredentials() {
        return password;
    }


    @Override
    public AbstractNAddUserCommand setCredentials(char[] password) {
        this.password = password;
        return this;
    }

    @Override
    public char[] getRemoteCredentials() {
        return remoteCredentials;
    }


    @Override
    public AbstractNAddUserCommand setRemoteCredentials(char[] password) {
        this.remoteCredentials = password;
        return this;
    }

    @Override
    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    @Override
    public AbstractNAddUserCommand setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
        return this;
    }

    @Override
    public AbstractNAddUserCommand addPermissions(String... permissions) {
        if (permissions != null) {
            return addPermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand removePermissions(String... permissions) {
        if (permissions != null) {
            return removePermissions(Arrays.asList(permissions));
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand addPermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (permission != null) {
                    this.permissions.add(permission);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand removePermissions(Collection<String> permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (permission != null) {
                    this.permissions.remove(permission);
                }
            }
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand addGroups(String... groups) {
        if (groups != null) {
            return addPermissions(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand removeGroups(String... groups) {
        if (groups != null) {
            return removeGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand addGroup(String group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand addPermission(String permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
        return this;
    }

    @Override
    public AbstractNAddUserCommand addGroups(Collection<String> groups) {
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
    public AbstractNAddUserCommand removeGroups(Collection<String> groups) {
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
    public List<String> getPermissions() {
        return new ArrayList<>(permissions);
    }

    @Override
    public List<String> getGroups() {
        return new ArrayList<>(groups);
    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get(session);
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
