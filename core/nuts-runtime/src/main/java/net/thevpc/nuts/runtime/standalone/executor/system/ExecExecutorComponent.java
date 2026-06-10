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
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.runtime.standalone.executor.exec.NExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ExecExecutorComponent implements NExecutorComponent {

    public static NId ID=NId.get("net.thevpc.nuts.exec:exec").get();

    public ExecExecutorComponent() {
    }

    @Override
    public NId id() {
        return ID;
    }

    @Override
    public int exec(NExecutionContext executionContext) {
        return execHelper(executionContext).exec();
    }

    @NScore
    public static int getScore(NScorableContext ctx) {
        NDefinition def = ctx.criteria(NDefinition.class);
        if (def != null) {
            NArtifactCall e = def.descriptor().executor();
            if(e!=null && e.id()!=null && e.id().toString().equals("exec")){
                return NScorable.DEFAULT_SCORE + 20;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    //@Override
    public IProcessExecHelper execHelper(NExecutionContext executionContext) {
        NDefinition def = executionContext.definition();
        HashMap<String, String> osEnv = new HashMap<>();
        NArtifactCall executor = def.descriptor().executor();
        NAssert.requireNonNull(executor, () -> NMsg.ofC("missing executor %s", def.id()));
        List<String> args = new ArrayList<>(executionContext.executorOptions());
        args.addAll(executionContext.arguments());
        String directory = null;
        return NExecHelper.ofDefinition(
                def,
                args.toArray(new String[0]), osEnv, directory, true,
                true, executionContext.sleepDuration(),
                executionContext.in(), executionContext.out(), executionContext.err(), executionContext.runAs()
        );
    }
}
