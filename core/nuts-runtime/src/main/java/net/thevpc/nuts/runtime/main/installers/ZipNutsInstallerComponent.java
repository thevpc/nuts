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
package net.thevpc.nuts.runtime.main.installers;

import java.io.IOException;
import java.io.UncheckedIOException;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.util.io.UnzipOptions;
import net.thevpc.nuts.runtime.util.io.ZipUtils;
import net.thevpc.nuts.runtime.DefaultNutsDefinition;

import java.nio.file.Path;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class ZipNutsInstallerComponent implements NutsInstallerComponent {

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        if (nutsDefinition != null && nutsDefinition.getConstraints().getDescriptor() != null) {
            if ("zip".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public void install(NutsExecutionContext executionContext) {
        DefaultNutsDefinition nutsDefinition = (DefaultNutsDefinition) executionContext.getDefinition();
        Path installFolder = executionContext.getWorkspace().locations().getStoreLocation(nutsDefinition.getId(), NutsStoreLocation.APPS);

        String skipRoot = (String) executionContext.getExecutorProperties().get("unzip-skip-root");
        try {
            ZipUtils.unzip(executionContext.getWorkspace(),nutsDefinition.getPath().toString(),
                    installFolder.toString(),
                    new UnzipOptions().setSkipRoot("true".equalsIgnoreCase(skipRoot))
            );
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        nutsDefinition.setInstallInformation(NutsWorkspaceExt.of(executionContext.getWorkspace()).getInstalledRepository().getInstallInformation(nutsDefinition.getId(), executionContext.getExecSession()));
        if (executionContext.getExecutorArguments().length > 0) {
            executionContext.getWorkspace()
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
