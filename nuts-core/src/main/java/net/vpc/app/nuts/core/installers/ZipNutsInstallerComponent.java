/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.installers;

import java.io.IOException;
import java.io.UncheckedIOException;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsDefinition;

import java.nio.file.Path;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.io.UnzipOptions;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 * Created by vpc on 1/7/17.
 */
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
        Path installFolder = executionContext.getWorkspace().config().getStoreLocation(nutsDefinition.getId(), NutsStoreLocation.APPS);

        String skipRoot = (String) executionContext.getExecutorProperties().remove("unzip-skip-root");
        try {
            ZipUtils.unzip(nutsDefinition.getPath().toString(),
                    installFolder.toString(),
                    new UnzipOptions().setSkipRoot("true".equalsIgnoreCase(skipRoot))
            );
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        nutsDefinition.setInstallInformation(NutsWorkspaceExt.of(executionContext.getWorkspace()).getInstalledRepository().getInstallInfo(nutsDefinition.getId()));
        if (executionContext.getExecutorOptions().length > 0) {
            executionContext.getWorkspace()
                    .exec()
                    .command(executionContext.getExecutorOptions())
                    .session(executionContext.getSession())
                    .env(executionContext.getExecutorProperties())
                    .directory(installFolder.toString())
                    .run().getResult();
        }
    }

    @Override
    public void update(NutsExecutionContext executionContext) {
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
    }

}
