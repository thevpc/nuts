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
package net.thevpc.nuts.runtime.standalone.installers;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsExecutionContext;
import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.runtime.bundles.io.UnzipOptions;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsInstallerComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.IOException;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class ZipInstallerComponent implements NutsInstallerComponent {

    @Override
    public int getSupportLevel(NutsSupportLevelContext ctx) {
        NutsDefinition def=ctx.getConstraints(NutsDefinition.class);
        if(def!=null) {
            if (def.getDescriptor() != null) {
                if ("zip".equals(def.getDescriptor().getPackaging())) {
                    return DEFAULT_SUPPORT;
                }
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public void install(NutsExecutionContext executionContext) {
        DefaultNutsDefinition nutsDefinition = (DefaultNutsDefinition) executionContext.getDefinition();
        String installFolder = executionContext.getTraceSession().locations().getStoreLocation(nutsDefinition.getId(), NutsStoreLocation.APPS);

        String skipRoot = (String) executionContext.getExecutorProperties().get("unzip-skip-root");
        try {
            ZipUtils.unzip(executionContext.getTraceSession(),nutsDefinition.getPath().toString(),
                    installFolder,
                    new UnzipOptions().setSkipRoot("true".equalsIgnoreCase(skipRoot))
            );
        } catch (IOException ex) {
            throw new NutsIOException(executionContext.getTraceSession(),ex);
        }
        nutsDefinition.setInstallInformation(NutsWorkspaceExt.of(executionContext.getTraceSession()).getInstalledRepository().getInstallInformation(nutsDefinition.getId(), executionContext.getExecSession()));
        if (executionContext.getExecutorArguments().length > 0) {
            executionContext.getTraceSession()
                    .exec()
                    .addCommand(executionContext.getExecutorArguments())
                    .setSession(executionContext.getExecSession())
                    .setEnv(executionContext.getExecutorProperties())
                    .setDirectory(installFolder.toString())
                    .getResult();
        }
    }

    @Override
    public void update(NutsExecutionContext executionContext) {
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
    }

}
