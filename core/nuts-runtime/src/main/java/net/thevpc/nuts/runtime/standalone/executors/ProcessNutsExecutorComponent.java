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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.io.IProcessExecHelper;
import net.thevpc.nuts.NutsExecutorComponent;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class ProcessNutsExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsWorkspace ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        this.ws=nutsDefinition.getWorkspace();
        if(ID==null){
            ID=ws.id().parser().parse("net.thevpc.nuts.exec:exec-native");
        }
        return DEFAULT_SUPPORT;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        execHelper(executionContext).exec();
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        execHelper(executionContext).dryExec();
    }

    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        NutsDefinition nutMainFile = executionContext.getDefinition();
        String storeFolder = nutMainFile.getInstallInformation().getInstallFolder();
        String[] execArgs = executionContext.getExecutorArguments();
        String[] appArgs = executionContext.getArguments();

        List<String> app = new ArrayList<>(Arrays.asList(appArgs));
        if (app.isEmpty()) {
            if (storeFolder == null) {
                app.add("${nuts.file}");
            } else {
                app.add("${nuts.store}/run");
            }
        }

        Map<String, String> osEnv = new HashMap<>();
        String bootArgumentsString = executionContext.getWorkspace().config().options()
                .format().exported().compact().getBootCommandLine();
        osEnv.put("nuts_boot_args", bootArgumentsString);
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
        String directory = CoreStringUtils.isBlank(dir) ? null : executionContext.getWorkspace().io().expandPath(dir);
        return NutsWorkspaceUtils.of(executionContext.getWorkspace()).execAndWait(nutMainFile,
                executionContext.getTraceSession(), 
                executionContext.getExecSession(), 
                executionContext.getExecutorProperties(),
                app.toArray(new String[0]),
                osEnv, directory, showCommand, true,
                executionContext.getSleepMillis()
        );
    }
}
