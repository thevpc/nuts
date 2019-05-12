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
import net.vpc.common.strings.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

/**
 * Created by vpc on 1/7/17.
 */
public class WhoCommand extends AbstractNutsCommand {

    public WhoCommand() {
        super("who", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandLine cmdLine = cmdLine(args, context);
        boolean argAll = false;
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAllOnce("--all", "-a")) {
                argAll = true;
            } else {
                cmdLine.setCommandName(getName()).unexpectedArgument();
            }
        }
        if (!cmdLine.isExecMode()) {
            return -1;
        }
        NutsWorkspace validWorkspace = context.getWorkspace();
        String login = validWorkspace.security().getCurrentLogin();

        context.out().printf("%s\n", login);

        if (argAll) {
            NutsEffectiveUser user = validWorkspace.security().findUser(login);
            Set<String> groups = new TreeSet<>(Arrays.asList(user.getGroups()));
            Set<String> rights = new TreeSet<>(Arrays.asList(user.getRights()));
            Set<String> inherited = new TreeSet<>(Arrays.asList(user.getInheritedRights()));
            String[] currentLoginStack = validWorkspace.security().getCurrentLoginStack();
            if (currentLoginStack.length > 1) {
                context.out().print("===stack===      :");
                for (String log : currentLoginStack) {
                    context.out().print(" [[" + log + "]]");
                }
                context.out().println();
            }
            if (!groups.isEmpty()) {
                context.out().printf("===identities=== : %s\n", groups.toString());
            }
            if (!NutsConstants.Names.USER_ADMIN.equals(login)) {
                if (!rights.isEmpty()) {
                    context.out().printf("===rights===     : %s\n", rights.toString());
                }
                if (!inherited.isEmpty()) {
                    context.out().printf("===inherited===  : %s\n", (inherited.isEmpty() ? "NONE" : inherited.toString()));
                }
            } else {
                context.out().printf("===rights===     : ALL\n");
            }
            if (user.getMappedUser() != null) {
                context.out().printf("===remote-id===  : %s\n", (user.getMappedUser() == null ? "NONE" : user.getMappedUser()));
            }
            for (NutsRepository repository : context.getWorkspace().config().getRepositories()) {
                NutsEffectiveUser ruser = repository.security().getEffectiveUser(login);
                if (ruser != null && (ruser.getGroups().length > 0
                        || ruser.getRights().length > 0
                        || !StringUtils.isEmpty(ruser.getMappedUser()))) {
                    context.out().printf("[ [[%s]] ]: \n", repository.config().getName());
                    Set<String> rgroups = new TreeSet<>(Arrays.asList(ruser.getGroups()));
                    Set<String> rrights = new TreeSet<>(Arrays.asList(ruser.getRights()));
                    Set<String> rinherited = new TreeSet<>(Arrays.asList(ruser.getInheritedRights()));
                    if (!rgroups.isEmpty()) {
                        context.out().printf("    ===identities=== : %s\n", rgroups.toString());
                    }
                    if (!NutsConstants.Names.USER_ADMIN.equals(login)) {
                        if (!rrights.isEmpty()) {
                            context.out().printf("    ===rights===     : %s\n", rrights.toString());
                        }
                        if (!rinherited.isEmpty()) {
                            context.out().printf("    ===inherited===  : %s\n", rinherited.toString());
                        }
                    } else {
                        context.out().printf("    ===rights===     : ALL\n");
                    }
                    if (ruser.getMappedUser() != null) {
                        context.out().printf("    ===remote-id===  : %s\n", ruser.getMappedUser());
                    }
                }
            }
        }

        return 0;
    }
}
