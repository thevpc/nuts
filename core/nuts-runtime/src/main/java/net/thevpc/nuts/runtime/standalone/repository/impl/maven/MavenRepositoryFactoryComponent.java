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

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfig;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/15/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class MavenRepositoryFactoryComponent implements NRepositoryFactoryComponent, NRepositorySpecDefaultResolverComponent,
        NRepositorySpecTemplateResolverComponent {
    private final List<NRepositorySpec> templates = new ArrayList<>();

    public MavenRepositoryFactoryComponent() {
        templates.add(new NRepositorySpec().setName("maven")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@maven"))
                .setEnv(NMaps.of(
                        "maven.solrsearch.url", "https://search.maven.org/solrsearch/select",
                        "maven.solrsearch.enable", "true"
                ))
                .setAliases("mvn"));
        templates.add(new NRepositorySpec().setName("maven-central")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@htmlfs+https://repo.maven.apache.org/maven2"))
                .setEnv(NMaps.of(
                                "maven.solrsearch.url", "https://search.maven.org/solrsearch/select",
                                "maven.solrsearch.enable", "true"
                        )
                ).setAliases("central"));
        templates.add(new NRepositorySpec().setName("jcenter")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@https://jcenter.bintray.com"))
        );
        templates.add(new NRepositorySpec().setName("jboss")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@https://repository.jboss.org/nexus/content/repositories/releases"))
        );
        templates.add(new NRepositorySpec().setName("clojars")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@https://repo.clojars.org"))
        );
        templates.add(new NRepositorySpec().setName("atlassian")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@htmlfs+https://packages.atlassian.com/maven/public"))
        );
        templates.add(new NRepositorySpec().setName("atlassian-atlassian")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@https://packages.atlassian.com/maven/public-snapshot"))
        );
        templates.add(new NRepositorySpec().setName("oracle")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@https://maven.oracle.com"))
        );
        templates.add(new NRepositorySpec().setName("google")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@https://maven.google.com"))
        );
        templates.add(new NRepositorySpec().setName("spring")
                .setFailSafe(false)
                .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                .setSourceLocation(NRepositoryLocation.of("maven@https://repo.spring.io/release"))
                .setAliases("spring-framework"));
        templates.add(new NRepositorySpec().setName("maven-local")
                .setFailSafe(false).setOrder(NRepositorySpec.ORDER_USER_LOCAL)
                .setSourceLocation(NRepositoryLocation.of("maven@"
                                + NPath.ofUserHome().resolve(".m2/repository").toString()
                        )
                )
                .setAliases(".m2", "m2"));
    }

    @Override
    public List<NRepositorySpec> getDefaultRepositoryDefinitions() {
        return Collections.singletonList(
                new NRepositorySpec().setName("maven")
        );
    }

    @Override
    public List<NRepositorySpec> getTemplateRepositoryDefinitions() {
        return Collections.unmodifiableList(templates.stream().map(x->x.copy()).collect(Collectors.toList()));
    }

    @Override
    public NRepository createRepository(NRepositoryFactoryContext context) {
        NRepositorySpec options = context.spec();
        NRepository parentRepository = context.parentRepository();
        String type = context.repositoryType();
        if (MavenUtils.isMavenSettingsRepository(options)) {
            return new MavenSettingsRepository(options, parentRepository);
        }
        if (NBlankable.isBlank(type)) {
            return null;
        }
        NPath p = NPath.of(options.getSourceLocation().getPath());
        String pr = NStringUtils.trim(p.getProtocol());
        switch (pr) {
            //non traversable!
            case "http":
            case "https": {

                NPath nr = p.resolve(".nuts-repository");
                if (nr.exists()) {
                    NElement e = null;
                    String repositoryType = null;
                    String repositoryName = null;
                    String repositoryLayout = null;
                    try {
                        e = NElementReader.ofJson().read(nr);
                    } catch (Exception ex) {
                        // just ignore
                    }
                    if (e != null && e.isAnyObject()) {
                        NObjectElement o = e.asObject().get();
                        repositoryType = o.getStringValue("repositoryType").orNull();
                        repositoryName = o.getStringValue("repositoryName").orNull();
                        repositoryLayout = o.getStringValue("repositoryLayout").orNull();
                    }
                    if (!NBlankable.isBlank(repositoryLayout)) {
                        options.setSourceLocation(options.getSourceLocation().setPath(NStringUtils.trim(repositoryLayout) + "+" + options.getSourceLocation().getPath()));
                    }
                    return new MavenFolderRepository(options, parentRepository);
                } else {
                    return new MavenRemoteXmlRepository(options, parentRepository);
                }
            }
        }
        return new MavenFolderRepository(options, parentRepository);
    }

    @NScore
    public static int getScore(NScorableContext criteria) {
        if (criteria != null) {
            NRepositoryFactoryContext context = criteria.getCriteria(NRepositoryFactoryContext.class);
            if (context != null) {
                String type = context.repositoryType();
                if (NBlankable.isBlank(type)) {
                    return NScorable.UNSUPPORTED_SCORE;
                }
                if (NConstants.RepoTypes.MAVEN.equals(type)) {
                    return NScorable.DEFAULT_SCORE + 10;
                }
                if (NBlankable.isBlank(type)) {
                    return NScorable.DEFAULT_SCORE + 5;
                }
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}
