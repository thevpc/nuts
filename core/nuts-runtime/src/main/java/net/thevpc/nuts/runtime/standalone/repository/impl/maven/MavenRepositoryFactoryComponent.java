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
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.NRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by vpc on 1/15/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class MavenRepositoryFactoryComponent implements NRepositoryFactoryComponent {

    @Override
    public List<NAddRepositoryOptions> getDefaultRepositories(NSession session) {
        return Arrays.asList(
                NRepositorySelectorHelper.createRepositoryOptions(
                        NRepositoryLocation.of("maven", NRepositoryDB.of(session), session).get(),
                        true, session)
        );
    }

    @Override
    public NRepository create(NAddRepositoryOptions options, NSession session, NRepository parentRepository) {
        if(MavenUtils.isMavenSettingsRepository(options)){
            return new MavenSettingsRepository(options, session, parentRepository);
        }
        final NRepositoryConfig config = options.getConfig();
        String type = NRepositoryUtils.getRepoType(config, session);
        if (NBlankable.isBlank(type)) {
            return null;
        }
        NPath p = NPath.of(config.getLocation().getPath(), session);
        String pr = NStringUtils.trim(p.getProtocol());
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
    public int getSupportLevel(NSupportLevelContext criteria) {
        if (criteria == null) {
            return NConstants.Support.NO_SUPPORT;
        }
        NSession session = criteria.getSession();
        NRepositoryConfig r = criteria.getConstraints(NRepositoryConfig.class);
        if (r != null) {
            String type = NRepositoryUtils.getRepoType(r, session);
            if (NBlankable.isBlank(type)) {
                return NConstants.Support.NO_SUPPORT;
            }
            if (NConstants.RepoTypes.MAVEN.equals(type)) {
                return NConstants.Support.DEFAULT_SUPPORT + 10;
            }
            if (NBlankable.isBlank(type)) {
                return NConstants.Support.DEFAULT_SUPPORT + 5;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }
}
