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
                .deployWeight(100)
                .name("system")
                .failSafe(true)
                .order(NRepositorySpec.ORDER_SYSTEM_LOCAL)
                .sourceLocation(NRepositoryLocation.of("nuts@"
                        + NPath.of(
                                NPlatformHome.SYSTEM.getWorkspaceLocation(
                                        NStoreType.LIB, NWorkspace.of().storedConfig().homeLocations(),
                                        NConstants.Names.DEFAULT_WORKSPACE_NAME))
                        .resolve(NConstants.Folders.ID)
                        .toString())
                ));
        templates.add(new NRepositorySpec().name("vpc-public-maven")
                .failSafe(false)
                .order(NRepositorySpec.ORDER_USER_REMOTE)
                .sourceLocation(NRepositoryLocation.of("maven@dotfilefs+https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"))
                .aliases( "maven-thevpc-git"));
        templates.add(new NRepositorySpec().name("nuts-public")
                .failSafe(false)
                .order(NRepositorySpec.ORDER_USER_REMOTE)
                .sourceLocation(NRepositoryLocation.of("nuts@dotfilefs+https://raw.githubusercontent.com/thevpc/nuts-public/master"))
                .aliases( "vpc-public-nuts", "nuts-thevpc-git"));
        templates.add(new NRepositorySpec().name("nuts-preview")
                .failSafe(false)
                .order(NRepositorySpec.ORDER_USER_REMOTE)
                .sourceLocation(NRepositoryLocation.of("nuts@dotfilefs+https://raw.githubusercontent.com/thevpc/nuts-preview/master"))
                .tags(NConstants.RepoTags.PREVIEW)
                .aliases( "preview"));
        templates.add(new NRepositorySpec().name("thevpc")
                .failSafe(false)
                .order(NRepositorySpec.ORDER_USER_REMOTE)
                .sourceLocation(NRepositoryLocation.of("maven@htmlfs+https://maven.thevpc.net"))
                .tags(NConstants.RepoTags.PREVIEW)
                .aliases( "dev"));
        templates.add(new NRepositorySpec().name("thevpc-goodies")
                .failSafe(false)
                .order(NRepositorySpec.ORDER_USER_REMOTE)
                .sourceLocation(NRepositoryLocation.of("maven@htmlfs+https://maven-goodies.thevpc.net"))
                .aliases( "thevpc-goodies", "goodies"));
        templates.add(new NRepositorySpec()
                .name(NConstants.Names.DEFAULT_REPOSITORY_NAME)
                .deployWeight(10)
                .failSafe(false)
                .sourceLocation(NRepositoryLocation.of("nuts@" + NConstants.Names.DEFAULT_REPOSITORY_NAME))
        );
    }

    @NScore
    public static int getScore(NScorableContext criteria) {
        if (criteria != null) {
            NRepositoryFactoryContext context = criteria.criteria(NRepositoryFactoryContext.class);
            if (context != null) {
                String type = context.repositoryType();
                if (NConstants.RepoTypes.NUTS.equals(type)) {
                    return NScorable.DEFAULT_SCORE + 10;
                }
                NRepositoryConfig r = context.config();
                if (NBlankable.isBlank(type)) {
                    NPath rp = NPath.of(r.location().path()).resolve("nuts-repository.json");
                    if (rp.exists()) {
                        r.location(r.location().locationType(NConstants.RepoTypes.NUTS));
                        return NScorable.DEFAULT_SCORE + 10;
                    }
                    return NScorable.DEFAULT_SCORE + 2;
                }
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public List<NRepositorySpec> templateRepositoryDefinitions() {
        return Collections.unmodifiableList(templates);
    }

    @Override
    public List<NRepositorySpec> defaultRepositoryDefinitions() {
        List<NRepositorySpec> all=new ArrayList<>();
        NSession session= NSession.of();
        if (!NWorkspace.of().isSystemWorkspace()) {
            all.add(new NRepositorySpec().name("system"));
        }
        all.add(new NRepositorySpec().name("nuts-public"));
        if(session.isPreviewRepo()){
            all.add(new NRepositorySpec().name("preview"));
            all.add(new NRepositorySpec().name("dev"));
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
            if (NBlankable.isBlank(options.sourceLocation()) ||
                    NPath.of(options.sourceLocation().path()).isLocal()
            ) {
                return new NFolderRepository(options, parentRepository);
            } else if (NPath.of(options.sourceLocation().path()).isURL()) {
                Map<String, String> e = options.env();
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
