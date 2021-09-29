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
package net.thevpc.nuts.runtime.standalone.repos;

import java.io.File;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsRepositoryFactoryComponent;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryType;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryURL;

/**
 * Created by vpc on 1/15/17.
 */
@NutsSingleton
public class DefaultNutsRepoFactoryComponent implements NutsRepositoryFactoryComponent {

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsRepositoryConfig> criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        String repositoryType = criteria.getConstraints().getType();
        if (NutsBlankable.isBlank(repositoryType)) {
            String location = criteria.getConstraints().getLocation();
            if (!NutsBlankable.isBlank(location)) {
                NutsRepositoryURL nru = new NutsRepositoryURL(location);
                if (nru.getRepositoryType().isNuts()) {
                    criteria.getConstraints().setType(nru.getRepositoryType().toString());
                    criteria.getConstraints().setLocation(nru.getLocation());
                    return DEFAULT_SUPPORT;
                }
                if (nru.isHttp()) {
                    NutsInput in = criteria.getWorkspace().io().input().setTypeName("nuts-repository.json").of(
                            nru.getLocation() + "/nuts-repository.json"
                    );
                    try (InputStream s = in.open()) {
                        Map<String, Object> m = criteria.getWorkspace().elem().setSession(criteria.getSession()).setContentType(NutsContentType.JSON)
                                .parse(s, Map.class);
                        if (m != null) {
                            String type = (String) m.get("type");
                            NutsRepositoryType nrt = new NutsRepositoryType(type);
                            if (nrt.isNuts()) {
                                criteria.getConstraints().setType(type);
                                return DEFAULT_SUPPORT;
                            }
                        }
                    } catch (Exception ex) {
                        //ignore
                    }
                } else if (nru.getPathProtocol().equals("file")) {
                    File file = CoreIOUtils.toFile(nru.getLocation());
                    if (file != null) {
                        if (Files.exists(file.toPath().resolve("nuts-repository.json"))) {
                            criteria.getConstraints().setType(NutsConstants.RepoTypes.NUTS);
                            return DEFAULT_SUPPORT;
                        }
                        criteria.getConstraints().setType(NutsConstants.RepoTypes.NUTS);
                        return DEFAULT_SUPPORT;
                    }
                } else if (nru.getProtocols().isEmpty()) {
                    if (Files.exists(Paths.get(location).resolve("nuts-repository.json"))) {
                        criteria.getConstraints().setType(NutsConstants.RepoTypes.NUTS);
                        return DEFAULT_SUPPORT;
                    }
                    File file = CoreIOUtils.toFile(nru.getLocation());
                    if (file != null) {
                        criteria.getConstraints().setType(NutsConstants.RepoTypes.NUTS);
                        return DEFAULT_SUPPORT;
                    }
                }
            }
            return NO_SUPPORT;
        }
        String location = criteria.getConstraints().getLocation();
        if (!NutsConstants.RepoTypes.NUTS.equals(repositoryType)
                && !"nuts:api".equals(repositoryType)) {
            return NO_SUPPORT;
        }
        if (NutsBlankable.isBlank(location)) {
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
    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
        if (!session.config().isGlobal()) {
            return new NutsAddRepositoryOptions[]{
                NutsRepositorySelector.createRepositoryOptions(NutsRepositorySelector.parseSelection("system"), true, session)
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
