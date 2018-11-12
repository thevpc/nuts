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
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class WgetCommand extends AbstractNutsCommand {


    public WgetCommand() {
        super("wget", DEFAULT_SUPPORT);
    }

    private static class Options {
        String outputDocument = null;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        Options options = new Options();
        List<String> files = new ArrayList<>();
        NutsPrintStream out = context.getTerminal().getOut();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.read("-O", "--output-document")) {
                options.outputDocument = cmdLine.readNonOption().getString();
            } else if (cmdLine.read("--version")) {
                out.printf("%s\n", "1.0");
                return 0;
            } else if (cmdLine.read("--help")) {
                out.printf("%s\n", getHelp());
                return 0;
            } else {
                files.add( cmdLine.readNonOption().getString());
            }
        }
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Missing Files");
        }
        for (String file : files) {
            download(file,options.outputDocument,context);
        }
        return 0;
    }

    protected void download(String path,String output,NutsCommandContext context) throws IOException {
        String output2=output;
        URL url=new URL(path);
        String urlName = URLUtils.getURLName(url);
        if(!StringUtils.isEmpty(output2)){
            output2=output2.replace("{}",urlName);
        }
        File file= new File(context.getAbsolutePath(StringUtils.isEmpty(output2)?urlName:output2));
        context.getValidWorkspace().downloadPath(path, file, context.getSession());
    }
}
