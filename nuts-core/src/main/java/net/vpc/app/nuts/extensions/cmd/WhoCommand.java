/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import java.util.Arrays;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Created by vpc on 1/7/17.
 */
public class WhoCommand extends AbstractNutsCommand {

    public WhoCommand() {
        super("who", CORE_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        boolean verbose = false;
        boolean argVisitedVerbose = false;
        boolean argAll = false;
        boolean argAllRights = false;
        while (!cmdLine.isEmpty()) {
            if (!argVisitedVerbose && cmdLine.read("--verbose", "-v")) {
                argVisitedVerbose = true;
                verbose = true;
            } else if (!argAllRights && cmdLine.read("--all", "-a")) {
                argAllRights = true;
                argAll = true;
            } else {
                cmdLine.requireEmpty();
            }
        }
        if (!cmdLine.isExecMode()) {
            return -1;
        }
        NutsWorkspace validWorkspace = context.getValidWorkspace();
        String login = validWorkspace.getCurrentLogin();
        NutsPrintStream out = context.getTerminal().getOut();

        out.println(login);

        if (argAll) {
            NutsUserInfo user = validWorkspace.findUser(login);
            Set<String> groups = new TreeSet<>(Arrays.asList(user.getGroups()));
            Set<String> rights = new TreeSet<>(Arrays.asList(user.getRights()));
            Set<String> inherited = new TreeSet<>(Arrays.asList(user.getInheritedRights()));
            String[] currentLoginStack = validWorkspace.getCurrentLoginStack();
            out.draw("===stack===      :");
            for (String log : currentLoginStack) {
                out.draw(" <<" + log + ">>");
            }
            out.println();
            if (!groups.isEmpty()) {
                out.drawln("===identities=== : " + groups.toString());
            }
            if (!NutsConstants.USER_ADMIN.equals(login)) {
                if (!rights.isEmpty()) {
                    out.drawln("===rights===     : " + rights.toString());
                }
                if (!inherited.isEmpty()) {
                    out.drawln("===inherited===  : " + (inherited.isEmpty() ? "NONE" : inherited.toString()));
                }
            } else {
                out.drawln("===rights===     : ALL");
            }
            if (user.getMappedUser() != null) {
                out.drawln("===remote-id===  : " + (user.getMappedUser() == null ? "NONE" : user.getMappedUser()));
            }
            for (NutsRepository repository : context.getWorkspace().getRepositories()) {
                NutsUserInfo ruser = repository.findUser(login);
                if (ruser != null && (ruser.getGroups().length > 0
                        || ruser.getRights().length > 0
                        || !CoreStringUtils.isEmpty(ruser.getMappedUser()))) {
                    out.drawln("[ [[" + repository.getRepositoryId() + "]] ]: ");
                    Set<String> rgroups = new TreeSet<>(Arrays.asList(ruser.getGroups()));
                    Set<String> rrights = new TreeSet<>(Arrays.asList(ruser.getRights()));
                    Set<String> rinherited = new TreeSet<>(Arrays.asList(ruser.getInheritedRights()));
                    if (!rgroups.isEmpty()) {
                        out.drawln("    ===identities=== : " + rgroups.toString());
                    }
                    if (!NutsConstants.USER_ADMIN.equals(login)) {
                        if (!rrights.isEmpty()) {
                            out.drawln("    ===rights===     : " + rrights.toString());
                        }
                        if (!rinherited.isEmpty()) {
                            out.drawln("    ===inherited===  : " + rinherited.toString());
                        }
                    } else {
                        out.drawln("    ===rights===     : ALL");
                    }
                    if (ruser.getMappedUser() != null) {
                        out.drawln("    ===remote-id===  : " + ruser.getMappedUser());
                    }
                }
            }
        }

        return 0;
    }
}
