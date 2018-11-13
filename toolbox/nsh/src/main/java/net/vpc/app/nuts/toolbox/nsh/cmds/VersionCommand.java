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
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class VersionCommand extends AbstractNutsCommand {

    public VersionCommand() {
        super("version", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsPrintStream out = context.getTerminal().getFormattedOut();
        NutsWorkspace ws = context.getWorkspace();
        NutsWorkspaceConfigManager config = ws.getConfigManager();
        boolean longFormat = false;
        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-l") || args[i].equals("--long")){
                longFormat=true;
            }
        }
        out.printf("workspace-location   : [[%s]]\n", config.getWorkspaceLocation());
        out.printf("nuts-boot            : [[%s]]\n", config.getWorkspaceBootId());
        out.printf("nuts-runtime         : [[%s]]\n", config.getWorkspaceRuntimeId());
        out.printf("nuts-home            : [[%s]]\n", ws.getConfigManager().getNutsHomeLocation());
        out.printf("platform-os          : [[%s]]\n", ws.getPlatformOs()+" ("+System.getProperty("os.name")+")");
        out.printf("platform-os-dist     : [[%s]]\n", ws.getPlatformOsDist());
        out.printf("platform-arch        : [[%s]]\n", ws.getPlatformArch());
        out.printf("platform-os-lib      : [[%s]]\n", ws.getPlatformOsLibPath());



        out.printf("boot-java-version    : [[%s]]\n", System.getProperty("java.version"));
        out.printf("boot-java-executable : [[%s]]\n", System.getProperty("java.home") + "/bin/java");
        if (longFormat) {
            out.printf("java.class.path      : \n");
            for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
                out.printf("                       [[%s]]\n", s);
            }
        } else {
            out.printf("java.class.path      : [[%s]]\n", System.getProperty("java.class.path"));
        }
        if (longFormat) {
            out.printf("java.library.path    : \n");
            if(System.getProperty("java.library.path")!=null) {
                for (String s : System.getProperty("java.library.path").split(File.pathSeparator)) {
                    out.printf("                       [[%s]]\n", s);
                }
            }
        } else {
            out.printf("java.library.path    : [[%s]]\n", System.getProperty("java.library.path"));
        }

        URL[] cl = ws.getConfigManager().getBootClassWorldURLs();
        List<String> runtimeClasPath = new ArrayList<>();
        if (cl != null) {
            for (URL url : cl) {
                if (url != null) {
                    if (URLUtils.isFileURL(url)) {
                        runtimeClasPath.add(URLUtils.toFile(url).getPath());
                    } else {
                        runtimeClasPath.add(url.toString());
                    }
                }
            }
        }
        if (longFormat) {
            out.printf("runtime-class-path   : \n");
            for (String s : runtimeClasPath) {
                out.printf("                       [[%s]]\n", s);
            }
        } else {
            out.printf("runtime-class-path   : [[%s]]\n", StringUtils.join(":", runtimeClasPath));
        }
        return 0;
    }
}
