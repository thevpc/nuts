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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.options.NutsIdNonOption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.toolbox.nsh.options.ValueNonOption;

/**
 * Created by vpc on 1/7/17.
 */
public class UpdateCommand extends AbstractNutsCommand {

    public UpdateCommand() {
        super("update", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        NutsConfirmAction force = NutsConfirmAction.IGNORE;
        String version = null;
        List<String> ids = new ArrayList<>();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.readOnce("--force", "-f")) {
                force = NutsConfirmAction.FORCE;
            } else if (cmdLine.readOnce("--version", "-v")) {
                force = NutsConfirmAction.FORCE;
                version = cmdLine.readNonOptionOrError(new ValueNonOption("Version", context)).getString();
            } else {
                String id = cmdLine.readNonOptionOrError(new NutsIdNonOption("NutsId", context)).getString();
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            if (cmdLine.isExecMode()) {
                //should update nuts
                updateWorkspace(version, force, context);
            }
        } else {
            for (String id : ids) {
                update(id, force, context);
            }
        }
        return 0;
    }

    private void update(String id, NutsConfirmAction uptoDateAction, NutsCommandContext context) throws IOException {
        NutsWorkspace ws = context.getValidWorkspace();
        NutsFile file = ws.update(id, uptoDateAction, context.getSession());
        NutsPrintStream out = context.getTerminal().getFormattedOut();
        if (file.isCached()) {
            out.printf("%s **already installed**\n", file.getId());
        } else {
            out.printf("%s ##installed successfully##\n", file.getId());
        }
    }

    private void updateWorkspace(String version, NutsConfirmAction foundAction, NutsCommandContext context) throws IOException {
        NutsWorkspace ws = context.getValidWorkspace();
        NutsFile file = ws.updateWorkspace(version, foundAction, context.getSession());
        NutsPrintStream out = context.getTerminal().getFormattedOut();
        if (file.isCached()) {
            out.printf("%s **already installed**\n", file.getId());
        } else {
            out.printf("%s ##installed successfully##\n", file.getId());
        }
    }
}
