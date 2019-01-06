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
package net.vpc.app.nuts.extensions.executors;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsExecutorComponent;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class ProcessNutsExecutorComponent implements NutsExecutorComponent {

    private static final Logger log = Logger.getLogger(ProcessNutsExecutorComponent.class.getName());
    public static final NutsId ID = CoreNutsUtils.parseNutsId("exec");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsDefinition nutsDefinition) {
        return DEFAULT_SUPPORT;
    }

    public int exec(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getNutsDefinition();
        String storeFolder = nutMainFile.getInstallFolder();
        String[] execArgs = executionContext.getExecutorOptions();
        String[] appArgs = executionContext.getArgs();
//        String[][] envAndApp0 = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getExecutorOptions());
//        String[][] envAndApp = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getArgs());


        List<String> app = new ArrayList<>(Arrays.asList(appArgs));
        if (app.isEmpty()) {
            if (storeFolder == null) {
                app.add("${nuts.file}");
            } else {
                app.add("${nuts.store}/run");
            }
        }

        Map<String, String> osEnv = new HashMap<>();
        String bootArgumentsString = executionContext.getWorkspace().getConfigManager().getOptions().getBootArgumentsString();
        osEnv.put("nuts_boot_args", bootArgumentsString);
        String dir = null;
        boolean showCommand = false;
        for (int i = 0; i < execArgs.length; i++) {
            String arg = execArgs[i];
            if (arg.equals("--show-command") || arg.equals("-show-command")) {
                showCommand = true;
            } else if (arg.equals("--dir") || arg.equals("-dir")) {
                i++;
                dir = execArgs[i];
            } else if (arg.startsWith("--dir=") || arg.startsWith("-dir=")) {
                dir = execArgs[i].substring(arg.indexOf('=') + 1);
            }
        }
        File directory = StringUtils.isEmpty(dir) ? null : new File(executionContext.getWorkspace().getIOManager().resolvePath(dir));
        return CoreIOUtils.execAndWait(
                nutMainFile, executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecutorProperties(),
                app.toArray(new String[0]),
                osEnv, directory, executionContext.getTerminal(), showCommand, executionContext.isFailFast()
        );
    }
}
