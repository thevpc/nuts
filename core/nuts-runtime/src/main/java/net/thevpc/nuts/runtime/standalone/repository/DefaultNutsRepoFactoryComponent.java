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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NutsFolderRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NutsHttpFolderRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NutsHttpSrvRepository;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.*;

/**
 * Created by vpc on 1/15/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class DefaultNutsRepoFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        NutsRepositoryConfig r = criteria.getConstraints(NutsRepositoryConfig.class);
        if (r != null) {
            if (NutsConstants.RepoTypes.NUTS.equals(r.getType())) {
                return DEFAULT_SUPPORT + 10;
            }
            if (NutsBlankable.isBlank(r.getType())) {
                NutsPath rp = NutsPath.of(r.getLocation(), criteria.getSession()).resolve("nuts-repository.json");
                if (rp.exists()) {
                    r.setType(NutsConstants.RepoTypes.NUTS);
                    return DEFAULT_SUPPORT + 10;
                }
                return DEFAULT_SUPPORT + 2;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
        if (!session.config().isGlobal()) {
            return new NutsAddRepositoryOptions[]{
                    NutsRepositorySelectorHelper.createRepositoryOptions(NutsRepositoryURL.of("system", DefaultNutsRepositoryDB.INSTANCE,session), true, session)
            };
        }
        return new NutsAddRepositoryOptions[0];
    }

    @Override
    public NutsRepository create(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        NutsRepositoryConfig config = options.getConfig();
        if (NutsBlankable.isBlank(config.getType())) {
            if (NutsBlankable.isBlank(config.getLocation())) {
                config.setType(NutsConstants.RepoTypes.NUTS);
            }
        }
        if (NutsConstants.RepoTypes.NUTS.equals(config.getType())) {
            if (NutsBlankable.isBlank(config.getLocation()) || CoreIOUtils.isPathFile(config.getLocation())) {
                return new NutsFolderRepository(options, session, parentRepository);
            }
            if (CoreIOUtils.isPathURL(config.getLocation())) {
                return (new NutsHttpFolderRepository(options, session, parentRepository));
            }
        }
        if ("nuts:api".equals(config.getType()) && CoreIOUtils.isPathHttp(config.getLocation())) {
            return (new NutsHttpSrvRepository(options, session, parentRepository));
        }
        return null;
    }
}
