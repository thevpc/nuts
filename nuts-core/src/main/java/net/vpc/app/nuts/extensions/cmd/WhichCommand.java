/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;

import net.vpc.app.nuts.extensions.cmd.cmdline.NutsIdNonOption;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 1/7/17.
 */
public class WhichCommand extends AbstractNutsCommand {

    public WhichCommand() {
        super("which", CORE_SUPPORT);
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws Exception {
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        NutsWorkspace validWorkspace = context.getValidWorkspace();
        NutsPrintStream out = context.getTerminal().getOut();
        if(cmdLine.isEmpty()){
            if (cmdLine.isExecMode()) {
                Map<String, String> runtimeProperties = getRuntimeProperties(context.getValidWorkspace(), context.getSession());
                out.drawln("nuts-version    : [[" + runtimeProperties.get("nuts.workspace.version") + "]]");
                out.drawln("nuts-location   : [["+runtimeProperties.get("nuts.workspace.location")+"]]");
                out.drawln("nuts-api        : [[" + runtimeProperties.get("nuts.workspace.api-component") + "]]");
                out.drawln("nuts-core       : [[" + runtimeProperties.get("nuts.workspace.core-component") + "]]");
                out.drawln("java-version    : [[" + System.getProperty("java.version") + "]]");
                out.drawln("java-executable : [[" + System.getProperty("java.home") + "/bin/java" + "]]");
            }
            return;
        }
        do {
            String id = cmdLine.readNonOptionOrError(new NutsIdNonOption("NutsId",context)).getString();
            if (cmdLine.isExecMode()) {
                NutsId found = validWorkspace.resolveId(id, context.getSession());
                if (found == null) {
                    context.getTerminal().getErr().println(id + " not found");
                } else {
                    out.println(found);
                }
            }
        } while (!cmdLine.isEmpty());
    }

    public static Map<String,String> getRuntimeProperties(NutsWorkspace workspace,NutsSession session) {
        Map<String,String> map=new HashMap<>();
        String cp_nutsFile="<unknown>";
        String cp_nutsCoreFile="<unknown>";
        String cp = System.getProperty("java.class.path");
        if(cp!=null){
            String[] splits = cp.split(System.getProperty("path.separator"));
            for (String split : splits) {
                String uniformPath = split.replace('\\', '/');
                if(uniformPath.matches("(.*/)?nuts-\\d.*\\.jar")){
                    cp_nutsFile=split;
                }else if(uniformPath.matches("(.*/)?nuts-core-\\d.*\\.jar")){
                    cp_nutsCoreFile=split;
                }else if(uniformPath.endsWith("/nuts/target/classes")){
                    cp_nutsFile=split;
                }else if(uniformPath.endsWith("/nuts-core/target/classes")){
                    cp_nutsCoreFile=split;
                }
            }
        }
        ClassLoader classLoader = WhichCommand.class.getClassLoader();
        if(classLoader instanceof URLClassLoader){
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                String split=url.toString();
                String uniformPath = split.replace('\\', '/');
                if(uniformPath.matches("(.*/)?nuts-\\d.*\\.jar")){
                    cp_nutsFile=split;
                }else if(uniformPath.matches("(.*/)?nuts-core-\\d.*\\.jar")){
                    cp_nutsCoreFile=split;
                }else if(uniformPath.endsWith("/nuts/target/classes")){
                    cp_nutsFile=split;
                }else if(uniformPath.endsWith("/nuts-core/target/classes")){
                    cp_nutsCoreFile=split;
                }
            }
        }

        NutsFile core = null;
        try {
            core=workspace.fetch(NutsConstants.NUTS_COMPONENT_CORE_ID, false,session.copy().setFetchMode(FetchMode.OFFLINE));
        } catch (Exception e) {
            //ignore
        }
        if(cp_nutsCoreFile.equals("<unknown>")){
            if(core==null){
                cp_nutsCoreFile="not found, will be downloaded on need";
            }else{
                cp_nutsCoreFile=core.getFile().getPath();
            }
        }
        map.put("nuts.workspace.version",Main.getBootVersion());
        map.put("nuts.workspace.api-component",cp_nutsFile);
        map.put("nuts.workspace.core-component",cp_nutsCoreFile);
        map.put("nuts.workspace.location",workspace.getWorkspaceLocation());
        return map;
    }
}
