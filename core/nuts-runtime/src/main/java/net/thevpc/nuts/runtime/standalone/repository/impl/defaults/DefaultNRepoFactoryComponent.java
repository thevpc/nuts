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
package net.thevpc.nuts.runtime.standalone.repository.impl.defaults;

import net.thevpc.nuts.core.NConstants;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfig;
import net.thevpc.nuts.platform.NPlatformHome;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NFolderRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.nuts.NHttpSrvRepository;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNRepoFactoryComponent implements NRepositoryFactoryComponent, NRepositorySpecDefaultResolverComponent,
        NRepositorySpecTemplateResolverComponent {
    private List<NRepositorySpec> templates =new ArrayList<>();

    public DefaultNRepoFactoryComponent() {
        templates.add(new NRepositorySpec()
                .setDeployWeight(100)
                .setName("system")
                .setFailSafe(true)
                .setOrder(NRepositorySpec.ORDER_SYSTEM_LOCAL)
                .setSourceLocation(NRepositoryLocation.of("nuts@"
                        + NPath.of(
                                NPlatformHome.SYSTEM.getWorkspaceLocation(
                                        NStoreType.LIB, NWorkspace.of().getStoredConfig().getHomeLocations(),
                                        NConstants.Names.DEFAULT_WORKSPACE_NAME))
                        .resolve(NConstants.Folders.ID)
                        .toString())
                ));
        templates.add(new NRepositorySpec().setName("vpc-public-maven")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@dotfilefs+https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"))
                .setAliases( "maven-thevpc-git"));
        templates.add(new NRepositorySpec().setName("nuts-public")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("nuts@dotfilefs+https://raw.githubusercontent.com/thevpc/nuts-public/master"))
                .setAliases( "vpc-public-nuts", "nuts-thevpc-git"));
        templates.add(new NRepositorySpec().setName("nuts-preview")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("nuts@dotfilefs+https://raw.githubusercontent.com/thevpc/nuts-preview/master"))
                .setTags(NConstants.RepoTags.PREVIEW)
                .setAliases( "preview"));
        templates.add(new NRepositorySpec().setName("thevpc")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@htmlfs+https://maven.thevpc.net"))
                .setTags(NConstants.RepoTags.PREVIEW)
                .setAliases( "dev"));
        templates.add(new NRepositorySpec().setName("thevpc-goodies")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@htmlfs+https://maven-goodies.thevpc.net"))
                .setAliases( "thevpc-goodies", "goodies"));
        templates.add(new NRepositorySpec()
                .setName(NConstants.Names.DEFAULT_REPOSITORY_NAME)
                .setDeployWeight(10)
                .setFailSafe(false)
                .setSourceLocation(NRepositoryLocation.of("nuts@" + NConstants.Names.DEFAULT_REPOSITORY_NAME))
        );
    }

    @NScore
    public static int getScore(NScorableContext criteria) {
        if (criteria != null) {
            NRepositoryFactoryContext context = criteria.getCriteria(NRepositoryFactoryContext.class);
            if (context != null) {
                String type = context.repositoryType();
                if (NConstants.RepoTypes.NUTS.equals(type)) {
                    return NScorable.DEFAULT_SCORE + 10;
                }
                NRepositoryConfig r = context.config();
                if (NBlankable.isBlank(type)) {
                    NPath rp = NPath.of(r.getLocation().getPath()).resolve("nuts-repository.json");
                    if (rp.exists()) {
                        r.setLocation(r.getLocation().setLocationType(NConstants.RepoTypes.NUTS));
                        return NScorable.DEFAULT_SCORE + 10;
                    }
                    return NScorable.DEFAULT_SCORE + 2;
                }
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public List<NRepositorySpec> getTemplateRepositoryDefinitions() {
        return Collections.unmodifiableList(templates);
    }

    @Override
    public List<NRepositorySpec> getDefaultRepositoryDefinitions() {
        List<NRepositorySpec> all=new ArrayList<>();
        NSession session= NSession.of();
        if (!NWorkspace.of().isSystemWorkspace()) {
            all.add(new NRepositorySpec().setName("system"));
        }
        all.add(new NRepositorySpec().setName("nuts-public"));
        if(session.isPreviewRepo()){
            all.add(new NRepositorySpec().setName("preview"));
            all.add(new NRepositorySpec().setName("dev"));
        }
        return all;
    }

    @Override
    public NRepository createRepository(NRepositoryFactoryContext context) {
        NRepositorySpec options=context.spec();
        NRepository parentRepository=context.parentRepository();
        String type = context.repositoryType();
        if (NBlankable.isBlank(type)) {
            return null;
        }
        if (NConstants.RepoTypes.NUTS.equals(type)) {
            if (NBlankable.isBlank(options.getSourceLocation()) ||
                    NPath.of(options.getSourceLocation().getPath()).isLocal()
            ) {
                return new NFolderRepository(options, parentRepository);
            } else if (NPath.of(options.getSourceLocation().getPath()).isURL()) {
                Map<String, String> e = options.getEnv();
                if (e != null) {
                    if (NLiteral.of(e.get("nuts-api-server")).asBoolean().orElse(false)) {
                        return (new NHttpSrvRepository(options, parentRepository));
                    }
                }
                return (new NFolderRepository(options, parentRepository));
            }
        }
        return null;
    }

}
