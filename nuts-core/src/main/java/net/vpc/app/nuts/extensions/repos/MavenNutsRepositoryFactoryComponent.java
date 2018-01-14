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
package net.vpc.app.nuts.extensions.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenNutsRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public int getSupportLevel(NutsRepoInfo criteria) {
        String repositoryType = criteria.getType();
        String location = criteria.getLocation();
        if (!"maven".equals(repositoryType)) {
            return NO_SUPPORT;
        }
        if (CoreStringUtils.isEmpty(location)) {
            return BOOT_SUPPORT;
        }
        if (location.startsWith("http://") || location.startsWith("https://")) {
            return BOOT_SUPPORT;
        }
        if (!location.contains("://")) {
            return BOOT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsRepository create(String repositoryId, String location, String repositoryType, File repositoryRoot) throws IOException {
        if ("maven".equals(repositoryType)) {
            NutsWorkspace workspace = NutsEnvironmentContext.WORKSPACE.get();
            if (location.startsWith("http://") || location.startsWith("https://")) {
                return (new MavenRemoteRepository(repositoryId, location, workspace, repositoryRoot));
            }
            if (!location.contains("://")) {
                return new MavenFolderRepository(repositoryId, location, workspace, repositoryRoot);
            }
        }
        return null;
    }
}
