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

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.ssh.SshXFile;
import net.vpc.common.xfile.XFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 * ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class MkdirCommand extends AbstractNutsCommand {

    public MkdirCommand() {
        super("mkdir", DEFAULT_SUPPORT);
    }

    public static class Options {
        boolean p;
        boolean verbose;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        List<XFile> files = new ArrayList<>();
        Options o = new Options();
        Argument a;
        while (cmdLine.hasNext()) {
            if (cmdLine.isOption()) {
                if (context.configure(cmdLine)) {
                    //
                }else if ((a = cmdLine.readBooleanOption("-p","--parent")) != null) {
                    o.p = a.getBooleanValue();
                }
            } else {
                files.add(ShellHelper.xfileOf(cmdLine.read().getExpression(),context.getShell().getCwd()));
            }
        }
        if (files.size() < 1) {
            throw new NutsExecutionException("Missing parameters",2);
        }
        ShellHelper.WsSshListener listener = o.verbose ? new ShellHelper.WsSshListener(context.getWorkspace(),context.getSession()) : null;
        for (XFile v : files) {
            if (v instanceof SshXFile) {
                ((SshXFile) v).setListener(listener);
            }
            v.mkdir(o.p);
        }
        return 0;
    }
}