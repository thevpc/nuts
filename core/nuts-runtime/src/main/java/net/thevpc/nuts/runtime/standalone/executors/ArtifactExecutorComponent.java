/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class ArtifactExecutorComponent implements NutsExecutorComponent {

    private NutsId id;
    NutsSession session;

    public ArtifactExecutorComponent(NutsId id,NutsSession session) {
        this.id = id;
        this.session = session;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
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
        String[] execArgs = executionContext.getExecutorArguments();
        String[] appArgs = executionContext.getArguments();

//        String dir = null;
//        boolean showCommand = CoreBooleanUtils.getSysBoolNutsProperty("show-command", false);
//        for (int i = 0; i < execArgs.length; i++) {
//            String arg = execArgs[i];
//            if (arg.equals("--show-command") || arg.equals("-show-command")) {
//                showCommand = true;
//            } else if (arg.equals("--dir") || arg.equals("-dir")) {
//                i++;
//                dir = execArgs[i];
//            } else if (arg.startsWith("--dir=") || arg.startsWith("-dir=")) {
//                dir = execArgs[i].substring(arg.indexOf('=') + 1);
//            }
//        }

        List<String> app = new ArrayList<>();
        app.add(id.toString());
        app.add(nutMainFile.getPath().toString());
        app.addAll(Arrays.asList(appArgs));

//        File directory = NutsBlankable.isBlank(dir) ? null : new File(executionContext.getWorkspace().io().expandPath(dir));
        executionContext.getWorkspace()
                .exec()
                .addCommand(app)
                .setSession(executionContext.getExecSession())
                .setEnv(executionContext.getEnv())
                .setDirectory(executionContext.getCwd())
                .setFailFast(true)
                .setExecutionType(executionContext.getExecutionType())
                .setDry(dry)
                .run();
    }

}
