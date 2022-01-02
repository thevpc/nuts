package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.spi.NutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
import net.thevpc.nuts.spi.NutsRepositorySelectorList;

import java.util.HashMap;
import java.util.Objects;

public class NutsRepositorySelectorHelper {

    public static NutsAddRepositoryOptions createRepositoryOptions(String s, boolean requireName, NutsSession session) {
        NutsRepositorySelectorList r = NutsRepositorySelectorList.of(s, NutsRepositoryDB.of(session), session);
        NutsRepositoryLocation[] all = r.resolve(null, NutsRepositoryDB.of(session));
        if (all.length != 1) {
            throw new IllegalArgumentException("unexpected");
        }
        return createRepositoryOptions(all[0], requireName, session);
    }

    public static NutsAddRepositoryOptions createRepositoryOptions(NutsRepositoryLocation loc, boolean requireName, NutsSession session) {
        String defaultName = null;
        NutsRepositoryDB db = NutsRepositoryDB.of(session);
        if (db.isDefaultRepositoryName(loc.getName())) {
            defaultName = loc.getName();
        } else {
            String nn = db.getRepositoryNameByURL(loc.getPath());
            if (nn != null) {
                defaultName = nn;
            }
        }
        if (defaultName != null) {
            NutsAddRepositoryOptions u = createDefaultRepositoryOptions(defaultName, session);
            if (u != null
                    && (loc.getPath().isEmpty()
                    || Objects.equals(loc.getPath(), u.getConfig().getLocation().getPath())
                    || Objects.equals(loc.getFullLocation(), u.getConfig().getLocation().getFullLocation()))) {
                //this is acceptable!
                if (!u.getName().equals(loc.getName())) {
                    u.setName(loc.getName());
                }
            }
            if (u != null) {
                return u;
            }
        }
        return createCustomRepositoryOptions(loc.getName(), loc.getFullLocation(), requireName, session);
    }

    public static NutsAddRepositoryOptions createCustomRepositoryOptions(String name, String url, boolean requireName, NutsSession session) {
        if ((name == null || name.isEmpty()) && requireName) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing repository name (<name>=<url>) for %s", name));
        }
        if (name == null || name.isEmpty()) {
            name = url;
            if (name.startsWith("http://")) {
                name = name.substring("http://".length());
            } else if (name.startsWith("https://")) {
                name = name.substring("https://".length());
            }

            name = name.replaceAll("[/\\\\:?.]", "-");
            while (name.endsWith("-")) {
                name = name.substring(0, name.length() - 1);
            }
            while (name.startsWith("-")) {
                name = name.substring(1);
            }
        }
        if (name.isEmpty() || url.isEmpty()) {
            throw new IllegalArgumentException("missing repository name (<name>=<url>) for " + name);
        }
//                boolean localRepo = CoreIOUtils.isPathFile(ppath);
        return new NutsAddRepositoryOptions().setName(name)
                .setFailSafe(false).setCreate(true)
                .setOrder((!NutsBlankable.isBlank(url) && NutsPath.of(url, session).isFile())
                        ? NutsAddRepositoryOptions.ORDER_USER_LOCAL
                        : NutsAddRepositoryOptions.ORDER_USER_REMOTE
                )
                .setConfig(new NutsRepositoryConfig()
                        .setLocation(NutsRepositoryLocation.of(url))
                );
    }

    public static NutsAddRepositoryOptions createDefaultRepositoryOptions(String nameOrURL, NutsSession session) {
        switch (nameOrURL) {
            case "local": {
                return new NutsAddRepositoryOptions()
                        .setName(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                        .setDeployWeight(10)
                        .setFailSafe(false)
                        .setCreate(true)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("nuts@" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME))
                        );
            }
            case "system": {
                return new NutsAddRepositoryOptions()
                        .setDeployWeight(100)
                        .setName("system")
                        .setFailSafe(true).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_SYSTEM_LOCAL)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("nuts@"
                                                + NutsPath.of(NutsUtilPlatforms.getPlatformHomeFolder(null,
                                                        NutsStoreLocation.LIB, session.config().stored().getHomeLocations(),
                                                        true,
                                                        NutsConstants.Names.DEFAULT_WORKSPACE_NAME), session)
                                                        .resolve(NutsConstants.Folders.ID)
                                                        .toString())
                                        )
                        );
            }
            case ".m2":
            case "m2":
            case "maven-local": {
                return new NutsAddRepositoryOptions().setName("maven-local")
                        .setFailSafe(false).setCreate(true).setOrder(NutsAddRepositoryOptions.ORDER_USER_LOCAL)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@"
                                                + NutsPath.ofUserHome(session).resolve(".m2/repository").toString()
                                        )
                                        )
                        );
            }
            case "maven":
            case "mvn":
            case "central":
            case "maven-central": {
                return new NutsAddRepositoryOptions().setName("maven-central")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@htmlfs:https://repo.maven.apache.org/maven2"))
                                        .setEnv(CoreCollectionUtils.fill(new HashMap<>(),
                                                "maven.solrsearch.url", "https://search.maven.org/solrsearch/select",
                                                "maven.solrsearch.enable", "true"
                                        ))
                        );
            }
            case "jcenter": {
                return new NutsAddRepositoryOptions().setName("jcenter")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@https://jcenter.bintray.com"))
                        );
            }
            case "jboss": {
                return new NutsAddRepositoryOptions().setName("jboss")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@https://repository.jboss.org/nexus/content/repositories/releases"))
                        );
            }
            case "clojars": {
                return new NutsAddRepositoryOptions().setName("clojars")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@https://repo.clojars.org"))
                        );
            }
            case "atlassian": {
                return new NutsAddRepositoryOptions().setName("atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@htmlfs:https://packages.atlassian.com/maven/public"))
                        );
            }
            case "atlassian-snapshot": {
                return new NutsAddRepositoryOptions().setName("atlassian-atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@https://packages.atlassian.com/maven/public-snapshot"))
                        );
            }
            case "oracle": {
                return new NutsAddRepositoryOptions().setName("oracle")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@https://maven.oracle.com"))
                        );
            }
            case "google": {
                return new NutsAddRepositoryOptions().setName("google")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@https://maven.google.com"))
                        );
            }
            case "spring":
            case "spring-framework": {
                return new NutsAddRepositoryOptions().setName("spring")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@https://repo.spring.io/release"))
                        );
            }
            case "maven-thevpc-git":
            case "vpc-public-maven": {
                return new NutsAddRepositoryOptions().setName("vpc-public-maven")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"))
                        );
            }
            case "nuts-thevpc-git":
            case "vpc-public-nuts": {
                return new NutsAddRepositoryOptions().setName("vpc-public-nuts")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master"))
                        );
            }
            case "dev":
            case "thevpc":
            case "preview": {
                return new NutsAddRepositoryOptions().setName("thevpc")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(NutsRepositoryLocation.of("maven@htmlfs:http://thevpc.net/maven"))
                        );
            }
        }
        return null;
    }

}
