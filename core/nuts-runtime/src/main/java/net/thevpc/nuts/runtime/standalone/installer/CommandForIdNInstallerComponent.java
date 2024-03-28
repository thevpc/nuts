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
package net.thevpc.nuts.runtime.standalone.installer;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.executor.NExecutionContextUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class CommandForIdNInstallerComponent implements NInstallerComponent {
    NDefinition runnerId;

    public CommandForIdNInstallerComponent(NDefinition runnerId) {
        this.runnerId = runnerId;
    }

    @Override
    public void install(NExecutionContext executionContext) {
        runMode(executionContext, "install");
    }

    @Override
    public void update(NExecutionContext executionContext) {
        runMode(executionContext, "update");
    }

    @Override
    public void uninstall(NExecutionContext executionContext, boolean deleteData) {
        runMode(executionContext, "uninstall");
    }

    public void runMode(NExecutionContext executionContext, String mode) {
        NSession session = executionContext.getSession();
        NWorkspaceUtils.of(session).checkReadOnly();
        if (runnerId == null) {
            NDefinition definition = executionContext.getDefinition();
            NDescriptor descriptor = definition.getDescriptor();
            if (descriptor.isApplication()) {
                DefaultNDefinition def2 = new DefaultNDefinition(definition, session)
                        .setInstallInformation(
                                new DefaultNInstallInfo(definition.getInstallInformation().get(session))
                                        .setInstallStatus(
                                                definition.getInstallInformation().get(session).getInstallStatus().withInstalled(true)
                                        )
                        );
                NExecCmd cmd = NExecCmd.of(session)
                        .setCommandDefinition(def2)
                        .addCommand("--nuts-exec-mode=" + mode);
                if (mode.equals("install")) {
                    cmd.addExecutorOptions("--nuts-auto-install=false");
                }else if (mode.equals("uninstall")) {
                    cmd.addExecutorOptions("--nuts-auto-install=false");
                }
                cmd.addCommand(executionContext.getArguments())
                        .setExecutionType(NBootManager.of(session).getBootOptions().getExecutionType().orNull())
                        .failFast()
                        .run();
            }
        } else {
            NDefinition definition = runnerId;
            NDescriptor descriptor = definition.getDescriptor();
            if (descriptor.isApplication()) {
                DefaultNDefinition def2 = new DefaultNDefinition(definition, session)
                        .setInstallInformation(
                                new DefaultNInstallInfo(definition.getInstallInformation().get(session))
                                        .setInstallStatus(
                                                definition.getInstallInformation().get(session).getInstallStatus().withInstalled(true)
                                        )
                        );
                List<String> eargs = new ArrayList<>();
                for (String a : executionContext.getExecutorOptions()) {
                    eargs.add(evalString(a, mode, executionContext));
                }
                eargs.addAll(executionContext.getArguments());
                NExecCmd.of(executionContext.getSession())
                        .setCommandDefinition(def2)
                        .addCommand(eargs)
                        .setExecutionType(NBootManager.of(executionContext.getSession()).getBootOptions().getExecutionType().orNull())
                        .setExecutionType(
                                "nsh".equals(def2.getId().getArtifactId()) ?
                                        NExecutionType.EMBEDDED : NExecutionType.SPAWN
                        )
                        .failFast()
                        .run();
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }


    private String evalString(String s, String mode, NExecutionContext executionContext) {
        return StringPlaceHolderParser.replaceDollarPlaceHolders(s, executionContext, executionContext.getSession(),
                (key, context, session) -> {
                    if ("NUTS_MODE".equals(key)) {
                        return mode;
                    }
                    return NExecutionContextUtils.EXECUTION_CONTEXT_PLACEHOLDER.get(key, context, session);
                }
        );
    }
}
