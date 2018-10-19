/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.installers;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.NutsExecutionContextImpl;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by vpc on 1/7/17.
 */
public class ZipNutsInstallerComponent implements NutsInstallerComponent {

    @Override
    public File getInstallFolder(NutsExecutionContext executionContext) {
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
                return DEFAULT_SUPPORT;
            }
        }
        return NO_SUPPORT;
    }

    private File getNutsFolder(NutsExecutionContext executionContext) {
        File store = CoreIOUtils.resolvePath(executionContext.getWorkspace().getConfigManager().getEnv(NutsConstants.ENV_STORE, NutsConstants.DEFAULT_STORE_ROOT),
                CoreIOUtils.createFileByCwd(executionContext.getWorkspace().getConfigManager().getWorkspaceLocation(), (executionContext.getWorkspace().getConfigManager().getCwd())),
                executionContext.getWorkspace().getConfigManager().getNutsHomeLocation());
        return CoreNutsUtils.getNutsFolder(executionContext.getNutsFile().getId(), store);
    }

    @Override
    public void install(NutsExecutionContext executionContext) {
        File installFolder = getNutsFolder(executionContext);
        try {
            CoreIOUtils.unzip(executionContext.getNutsFile().getFile(), installFolder, executionContext.getWorkspace().getConfigManager().getCwd());
            File log = new File(installFolder, ".nuts-install.log");
            CoreIOUtils.copy(new ByteArrayInputStream(String.valueOf(new Date()).getBytes()), log, true, true);
            if (executionContext.getExecArgs() != null && executionContext.getExecArgs().length > 0) {
                executionContext.getNutsFile().setInstallFolder(installFolder);
                CoreIOUtils.execAndWait(executionContext.getNutsFile(), executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecProperties(),
                        executionContext.getExecArgs(),
                        null, null, executionContext.getTerminal(), true
                );
            }
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    public boolean isInstalled(NutsExecutionContext executionContext) {
        File installFolder = getNutsFolder(executionContext);
        File log = new File(installFolder, ".nuts-install.log");
        return log.exists();
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext) {
        File installFolder = getNutsFolder(executionContext);
        try {
            CoreIOUtils.delete(installFolder);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    public boolean isInstalled(NutsFile nutToInstall, NutsWorkspace workspace, NutsSession session) {
        NutsExecutorDescriptor installer = nutToInstall.getDescriptor().getInstaller();
        NutsExecutionContext executionContext = new NutsExecutionContextImpl(nutToInstall, new String[0], installer == null ? null : installer.getArgs(), null, installer == null ? null : installer.getProperties(), session, workspace);
        return isInstalled(executionContext);
    }

}
