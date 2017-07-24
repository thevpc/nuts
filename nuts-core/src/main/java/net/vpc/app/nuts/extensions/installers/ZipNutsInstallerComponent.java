/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.installers;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsInstallerComponent;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by vpc on 1/7/17.
 */
public class ZipNutsInstallerComponent implements NutsInstallerComponent {
    @Override
    public File getInstallFolder(NutsExecutionContext executionContext) throws IOException {
        File installFolder = getNutsFolder(executionContext);
        File log = new File(installFolder, ".nuts-install.log");
        if (log.exists()) {
            return installFolder;
        }
        return null;
    }

    @Override
    public int getSupportLevel(NutsFile nutsFile) {
        if (nutsFile != null && nutsFile.getDescriptor() != null) {
            if ("zip".equals(nutsFile.getDescriptor().getPackaging())) {
                return CORE_SUPPORT;
            }
        }
        return NO_SUPPORT;
    }

    private File getNutsFolder(NutsExecutionContext executionContext) {
        File store = IOUtils.resolvePath(executionContext.getWorkspace().getConfig().getEnv(NutsConstants.ENV_STORE, NutsConstants.DEFAULT_STORE_ROOT),
                IOUtils.createFile(executionContext.getWorkspace().getWorkspaceLocation()));
        return CoreNutsUtils.getNutsFolder(executionContext.getNutsFile().getId(), store);
    }

    @Override
    public void install(NutsExecutionContext executionContext) throws IOException {
        File installFolder = getNutsFolder(executionContext);
        IOUtils.unzip(executionContext.getNutsFile().getFile(), installFolder);
        File log = new File(installFolder, ".nuts-install.log");
        IOUtils.copy(new ByteArrayInputStream(String.valueOf(new Date()).getBytes()), log, true, true);
        if (executionContext.getExecArgs() != null && executionContext.getExecArgs().length > 0) {
            executionContext.getNutsFile().setInstallFolder(installFolder);
            IOUtils.execAndWait(executionContext.getNutsFile(), executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecProperties(),
                    executionContext.getExecArgs(),
                    null, null, executionContext.getSession().getTerminal()
            );
        }
    }

    @Override
    public boolean isInstalled(NutsExecutionContext executionContext) throws IOException {
        File installFolder = getNutsFolder(executionContext);
        File log = new File(installFolder, ".nuts-install.log");
        return log.exists();
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext) throws IOException {
        File installFolder = getNutsFolder(executionContext);
        IOUtils.delete(installFolder);
    }


}
