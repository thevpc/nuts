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
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNutsRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsRepositoryConfig> criteria) {
        String repositoryType = criteria.getConstraints().getType();
        String location = criteria.getConstraints().getLocation();
        if (!NutsConstants.RepoTypes.NUTS.equals(repositoryType)
                && !NutsConstants.RepoTypes.NUTS_SERVER.equals(repositoryType)) {
            return NO_SUPPORT;
        }
        if (CoreStringUtils.isBlank(location)) {
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
    public NutsRepository create(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        NutsRepositoryConfig config = options.getConfig();
        if (CoreStringUtils.isBlank(config.getType())) {
            if (CoreStringUtils.isBlank(config.getLocation())) {
                config.setType(NutsConstants.RepoTypes.NUTS);
            }
        }
        if (NutsConstants.RepoTypes.NUTS.equals(config.getType())) {
            if (CoreStringUtils.isBlank(config.getLocation()) || CoreIOUtils.isPathFile(config.getLocation())) {
                return new NutsFolderRepository(options, workspace, parentRepository);
            }
            if (CoreIOUtils.isPathURL(config.getLocation())) {
                return (new NutsHttpFolderRepository(options, workspace, parentRepository));
            }
        }
        if (NutsConstants.RepoTypes.NUTS_SERVER.equals(config.getType())) {
            if (CoreIOUtils.isPathHttp(config.getLocation())) {
                return (new NutsHttpSrvRepository(options, workspace, parentRepository));
            }
        }
        return null;
    }

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories(NutsWorkspace workspace) {
        if (!workspace.config().current().isGlobal()) {
            
            return new NutsRepositoryDefinition[]{
                new NutsRepositoryDefinition()
                .setDeployOrder(100)
                .setName("system")
                .setLocation(
                CoreIOUtils.getNativePath(
                        NutsPlatformUtils.getPlatformHomeFolder(null, 
                                NutsStoreLocation.CONFIG, null, 
                                true, 
                                NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                + "/" + NutsConstants.Folders.REPOSITORIES
                + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
                )).setType(NutsConstants.RepoTypes.NUTS).setProxy(false).setReference(true).setFailSafe(true).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_SYSTEM_LOCAL),};
        }
        return new NutsRepositoryDefinition[0];
    }
}
