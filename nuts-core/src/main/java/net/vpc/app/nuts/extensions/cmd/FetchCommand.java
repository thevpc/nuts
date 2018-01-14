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
import net.vpc.app.nuts.extensions.cmd.cmdline.FileNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.NutsIdNonOption;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

import java.io.File;

/**
 * Created by vpc on 1/7/17.
 */
public class FetchCommand extends AbstractNutsCommand {

    public FetchCommand() {
        super("fetch", CORE_SUPPORT);
    }

    public int run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        cmdLine.requireNonEmpty();
        String lastLocationFile = null;
        boolean descMode = false;
        boolean effective = false;
        while (!cmdLine.isEmpty()) {
            if (cmdLine.acceptAndRemove("-t", "--to")) {
                lastLocationFile = (cmdLine.readNonOptionOrError(new FileNonOption("FileOrFolder")).getString());
            } else if (cmdLine.acceptAndRemove("-d", "--desc")) {
                descMode = true;
            } else if (cmdLine.acceptAndRemove("-n", "--nuts")) {
                descMode = false;
            } else if (cmdLine.acceptAndRemove("-e", "--effective")) {
                effective = true;
            } else {
                String id = cmdLine.readNonOptionOrError(new NutsIdNonOption("NutsId", context)).getString();
                if (cmdLine.isExecMode()) {
                    if (descMode) {
                        NutsFile file = null;
                        if (lastLocationFile == null) {
                            context.getValidWorkspace().fetchDescriptor(id, effective, context.getSession());
                            file = new NutsFile(CoreNutsUtils.parseNutsId(id), null,null,false,false);
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || CoreIOUtils.createFileByCwd(lastLocationFile,new File(context.getCommandLine().getCwd())).isDirectory()) {
                            File folder = CoreIOUtils.createFileByCwd(lastLocationFile,new File(context.getCommandLine().getCwd()));
                            folder.mkdirs();
                            NutsDescriptor descriptor = context.getValidWorkspace().fetchDescriptor(id, effective, context.getSession());
                            File target = new File(folder, CoreNutsUtils.getNutsFileName(CoreNutsUtils.parseNutsId(id), ".effective.nuts"));
                            descriptor.write(target,true);
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), descriptor, target, false, true);
                        } else {
                            File target = CoreIOUtils.createFileByCwd(lastLocationFile,new File(context.getCommandLine().getCwd()));
                            NutsDescriptor descriptor = context.getValidWorkspace().fetchDescriptor(id, effective, context.getSession());
                            descriptor.write(target,true);
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), descriptor, target, false, true);
                            lastLocationFile=null;
                        }
                        printFetchedFile(file, context);
                    } else {
                        NutsFile file = null;
                        if (lastLocationFile == null) {
                            file = context.getValidWorkspace().fetch(id, context.getSession());
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || CoreIOUtils.createFileByCwd(lastLocationFile,new File(context.getCommandLine().getCwd())).isDirectory()) {
                            File folder = CoreIOUtils.createFileByCwd(lastLocationFile,new File(context.getCommandLine().getCwd()));
                            folder.mkdirs();
                            File fetched = context.getValidWorkspace().fetch(id, folder, context.getSession());
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), null, fetched, false, true);
                        } else {
                            File simpleFile = CoreIOUtils.createFileByCwd(lastLocationFile,new File(context.getCommandLine().getCwd()));
                            File fetched = context.getValidWorkspace().fetch(id, simpleFile, context.getSession());
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), null, fetched, false, true);
                            lastLocationFile = null;
                        }
                        printFetchedFile(file, context);
                    }
                }
            }
        }
        return 0;
    }

    private void printFetchedFile(NutsFile file, NutsCommandContext context) {
        if (!file.isCached()) {
            if (file.isTemporary()) {
                context.getTerminal().getOut().println(file.getId() + " fetched successfully temporarily to " + file.getFile().getPath());
            } else {
                context.getTerminal().getOut().println(file.getId() + " fetched successfully");
            }
        } else {
            if (file.isTemporary()) {
                context.getTerminal().getOut().println(file.getId() + " already fetched temporarily to " + file.getFile().getPath());
            } else {
                context.getTerminal().getOut().println(file.getId() + " already fetched");
            }
        }
    }
}
