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

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.app.options.NutsIdNonOption;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.FileNonOption;

import java.io.File;
import java.io.PrintStream;

/**
 * Created by vpc on 1/7/17.
 */
public class FetchCommand extends AbstractNutsCommand {

    public FetchCommand() {
        super("fetch", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        String lastLocationFile = null;
        boolean descMode = false;
        boolean effective = false;
        Argument a;
        do {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAll("-t", "--to")) {
                lastLocationFile = (cmdLine.readRequiredNonOption(new FileNonOption("FileOrFolder")).getString());
            } else if (cmdLine.readAll("-d", "--desc")) {
                descMode = true;
            } else if (cmdLine.readAll("-n", "--nuts")) {
                descMode = false;
            } else if (cmdLine.readAll("-e", "--effective")) {
                effective = true;
            } else {
                NutsWorkspace ws = context.getWorkspace();
                String id = cmdLine.readRequiredNonOption(new NutsIdNonOption("NutsId", context.getWorkspace())).getString();
                if (cmdLine.isExecMode()) {
                    if (descMode) {
                        NutsDefinition file = null;
                        if (lastLocationFile == null) {
                            context.getWorkspace().fetchDescriptor(id, effective, context.getSession());
                            file = new NutsDefinition(context.getWorkspace().getParseManager().parseId(id), null, null, false, false, null,null);
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || new File(context.getShell().getAbsolutePath(lastLocationFile)).isDirectory()) {
                            File folder = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            folder.mkdirs();
                            NutsDescriptor descriptor = context.getWorkspace().fetchDescriptor(id, effective, context.getSession());
                            File target = new File(folder, ws.getFileName(context.getWorkspace().getParseManager().parseId(id), ".effective.nuts"));
                            context.getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor,target);
                            file = new NutsDefinition(ws.getParseManager().parseRequiredId(id), descriptor, target.getPath(), false, true, null,null);
                        } else {
                            File target = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            NutsDescriptor descriptor = context.getWorkspace().fetchDescriptor(id, effective, context.getSession());
                            context.getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor,target);
                            file = new NutsDefinition(ws.getParseManager().parseRequiredId(id), descriptor, target.getPath(), false, true, null,null);
                            lastLocationFile = null;
                        }
                        printFetchedFile(file, context);
                    } else {
                        NutsDefinition file = null;
                        if (lastLocationFile == null) {
                            file = context.getWorkspace().fetch(id, context.getSession());
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || new File(context.getShell().getAbsolutePath(lastLocationFile)).isDirectory()) {
                            File folder = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            folder.mkdirs();
                            String fetched = context.getWorkspace().copyTo(id, folder.getPath(), context.getSession());
                            file = new NutsDefinition(ws.getParseManager().parseRequiredId(id), null, fetched, false, true, null,null);
                        } else {
                            File simpleFile = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            String fetched = context.getWorkspace().copyTo(id, simpleFile.getPath(), context.getSession());
                            file = new NutsDefinition(ws.getParseManager().parseRequiredId(id), null, fetched, false, true, null,null);
                            lastLocationFile = null;
                        }
                        printFetchedFile(file, context);
                    }
                }
            }
        }while (cmdLine.hasNext());
        return 0;
    }

    private void printFetchedFile(NutsDefinition file, NutsCommandContext context) {
        PrintStream out = context.out();
        if (!file.isCached()) {
            if (file.isTemporary()) {
                out.printf("%s fetched successfully temporarily to %s\n", file.getId(), file.getFile());
            } else {
                out.printf("%s fetched successfully\n", file.getId());
            }
        } else {
            if (file.isTemporary()) {
                out.printf("%s already fetched temporarily to %s\n", file.getId(), file.getFile());
            } else {
                out.printf("%s already fetched\n", file.getId());
            }
        }
    }
}
