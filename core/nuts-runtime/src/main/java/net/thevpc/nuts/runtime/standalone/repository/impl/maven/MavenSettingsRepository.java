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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.NMavenSettings;
import net.thevpc.nuts.env.NMavenSettingsLoader;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryList;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenSettingsRepository extends NRepositoryList {

    private NMavenSettings settings;

    public MavenSettingsRepository(NAddRepositoryOptions options, NSession session, NRepository parentRepository) {
        super(options, new NRepository[0], session, parentRepository, null, false, NConstants.RepoTypes.MAVEN, false);
        this.LOG = NLog.of(MavenSettingsRepository.class, session);
        this.settings = new NMavenSettingsLoader(LOG).loadSettingsRepos();
        List<NRepository> base = new ArrayList<>();

        base.add(createChild(options, "maven-local", "maven-local", settings.getLocalRepository(), session));
        base.add(createChild(options, "maven-central", "maven-central", settings.getRemoteRepository(), session));
        for (NRepositoryLocation activeRepository : settings.getActiveRepositories()) {
            base.add(createChild(options, "extra", activeRepository.getName(), activeRepository.getPath(), session));
        }
        this.repoItems = base.toArray(base.toArray(new NRepository[0]));
    }

    @Override
    public boolean isEnabled(NSession session) {
        if(Boolean.getBoolean("nomaven")){
            return false;
        }
        return super.isEnabled(session);
    }

    private MavenFolderRepository createChild(NAddRepositoryOptions options0, String type, String id, String url, NSession session) {
        NPath p = NPath.of(url, session);
        String pr = NStringUtils.trim(p.getProtocol());
        MavenFolderRepository mavenChild = null;
        NAddRepositoryOptions options = new NAddRepositoryOptions();
        options.setName(id);
        options.setLocation(id);
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
                mavenChild = new MavenRemoteXmlRepository(options, session, null);
                break;
            }
            default: {
                mavenChild = new MavenFolderRepository(options, session, null);
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
