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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.executor.zip;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.runtime.standalone.executor.exec.NutsExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsExecutorComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class ZipExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession session;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        execHelper(executionContext).exec();
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        execHelper(executionContext).dryExec();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext ctx) {
        this.session = ctx.getSession();
        if (ID == null) {
            ID = NutsId.of("net.thevpc.nuts.exec:zip").get(session);
        }
        NutsDefinition def = ctx.getConstraints(NutsDefinition.class);
        if (def != null) {
            String shortName = def.getId().getShortName();
            //for executors
            if ("net.thevpc.nuts.exec:exec-zip".equals(shortName)) {
                return DEFAULT_SUPPORT + 10;
            }
            if ("zip".equals(shortName)) {
                return DEFAULT_SUPPORT + 10;
            }
            switch (NutsStringUtils.trim(def.getDescriptor().getPackaging())) {
                case "zip": {
                    return DEFAULT_SUPPORT + 10;
                }
            }
        }
        return NO_SUPPORT;
    }

    //@Override
    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        NutsDefinition def = executionContext.getDefinition();
        NutsSession session = executionContext.getSession();
        HashMap<String, String> osEnv = new HashMap<>();
        NutsArtifactCall executor = def.getDescriptor().getExecutor();
        NutsUtils.requireNonNull(executor, () -> NutsMessage.ofCstyle("missing executor %s", def.getId()), session);
        List<String> args = new ArrayList<>(executionContext.getExecutorOptions());
        args.addAll(executionContext.getArguments());
        if (executor.getId() != null && !executor.getId().toString().equals("exec")) {
            // TODO: delegate to another executor!
            throw new NutsIOException(session, NutsMessage.ofCstyle("unsupported executor %s for %s", executor.getId(), def.getId()));
        }
        String directory = null;
        return NutsExecHelper.ofDefinition(def,
                args.toArray(new String[0]), osEnv, directory, executionContext.getExecutorProperties(), true, true, executionContext.getSleepMillis(), false, false, null, null, executionContext.getRunAs(), executionContext.getSession(),
                executionContext.getExecSession()
        );
    }
}
