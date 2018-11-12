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
package net.vpc.app.nuts.extensions.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.io.FileUtils;
import net.vpc.common.strings.StringUtils;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNutsRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public int getSupportLevel(NutsRepoInfo criteria) {
        String repositoryType = criteria.getType();
        String location = criteria.getLocation();
        if (
                !NutsConstants.REPOSITORY_TYPE_NUTS.equals(repositoryType)
                        && !NutsConstants.REPOSITORY_TYPE_NUTS_FOLDER.equals(repositoryType)
                        && !NutsConstants.REPOSITORY_TYPE_NUTS_SERVER.equals(repositoryType)
                ) {
            return NO_SUPPORT;
        }
        if (StringUtils.isEmpty(location)) {
            return DEFAULT_SUPPORT;
        }
        if (!location.contains("://")) {
            return DEFAULT_SUPPORT;
        }
        if (location.startsWith("http://") || location.startsWith("https://")) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsRepository create(String repositoryId, String location, String repositoryType, NutsWorkspace workspace, NutsRepository parentRepository, String repositoryRoot) {
        if (
                NutsConstants.REPOSITORY_TYPE_NUTS.equals(repositoryType)
                        || NutsConstants.REPOSITORY_TYPE_NUTS_FOLDER.equals(repositoryType)
                        || NutsConstants.REPOSITORY_TYPE_NUTS_SERVER.equals(repositoryType)
                ) {
            if (location == null || !location.contains("://")) {
                return new NutsFolderRepository(repositoryId, location, workspace, parentRepository);
            }
            if (location.startsWith("http://") || location.startsWith("https://")) {
                if (NutsConstants.REPOSITORY_TYPE_NUTS_FOLDER.equals(repositoryType)) {
                    return (new NutsRemoteFolderHttpRepository(repositoryId, location, workspace, parentRepository, repositoryRoot));
                }
                return (new NutsRemoteHttpRepository(repositoryId, location, workspace, parentRepository, repositoryRoot));
            }
        }
        return null;
    }

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories(NutsWorkspace workspace) {
        return new NutsRepositoryDefinition[]{
                new NutsRepositoryDefinition("system", workspace.getPlatformOsLib() + FileUtils.getNativePath("/nuts/system-repository"), "nuts", false)
        };
    }
}
