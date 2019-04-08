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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.app.options.NutsIdNonOption;
import net.vpc.common.commandline.Argument;

import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/7/17.
 */
public class WhichCommand extends AbstractNutsCommand {

    public WhichCommand() {
        super("which", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        NutsWorkspace validWorkspace = context.getWorkspace();
        List<String> elems = new ArrayList<>();
        Argument a;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            } else {
                elems.add(cmdLine.readRequiredNonOption(new NutsIdNonOption("NutsId", context.getWorkspace())).getStringExpression());

            }
        }
        PrintStream out = context.out();

        int ret = 0;
        if (elems.isEmpty()) {
            if (cmdLine.isExecMode()) {
                Map<String, String> runtimeProperties = getRuntimeProperties(context.getWorkspace(), context.getSession());
                out.printf("nuts-version    : [[%s]]\n", runtimeProperties.get("nuts.workspace.version"));
                out.printf("nuts-location   : [[%s]]\n", runtimeProperties.get("nuts.workspace.location"));
                out.printf("nuts-api        : [[%s]]\n", runtimeProperties.get("nuts.workspace.api-component"));
                out.printf("nuts-core       : [[%s]]\n", runtimeProperties.get("nuts.workspace.core-component"));
                out.printf("java-version    : [[%s]]\n", System.getProperty("java.version"));
                out.printf("java-executable : [[%s]]\n", System.getProperty("java.home") + "/bin/java");
            }
        } else {
            for (String id : elems) {
                if (cmdLine.isExecMode()) {
                    NutsId found = validWorkspace.fetch().id(id).setSession(context.getSession()).getResultId();
                    if (found == null) {
                        context.err().printf("%s not found\n", id);
                        ret = 1;
                    } else {
                        out.println(found);
                        ret = 0;
                    }
                }
            }
        }
        return ret;
    }

    public static Map<String, String> getRuntimeProperties(NutsWorkspace workspace, NutsSession session) {
        Map<String, String> map = new HashMap<>();
        String cp_nutsFile = "<unknown>";
        String cp_nutsCoreFile = "<unknown>";
        String cp = System.getProperty("java.class.path");
        if (cp != null) {
            String[] splits = cp.split(System.getProperty("path.separator"));
            for (String split : splits) {
                String uniformPath = split.replace('\\', '/');
                if (uniformPath.matches("(.*/)?nuts-\\d.*\\.jar")) {
                    cp_nutsFile = split;
                } else if (uniformPath.matches("(.*/)?nuts-core-\\d.*\\.jar")) {
                    cp_nutsCoreFile = split;
                } else if (uniformPath.endsWith("/nuts/target/classes")) {
                    cp_nutsFile = split;
                } else if (uniformPath.endsWith("/nuts-core/target/classes")) {
                    cp_nutsCoreFile = split;
                }
            }
        }
        ClassLoader classLoader = WhichCommand.class.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                String split = url.toString();
                String uniformPath = split.replace('\\', '/');
                if (uniformPath.matches("(.*/)?nuts-\\d.*\\.jar")) {
                    cp_nutsFile = split;
                } else if (uniformPath.matches("(.*/)?nuts-core-\\d.*\\.jar")) {
                    cp_nutsCoreFile = split;
                } else if (uniformPath.endsWith("/nuts/target/classes")) {
                    cp_nutsFile = split;
                } else if (uniformPath.endsWith("/nuts-core/target/classes")) {
                    cp_nutsCoreFile = split;
                }
            }
        }

        NutsDefinition core = null;
        try {
            core = workspace.fetch().id(NutsConstants.Ids.NUTS_RUNTIME)
                    .setSession(session)
                    .offline()
                    .getResultDefinition();
        } catch (Exception e) {
            //ignore
        }
        if (cp_nutsCoreFile.equals("<unknown>")) {
            if (core == null) {
                cp_nutsCoreFile = "not found, will be downloaded on need";
            } else {
                cp_nutsCoreFile = core.getContent().getPath().toString();
            }
        }
        map.put("nuts.workspace.version", workspace.config().getRunningContext().getApiId().getVersion().getValue());
        map.put("nuts.workspace.api-component", cp_nutsFile);
        map.put("nuts.workspace.core-component", cp_nutsCoreFile);
        map.put("nuts.workspace.location", workspace.config().getWorkspaceLocation().toString());
        return map;
    }
}
