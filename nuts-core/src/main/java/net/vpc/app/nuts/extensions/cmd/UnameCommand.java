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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.util.CorePlatformUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class UnameCommand extends AbstractNutsCommand {

    public UnameCommand() {
        super("uname", CORE_SUPPORT);
    }

    public int run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        boolean farch = false;
        boolean fos = false;
        boolean fdist = false;
        while (!cmdLine.isEmpty()) {
            if (cmdLine.acceptAndRemove("-m")) {
                farch = true;
            } else if (cmdLine.acceptAndRemove("-r")) {
                fos = true;
            } else if (cmdLine.acceptAndRemove("-d")) {
                fdist = true;
            } else if (cmdLine.acceptAndRemove("-a")) {
                fdist = true;
                fos = true;
                farch = true;
            } else {
                cmdLine.requireEmpty();
            }
        }
        if (cmdLine.isExecMode()) {
            String osdist = CorePlatformUtils.getOsdist();
            String os = CorePlatformUtils.getOs();
            String arch = CorePlatformUtils.getArch();

            NutsPrintStream out = context.getTerminal().getOut();
            StringBuilder sb = new StringBuilder();
            if (!farch && !fos && !fdist) {
                if (!CoreStringUtils.isEmpty(osdist)) {
                    sb.append(osdist);
                } else if (!CoreStringUtils.isEmpty(os)) {
                    sb.append(os);
                } else if (!CoreStringUtils.isEmpty(arch)) {
                    sb.append(arch);
                }

            } else {
                if (farch) {
                    if (!CoreStringUtils.isEmpty(arch)) {
                        sb.append(arch);
                    }
                }
                if (fos) {
                    if (!CoreStringUtils.isEmpty(os)) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(os);
                    }
                }
                if (fdist) {
                    if (!CoreStringUtils.isEmpty(osdist)) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(osdist);
                    }
                }
            }
            if (sb.length() == 0) {
                sb.append("<UNKNOWN>");
            }
            out.println(sb);
        }
        return 0;
    }
}
