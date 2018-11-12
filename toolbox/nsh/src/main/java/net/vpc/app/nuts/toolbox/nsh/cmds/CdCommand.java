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

import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.FolderNonOption;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.io.FileUtils;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by vpc on 1/7/17.
 */
public class CdCommand extends AbstractNutsCommand {

    public CdCommand() {
        super("cd", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        cmdLine.requireNonEmpty();
        String folder = cmdLine.readNonOptionOrError(new FolderNonOption("Folder")).getString();
        File[] validFiles = FileUtils.findFilesOrError(folder, new File(context.getCwd()), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        NutsPrintStream out = context.getTerminal().getFormattedOut();
        int result = 0;
        switch (validFiles.length) {
            case 1:
                context.setCwd(validFiles[0].getPath());
                result = 0;
                break;
            case 0:
                out.printf("@@invalid folder %s @@\n", folder);
                result = 1;
                break;
            default:
                for (File validFile : validFiles) {
                    out.printf("%s\n", validFile.getPath());
                }
                result = 0;
                break;
        }
        cmdLine.requireEmpty();
        return result;
    }
}
