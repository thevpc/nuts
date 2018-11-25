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

import net.vpc.app.nuts.NutsFormattedPrintStream;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.FileNonOption;
import net.vpc.common.io.InputStreamVisitor;
import net.vpc.common.io.UnzipOptions;
import net.vpc.common.io.ZipUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class UnzipCommand extends AbstractNutsCommand {


    public UnzipCommand() {
        super("unzip", DEFAULT_SUPPORT);
    }

    private static class Options {
        boolean l = false;
        boolean skipRoot = false;
        String dir = null;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        PrintStream out = context.getTerminal().getFormattedOut();
        List<String> files = new ArrayList<>();
        while (cmdLine.hasNext()) {
            if (cmdLine.readAll("-l")) {
                options.l = true;
            } else if (cmdLine.readAll("-d")) {
                options.dir = cmdLine.read().getExpression();
            } else if (cmdLine.isOption()) {
                throw new IllegalArgumentException("Not yet supported");
            } else {
                String path = cmdLine.readRequiredNonOption(new FileNonOption("File")).getString();
                File file = new File(context.getAbsolutePath(path));
                files.add(file.getPath());
            }
        }
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Not yet supported");
        }
        for (String file : files) {
            if (options.l) {
                ZipUtils.visitZipFile(new File(file), null, new InputStreamVisitor() {
                    @Override
                    public boolean visit(String path, InputStream inputStream) throws IOException {
                        out.printf("%s\n",path);
                        return true;
                    }
                });
            } else {
                String dir = options.dir;
                if (StringUtils.isEmpty(dir)) {
                    dir = context.getCwd();
                }
                dir = context.getAbsolutePath(dir);
                ZipUtils.unzip(context.getAbsolutePath(file), dir, new UnzipOptions().setSkipRoot(options.skipRoot));
            }
        }
        return 0;
    }
}
