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
package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryType;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryURL;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repos.FilesFoldersApi;
import net.thevpc.nuts.runtime.standalone.repos.RemoteRepoApi;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsRepositoryFactoryComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class MavenRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
        return new NutsAddRepositoryOptions[]{
                NutsRepositorySelector.createRepositoryOptions(NutsRepositorySelector.parseSelection("maven-local"),
                        true, session),
                NutsRepositorySelector.createRepositoryOptions(NutsRepositorySelector.parseSelection("maven-central"),
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
        if (CoreIOUtils.isPathHttp(config.getLocation())) {
            return (new MavenRemoteRepository(options, session, parentRepository, type));
        }
        if (CoreIOUtils.isPathFile(config.getLocation())) {
            return new MavenFolderRepository(options, session, parentRepository);
        }
        return null;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        NutsRepositoryConfig r = criteria.getConstraints(NutsRepositoryConfig.class);
        if(r!=null) {
            String repositoryType = r.getType();
            String location = r.getLocation();
            if (NutsBlankable.isBlank(repositoryType)) {
                if (!NutsBlankable.isBlank(location)) {
                    NutsRepositoryURL nru = new NutsRepositoryURL(location);
                    if (nru.getRepositoryType().isMaven()) {
                        r.setType(nru.getRepositoryType().toString());
                        r.setLocation(nru.getLocation());
                        return DEFAULT_SUPPORT;
                    }
                    if (nru.isHttp()) {
                        try (InputStream s = NutsPath.of(nru.getLocation(), criteria.getSession()).resolve("nuts-repository.json")
                                .setUserKind("nuts-repository.json").getInputStream()) {
                            Map<String, Object> m = NutsElements.of(criteria.getSession()).setContentType(NutsContentType.JSON)
                                    .parse(s, Map.class);
                            if (m != null) {
                                String type = (String) m.get("type");
                                NutsRepositoryType nrt = new NutsRepositoryType(type);
                                if (nrt.isMaven()) {
                                    r.setType(type);
                                    return DEFAULT_SUPPORT;
                                }
                            }
                        } catch (Exception ex) {
                            //ignore
                        }
                        FilesFoldersApi.Item[] dirList = FilesFoldersApi.getDirItems(true, true, RemoteRepoApi.DIR_LIST, location, criteria.getSession());
                        if (dirList != null) {
                            r.setType("maven+dirlist");
                            return DEFAULT_SUPPORT;
                        }
                        dirList = FilesFoldersApi.getDirItems(true, true, RemoteRepoApi.DIR_TEXT, location, criteria.getSession());
                        if (dirList != null) {
                            r.setType("maven+dirtext");
                            return DEFAULT_SUPPORT;
                        }
                        if (NutsPath.of(location, criteria.getSession()).resolve("archetype-catalog.xml")
                                .setUserKind("archetype-catalog.xml").exists()) {
                            r.setType(NutsConstants.RepoTypes.MAVEN);
                            return DEFAULT_SUPPORT;
                        }
                    } else if (nru.getPathProtocol().equals("file")) {
                        File file = CoreIOUtils.toFile(nru.getLocation());
                        if (file != null) {
                            if (Files.exists(file.toPath().resolve("repository.xml"))) {
                                r.setType(NutsConstants.RepoTypes.MAVEN);
                                return DEFAULT_SUPPORT;
                            }
                        }
                    } else if (nru.getProtocols().isEmpty()) {
                        if (Files.exists(Paths.get(location).resolve("repository.xml"))) {
                            r.setType(NutsConstants.RepoTypes.MAVEN);
                            return DEFAULT_SUPPORT;
                        }
                    }
                }
                return NO_SUPPORT;
            }
            switch (repositoryType) {
                case "maven":
                case "maven+dirlist":
                case "maven+dirtext":
                case "maven+github": {
                    return DEFAULT_SUPPORT;
                }
            }
        }
        return NO_SUPPORT;
    }
}
