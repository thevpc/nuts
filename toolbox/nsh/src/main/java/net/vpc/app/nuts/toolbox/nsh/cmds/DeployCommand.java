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
import net.vpc.app.nuts.toolbox.nsh.NutsCommandSyntaxError;
import net.vpc.common.commandline.FileNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.NutsIdNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryNonOption;
import net.vpc.common.strings.StringUtils;

import java.io.File;

/**
 * Created by vpc on 1/7/17.
 */
public class DeployCommand extends AbstractNutsCommand {

    public DeployCommand() {
        super("deploy", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        boolean fileMode = false;
        boolean idMode = false;
        String to = null;
        String from = null;
        String id = null;
        String contentFile = null;
        String descriptorFile = null;
        while (!cmdLine.isEmpty()) {
            if (contentFile == null && id == null && cmdLine.readOnce("--file", "-f")) {
                fileMode = true;
            } else if (!idMode && cmdLine.readOnce("--desc", "-d")) {
                descriptorFile = cmdLine.readNonOption(new FileNonOption("DescriptorFile")).getString();
            } else if (cmdLine.readOnce("--id", "-i")) {
                idMode = true;
            } else if (!fileMode && cmdLine.readOnce("--source", "-s")) {
                from = cmdLine.readNonOption(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            } else if (cmdLine.readOnce("--to", "-t")) {
                to = cmdLine.readNonOption(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            } else {
                if (contentFile != null || id != null) {
                    cmdLine.requireEmpty();
                } else {
                    if (!idMode && !fileMode) {
                        if (cmdLine.isAutoCompleteMode()) {
                            return -1;
                        } else {
                            throw new NutsCommandSyntaxError("Missing --id or --file");
                        }
                    }
                    if (fileMode) {
                        contentFile = cmdLine.readNonOptionOrError(new FileNonOption("File")).getString();
                    } else if (idMode) {
                        id = cmdLine.readNonOptionOrError(new NutsIdNonOption("Nuts", context)).getString();
                    }
                }
            }
        }
        if (cmdLine.isAutoCompleteMode()) {
            return 0;
        }
        NutsPrintStream out = context.getTerminal().getFormattedOut();
        if (fileMode) {
            if (StringUtils.isEmpty(contentFile)) {
                throw new NutsCommandSyntaxError("Missing File");
            }
            for (String s : context.expandPath(contentFile)) {
                NutsId nid = null;
                nid = context.getValidWorkspace().deploy(
                        new NutsDeployment()
                                .setContentPath(s)
                                .setDescriptorPath(descriptorFile)
                                .setRepositoryId(to),
                        context.getSession()
                );
                out.printf("File ==%s== deployed successfully as ==%s== to ==%s==\n" + nid, s, nid, to == null ? "<default-repo>" : to);
            }
        } else if (idMode) {
            if (StringUtils.isEmpty(id)) {
                throw new NutsCommandSyntaxError("Missing Id");
            }
            for (NutsId nutsId : context.getValidWorkspace().find(new NutsSearch(id).setLastestVersions(true).setRepositoryFilter(from), context.getSession())) {
                NutsFile fetched = context.getValidWorkspace().fetch(nutsId.toString(), context.getSession());
                if (fetched.getFile() != null) {
                    NutsId nid = null;
                    nid = context.getValidWorkspace().deploy(
                            new NutsDeployment()
                                    .setContent(new File(fetched.getFile()))
                                    .setDescriptor(fetched.getDescriptor())
                                    .setRepositoryId(to),
                            context.getSession()
                    );
                    out.printf("Nuts ==%s== deployed successfully to ==%s==\n" + nid, nutsId, to == null ? "<default-repo>" : to);
                }
            }
        }
        return 0;

    }
}
