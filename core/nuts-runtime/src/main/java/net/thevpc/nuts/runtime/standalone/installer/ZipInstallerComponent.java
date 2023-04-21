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
package net.thevpc.nuts.runtime.standalone.installer;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.io.util.UnzipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NRef;

import java.io.IOException;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class ZipInstallerComponent implements NInstallerComponent {

    @Override
    public int getSupportLevel(NSupportLevelContext ctx) {
        NDefinition def = ctx.getConstraints(NDefinition.class);
        if (def != null) {
            if (def.getDescriptor() != null) {
                if ("zip".equals(def.getDescriptor().getPackaging())) {
                    return DEFAULT_SUPPORT;
                }
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public void install(NExecutionContext executionContext) {
        DefaultNDefinition nutsDefinition = (DefaultNDefinition) executionContext.getDefinition();
        NSession session = executionContext.getSession();
        NPath installFolder = NLocations.of(session).getStoreLocation(nutsDefinition.getId(), NStoreType.BIN);
        NCmdLine cmd = NCmdLine.of(executionContext.getArguments(), session);
        UnzipOptions unzipOptions = new UnzipOptions();
        while (cmd.hasNext()) {
            if (!cmd.withNextFlag((v, a, s) -> {
                unzipOptions.setSkipRoot(v);
            }, "--unzip-skip-root")) {
                cmd.next();
            }
        }
        try {
            ZipUtils.unzip(session,
                    nutsDefinition.getContent().map(Object::toString).get(session),
                    installFolder.toString(),
                    unzipOptions
            );
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        nutsDefinition.setInstallInformation(NWorkspaceExt.of(session).getInstalledRepository().getInstallInformation(nutsDefinition.getId(),
                executionContext.getSession()));
        if (executionContext.getExecutorOptions().size() > 0) {
            NExecCommand.of(session)
                    .addCommand(executionContext.getExecutorOptions())
                    .addExecutorOptions(executionContext.getExecutorOptions())
                    .setEnv(executionContext.getEnv())
                    .setDirectory(installFolder)
                    .getResult();
        }
    }

    @Override
    public void update(NExecutionContext executionContext) {
    }

    @Override
    public void uninstall(NExecutionContext executionContext, boolean deleteData) {
    }

}
