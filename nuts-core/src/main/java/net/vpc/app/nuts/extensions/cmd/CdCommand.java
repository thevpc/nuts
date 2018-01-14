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

import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.ObjectFilter;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.FolderNonOption;
import net.vpc.app.nuts.extensions.util.CoreCollectionUtils;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Created by vpc on 1/7/17.
 */
public class CdCommand extends AbstractNutsCommand {

    public CdCommand() {
        super("cd", CORE_SUPPORT);
    }

    public int run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        cmdLine.requireNonEmpty();
        String folder = cmdLine.readNonOptionOrError(new FolderNonOption("Folder")).getString();
        File[] validFiles = CoreCollectionUtils.filterArray(File.class, CoreIOUtils.findFilesOrError(folder, new File(context.getCommandLine().getCwd())),
                new ObjectFilter<File>() {
                    @Override
                    public boolean accept(File value) {
                        return value.isDirectory();
                    }
                });
        NutsPrintStream out = context.getTerminal().getOut();
        int result=0;
        switch (validFiles.length) {
            case 1:
                context.getCommandLine().setCwd(validFiles[0].getPath());
                result=0;
                break;
            case 0:
                out.println("invalid folder " + folder);
                result=1;
                break;
            default:
                for (File validFile : validFiles) {
                    out.println(validFile.getPath());
                }
                result=0;
                break;
        }
        cmdLine.requireEmpty();
        return result;
    }
}
