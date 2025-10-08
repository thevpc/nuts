/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.core.NWorkspaceOptionsConfig;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NBlankable;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ProcessExecutorComponent implements NExecutorComponent {

    public static NId ID;

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public int getScore(NScorableContext nutsDefinition) {
        if(ID==null){
            ID= NId.get("net.thevpc.nuts.exec:exec-native").get();
        }
        return DEFAULT_SCORE;
    }

    @Override
    public int exec(NExecutionContext executionContext) {
        return execHelper(executionContext).exec();
    }

    public IProcessExecHelper execHelper(NExecutionContext executionContext) {
        NDefinition nutMainFile = executionContext.getDefinition();
        NPath storeFolder = nutMainFile.getInstallInformation().get().getInstallFolder();
        List<String> execArgs = executionContext.getExecutorOptions();
        List<String> appArgs = executionContext.getArguments();

        List<String> app = new ArrayList<>(appArgs);
        if (app.isEmpty()) {
            if (storeFolder == null) {
                app.add("${nuts.file}");
            } else {
                app.add("${nuts.store}/run");
            }
        }

        Map<String, String> osEnv = new HashMap<>();
        String bootArgumentsString = JavaExecutorComponent.createChildOptions(executionContext)
                .toCmdLine(new NWorkspaceOptionsConfig().setCompact(true))
                .toString();
        osEnv.put("nuts_boot_args", bootArgumentsString);
        String dir = null;
        boolean showCommand = CoreNUtils.isShowCommand();
        for (int i = 0; i < execArgs.size(); i++) {
            String arg = execArgs.get(i);
            if (arg.equals("--show-command") || arg.equals("-show-command")) {
                showCommand = true;
            } else if (arg.equals("--dir") || arg.equals("-dir")) {
                i++;
                dir = execArgs.get(i);
            } else if (arg.startsWith("--dir=") || arg.startsWith("-dir=")) {
                dir = execArgs.get(i).substring(arg.indexOf('=') + 1);
            }
        }
        String directory = NBlankable.isBlank(dir) ? null :
                NPath.of(dir).toAbsolute().toString();
        return ProcessExecHelper.ofDefinition(nutMainFile,
                app.toArray(new String[0]), osEnv, directory,
                showCommand, true,
                executionContext.getSleepMillis(),
                executionContext.getIn(), executionContext.getOut(), executionContext.getErr(),
                executionContext.getRunAs(),
                executionContext.getExecutorOptions().toArray(new String[0]),
                executionContext.isDry(), executionContext.getSession()
        );
    }
}
