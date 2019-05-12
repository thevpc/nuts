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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsUserConfig;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsRepositoryConfigManagerExt;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsAddUserCommand;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public class DefaultNutsAddUserCommand extends NutsWorkspaceCommandBase<NutsAddUserCommand> implements NutsAddUserCommand {

    private String login;
    private String remoteIdentity;
    private String password;
    private final Set<String> rights = new HashSet<>();
    private final Set<String> groups = new HashSet<>();
    private NutsRepository repo;

    public DefaultNutsAddUserCommand(NutsWorkspace ws) {
        super(ws);
    }
    public DefaultNutsAddUserCommand(NutsRepository repo) {
        super(repo.getWorkspace());
        this.repo = repo;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public DefaultNutsAddUserCommand login(String login) {
        return setLogin(login);
    }

    @Override
    public DefaultNutsAddUserCommand setLogin(String login) {
        this.login = login;
        return this;
    }

    @Override
    public String getCredentials() {
        return password;
    }

    @Override
    public DefaultNutsAddUserCommand credentials(String password) {
        return setCredentials(password);
    }

    @Override
    public DefaultNutsAddUserCommand setCredentials(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String getRemoteIdentity() {
        return remoteIdentity;
    }

    @Override
    public DefaultNutsAddUserCommand remoteIdentity(String remoteIdentity) {
        return setRemoteIdentity(remoteIdentity);
    }

    @Override
    public DefaultNutsAddUserCommand setRemoteIdentity(String remoteIdentity) {
        this.remoteIdentity = remoteIdentity;
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand right(String right) {
        return addRight(right);
    }

    @Override
    public DefaultNutsAddUserCommand rights(String... rights) {
        return addRights(rights);
    }

    @Override
    public DefaultNutsAddUserCommand rights(Collection<String> rights) {
        return addRights(rights);
    }

    @Override
    public DefaultNutsAddUserCommand addRights(String... rights) {
        if (rights != null) {
            return addRights(Arrays.asList(rights));
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand removeRights(String... rights) {
        if (rights != null) {
            return removeRights(Arrays.asList(rights));
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand addRights(Collection<String> rights) {
        if (rights != null) {
            for (String right : rights) {
                if (right != null) {
                    this.rights.add(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand removeRights(Collection<String> rights) {
        if (rights != null) {
            for (String right : rights) {
                if (right != null) {
                    this.rights.remove(right);
                }
            }
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand group(String group) {
        return addGroup(group);
    }

    @Override
    public DefaultNutsAddUserCommand groups(String... groups) {
        return addGroups(groups);
    }

    @Override
    public DefaultNutsAddUserCommand groups(Collection<String> groups) {
        return addGroups(groups);
    }

    @Override
    public DefaultNutsAddUserCommand addGroups(String... groups) {
        if (groups != null) {
            return addRights(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand removeGroups(String... groups) {
        if (groups != null) {
            return removeGroups(Arrays.asList(groups));
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand addGroup(String group) {
        if (group != null) {
            this.groups.add(group);
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand addRight(String right) {
        if (right != null) {
            this.rights.add(right);
        }
        return this;
    }

    @Override
    public DefaultNutsAddUserCommand addGroups(Collection<String> groups) {
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
    public DefaultNutsAddUserCommand removeGroups(Collection<String> groups) {
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
    public String[] getRights() {
        return rights.toArray(new String[0]);
    }

    @Override
    public String[] getGroups() {
        return groups.toArray(new String[0]);
    }

    @Override
    public NutsAddUserCommand run() {
        if (CoreStringUtils.isBlank(getLogin())) {
            throw new NutsIllegalArgumentException("Invalid user");
        }
        if (repo != null) {
            NutsUserConfig security = new NutsUserConfig(getLogin(), repo.security().getAuthenticationAgent()
                    .setCredentials(getCredentials(), repo.config()), getGroups(), getRights());
            security.setMappedUser(remoteIdentity);
            NutsRepositoryConfigManagerExt.of(repo.config()).setUser(security);
        } else {
            NutsUserConfig security = new NutsUserConfig(getLogin(), ws.security().getAuthenticationAgent()
                    .setCredentials(getCredentials(), ws.config()), getGroups(), getRights());
            security.setMappedUser(remoteIdentity);
            NutsWorkspaceConfigManagerExt.of(ws.config()).setUser(security);
        }
        return this;
    }

    @Override
    public NutsAddUserCommand parseOptions(String... args) {
        NutsCommandLine cmd = ws.parser().parseCommandLine(args);
        NutsArgument a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                default: {
                    if (!super.parseOption(a, cmd)) {
                        if (a.isOption()) {
                            throw new NutsIllegalArgumentException("Unsupported option " + a);
                        } else {
                            //id(a.getString());
                        }
                    }
                }
            }
        }
        return this;
    }
}
