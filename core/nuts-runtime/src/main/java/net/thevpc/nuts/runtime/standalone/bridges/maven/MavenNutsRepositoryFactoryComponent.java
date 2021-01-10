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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.standalone.util.RemoteRepoApi;
import net.thevpc.nuts.runtime.standalone.util.io.FilesFoldersApi;
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
        if(CoreStringUtils.isBlank(repositoryType)){
            if (!CoreStringUtils.isBlank(location)) {
                String prot = CoreNutsUtils.extractUrlProtocol(location);
                if(prot!=null){
                    switch (prot){
                        case "maven":{
                            criteria.getConstraints().setType("maven");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+http":{
                            criteria.getConstraints().setType("maven");
                            criteria.getConstraints().setLocation("http"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+https":{
                            criteria.getConstraints().setType("maven");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+dirtext":{
                            criteria.getConstraints().setType("maven+dirtext");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+dirtext+http":{
                            criteria.getConstraints().setType("maven+dirtext");
                            criteria.getConstraints().setLocation("http"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+dirtext+https":{
                            criteria.getConstraints().setType("maven+dirtext");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+dirlist":{
                            criteria.getConstraints().setType("maven+dirlist");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+dirlist+http":{
                            criteria.getConstraints().setType("maven+dirlist");
                            criteria.getConstraints().setLocation("http"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+dirlist+https":{
                            criteria.getConstraints().setType("maven+dirlist");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+api":{
                            criteria.getConstraints().setType("maven+api");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+api+http":{
                            criteria.getConstraints().setType("maven+api");
                            criteria.getConstraints().setLocation("http"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                        case "maven+api+https":{
                            criteria.getConstraints().setType("maven+api");
                            criteria.getConstraints().setLocation("https"+location.substring(prot.length()));
                            return DEFAULT_SUPPORT;
                        }
                    }
                }
                if (CoreIOUtils.isPathHttp(location)) {
                    NutsInput in = criteria.getWorkspace().io().input().setTypeName("nuts-repository.json").of(
                            location + "/nuts-repository.json"
                    );
                    try (InputStream s = in.open()) {
                        Map<String,Object> m=criteria.getWorkspace().formats().element().setContentType(NutsContentType.JSON)
                                .parse(s,Map.class);
                        if(m!=null){
                            String type = (String) m.get("type");
                            if(type!=null) {
                                switch (type) {
                                    case "maven":
                                    case "maven+dirtext":
                                    case "maven+dirlist":
                                    case "maven+github": {
                                        criteria.getConstraints().setType(type);
                                        return DEFAULT_SUPPORT;
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        //ignore
                    }
                    FilesFoldersApi.Item[] dirList = FilesFoldersApi.getDirItems(true, true, RemoteRepoApi.DIR_LIST, location, criteria.getWorkspace().createSession());
                    if(dirList!=null){
                        criteria.getConstraints().setType("maven+dirlist");
                        return DEFAULT_SUPPORT;
                    }
                    dirList = FilesFoldersApi.getDirItems(true, true, RemoteRepoApi.DIR_TEXT, location, criteria.getWorkspace().createSession());
                    if(dirList!=null){
                        criteria.getConstraints().setType("maven+dirtext");
                        return DEFAULT_SUPPORT;
                    }
                    in = criteria.getWorkspace().io().input().setTypeName("archetype-catalog.xml").of(
                            location + "/archetype-catalog.xml"
                    );
                    boolean exists = false;
                    try (InputStream s = in.open()) {
                        exists = true;
                    } catch (Exception ex) {
                        exists = false;
                    }
                    if (exists) {
                        criteria.getConstraints().setType(NutsConstants.RepoTypes.MAVEN);
                        return DEFAULT_SUPPORT;
                    }
                } else if (CoreIOUtils.isPathFile(location)) {
                    if (Files.exists(Paths.get(location).resolve("repository.xml"))) {
                        criteria.getConstraints().setType(NutsConstants.RepoTypes.MAVEN);
                        return DEFAULT_SUPPORT;
                    }
                }
            }
            return NO_SUPPORT;
        }
        switch (repositoryType){
            case "maven":
            case "maven+dirlist":
            case "maven+dirtext":
            case "maven+github":{
                return DEFAULT_SUPPORT;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsRepository create(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        final NutsRepositoryConfig config = options.getConfig();
        String type = config.getType();
        if(type==null){
            return null;
        }
        if (CoreIOUtils.isPathHttp(config.getLocation())) {
            return (new MavenRemoteRepository(options, workspace, parentRepository,type));
        }
        if (CoreIOUtils.isPathFile(config.getLocation())) {
            return new MavenFolderRepository(options, workspace, parentRepository);
        }
        return null;
    }
}
