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

import net.vpc.app.nuts.RootFolderType;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.io.IOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 * ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class NutsAdminCommand extends AbstractNutsCommand {

    public NutsAdminCommand() {
        super("nuts-admin", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        cmdLine.requireNonEmpty();
        if (cmdLine.read("update-index")) {
            List<String> repos = new ArrayList<>();
            while (!cmdLine.isEmpty()) {
                repos.add(cmdLine.readValue());
            }
            if (repos.isEmpty()) {
                context.getFormattedOut().printf("[[%s]] Updating all indices\n",context.getValidWorkspace().getConfigManager().getWorkspaceLocation());
                context.getValidWorkspace().updateAllIndices();
            } else {
                for (String repo : repos) {
                    context.getFormattedOut().printf("[[%s]] Updating index %s\n",context.getValidWorkspace().getConfigManager().getWorkspaceLocation(),repo);
                    context.getValidWorkspace().updateIndex(repo);
                }
            }
        }else if (cmdLine.read("delete-all-logs")) {
            File file = new File(context.getValidWorkspace().getStoreRoot(RootFolderType.LOGS));
            context.getFormattedOut().printf("@@Deleting@@ %s ...\n",file.getPath());
            IOUtils.delete(file);
            file = new File(context.getValidWorkspace().getConfigManager().getNutsHomeLocation(),"log");
            context.getFormattedOut().printf("@@Deleting@@ %s ...\n",file.getPath());
            IOUtils.delete(file);
        }else if (cmdLine.read("delete-all-vars")) {
            File file = new File(context.getValidWorkspace().getStoreRoot(RootFolderType.VAR));
            context.getFormattedOut().printf("@@Deleting@@ %s ...\n",file.getPath());
            IOUtils.delete(file);
        }else if (cmdLine.read("delete-all-programs")) {
            File file = new File(context.getValidWorkspace().getStoreRoot(RootFolderType.PROGRAMS));
            context.getFormattedOut().printf("@@Deleting@@ %s ...\n",file.getPath());
            IOUtils.delete(file);
        }else if (cmdLine.read("delete-all-configs")) {
            File file = new File(context.getValidWorkspace().getStoreRoot(RootFolderType.CONFIG));
            context.getFormattedOut().printf("@@Deleting@@ %s ...\n",file.getPath());
            IOUtils.delete(file);
        }
        cmdLine.requireEmpty();
        return 0;
    }
}
