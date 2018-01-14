/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by vpc on 1/7/17.
 */
public class WhoCommand extends AbstractNutsCommand {

    public WhoCommand() {
        super("who", CORE_SUPPORT);
    }

    public int run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        boolean verbose = false;
        boolean argVisitedVerbose = false;
        boolean argAll = false;
        boolean argAllRights = false;
        while (!cmdLine.isEmpty()) {
            if (!argVisitedVerbose && cmdLine.acceptAndRemove("--verbose", "-v")) {
                argVisitedVerbose = true;
                verbose = true;
            } else if (!argAllRights && cmdLine.acceptAndRemove("--all", "-a")) {
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
        if (CoreStringUtils.isEmpty(login)) {
            out.println(NutsConstants.USER_ANONYMOUS);
        } else {
            out.println(login);
            Set<String> visitedGroups = new HashSet<>();
            Set<String> visitedRights = new HashSet<>();
            if (verbose) {
                Stack<String> items = new Stack<>();
                visitedGroups.add(login);
                items.push(login);
                NutsWorkspaceConfig c = validWorkspace.getConfig();
                while (items.isEmpty()) {
                    String n = items.pop();
                    NutsSecurityEntityConfig s = c.getSecurity(n);
                    if (s != null) {
                        for (String r : s.getRights()) {
                            if (!visitedRights.contains(r)) {
                                visitedRights.add(r);
                            }
                        }
                        for (String g : s.getGroups()) {
                            if (!visitedGroups.contains(g)) {
                                visitedGroups.add(g);
                                items.push(g);
                            }
                        }
                    }
                }
            }
            if (argAll) {
                if (!visitedGroups.isEmpty()) {
                    out.println("identities : " + (visitedGroups.isEmpty() ? "NONE" : visitedGroups.toString()));
                } else {
                    out.println("identities : NONE");
                }
                if (!NutsConstants.USER_ADMIN.equals(login)) {
                    out.println("rights     : " + (visitedRights.isEmpty() ? "NONE" : visitedRights.toString()));
                } else {
                    out.println("rights     : ALL");
                }
            }
        }
        return 0;
    }
}
