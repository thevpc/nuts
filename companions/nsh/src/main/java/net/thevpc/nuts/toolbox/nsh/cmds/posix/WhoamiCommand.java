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
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NUser;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class WhoamiCommand extends NShellBuiltinDefault {

    public WhoamiCommand() {
        super("whoami", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options config = context.getOptions();
        NSession session = context.getSession();
        switch (cmdLine.peek().get().key()) {
            case "--all":
            case "-a": {
                config.argAll = true;
                config.nutsUser = true;
                cmdLine.skip();
                return true;
            }
            case "--nuts":
            case "-n": {
                config.nutsUser = true;
                cmdLine.skip();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Result result = new Result();
        Options options = context.getOptions();
        if (!options.nutsUser) {
            result.login = System.getProperty("user.name");
        } else {
            NSession session = context.getSession();
            String login = NWorkspaceSecurityManager.of().getCurrentUsername();
            result.login = login;
            if (options.argAll) {
                NUser user = NWorkspaceSecurityManager.of().findUser(login);
                Set<String> groups = new TreeSet<>((user.getGroups()));
                Set<String> rights = new TreeSet<>((user.getPermissions()));
                Set<String> inherited = new TreeSet<>((user.getInheritedPermissions()));
                result.loginStack = NWorkspaceSecurityManager.of().getCurrentLoginStack();
                if (result.loginStack.length <= 1) {
                    result.loginStack = null;
                }
                result.groups = groups.toArray(new String[0]);
                if (result.groups.length == 0) {
                    result.groups = null;
                }
                if (!NConstants.Users.ADMIN.equals(login)) {
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
                for (NRepository repository : NWorkspace.of().getRepositories()) {
                    NUser ruser = repository.security().getEffectiveUser(login);
                    if (ruser != null && (ruser.getGroups().size() > 0
                            || ruser.getPermissions().size() > 0
                            || !NBlankable.isBlank(ruser.getRemoteIdentity()))) {
                        RepoResult rt = new RepoResult();
                        rr.add(rt);
                        rt.name = repository.getName();
                        Set<String> rgroups = new TreeSet<>((ruser.getGroups()));
                        Set<String> rrights = new TreeSet<>((ruser.getPermissions()));
                        Set<String> rinherited = new TreeSet<>((ruser.getInheritedPermissions()));
                        if (!rgroups.isEmpty()) {
                            rt.identities = rgroups.toArray(new String[0]);
                        }
                        if (!NConstants.Users.ADMIN.equals(login)) {
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
        switch (context.getSession().getOutputFormat().orDefault()) {
            case PLAIN: {
                NPrintStream out = context.getSession().out();
                out.println(NMsg.ofC("%s", result.login));
                if (options.nutsUser) {
                    NTexts factory = NTexts.of();
                    if (result.loginStack != null) {
                        out.print(NMsg.ofC("%s      :",
                                factory.ofStyled("stack", NTextStyle.primary5())
                        ));
                        for (String log : result.loginStack) {
                            out.print(NMsg.ofC(" %s",
                                    factory.ofStyled(log, NTextStyle.primary3())
                            ));
                        }
                        out.println();
                    }
                    if (result.groups != null && result.groups.length > 0) {
                        out.println(NMsg.ofC("%s : %s",
                                factory.ofStyled("identities", NTextStyle.primary5()),
                                Arrays.toString(result.groups)));
                    }
                    if (result.rights != null && result.rights.length > 0) {
                        out.println(NMsg.ofC("%s     : %s",
                                factory.ofStyled("rights", NTextStyle.primary5()),
                                Arrays.toString(result.rights)));
                    }
                    if (result.inherited != null && result.inherited.length > 0) {
                        out.println(NMsg.ofC("%s  : %s",
                                factory.ofStyled("inherited", NTextStyle.primary5()),
                                Arrays.toString(result.inherited)));
                    } else {
                        out.println(NMsg.ofC("%s  : %s",
                                factory.ofStyled("inherited", NTextStyle.primary5()),
                                "NONE"));
                    }
                    if (result.remoteId != null) {
                        out.println(NMsg.ofC("%s  : %s",
                                factory.ofStyled("remote-id", NTextStyle.primary5()),
                                result.remoteId));
                    }
                    if (result.repos != null) {
                        for (RepoResult repo : result.repos) {
                            out.println(NMsg.ofC(
                                    "[ %s ]: ",
                                    factory.ofStyled(repo.name, NTextStyle.primary4())
                            ));
                            if (repo.identities.length > 0) {
                                out.println(NMsg.ofC("    %s : %s",
                                        factory.ofStyled("identities", NTextStyle.primary5()),
                                        Arrays.toString(repo.identities)));
                            }
                            if (result.rights != null && repo.rights.length > 0) {
                                out.println(NMsg.ofC("    %s     : %s",
                                        factory.ofStyled("rights", NTextStyle.primary5()),
                                        Arrays.toString(repo.rights)));
                            }
                            if (repo.inherited != null && repo.inherited.length > 0) {
                                out.println(NMsg.ofC("    %s  : %s",
                                        factory.ofStyled("inherited", NTextStyle.primary5()),
                                        Arrays.toString(repo.inherited)));
                            }
                            if (repo.remoteId != null) {
                                out.println(NMsg.ofC("    %s  : %s",
                                        factory.ofStyled("remote-id", NTextStyle.primary5()),
                                        repo.remoteId));
                            }
                        }
                    }
                }
                break;
            }
            default: {
                context.getSession().out().println(result);
            }
        }
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
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
