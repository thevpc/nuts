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
import net.vpc.app.nuts.toolbox.nsh.util.FilePath;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.ssh.SShConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 * ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class RmCommand extends AbstractNutsCommand {

    public RmCommand() {
        super("rm", DEFAULT_SUPPORT);
    }

    public static class Options {
        boolean R = false;
        boolean verbose = false;

    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        List<FilePath> files = new ArrayList<>();
        Options o = new Options();
        Argument a;
        while (cmdLine.hasNext()) {
            if (cmdLine.isOption()) {
                if ((a=cmdLine.readBooleanOption("--verbose"))!=null) {
                    o.verbose=a.getBooleanValue();
                }else if (cmdLine.isOption("-R")) {
                    o.R = true;
                }
            } else {
                files.add(FilePath.of(cmdLine.read().getExpression()));
            }
        }
        if (files.size() < 1) {
            throw new IllegalArgumentException("Missing parameters");
        }
        ShellHelper.WsSshListener listener = o.verbose?new ShellHelper.WsSshListener(context.getSession()):null;
        for (int i = 0; i < files.size(); i++) {
            FilePath p = files.get(i);
            if(p instanceof FilePath.SshFilePath){
                ((FilePath.SshFilePath) p).setListener(listener);
            }
            p.rm(o.R);
        }
        return 0;
    }



}
