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
import net.vpc.app.nuts.extensions.repos.NutsFolderRepository;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 * ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class NutsAdminCommand extends AbstractNutsCommand {

    public NutsAdminCommand() {
        super("nutsadmin", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        CpCommand.Options o = new CpCommand.Options();
        boolean reindex = false;
        List<String> repos = new ArrayList<>();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.isOption()) {
                if (cmdLine.isOption(null, "reindex")) {
                    cmdLine.read();
                    reindex = true;
                    while (!cmdLine.isEmpty()) {
                        repos.add(cmdLine.readValue());
                    }
                }else{
                    cmdLine.read();
                }
            }else{
                cmdLine.requireEmpty();
            }
        }
        if (reindex) {
            if (repos.isEmpty()) {
                for (NutsRepository nutsRepository : context.getWorkspace().getRepositoryManager().getRepositories()) {
                    if (nutsRepository instanceof NutsFolderRepository) {
                        ((NutsFolderRepository) nutsRepository).reindexFolder();
                    }
                }
            } else {
                for (String repo : repos) {
                    if(repo.contains("/") || repo.contains("\\")){
                        NutsFolderRepository r=new NutsFolderRepository(
                                "temp",
                                repo,
                                context.getWorkspace(),
                                null
                        );
                        r.getConfigManager().setComponentsLocation(".");
                        ((NutsFolderRepository) r).reindexFolder();
                    }else{
                        NutsRepository r = context.getWorkspace().getRepositoryManager().findRepository(repo);
                        if(r!=null){
                            ((NutsFolderRepository) r).reindexFolder();
                        }
                    }
                }
            }
        }
        return 0;
    }
}
