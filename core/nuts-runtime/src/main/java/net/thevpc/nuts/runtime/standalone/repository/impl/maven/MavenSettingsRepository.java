/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryList;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenSettingsRepository extends NRepositoryList {

    private NMavenSettings settings;

    public MavenSettingsRepository(NAddRepositoryOptions options, NWorkspace workspace, NRepository parentRepository) {
        super(options, new NRepository[0], workspace, parentRepository, null, false, NConstants.RepoTypes.MAVEN, false);
        NLog LOG = NLog.of(MavenSettingsRepository.class);
        this.settings = new NMavenSettingsLoader(LOG).loadSettingsRepos();
        List<NRepository> base = new ArrayList<>();

        base.add(createChild(options, "maven-local", getName()+"-local", settings.getLocalRepository()));
        base.add(createChild(options, "maven-central", getName()+"-central", settings.getRemoteRepository()));
        for (NRepositoryLocation activeRepository : settings.getActiveRepositories()) {
            base.add(createChild(options, "maven-extra", getName()+"-" + activeRepository.getName(), activeRepository.getPath()));
        }
        this.repoItems = base.toArray(base.toArray(new NRepository[0]));
    }

    private MavenFolderRepository createChild(NAddRepositoryOptions options0, String type, String id, String url) {
        NPath p = NPath.of(url);
        String pr = NStringUtils.trim(p.getProtocol());
        MavenFolderRepository mavenChild = null;
        NAddRepositoryOptions options = new NAddRepositoryOptions();
        options.setName(id);
        options.setLocation(
                NPath.of(id).toAbsolute(NWorkspaceExt.of().getConfigModel().getRepositoriesRoot()).toString()
        );
        options.setEnabled(true);
        options.setTemporary(true);
        options.setFailSafe(false);
        NRepositoryConfig config = new NRepositoryConfig();
        options.setConfig(config);
        config.setName(id);
        config.setEnv(options0.getConfig().getEnv());
        config.setLocation(new NRepositoryLocation(id, "maven", url));
        switch (pr) {
            //non traversable!
            case "http":
            case "https": {
                mavenChild = new MavenRemoteXmlRepository(options, getWorkspace(), null);
                break;
            }
            default: {
                mavenChild = new MavenFolderRepository(options, getWorkspace(), null);
            }
        }
        mavenChild.getCache().setReadEnabled(false);
        mavenChild.getCache().setWriteEnabled(false);
        mavenChild.getLib().setReadEnabled(false);
        mavenChild.getLib().setWriteEnabled(false);
        mavenChild.setLockEnabled(false);
        return mavenChild;
    }
}
