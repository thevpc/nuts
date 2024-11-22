package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.common.collections.NMaps;
import net.thevpc.nuts.runtime.standalone.util.NMapWithAlias;
import net.thevpc.nuts.spi.NRepositoryDB;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NPlatformHome;

import java.util.*;

public class DefaultNRepositoryDB implements NRepositoryDB {
    private final NMapWithAlias<String, NAddRepositoryOptions> optionByName = new NMapWithAlias<>();
    private final NMapWithAlias<String, NAddRepositoryOptions> optionByLocation = new NMapWithAlias<>();

    private DefaultNRepositoryDB() {
        reg(new NAddRepositoryOptions()
                .setDeployWeight(100)
                .setName("system")
                .setFailSafe(true).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_SYSTEM_LOCAL)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("nuts@"
                                        + NPath.of(
                                                NPlatformHome.SYSTEM.getWorkspaceLocation(
                                                        NStoreType.LIB, NConfigs.of().stored().getHomeLocations(),
                                                        NConstants.Names.DEFAULT_WORKSPACE_NAME))
                                        .resolve(NConstants.Folders.ID)
                                        .toString())
                                )
                ));
        reg(new NAddRepositoryOptions().setName("maven")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@maven"))
                                .setEnv(NMaps.of(
                                        "maven.solrsearch.url", "https://search.maven.org/solrsearch/select",
                                        "maven.solrsearch.enable", "true"
                                ))
                ),"mvn");
        reg(new NAddRepositoryOptions().setName("maven-central")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@htmlfs:https://repo.maven.apache.org/maven2"))
                                .setEnv(NMaps.of(
                                        "maven.solrsearch.url", "https://search.maven.org/solrsearch/select",
                                        "maven.solrsearch.enable", "true"
                                ))
                ),"central");
        reg(new NAddRepositoryOptions().setName("jcenter")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@https://jcenter.bintray.com"))
                ));
        reg(new NAddRepositoryOptions().setName("jboss")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@https://repository.jboss.org/nexus/content/repositories/releases"))
                ));
        reg(new NAddRepositoryOptions().setName("clojars")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@https://repo.clojars.org"))
                ));
        reg(new NAddRepositoryOptions().setName("atlassian")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@htmlfs:https://packages.atlassian.com/maven/public"))
                ));
        reg(new NAddRepositoryOptions().setName("atlassian-atlassian")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@https://packages.atlassian.com/maven/public-snapshot"))
                ));
        reg(new NAddRepositoryOptions().setName("oracle")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@https://maven.oracle.com"))
                ));
        reg(new NAddRepositoryOptions().setName("google")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@https://maven.google.com"))
                ));
        reg(new NAddRepositoryOptions().setName("spring")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@https://repo.spring.io/release"))
                ), "spring-framework");
        reg(new NAddRepositoryOptions().setName("vpc-public-maven")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"))
                ), "maven-thevpc-git");
        reg(new NAddRepositoryOptions().setName("nuts-public")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("nuts@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master"))
                ),  "vpc-public-nuts", "nuts-thevpc-git");
        reg(new NAddRepositoryOptions().setName("nuts-preview")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("nuts@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master"))
                                .setTags(new String[]{NConstants.RepoTags.PREVIEW})
                ), "preview");
        reg(new NAddRepositoryOptions().setName("thevpc")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@htmlfs:https://thevpc.net/maven"))
                                .setTags(new String[]{NConstants.RepoTags.PREVIEW})
                ), "dev");
        reg(new NAddRepositoryOptions().setName("thevpc-goodies")
                .setFailSafe(false).setCreate(true)
                .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@htmlfs:https://thevpc.net/maven-goodies"))
                ), "thevpc-goodies", "goodies");
        reg(new NAddRepositoryOptions()
                .setName(NConstants.Names.DEFAULT_REPOSITORY_NAME)
                .setDeployWeight(10)
                .setFailSafe(false)
                .setCreate(true)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("nuts@" + NConstants.Names.DEFAULT_REPOSITORY_NAME))
                ));
        reg(new NAddRepositoryOptions().setName("maven-local")
                .setFailSafe(false).setCreate(true).setOrder(NAddRepositoryOptions.ORDER_USER_LOCAL)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(NRepositoryLocation.of("maven@"
                                                + NPath.ofUserHome().resolve(".m2/repository").toString()
                                        )
                                )
                ),".m2","m2");
    }

    @Override
    public Set<String> findAllNamesByName(String name) {
        return optionByName.keySetWithAlias(name);
    }

    @Override
    public NOptional<NAddRepositoryOptions> getRepositoryOptionsByName(String name) {
        return NOptional.of(optionByName.get(name),()-> NMsg.ofC("repository %s",name)).map(NAddRepositoryOptions::copy);
    }

    @Override
    public NOptional<NAddRepositoryOptions> getRepositoryOptionsByLocation(String name) {
        return NOptional.of(optionByLocation.get(name),()-> NMsg.ofC("repository %s",name)).map(NAddRepositoryOptions::copy);
    }

    private void reg(NAddRepositoryOptions options, String... aliases) {
        String name = options.getName();
        String location = options.getConfig().getLocation().toString();
        optionByName.put(name, options);
        for (String alias : aliases) {
            optionByName.alias(alias, name);
        }
        optionByLocation.put(location, options);
    }



    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
