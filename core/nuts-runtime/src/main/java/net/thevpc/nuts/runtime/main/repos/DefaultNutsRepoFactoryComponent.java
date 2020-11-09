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
package net.thevpc.nuts.runtime.main.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

/**
 * Created by vpc on 1/15/17.
 */
@NutsSingleton
public class DefaultNutsRepoFactoryComponent implements NutsRepositoryFactoryComponent {

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
        if (CoreIOUtils.isPathHttp(location)) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsRepository create(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
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
        if (!workspace.config().isGlobal()) {
            
            return new NutsRepositoryDefinition[]{
                new NutsRepositoryDefinition()
                .setDeployOrder(100)
                .setName("system")
                .setLocation(
                CoreIOUtils.getNativePath(
                        Nuts.getPlatformHomeFolder(null,
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
