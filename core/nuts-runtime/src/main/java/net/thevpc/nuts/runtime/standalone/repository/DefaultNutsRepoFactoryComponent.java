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
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NutsHttpSrvRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NutsRepositoryUtils;
import net.thevpc.nuts.spi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            String type = NutsRepositoryUtils.getRepoType(r);
            if (NutsConstants.RepoTypes.NUTS.equals(type)) {
                return DEFAULT_SUPPORT + 10;
            }
            if (NutsBlankable.isBlank(type)) {
                NutsPath rp = NutsPath.of(r.getLocation().getPath(), criteria.getSession()).resolve("nuts-repository.json");
                if (rp.exists()) {
                    r.setLocation(r.getLocation().setLocationType(NutsConstants.RepoTypes.NUTS));
                    return DEFAULT_SUPPORT + 10;
                }
                return DEFAULT_SUPPORT + 2;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
        List<NutsAddRepositoryOptions> all=new ArrayList<>();
        if (!session.config().isGlobal()) {
            all.add(NutsRepositorySelectorHelper.createRepositoryOptions(
                    Objects.requireNonNull(NutsRepositoryLocation.of("system", NutsRepositoryDB.of(session), session)),
                    true, session));
        }
        all.add(NutsRepositorySelectorHelper.createRepositoryOptions(
                Objects.requireNonNull(NutsRepositoryLocation.of("vpc-public-nuts", NutsRepositoryDB.of(session), session)),
                true, session));
        all.add(NutsRepositorySelectorHelper.createRepositoryOptions(
                Objects.requireNonNull(NutsRepositoryLocation.of("goodies", NutsRepositoryDB.of(session), session)),
                true, session));
        return all.toArray(new NutsAddRepositoryOptions[0]);
    }

    @Override
    public NutsRepository create(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        NutsRepositoryConfig config = options.getConfig();
        String type = NutsRepositoryUtils.getRepoType(config);
        if (NutsBlankable.isBlank(type)) {
            return null;
        }
        if (NutsConstants.RepoTypes.NUTS.equals(type)) {
            if (NutsBlankable.isBlank(config.getLocation()) ||
                    NutsPath.of(config.getLocation().getPath(), session).isFile()
            ) {
                return new NutsFolderRepository(options, session, parentRepository);
            } else if (NutsPath.of(config.getLocation().getPath(), session).isURL()) {
                Map<String, String> e = config.getEnv();
                if (e != null) {
                    if (NutsUtilStrings.parseBoolean(e.get("nuts-api-server"), false, false)) {
                        return (new NutsHttpSrvRepository(options, session, parentRepository));
                    }
                }
                return (new NutsFolderRepository(options, session, parentRepository));
            }
        }
        return null;
    }
}
