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
package net.vpc.app.nuts.core.impl.def.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class NutsShellNutsExecutorComponent implements NutsExecutorComponent {

    public static final NutsId ID = CoreNutsUtils.parseNutsId("net.vpc.app.nuts.exec:exec-nsh");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        if (nutsDefinition != null) {
            if ("nsh".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())
                    || "nuts".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    public void exec(NutsExecutionContext executionContext) {
        execHelper(executionContext,false);
    }

    public void dryExec(NutsExecutionContext executionContext) {
        execHelper(executionContext,true);
    }

    public void execHelper(NutsExecutionContext executionContext,boolean dry) {
        NutsDefinition nutMainFile = executionContext.getDefinition();
        String[] execArgs = executionContext.getExecutorOptions();
        String[] appArgs = executionContext.getArguments();

        String dir = null;
        boolean showCommand = CoreCommonUtils.getSysBoolNutsProperty("show-command", false);
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

//        List<String> env = new ArrayList<>();
//        env.addAll(Arrays.asList(envAndApp0[0]));
//        env.addAll(Arrays.asList(envAndApp[0]));
        List<String> app = new ArrayList<>();
        app.add(NutsConstants.Ids.NUTS_SHELL);
        app.add(nutMainFile.getPath().toString());
        app.addAll(Arrays.asList(appArgs));

        File directory = CoreStringUtils.isBlank(dir) ? null : new File(executionContext.getWorkspace().io().expandPath(dir));
        executionContext.getWorkspace()
                .exec()
                .command(app)
                .session(executionContext.getSession())
                .env(executionContext.getEnv())
                .directory(directory == null ? null : directory.getPath())
                .failFast()
                //                .showCommand(showCommand)
                .executionType(executionContext.getExecutionType())
                .dry(dry)
                .run();
    }

}
