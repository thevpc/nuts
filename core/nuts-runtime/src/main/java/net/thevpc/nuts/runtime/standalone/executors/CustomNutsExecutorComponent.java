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
 *
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
import net.thevpc.nuts.NutsExecutorComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class CustomNutsExecutorComponent implements NutsExecutorComponent {

    public NutsId id;

    public CustomNutsExecutorComponent(NutsId id) {
        this.id = id;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        return NO_SUPPORT;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        List<String> args = new ArrayList<>();
        args.add(id.toString());
        args.addAll(Arrays.asList(executionContext.getArguments()));
        executionContext.getWorkspace()
                .exec()
                .addCommand(args)
                .setSession(executionContext.getExecSession())
                .setEnv(executionContext.getEnv())
                .setDirectory(executionContext.getCwd())
                .setFailFast(true)
                .run();
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) {
        List<String> args = new ArrayList<>();
        args.add(id.toString());
        args.addAll(Arrays.asList(executionContext.getArguments()));
        executionContext.getWorkspace()
                .exec()
                .addCommand(args)
                .setSession(executionContext.getExecSession())
                .setEnv(executionContext.getEnv())
                .setDirectory(executionContext.getCwd())
                .setFailFast(true)
                .setDry(true)
                .run();
    }

}
