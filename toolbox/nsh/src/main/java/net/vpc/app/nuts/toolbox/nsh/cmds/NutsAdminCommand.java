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

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.RootFolderType;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.commandline.Argument;
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
        Argument a;
        boolean noColors=false;
        while(cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAll("update-index")) {
                List<String> repos = new ArrayList<>();
                while (cmdLine.hasNext()) {
                    repos.add(cmdLine.read().getExpression());
                }
                updateIndex(context, repos.toArray(new String[0]));
                cmdLine.unexpectedArgument(getName());
            } else if (cmdLine.readAll("delete-log")) {
                boolean force=false;
                while(cmdLine.hasNext()) {
                    if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                        force=a.getBooleanValue();
                    }else{
                        cmdLine.unexpectedArgument(getName());
                    }
                }
                deleteLog(context,force);
            } else if (cmdLine.readAll("delete-var")) {
                boolean force=false;
                while(cmdLine.hasNext()) {
                    if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                        force=a.getBooleanValue();
                    }else{
                        cmdLine.unexpectedArgument(getName());
                    }
                }
                deleteVar(context,force);
                cmdLine.unexpectedArgument(getName());
            } else if (cmdLine.readAll("delete-programs")) {
                boolean force=false;
                while(cmdLine.hasNext()) {
                    if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                        force=a.getBooleanValue();
                    }else{
                        cmdLine.unexpectedArgument(getName());
                    }
                }
                deletePrgrams(context,force);
                cmdLine.unexpectedArgument(getName());
            } else if (cmdLine.readAll("delete-config")) {
                boolean force=false;
                while(cmdLine.hasNext()) {
                    if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                        force=a.getBooleanValue();
                    }else{
                        cmdLine.unexpectedArgument(getName());
                    }
                }
                deleteConfig(context,force);
                cmdLine.unexpectedArgument(getName());
            } else if (cmdLine.readAll("delete-cache")) {
                boolean force=false;
                while(cmdLine.hasNext()) {
                    if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                        force=a.getBooleanValue();
                    }else{
                        cmdLine.unexpectedArgument(getName());
                    }
                }
                deleteCache(context,force);
                cmdLine.unexpectedArgument(getName());
            } else if (cmdLine.readAll("cleanup")) {
                boolean force=false;
                while(cmdLine.hasNext()) {
                    if ((a = cmdLine.readBooleanOption("-f","--force")) != null) {
                        force=a.getBooleanValue();
                    }else{
                        cmdLine.unexpectedArgument(getName());
                    }
                }
                deleteCache(context,force);
                deleteLog(context,force);
                cmdLine.unexpectedArgument(getName());
            } else {
                cmdLine.unexpectedArgument(getName());
            }
        }
        return 0;
    }

    private void updateIndex(NutsCommandContext context, String[] repos) {
        if (repos.length==0) {
            context.out().printf("[[%s]] Updating all indices\n", context.getWorkspace().getConfigManager().getWorkspaceLocation());
            context.getWorkspace().updateAllRepositoryIndices();
        } else {
            for (String repo : repos) {
                context.out().printf("[[%s]] Updating index %s\n", context.getWorkspace().getConfigManager().getWorkspaceLocation(), repo);
                context.getWorkspace().updateRepositoryIndex(repo);
            }
        }
    }

    private void deleteLog(NutsCommandContext context, boolean force) {
        File file = new File(context.getWorkspace().getStoreRoot(RootFolderType.LOGS));
        if(file.exists()) {
            context.out().printf("@@Deleting@@ ##log## folder %s ...\n", file.getPath());
            if(force ||ShellHelper.readAccept(context.getTerminal())){
                IOUtils.delete(file);
            }
        }
        file = new File(context.getWorkspace().getConfigManager().getHomeLocation(), "log");
        if(file.exists()) {
            context.out().printf("@@Deleting@@ ##log## folder %s ...\n", file.getPath());
            if (force || ShellHelper.readAccept(context.getTerminal())) {
                IOUtils.delete(file);
            }
        }
    }

    private void deleteVar(NutsCommandContext context, boolean force) {
        File file = new File(context.getWorkspace().getStoreRoot(RootFolderType.VAR));
        if(file.exists()) {
            context.out().printf("@@Deleting@@ ##var## folder %s ...\n", file.getPath());
            if (force || ShellHelper.readAccept(context.getTerminal())) {
                IOUtils.delete(file);
            }
        }
    }

    private void deletePrgrams(NutsCommandContext context, boolean force) {
        File file = new File(context.getWorkspace().getStoreRoot(RootFolderType.PROGRAMS));
        if(file.exists()) {
            context.out().printf("@@Deleting@@ ##programs## folder %s ...\n", file.getPath());
            if (force || ShellHelper.readAccept(context.getTerminal())) {
                IOUtils.delete(file);
            }
        }
    }

    private void deleteConfig(NutsCommandContext context, boolean force) {
        File file = new File(context.getWorkspace().getStoreRoot(RootFolderType.CONFIG));
        if(file.exists()) {
            context.out().printf("@@Deleting@@ ##config## folder %s ...\n", file.getPath());
            if (force || ShellHelper.readAccept(context.getTerminal())) {
                IOUtils.delete(file);
            }
        }
    }

    private void deleteCache(NutsCommandContext context, boolean force) {
        for (NutsRepository repository : context.getWorkspace().getRepositoryManager().getRepositories()) {
            deleteRepoCache(repository,context,force);
        }
    }

    private static void deleteRepoCache(NutsRepository repository, NutsCommandContext context, boolean force){
        String s = repository.getStoreRoot();
        if(s!=null){
            File file = new File(s);
            if(file.exists()) {
                context.out().printf("@@Deleting@@ ##cache## folder %s ...\n", s);
                if (force || ShellHelper.readAccept(context.getTerminal())) {
                    IOUtils.delete(file);
                }
            }
        }
        for (NutsRepository mirror : repository.getMirrors()) {
            deleteRepoCache(mirror,context,force);
        }
    }
}
