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
package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NFolderRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NHttpSrvRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNRepoFactoryComponent implements NRepositoryFactoryComponent {
    private NWorkspace workspace;

    public DefaultNRepoFactoryComponent(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        if (criteria == null) {
            return NConstants.Support.NO_SUPPORT;
        }
        NRepositoryConfig r = criteria.getConstraints(NRepositoryConfig.class);
        if (r != null) {
            String type = NRepositoryUtils.getRepoType(r);
            if (NConstants.RepoTypes.NUTS.equals(type)) {
                return NConstants.Support.DEFAULT_SUPPORT + 10;
            }
            if (NBlankable.isBlank(type)) {
                NPath rp = NPath.of(r.getLocation().getPath()).resolve("nuts-repository.json");
                if (rp.exists()) {
                    r.setLocation(r.getLocation().setLocationType(NConstants.RepoTypes.NUTS));
                    return NConstants.Support.DEFAULT_SUPPORT + 10;
                }
                return NConstants.Support.DEFAULT_SUPPORT + 2;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public List<NAddRepositoryOptions> getDefaultRepositories() {
        List<NAddRepositoryOptions> all=new ArrayList<>();
        NSession session=workspace.currentSession();
        if (!NWorkspace.of().isSystemWorkspace()) {
            all.add(NRepositorySelectorHelper.createRepositoryOptions(
                    NRepositoryLocation.of("system", NRepositoryDB.of()).get(),
                    true));
        }
        all.add(NRepositorySelectorHelper.createRepositoryOptions(
                NRepositoryLocation.of("nuts-public", NRepositoryDB.of()).get(),
                true));
        if(session.isPreviewRepo()){
            all.add(NRepositorySelectorHelper.createRepositoryOptions(
                    NRepositoryLocation.of("preview", NRepositoryDB.of()).get(),
                    true));
            all.add(NRepositorySelectorHelper.createRepositoryOptions(
                    NRepositoryLocation.of("dev", NRepositoryDB.of()).get(),
                    true));
        }
        return all;
    }

    @Override
    public NRepository create(NAddRepositoryOptions options, NRepository parentRepository) {
        NRepositoryConfig config = options.getConfig();
        String type = NRepositoryUtils.getRepoType(config);
        if (NBlankable.isBlank(type)) {
            return null;
        }
        if (NConstants.RepoTypes.NUTS.equals(type)) {
            if (NBlankable.isBlank(config.getLocation()) ||
                    NPath.of(config.getLocation().getPath()).isLocal()
            ) {
                return new NFolderRepository(options, workspace, parentRepository);
            } else if (NPath.of(config.getLocation().getPath()).isURL()) {
                Map<String, String> e = config.getEnv();
                if (e != null) {
                    if (NLiteral.of(e.get("nuts-api-server")).asBoolean().orElse(false)) {
                        return (new NHttpSrvRepository(options, workspace, parentRepository));
                    }
                }
                return (new NFolderRepository(options, workspace, parentRepository));
            }
        }
        return null;
    }
}
