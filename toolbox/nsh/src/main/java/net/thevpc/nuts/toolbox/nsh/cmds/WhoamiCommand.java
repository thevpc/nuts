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
 * <br>
 *
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
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;

import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.bundles._StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
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
            NutsSession session = context.getSession();
            String login = validWorkspace.security().getCurrentUsername();
            result.login = login;
            if (config.argAll) {
                NutsUser user = validWorkspace.security().findUser(login);
                Set<String> groups = new TreeSet<>(Arrays.asList(user.getGroups()));
                Set<String> rights = new TreeSet<>(Arrays.asList(user.getPermissions()));
                Set<String> inherited = new TreeSet<>(Arrays.asList(user.getInheritedPermissions()));
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
                if (user.getRemoteIdentity() != null) {
                    result.remoteId = user.getRemoteIdentity();
                }
                List<RepoResult> rr = new ArrayList<>();
                for (NutsRepository repository : context.getWorkspace().repos().getRepositories()) {
                    NutsUser ruser = repository.security().getEffectiveUser(login);
                    if (ruser != null && (ruser.getGroups().length > 0
                            || ruser.getPermissions().length > 0
                            || !_StringUtils.isBlank(ruser.getRemoteIdentity()))) {
                        RepoResult rt = new RepoResult();
                        rr.add(rt);
                        rt.name = repository.getName();
                        Set<String> rgroups = new TreeSet<>(Arrays.asList(ruser.getGroups()));
                        Set<String> rrights = new TreeSet<>(Arrays.asList(ruser.getPermissions()));
                        Set<String> rinherited = new TreeSet<>(Arrays.asList(ruser.getInheritedPermissions()));
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
                        if (ruser.getRemoteIdentity() != null) {
                            rt.remoteId = ruser.getRemoteIdentity();
                        }
                    }
                }
                result.repos = rr.isEmpty() ? null : rr.toArray(new RepoResult[0]);
            }
        }
        context.setPrintlnOutObject(result);
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context, NutsSession session) {
        Options options = context.getOptions();
        Result result = context.getResult();
        context.out().printf("%s\n", result.login);
        if (options.nutsUser) {
            NutsTextManager factory = session.getWorkspace().text();
            if (result.loginStack != null) {
                context.out().printf("%s      :",
                        factory.forStyled("stack",NutsTextStyle.primary5())
                        );
                for (String log : result.loginStack) {
                    context.out().printf(" %s",
                            factory.forStyled(log,NutsTextStyle.primary3())
                            );
                }
                context.out().println();
            }
            if (result.groups != null && result.groups.length > 0) {
                context.out().printf("%s : %s\n",
                        factory.forStyled("identities",NutsTextStyle.primary5()),
                        Arrays.toString(result.groups));
            }
            if (result.rights != null && result.rights.length > 0) {
                context.out().printf("%s     : %s\n",
                        factory.forStyled("rights",NutsTextStyle.primary5()),
                        Arrays.toString(result.rights));
            }
            if (result.inherited != null && result.inherited.length > 0) {
                context.out().printf("%s  : %s\n",
                        factory.forStyled("inherited",NutsTextStyle.primary5()),
                        Arrays.toString(result.inherited));
            } else {
                context.out().printf("%s  : %s\n",
                        factory.forStyled("inherited",NutsTextStyle.primary5()),
                        "NONE");
            }
            if (result.remoteId != null) {
                context.out().printf("%s  : %s\n",
                        factory.forStyled("remote-id",NutsTextStyle.primary5()),
                        result.remoteId);
            }
            if (result.repos != null) {
                for (RepoResult repo : result.repos) {
                    context.out().printf(
                            "[ %s ]: \n",
                            factory.forStyled(repo.name,NutsTextStyle.primary4())
                    );
                    if (repo.identities.length > 0) {
                        context.out().printf("    %s : %s\n",
                                factory.forStyled("identities",NutsTextStyle.primary5()),
                                Arrays.toString(repo.identities));
                    }
                    if (result.rights != null && repo.rights.length > 0) {
                        context.out().printf("    %s     : %s\n",
                                factory.forStyled("rights",NutsTextStyle.primary5()),
                                Arrays.toString(repo.rights));
                    }
                    if (repo.inherited != null && repo.inherited.length > 0) {
                        context.out().printf("    %s  : %s\n",
                                factory.forStyled("inherited",NutsTextStyle.primary5()),
                                Arrays.toString(repo.inherited));
                    }
                    if (repo.remoteId != null) {
                        context.out().printf("    %s  : %s\n",
                                factory.forStyled("remote-id",NutsTextStyle.primary5()),
                                repo.remoteId);
                    }
                }
            }
        }
    }

}
