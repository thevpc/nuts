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
package net.thevpc.nuts.runtime.standalone.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.boot.NBootWorkspaceFactory;
import net.thevpc.nuts.util.NMsg;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNBootWorkspaceFactory implements NBootWorkspaceFactory {

    public DefaultNBootWorkspaceFactory() {
    }

    @Override
    public int getBootSupportLevel(NBootOptionsInfo options) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NWorkspace createWorkspace(NBootOptionsInfo bOptions) {
        if(bOptions==null){
            bOptions=new NBootOptionsInfo();
        }
        NBootOptions info2=new DefaultNBootOptionsBuilder(bOptions).build();
        String workspaceLocation = info2.getWorkspace().orNull();
        if(workspaceLocation!=null && workspaceLocation.matches("[a-z-]+://.*")){
            String protocol=workspaceLocation.substring(0,workspaceLocation.indexOf("://"));
            switch (protocol){
                case "local":{
                    return null;
                }
            }
            return null;
        }
        return new DefaultNWorkspace(bOptions,info2);
    }

    @Override
    public NWorkspace runWorkspace(NBootOptionsInfo options) {
        NWorkspace workspace = createWorkspace(options);
        if(workspace==null){
            return null;
        }
        workspace.runWith(() -> {
            NBootOptions info2=new DefaultNBootOptionsBuilder(options).build();
            NApp.of().setId(workspace.getApiId());
            NLogOp logOp = NLog.of(NBootWorkspaceImpl.class).with().level(Level.CONFIG);
            logOp.verb(NLogVerb.SUCCESS).log(NMsg.ofC("running workspace in %s mode", getRunModeString(info2)));
            NExecCmd execCmd = NExecCmd.of()
                    .setExecutionType(info2.getExecutionType().orNull())
                    .setRunAs(info2.getRunAs().orNull())
                    .failFast();
            List<String> executorOptions = info2.getExecutorOptions().orNull();
            if (executorOptions != null) {
                execCmd.configure(true, executorOptions.toArray(new String[0]));
            }
            NCmdLine executorOptionsCmdLine = NCmdLine.of(executorOptions).setExpandSimpleOptions(false);
            while (executorOptionsCmdLine.hasNext()) {
                execCmd.configureLast(executorOptionsCmdLine);
            }
            if (info2.getApplicationArguments().get().isEmpty()) {
                if (info2.getSkipWelcome().orElse(false)) {
                    return;
                }
                execCmd.addCommand("welcome");
            } else {
                execCmd.addCommand(info2.getApplicationArguments().get());
            }
            execCmd.run();
        });
        return workspace;
    }

    private String getRunModeString(NBootOptions options) {
        if (options.getReset().orElse(false)) {
            return "reset";
        } else if (options.getRecover().orElse(false)) {
            return "recover";
        } else {
            return "exec";
        }
    }

    @Override
    public String toString() {
        return "NBootWorkspaceFactory";
    }
}
