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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.installer;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.UnzipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.io.IOException;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ZipInstallerComponent implements NInstallerComponent {

    @Override
    public int getSupportLevel(NSupportLevelContext ctx) {
        NDefinition def = ctx.getConstraints(NDefinition.class);
        if (def != null) {
            if (def.getDescriptor() != null) {
                if ("zip".equals(def.getDescriptor().getPackaging())) {
                    return NConstants.Support.DEFAULT_SUPPORT;
                }
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public void install(NExecutionContext executionContext) {
        DefaultNDefinition nutsDefinition = (DefaultNDefinition) executionContext.getDefinition();
        NPath installFolder = NWorkspace.get().getStoreLocation(nutsDefinition.getId(), NStoreType.BIN);
        NCmdLine cmd = NCmdLine.of(executionContext.getArguments());
        UnzipOptions unzipOptions = new UnzipOptions();
        while (cmd.hasNext()) {
            if (!cmd.withNextFlag((v, a) -> {
                unzipOptions.setSkipRoot(v);
            }, "--unzip-skip-root")) {
                cmd.next();
            }
        }
        try {
            ZipUtils.unzip(
                    nutsDefinition.getContent().map(Object::toString).get(),
                    installFolder.toString(),
                    unzipOptions
            );
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        nutsDefinition.setInstallInformation(NWorkspaceExt.of().getInstalledRepository().getInstallInformation(nutsDefinition.getId()
        ));
        if (executionContext.getExecutorOptions().size() > 0) {
            NExecCmd.of()
                    .addCommand(executionContext.getExecutorOptions())
                    .addExecutorOptions(executionContext.getExecutorOptions())
                    .setEnv(executionContext.getEnv())
                    .setDirectory(installFolder)
                    .getResultCode();
        }
    }

    @Override
    public void update(NExecutionContext executionContext) {
    }

    @Override
    public void uninstall(NExecutionContext executionContext, boolean deleteData) {
    }

}
