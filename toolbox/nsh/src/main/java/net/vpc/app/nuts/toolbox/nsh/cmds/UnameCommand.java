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

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.strings.StringUtils;

import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

/**
 * Created by vpc on 1/7/17.
 */
public class UnameCommand extends AbstractNutsCommand {

    public UnameCommand() {
        super("uname", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandLine cmdLine = cmdLine(args, context);
        boolean farch = false;
        boolean fos = false;
        boolean fdist = false;
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAll("-m")) {
                farch = true;
            } else if (cmdLine.readAll("-r")) {
                fos = true;
            } else if (cmdLine.readAll("-d")) {
                fdist = true;
            } else if (cmdLine.readAll("-a")) {
                fdist = true;
                fos = true;
                farch = true;
            } else {
                cmdLine.unexpectedArgument(getName());
            }
        }
        if (cmdLine.isExecMode()) {
            NutsWorkspace ws = context.getWorkspace();
            NutsId osdist = ws.config().getPlatformOsDist();
            NutsId os = ws.config().getPlatformOs();
            NutsId arch = ws.config().getPlatformArch();

            List<String> sb = new ArrayList<>();
            if (!farch && !fos && !fdist) {
                if(osdist!=null) {
                    sb.add(osdist.toString());
                }
                sb.add(os.toString());
                sb.add(arch.toString());
            } else {
                if (farch) {
                    sb.add(arch.toString());
                }
                if (fos) {
                    sb.add(os.toString());
                }
                if (fdist) {
                    sb.add(osdist.toString());
                }
            }
            if (sb.isEmpty()) {
                sb.add("<UNKNOWN>");
            }
            context.out().println(StringUtils.join(" ", sb));
        }
        return 0;
    }
}
