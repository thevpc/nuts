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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.strings.StringUtils;
import net.vpc.app.nuts.NutsArgument;

/**
 * Created by vpc on 1/7/17.
 */
public class LoginCommand extends AbstractNshBuiltin {

    public LoginCommand() {
        super("login", DEFAULT_SUPPORT);
    }

    public void exec(String[] args, NutsCommandContext context) {
        NutsCommand cmdLine = cmdLine(args, context);
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //
            } else {
                String login = cmdLine.required().nextNonOption(cmdLine.createNonOption("username")).getString();
                char[] password = cmdLine.nextNonOption(cmdLine.createNonOption("password")).getString().toCharArray();
                cmdLine.setCommandName(getName()).unexpectedArgument();
                if (cmdLine.isExecMode()) {
                    if (!NutsConstants.Users.ANONYMOUS.equals(login) && StringUtils.isBlank(new String(password))) {
                        password = context.getTerminal().readPassword("Password:");
                    }
                    context.getWorkspace().security().login(login, password);
                }
            }
        }
    }
}
