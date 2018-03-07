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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.extensions.cmd.cmdline.RepositoryNonOption;
import net.vpc.common.commandline.DefaultNonOption;

/**
 * Created by vpc on 1/7/17.
 */
public class PushCommand extends AbstractNutsCommand {

    public PushCommand() {
        super("push", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        String repo = null;
        cmdLine.requireNonEmpty();
        boolean force = false;
        while (!cmdLine.isEmpty()) {
            if (cmdLine.readOnce("--repo", "-r")) {
                repo = cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            } else if (cmdLine.readOnce("--force", "-f")) {
                force = true;
            } else {
                String id = cmdLine.readNonOptionOrError(new DefaultNonOption("NewNutsId")).toString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().push(id, repo, force, context.getSession());
                    context.getTerminal().getOut().printf("%s pushed successfully\n", id);
                }
            }
        }
        return 0;
    }
}
