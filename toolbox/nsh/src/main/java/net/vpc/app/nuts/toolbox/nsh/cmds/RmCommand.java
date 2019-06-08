/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.io.IOException;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.ssh.SshXFile;
import net.vpc.common.xfile.XFile;

import java.util.ArrayList;
import java.util.List;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class RmCommand extends SimpleNshBuiltin {

    public RmCommand() {
        super("rm", DEFAULT_SUPPORT);
    }

    public static class Options {

        boolean R = false;
        boolean verbose = false;
        List<XFile> files = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextBoolean("-R")) != null) {
            options.R = a.getBooleanValue();
            return true;
        } else if (commandLine.peek().isNonOption()) {
            options.files.add(ShellHelper.xfileOf(commandLine.next().getString(), context.getGlobalContext().getCwd()));
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.files.size() < 1) {
            throw new NutsExecutionException(context.getWorkspace(), "Missing parameters", 2);
        }
        ShellHelper.WsSshListener listener = options.verbose ? new ShellHelper.WsSshListener(context.getWorkspace(), context.getSession()) : null;
        for (XFile p : options.files) {
            if (p instanceof SshXFile) {
                ((SshXFile) p).setListener(listener);
            }
            try {
                p.rm(options.R);
            } catch (IOException ex) {
                throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
            }
        }
    }

}
