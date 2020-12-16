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
package net.thevpc.nuts.runtime.standalone.bridges.maven;

import java.util.Arrays;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;
import net.thevpc.nuts.spi.NutsRepositoryFactoryComponent;

/**
 * Created by vpc on 1/15/17.
 */
@NutsSingleton
public class MavenNutsRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    private static final NutsRepositoryDefinition[] DEFAULTS = {
        new NutsRepositoryDefinition().setName("maven-local").setLocation(System.getProperty("maven-local", "~/.m2/repository")).setType(NutsConstants.RepoTypes.MAVEN).setProxy(CoreCommonUtils.getSysBoolNutsProperty("cache.cache-local-files", false)).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_LOCAL),
        new NutsRepositoryDefinition().setName("maven-central").setLocation(NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL).setType(NutsConstants.RepoTypes.MAVEN).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_REMOTE),
//        new NutsRepositoryDefinition().setName("vpc-public-maven").setLocation(NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT).setType(NutsConstants.RepoTypes.MAVEN).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_REMOTE),
//        new NutsRepositoryDefinition().setName("vpc-public-nuts").setLocation(NutsConstants.BootstrapURLs.REMOTE_NUTS_GIT).setType(NutsConstants.RepoTypes.NUTS).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_REMOTE)
    };

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories(NutsWorkspace workspace) {
        return Arrays.copyOf(DEFAULTS, DEFAULTS.length);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsRepositoryConfig> criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        String repositoryType = criteria.getConstraints().getType();
        String location = criteria.getConstraints().getLocation();
        if (!NutsConstants.RepoTypes.MAVEN.equals(repositoryType)) {
            return NO_SUPPORT;
        }
        if (CoreStringUtils.isBlank(location)) {
            return DEFAULT_SUPPORT;
        }
        if (CoreIOUtils.isPathHttp(location)) {
            return DEFAULT_SUPPORT;
        }
        if (!location.contains("://")) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsRepository create(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        final NutsRepositoryConfig config = options.getConfig();
        if (NutsConstants.RepoTypes.MAVEN.equals(config.getType())) {
            if (CoreIOUtils.isPathHttp(config.getLocation())) {
                return (new MavenRemoteRepository(options, workspace, parentRepository));
            }
            if (CoreIOUtils.isPathFile(config.getLocation())) {
                return new MavenFolderRepository(options, workspace, parentRepository);
            }
        }
        return null;
    }
}
