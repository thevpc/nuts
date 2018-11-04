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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.cmdline.FileNonOption;
import net.vpc.app.nuts.toolbox.nsh.cmdline.NutsIdNonOption;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

import java.io.File;

/**
 * Created by vpc on 1/7/17.
 */
public class FetchCommand extends AbstractNutsCommand {

    public FetchCommand() {
        super("fetch", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        cmdLine.requireNonEmpty();
        String lastLocationFile = null;
        boolean descMode = false;
        boolean effective = false;
        while (!cmdLine.isEmpty()) {
            if (cmdLine.read("-t", "--to")) {
                lastLocationFile = (cmdLine.readNonOptionOrError(new FileNonOption("FileOrFolder")).getString());
            } else if (cmdLine.read("-d", "--desc")) {
                descMode = true;
            } else if (cmdLine.read("-n", "--nuts")) {
                descMode = false;
            } else if (cmdLine.read("-e", "--effective")) {
                effective = true;
            } else {
                String id = cmdLine.readNonOptionOrError(new NutsIdNonOption("NutsId", context)).getString();
                if (cmdLine.isExecMode()) {
                    if (descMode) {
                        NutsFile file = null;
                        if (lastLocationFile == null) {
                            context.getValidWorkspace().fetchDescriptor(id, effective, context.getSession());
                            file = new NutsFile(context.getValidWorkspace().getExtensionManager().parseNutsId(id), null, null, false, false, null);
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || new File(context.resolvePath(lastLocationFile)).isDirectory()) {
                            File folder = new File(context.resolvePath(lastLocationFile));
                            folder.mkdirs();
                            NutsDescriptor descriptor = context.getValidWorkspace().fetchDescriptor(id, effective, context.getSession());
                            File target = new File(folder, CoreNutsUtils.getNutsFileName(context.getValidWorkspace().getExtensionManager().parseNutsId(id), ".effective.nuts"));
                            descriptor.write(target, true);
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), descriptor, target.getPath(), false, true, null);
                        } else {
                            File target = new File(context.resolvePath(lastLocationFile));
                            NutsDescriptor descriptor = context.getValidWorkspace().fetchDescriptor(id, effective, context.getSession());
                            descriptor.write(target, true);
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), descriptor, target.getPath(), false, true, null);
                            lastLocationFile = null;
                        }
                        printFetchedFile(file, context);
                    } else {
                        NutsFile file = null;
                        if (lastLocationFile == null) {
                            file = context.getValidWorkspace().fetch(id, context.getSession());
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || new File(context.resolvePath(lastLocationFile)).isDirectory()) {
                            File folder = new File(context.resolvePath(lastLocationFile));
                            folder.mkdirs();
                            String fetched = context.getValidWorkspace().copyTo(id, folder.getPath(), context.getSession());
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), null, fetched, false, true, null);
                        } else {
                            File simpleFile = new File(context.resolvePath(lastLocationFile));
                            String fetched = context.getValidWorkspace().copyTo(id, simpleFile.getPath(), context.getSession());
                            file = new NutsFile(CoreNutsUtils.parseOrErrorNutsId(id), null, fetched, false, true, null);
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
        NutsPrintStream out = context.getTerminal().getFormattedOut();
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
