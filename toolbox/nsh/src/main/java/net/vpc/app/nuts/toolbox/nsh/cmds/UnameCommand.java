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

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.strings.StringUtils;

import java.util.ArrayList;
import java.util.List;

import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class UnameCommand extends SimpleNshBuiltin {

    public UnameCommand() {
        super("uname", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean farch = false;
        boolean fos = false;
        boolean fdist = false;
    }

    private static class Result {

        NutsId osdist;
        NutsId os;
        NutsId arch;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        switch (cmdLine.peek().getStringKey()) {
            case "-m": {
                config.farch = true;
                return true;
            }
            case "-r": {
                config.fos = true;
                return true;
            }
            case "-d": {
                config.fdist = true;
                return true;
            }
            case "-a": {
                config.fdist = true;
                config.fos = true;
                config.farch = true;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        NutsWorkspace ws = context.getWorkspace();

        Result rr = new Result();
        rr.osdist = ws.config().getPlatformOsDist();
        rr.os = ws.config().getPlatformOs();
        rr.arch = ws.config().getPlatformArch();
        if (!config.farch && !config.fos && !config.fdist) {
            config.farch = true;
            config.fos = true;
            config.fdist = true;
        }
        if (!config.farch && rr.arch != null) {
            rr.arch = null;
        }
        if (!config.fos && rr.os != null) {
            rr.os = null;
        }
        if (!config.fdist && rr.osdist != null) {
            rr.osdist = null;
        }
        context.setPrintlnOutObject(rr);
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        Result result = context.getResult();
        List<String> sb = new ArrayList<>();
        if (result.arch != null) {
            sb.add(result.arch.toString());
        }
        if (result.os != null) {
            sb.add(result.os.toString());
        }
        if (result.osdist != null) {
            sb.add(result.osdist.toString());
        }
        if (sb.isEmpty()) {
            sb.add("UNKNOWN");
        }
        context.out().println(StringUtils.join(" ", sb));
    }

}
