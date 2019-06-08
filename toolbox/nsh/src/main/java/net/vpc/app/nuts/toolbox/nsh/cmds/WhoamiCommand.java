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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsEffectiveUser;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.strings.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class WhoamiCommand extends SimpleNshBuiltin {

    public WhoamiCommand() {
        super("whoami", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean argAll = false;
        boolean nutsUser = false;
    }

    private static class RepoResult {

        private String name;
        private String[] identities;
        private String[] rights;
        private String[] inherited;
        private String remoteId;

    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    private static class Result {

        private String login;
        private String[] loginStack;
        private String[] groups;
        private String[] rights;
        private String[] inherited;
        private String remoteId;
        private RepoResult[] repos;
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        switch (commandLine.peek().getStringKey()) {
            case "--all":
            case "-a": {
                config.argAll = true;
                config.nutsUser = true;
                commandLine.skip();
                return true;
            }
            case "--nuts":
            case "-n": {
                config.nutsUser = true;
                commandLine.skip();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Result result = new Result();
        Options config = context.getOptions();
        if (!config.nutsUser) {
            result.login = System.getProperty("user.name");
        } else {
            NutsWorkspace validWorkspace = context.getWorkspace();
            String login = validWorkspace.security().getCurrentLogin();
            result.login = login;
            if (config.argAll) {
                NutsEffectiveUser user = validWorkspace.security().findUser(login);
                Set<String> groups = new TreeSet<>(Arrays.asList(user.getGroups()));
                Set<String> rights = new TreeSet<>(Arrays.asList(user.getRights()));
                Set<String> inherited = new TreeSet<>(Arrays.asList(user.getInheritedRights()));
                result.loginStack = validWorkspace.security().getCurrentLoginStack();
                if (result.loginStack.length <= 1) {
                    result.loginStack = null;
                }
                result.groups = groups.toArray(new String[0]);
                if (result.groups.length == 0) {
                    result.groups = null;
                }
                if (!NutsConstants.Users.ADMIN.equals(login)) {
                    if (!rights.isEmpty()) {
                        result.rights = rights.toArray(new String[0]);
                        if (result.rights.length == 0) {
                            result.rights = null;
                        }
                    }
                    result.inherited = inherited.toArray(new String[0]);
                    if (result.inherited.length == 0) {
                        result.inherited = null;
                    }
                } else {
                    result.rights = new String[]{"ALL"};
                }
                if (user.getMappedUser() != null) {
                    result.remoteId = user.getMappedUser();
                }
                List<RepoResult> rr = new ArrayList<>();
                for (NutsRepository repository : context.getWorkspace().config().getRepositories()) {
                    NutsEffectiveUser ruser = repository.security().getEffectiveUser(login);
                    if (ruser != null && (ruser.getGroups().length > 0
                            || ruser.getRights().length > 0
                            || !StringUtils.isBlank(ruser.getMappedUser()))) {
                        RepoResult rt = new RepoResult();
                        rr.add(rt);
                        rt.name = repository.config().getName();
                        Set<String> rgroups = new TreeSet<>(Arrays.asList(ruser.getGroups()));
                        Set<String> rrights = new TreeSet<>(Arrays.asList(ruser.getRights()));
                        Set<String> rinherited = new TreeSet<>(Arrays.asList(ruser.getInheritedRights()));
                        if (!rgroups.isEmpty()) {
                            rt.identities = rgroups.toArray(new String[0]);
                        }
                        if (!NutsConstants.Users.ADMIN.equals(login)) {
                            if (!rrights.isEmpty()) {
                                rt.rights = rrights.toArray(new String[0]);
                            }
                            if (!rinherited.isEmpty()) {
                                rt.inherited = rinherited.toArray(new String[0]);
                            }
                        } else {
                            rt.rights = new String[]{"ALL"};
                        }
                        if (ruser.getMappedUser() != null) {
                            rt.remoteId = ruser.getMappedUser();
                        }
                    }
                }
                result.repos = rr.isEmpty() ? null : rr.toArray(new RepoResult[0]);
            }
        }
        context.setPrintlnOutObject(result);
    }

    @Override
    protected void printObjectPlain(SimpleNshCommandContext context) {
        Result result = context.getResult();
        context.out().printf("%s\n", result.login);
        if (result.loginStack != null) {
            context.out().print("===stack===      :");
            for (String log : result.loginStack) {
                context.out().print(" [[" + log + "]]");
            }
            context.out().println();
        }
        if (result.groups != null && result.groups.length > 0) {
            context.out().printf("===identities=== : %s\n", Arrays.toString(result.groups));
        }
        if (result.rights != null && result.rights.length > 0) {
            context.out().printf("===rights===     : %s\n", Arrays.toString(result.rights));
        }
        if (result.inherited != null && result.inherited.length > 0) {
            context.out().printf("===inherited===  : %s\n", Arrays.toString(result.inherited));
        } else {
            context.out().printf("===inherited===  : %s\n", "NONE");
        }
        if (result.remoteId != null) {
            context.out().printf("===remote-id===  : %s\n", result.remoteId);
        }
        if (result.repos != null) {
            for (RepoResult repo : result.repos) {
                context.out().printf("[ [[%s]] ]: \n", repo.name);
                if (repo.identities.length > 0) {
                    context.out().printf("    ===identities=== : %s\n", Arrays.toString(repo.identities));
                }
                if (result.rights != null && repo.rights.length > 0) {
                    context.out().printf("    ===rights===     : %s\n", Arrays.toString(repo.rights));
                }
                if (repo.inherited != null && repo.inherited.length > 0) {
                    context.out().printf("    ===inherited===  : %s\n", Arrays.toString(repo.inherited));
                }
                if (repo.remoteId != null) {
                    context.out().printf("    ===remote-id===  : %s\n", repo.remoteId);
                }
            }
        }
    }

}
