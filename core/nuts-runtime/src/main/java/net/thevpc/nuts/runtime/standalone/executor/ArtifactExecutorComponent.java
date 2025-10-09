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
package net.thevpc.nuts.runtime.standalone.executor;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.util.NScorableContext;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ArtifactExecutorComponent implements NExecutorComponent {

    private NId id;

    public ArtifactExecutorComponent(NId id) {
        this.id = id;
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public int getScore(NScorableContext nutsDefinition) {
        return UNSUPPORTED_SCORE;
    }

    public int exec(NExecutionContext executionContext) {
        return execHelper(executionContext,false);
    }

    public void dryExec(NExecutionContext executionContext) {
        execHelper(executionContext,true);
    }

    public int execHelper(NExecutionContext executionContext, boolean dry) {
        NDefinition nutMainFile = executionContext.getDefinition();
        List<String> execArgs = executionContext.getExecutorOptions();
        List<String> appArgs = executionContext.getArguments();

        List<String> app = new ArrayList<>();
        app.add(id.toString());
        app.add(nutMainFile.getContent().map(Object::toString).get());
        app.addAll(appArgs);

//        File directory = NutsBlankable.isBlank(dir) ? null : new File(executionContext.getWorkspace().io().expandPath(dir));
        return NExecCmd.of()
                .addCommand(app)
                .setEnv(executionContext.getEnv())
                .setDirectory(executionContext.getDirectory())
                .failFast()
                .setExecutionType(executionContext.getExecutionType())
                .run().getResultCode();
    }

}
