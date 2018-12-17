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

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsEffectiveUser;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by vpc on 1/7/17.
 */
public class WhoCommand extends AbstractNutsCommand {

    public WhoCommand() {
        super("who", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        boolean argAll = false;
        Argument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAllOnce("--all", "-a")) {
                argAll = true;
            } else {
                cmdLine.unexpectedArgument(getName());
            }
        }
        if (!cmdLine.isExecMode()) {
            return -1;
        }
        NutsWorkspace validWorkspace = context.getWorkspace();
        String login = validWorkspace.getSecurityManager().getCurrentLogin();
        PrintStream out = context.getTerminal().getFormattedOut();

        out.printf("%s\n", login);

        if (argAll) {
            NutsEffectiveUser user = validWorkspace.getSecurityManager().findUser(login);
            Set<String> groups = new TreeSet<>(Arrays.asList(user.getGroups()));
            Set<String> rights = new TreeSet<>(Arrays.asList(user.getRights()));
            Set<String> inherited = new TreeSet<>(Arrays.asList(user.getInheritedRights()));
            String[] currentLoginStack = validWorkspace.getSecurityManager().getCurrentLoginStack();
            if (currentLoginStack.length > 1) {
                out.print("===stack===      :");
                for (String log : currentLoginStack) {
                    out.print(" [[" + log + "]]");
                }
                out.println();
            }
            if (!groups.isEmpty()) {
                out.printf("===identities=== : %s\n", groups.toString());
            }
            if (!NutsConstants.USER_ADMIN.equals(login)) {
                if (!rights.isEmpty()) {
                    out.printf("===rights===     : %s\n", rights.toString());
                }
                if (!inherited.isEmpty()) {
                    out.printf("===inherited===  : %s\n", (inherited.isEmpty() ? "NONE" : inherited.toString()));
                }
            } else {
                out.printf("===rights===     : ALL\n");
            }
            if (user.getMappedUser() != null) {
                out.printf("===remote-id===  : %s\n", (user.getMappedUser() == null ? "NONE" : user.getMappedUser()));
            }
            for (NutsRepository repository : context.getWorkspace().getRepositoryManager().getRepositories()) {
                NutsEffectiveUser ruser = repository.getSecurityManager().getEffectiveUser(login);
                if (ruser != null && (ruser.getGroups().length > 0
                        || ruser.getRights().length > 0
                        || !StringUtils.isEmpty(ruser.getMappedUser()))) {
                    out.printf("[ [[%s]] ]: \n", repository.getRepositoryId());
                    Set<String> rgroups = new TreeSet<>(Arrays.asList(ruser.getGroups()));
                    Set<String> rrights = new TreeSet<>(Arrays.asList(ruser.getRights()));
                    Set<String> rinherited = new TreeSet<>(Arrays.asList(ruser.getInheritedRights()));
                    if (!rgroups.isEmpty()) {
                        out.printf("    ===identities=== : %s\n", rgroups.toString());
                    }
                    if (!NutsConstants.USER_ADMIN.equals(login)) {
                        if (!rrights.isEmpty()) {
                            out.printf("    ===rights===     : %s\n", rrights.toString());
                        }
                        if (!rinherited.isEmpty()) {
                            out.printf("    ===inherited===  : %s\n", rinherited.toString());
                        }
                    } else {
                        out.printf("    ===rights===     : ALL\n");
                    }
                    if (ruser.getMappedUser() != null) {
                        out.printf("    ===remote-id===  : %s\n", ruser.getMappedUser());
                    }
                }
            }
        }

        return 0;
    }
}
