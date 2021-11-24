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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.DefaultNutsRepositoryDB;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.MavenFolderRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.MavenRemoteXmlRepository;
import net.thevpc.nuts.spi.*;

/**
 * Created by vpc on 1/15/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class MavenRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
        return new NutsAddRepositoryOptions[]{
                NutsRepositorySelectorHelper.createRepositoryOptions(NutsRepositoryURL.of("maven-local", DefaultNutsRepositoryDB.INSTANCE,session),
                        true, session),
                NutsRepositorySelectorHelper.createRepositoryOptions(NutsRepositoryURL.of("maven-central", DefaultNutsRepositoryDB.INSTANCE,session),
                        true, session)
        };
    }

    @Override
    public NutsRepository create(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parentRepository) {
        final NutsRepositoryConfig config = options.getConfig();
        String type = config.getType();
        if (type == null) {
            return null;
        }
        NutsPath p = NutsPath.of(config.getLocation(), session);
        String pr = NutsUtilStrings.trim(p.getProtocol());
        switch (pr) {
            //non traversable!
            case "http":
            case "https": {
                return new MavenRemoteXmlRepository(options, session, parentRepository);
            }
        }
        return new MavenFolderRepository(options, session, parentRepository);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        NutsRepositoryConfig r = criteria.getConstraints(NutsRepositoryConfig.class);
        if (r != null) {
            if (NutsConstants.RepoTypes.MAVEN.equals(r.getType())) {
                return DEFAULT_SUPPORT + 10;
            }
            if (NutsBlankable.isBlank(r.getType())) {
                return DEFAULT_SUPPORT + 5;
            }
        }
        return NO_SUPPORT;
    }
}
