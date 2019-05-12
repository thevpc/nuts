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

import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

/**
 * Created by vpc on 1/7/17.
 */
public class CatCommand extends AbstractNutsCommand {


    public CatCommand() {
        super("cat", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean n = false;
        boolean T = false;
        boolean E = false;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandLine cmdLine = cmdLine(args, context);
        Options o = new Options();
        List<File> files = new ArrayList<>();
        PrintStream out = context.out();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            } else if (cmdLine.readAll("-")) {
                files.add(null);
            } else if (cmdLine.readAll("-n", "--number")) {
                o.n = true;
            } else if (cmdLine.readAll("-t", "--show-tabs")) {
                o.T = true;
            } else if (cmdLine.readAll("-E", "--show-ends")) {
                o.E = true;
            } else {
                String path = cmdLine.readRequiredNonOption(cmdLine.createNonOption("file")).getString();
                File file = new File(context.getShell().getAbsolutePath(path));
                files.add(file);
            }
        }
        if (files.isEmpty()) {
            files.add(null);
        }
        if (o.n || o.T || o.E) {
            int nn = 1;
            //text mode
            for (File f : files) {
                Reader reader = null;
                try {
                    if (f == null) {
                        reader = new InputStreamReader(context.in());
                    } else {
                        reader = new FileReader(f);
                    }
                    try (BufferedReader r = new BufferedReader(reader)) {
                        String line = null;
                        while ((line = r.readLine()) != null) {
                            if (o.n) {
                                out.print(StringUtils.alignRight(String.valueOf(nn), 6));
                                out.print("  ");
                            }
                            if (o.T) {
                                line = line.replace("\t", "^I");
                            }
                            out.print(line);
                            if (o.E) {
                                out.println("$");
                            }
                            out.println();
                            nn++;
                        }
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
        } else {
            for (File f : files) {
                if (f == null) {
                    IOUtils.copy(context.in(), out);
                } else {
                    IOUtils.copy(f, out);
                }
            }
        }
        return 0;
    }
}
