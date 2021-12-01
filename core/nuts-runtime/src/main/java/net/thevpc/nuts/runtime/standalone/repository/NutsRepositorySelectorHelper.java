package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositorySelectorList;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsRepositoryURL;

public class NutsRepositorySelectorHelper {
    public static NutsAddRepositoryOptions createRepositoryOptions(String s, boolean requireName, NutsSession session) {
        NutsRepositorySelectorList r = NutsRepositorySelectorList.of(s,DefaultNutsRepositoryDB.INSTANCE,session);
        NutsRepositoryURL[] all = r.resolve(null,DefaultNutsRepositoryDB.INSTANCE);
        if (all.length != 1) {
            throw new IllegalArgumentException("unexpected");
        }
        return createRepositoryOptions(all[0], requireName, session);
    }

    public static NutsAddRepositoryOptions createRepositoryOptions(NutsRepositoryURL s, boolean requireName, NutsSession session) {
        NutsRepositoryURL nru = NutsRepositoryURL.of(s.getLocation()).setName(s.getName());
        String defaultName = null;
        NutsRepositoryDB db = DefaultNutsRepositoryDB.INSTANCE;
        if (db.isDefaultRepositoryName(nru.getName())) {
            defaultName = nru.getName();
        } else {
            String nn = db.getRepositoryNameByURL(nru.getLocation());
            if (nn != null) {
                defaultName = nn;
            }
        }
        if (defaultName != null) {
            NutsAddRepositoryOptions u = createDefaultRepositoryOptions(defaultName, session);
            if (u != null
                    && (nru.getLocation().isEmpty()
                    || nru.getLocation().equals(u.getConfig().getLocation())
                    || nru.getURLString().equals(u.getConfig().getLocation()))) {
                //this is acceptable!
                if (!u.getName().equals(s.getName())) {
                    u.setName(s.getName());
                }
            }
            if (u != null) {
                return u;
            }
        }
        return createCustomRepositoryOptions(nru.getName(), nru.getURLString(), requireName, session);
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
                .setOrder(CoreIOUtils.isPathFile(url)
                        ? NutsAddRepositoryOptions.ORDER_USER_LOCAL
                        : NutsAddRepositoryOptions.ORDER_USER_REMOTE
                )
                .setConfig(new NutsRepositoryConfig()
                        .setLocation(url)
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
                                        .setLocation(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                                        .setType(NutsConstants.RepoTypes.NUTS)
                        );
            }
            case "system": {
                return new NutsAddRepositoryOptions()
                        .setDeployWeight(100)
                        .setName("system")
                        //                        .setLocation(
                        //                                CoreIOUtils.getNativePath(
                        //                                        Nuts.getPlatformHomeFolder(null,
                        //                                                NutsStoreLocation.CONFIG, null,
                        //                                                true,
                        //                                                NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                        //                                        + "/" + NutsConstants.Folders.REPOSITORIES
                        //                                        + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
                        //                                ))
                        .setFailSafe(true).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_SYSTEM_LOCAL)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(
                                                CoreIOUtils.getNativePath(
                                                        NutsUtilPlatforms.getPlatformHomeFolder(null,
                                                                NutsStoreLocation.CONFIG, session.config().stored().getHomeLocations(),
                                                                true,
                                                                NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                                                                + "/" + NutsConstants.Folders.REPOSITORIES
                                                                + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
                                                )
                                        )
                                        .setType(NutsConstants.RepoTypes.NUTS)
                        );
            }
            case ".m2":
            case "m2":
            case "maven-local": {
                return new NutsAddRepositoryOptions().setName("maven-local")
                        .setFailSafe(false).setCreate(true).setOrder(NutsAddRepositoryOptions.ORDER_USER_LOCAL)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(System.getProperty("user.home") + CoreIOUtils.getNativePath("/.m2/repository")).setType(NutsConstants.RepoTypes.MAVEN)
                        );
            }
            case "maven":
            case "central":
            case "maven-central": {
                return new NutsAddRepositoryOptions().setName("maven-central")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("htmlfs:https://repo.maven.apache.org/maven2")
                                        .setType("maven")
                        );
            }
            case "jcenter": {
                return new NutsAddRepositoryOptions().setName("jcenter")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://jcenter.bintray.com")
                                        .setType("maven")
                        );
            }
            case "jboss": {
                return new NutsAddRepositoryOptions().setName("jboss")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repository.jboss.org/nexus/content/repositories/releases")
                                        .setType("maven")
                        );
            }
            case "clojars": {
                return new NutsAddRepositoryOptions().setName("clojars")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.clojars.org")
                                        .setType("maven")
                        );
            }
            case "atlassian": {
                return new NutsAddRepositoryOptions().setName("atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("htmlfs:https://packages.atlassian.com/maven/public")
                                        .setType("maven")
                        );
            }
            case "atlassian-snapshot": {
                return new NutsAddRepositoryOptions().setName("atlassian-atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://packages.atlassian.com/maven/public-snapshot")
                                        .setType("maven")
                        );
            }
            case "oracle": {
                return new NutsAddRepositoryOptions().setName("oracle")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://maven.oracle.com")
                                        .setType("maven")
                        );
            }
            case "google": {
                return new NutsAddRepositoryOptions().setName("google")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://maven.google.com")
                                        .setType("maven")
                        );
            }
            case "spring":
            case "spring-framework": {
                return new NutsAddRepositoryOptions().setName("spring")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.spring.io/release")
                                        .setType("maven")
                        );
            }
            case "maven-thevpc-git":
            case "vpc-public-maven": {
                return new NutsAddRepositoryOptions().setName("vpc-public-maven")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-maven/master")
                                        .setType("maven")
                        );
            }
            case "nuts-thevpc-git":
            case "vpc-public-nuts": {
                return new NutsAddRepositoryOptions().setName("vpc-public-nuts")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master")
                                        .setType("nuts")
                        );
            }
            case "dev":
            case "thevpc": {
                return new NutsAddRepositoryOptions().setName("thevpc")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("htmlfs:http://thevpc.net/maven")
                                        .setType("maven")
                        );
            }
        }
        return null;
    }



}
