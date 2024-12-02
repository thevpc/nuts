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

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.executor.exec.NExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ZipExecutorComponent implements NExecutorComponent {

    public static NId ID;
    NSession session;

    public ZipExecutorComponent(NSession session) {
        this.session = session;
        ID = NId.get("net.thevpc.nuts.exec:zip").get();
    }

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public int exec(NExecutionContext executionContext) {
        return execHelper(executionContext).exec();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext ctx) {
        NDefinition def = ctx.getConstraints(NDefinition.class);
        if (def != null) {
            String shortName = def.getId().getShortName();
            //for executors
            if ("net.thevpc.nuts.exec:exec-zip".equals(shortName)) {
                return NConstants.Support.DEFAULT_SUPPORT + 10;
            }
            if ("zip".equals(shortName)) {
                return NConstants.Support.DEFAULT_SUPPORT + 10;
            }
            switch (NStringUtils.trim(def.getDescriptor().getPackaging())) {
                case "zip": {
                    return NConstants.Support.DEFAULT_SUPPORT + 10;
                }
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    //@Override
    public IProcessExecHelper execHelper(NExecutionContext executionContext) {
        NDefinition def = executionContext.getDefinition();
        HashMap<String, String> osEnv = new HashMap<>();
        NArtifactCall executor = def.getDescriptor().getExecutor();
        NAssert.requireNonNull(executor, () -> NMsg.ofC("missing executor %s", def.getId()));
        List<String> args = new ArrayList<>(executionContext.getExecutorOptions());
        args.addAll(executionContext.getArguments());
        if (executor.getId() != null && !executor.getId().toString().equals("exec")) {
            // TODO: delegate to another executor!
            throw new NIOException(NMsg.ofC("unsupported executor %s for %s", executor.getId(), def.getId()));
        }
        String directory = null;
        return NExecHelper.ofDefinition(
                def,
                args.toArray(new String[0]), osEnv, directory, true,
                true, executionContext.getSleepMillis(),
                executionContext.getIn(), executionContext.getOut(), executionContext.getErr(), executionContext.getRunAs(),
                executionContext.getWorkspace()
        );
    }
}
