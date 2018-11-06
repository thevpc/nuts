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

import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.options.FileNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<File> files = new ArrayList<>();
        NutsPrintStream out = context.getTerminal().getOut();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.read("-")) {
                files.add(null);
            } else if (cmdLine.read("-n", "--number")) {
                options.n = true;
            } else if (cmdLine.read("-t", "--show-tabs")) {
                options.T = true;
            } else if (cmdLine.read("-E", "--show-ends")) {
                options.E = true;
            } else if (cmdLine.read("--version")) {
                out.printf("%s\n", "1.0");
                return 0;
            } else if (cmdLine.read("--help")) {
                out.printf("%s\n", getHelp());
                return 0;
            } else {
                String path = cmdLine.readNonOptionOrError(new FileNonOption("File")).getString();
                File file = new File(context.resolvePath(path));
                files.add(file);
            }
        }
        if (files.isEmpty()) {
            files.add(null);
        }
        if (options.n || options.T || options.E) {
            int nn=1;
            //text mode
            for (File f : files) {
                Reader reader=null;
                try {
                    if (f == null) {
                        reader = new InputStreamReader(context.getTerminal().getIn());
                    } else {
                        reader = new FileReader(f);
                    }
                    try (BufferedReader r = new BufferedReader(reader)) {
                        String line = null;
                        while ((line = r.readLine()) != null) {
                            if(options.n){
                                out.print(CoreStringUtils.alignRight(String.valueOf(nn),6));
                                out.print("  ");
                            }
                            if(options.T){
                                line=line.replace("\t","^I");
                            }
                            out.print(line);
                            if(options.E){
                                out.println("$");
                            }
                            out.println();
                            nn++;
                        }
                    }
                }finally {
                    if(reader!=null){
                        reader.close();
                    }
                }
            }
        } else {
            for (File f : files) {
                if(f==null){
                    IOUtils.copy(context.getTerminal().getIn(),out);
                }else{
                    IOUtils.copy(f,out);
                }
            }
        }
        return 0;
    }
}
