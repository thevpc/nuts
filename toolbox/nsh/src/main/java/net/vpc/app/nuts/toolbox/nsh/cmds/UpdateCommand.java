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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsConfirmAction;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceUpdateOptions;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.app.options.NutsIdNonOption;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.ValueNonOption;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
        Argument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAllOnce("--force", "-f")) {
                force = NutsConfirmAction.FORCE;
            } else if (cmdLine.readAllOnce("--set-version", "-v")) {
                force = NutsConfirmAction.FORCE;
                version = cmdLine.readRequiredNonOption(new ValueNonOption("Version")).getStringExpression();
            } else {
                String id = cmdLine.readRequiredNonOption(new NutsIdNonOption("NutsId", context.getWorkspace())).getStringExpression();
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            if (cmdLine.isExecMode()) {
                //should update nuts
                if(context.getWorkspace().checkWorkspaceUpdates(
                        new NutsWorkspaceUpdateOptions()
                        .setEnableMajorUpdates(force==NutsConfirmAction.FORCE)
                        .setForceBootAPIVersion(version)
                        .setLogUpdates(true)
                        .setUpdateExtensions(true)
                        .setApplyUpdates(true)
                        , context.getSession()).length==0){
                    context.out().printf("workspace **upto-date**\n");
                }
            }
        } else {
            for (String id : ids) {
                update(id, force, context);
            }
        }
        return 0;
    }

    private void update(String id, NutsConfirmAction uptoDateAction, NutsCommandContext context) throws IOException {
        NutsWorkspace ws = context.getWorkspace();
        NutsDefinition file = ws.update(id, uptoDateAction, context.getSession());
        if (file.isCached()) {
            context.out().printf("%s **already installed**\n", file.getId());
        } else {
            context.out().printf("%s ##installed successfully##\n", file.getId());
        }
    }
}
