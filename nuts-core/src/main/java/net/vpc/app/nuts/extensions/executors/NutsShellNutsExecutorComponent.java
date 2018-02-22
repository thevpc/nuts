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
package net.vpc.app.nuts.extensions.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsShellNutsExecutorComponent implements NutsExecutorComponent {

    public static final Logger log = Logger.getLogger(NutsShellNutsExecutorComponent.class.getName());
    public static final NutsId ID = CoreNutsUtils.parseNutsId("nuts");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsFile nutsFile) {
        if (nutsFile != null) {
            if ("nsh".equals(nutsFile.getDescriptor().getPackaging())
                    || "nuts".equals(nutsFile.getDescriptor().getPackaging())) {
                return CORE_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    public int exec(NutsExecutionContext executionContext) {
        NutsFile nutMainFile = executionContext.getNutsFile();
        String[][] envAndApp0 = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getExecArgs());
        String[][] envAndApp = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getArgs());

        List<String> env = new ArrayList<>();
        env.addAll(Arrays.asList(envAndApp0[0]));
        env.addAll(Arrays.asList(envAndApp[0]));

        List<String> app = new ArrayList<>();
        app.addAll(Arrays.asList(envAndApp0[1]));
        app.addAll(Arrays.asList(envAndApp[1]));

        NutsConsole commandLine = null;
        commandLine = executionContext.getWorkspace().getExtensionManager().getFactory().createConsole(executionContext.getSession());
        return commandLine.runFile(nutMainFile.getFile(), app.toArray(new String[app.size()]));
    }

}
