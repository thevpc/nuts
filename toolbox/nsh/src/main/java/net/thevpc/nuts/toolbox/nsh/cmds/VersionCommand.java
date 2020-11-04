/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.common.mvn.PomIdResolver;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsVersionFormat;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends AbstractNshBuiltin {

    public VersionCommand() {
        super("version", DEFAULT_SUPPORT);
    }

    @Override
    public void exec(String[] args, NshExecutionContext context) {
        NutsWorkspace ws = context.getWorkspace();
        NutsCommandLine cmdLine = context.getWorkspace().commandLine().create(args);
        NutsVersionFormat version = ws.version().formatter();
        version.configure(true, cmdLine);
        version
                .setSession(context.getSession())
                .addProperty("nsh-version", PomIdResolver.resolvePomId(getClass()).toString())
                .println(context.out());
    }
}
