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
package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.*;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class ProcessExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext nutsDefinition) {
        this.ws=nutsDefinition.getSession();
        if(ID==null){
            ID=NutsId.of("net.thevpc.nuts.exec:exec-native",ws);
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
        NutsPath storeFolder = nutMainFile.getInstallInformation().getInstallFolder();
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
        String bootArgumentsString = executionContext.getSession().boot().getBootOptions()
                .formatter().setExported(true).setCompact(true).getBootCommandLine().toString();
        osEnv.put("nuts_boot_args", bootArgumentsString);
        String dir = null;
        boolean showCommand = executionContext.getSession().boot().getBootCustomBoolArgument(false,false,false,"---show-command");
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
        String directory = NutsBlankable.isBlank(dir) ? null :
                NutsPath.of(dir,executionContext.getSession()).toAbsolute().toString();
        return ProcessExecHelper.ofDefinition(nutMainFile,
                app.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), showCommand, true, executionContext.getSleepMillis(), false, false, null, null, executionContext.getRunAs(), executionContext.getSession(),
                executionContext.getExecSession()
        );
    }
}
