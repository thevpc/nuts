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
package net.thevpc.nuts.runtime.standalone.installer;

import net.thevpc.nuts.core.NConstants;


import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionBuilder;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder2;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.executor.NExecutionContextUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.spi.NScorableContext;

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
        NWorkspaceUtils.of().checkReadOnly();
        if (runnerId == null) {
            NDefinition definition = executionContext.getDefinition();
            NDescriptor descriptor = definition.getDescriptor();
            if (descriptor.isNutsApplication()) {
                DefaultNDefinitionBuilder2 def2 = new DefaultNDefinitionBuilder2(definition)
                        .setInstallInformation(
                                ()->new DefaultNInstallInfo(definition.getInstallInformation().get())
                                        .setInstallStatus(
                                                definition.getInstallInformation().get().getInstallStatus().withInstalled(true)
                                        )
                        );
                NExecCmd cmd = NExecCmd.of()
                        .setCommandDefinition(def2.build())
                        .addCommand("--nuts-exec-mode=" + mode);
                if (mode.equals("install")) {
                    cmd.addExecutorOptions("--nuts-auto-install=false");
                }else if (mode.equals("uninstall")) {
                    cmd.addExecutorOptions("--nuts-auto-install=false");
                }
                cmd.addCommand(executionContext.getArguments())
                        .setExecutionType(NWorkspace.of().getBootOptions().getExecutionType().orNull())
                        .failFast()
                        .run();
            }
        } else {
            NDefinition definition = runnerId;
            NDescriptor descriptor = definition.getDescriptor();
            if (descriptor.isNutsApplication()) {
                NDefinitionBuilder def2 = definition.builder()
                        .setInstallInformation(
                                new DefaultNInstallInfo(definition.getInstallInformation().get())
                                        .setInstallStatus(
                                                definition.getInstallInformation().get().getInstallStatus().withInstalled(true)
                                        )
                        );
                List<String> eargs = new ArrayList<>();
                for (String a : executionContext.getExecutorOptions()) {
                    eargs.add(evalString(a, mode, executionContext));
                }
                eargs.addAll(executionContext.getArguments());
                NExecCmd.of()
                        .setCommandDefinition(def2.build())
                        .addCommand(eargs)
                        .setExecutionType(NWorkspace.of().getBootOptions().getExecutionType().orNull())
                        .setExecutionType(
                                NConstants.Ids.NSH.equals(def2.getId().getShortName()) ?
                                        NExecutionType.EMBEDDED : NExecutionType.SPAWN
                        )
                        .failFast()
                        .run();
            }
        }
    }

    @Override
    public int getScore(NScorableContext criteria) {
        return DEFAULT_SCORE;
    }


    private String evalString(String s, String mode, NExecutionContext executionContext) {
        return StringPlaceHolderParser.replaceDollarPlaceHolders(s, executionContext,
                (key, context) -> {
                    if ("NUTS_MODE".equals(key)) {
                        return mode;
                    }
                    return NExecutionContextUtils.EXECUTION_CONTEXT_PLACEHOLDER.get(key, context);
                }
        );
    }
}
