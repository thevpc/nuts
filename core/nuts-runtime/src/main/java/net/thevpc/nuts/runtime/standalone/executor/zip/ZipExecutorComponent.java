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
package net.thevpc.nuts.runtime.standalone.executor.zip;

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.executor.exec.NExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.NMsg;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ZipExecutorComponent implements NExecutorComponent {

    public static NId ID=NId.get("net.thevpc.nuts.exec:zip").get();

    public ZipExecutorComponent() {
    }

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public int exec(NExecutionContext executionContext) {
        return execHelper(executionContext).exec();
    }

    @NScore
    public static int getScore(NScorableContext ctx) {
        NDefinition def = ctx.getCriteria(NDefinition.class);
        if (def != null) {
            String shortName = def.id().shortName();
            //for executors
            if ("net.thevpc.nuts.exec:exec-zip".equals(shortName)) {
                return NScorable.DEFAULT_SCORE + 10;
            }
            if ("zip".equals(shortName)) {
                return NScorable.DEFAULT_SCORE + 10;
            }
            switch (NStringUtils.trim(def.descriptor().packaging())) {
                case "zip": {
                    return NScorable.DEFAULT_SCORE + 10;
                }
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
        if (executor.id() != null && !executor.id().toString().equals("exec")) {
            // TODO: delegate to another executor!
            throw new NIOException(NMsg.ofC("unsupported executor %s for %s", executor.id(), def.id()));
        }
        String directory = null;
        return NExecHelper.ofDefinition(
                def,
                args.toArray(new String[0]), osEnv, directory, true,
                true, executionContext.sleepDuration(),
                executionContext.in(), executionContext.out(), executionContext.err(), executionContext.runAs()
        );
    }
}
