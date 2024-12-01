package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NRepositoryDB;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NRepositorySelectorList;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;

public class NRepositorySelectorHelper {

    public static NAddRepositoryOptions createRepositoryOptions(String s, boolean requireName) {
        return createRepositoryOptions(s,requireName,null);
    }

    public static NAddRepositoryOptions createRepositoryOptions(String s, boolean requireName, String[] tags) {
        NRepositorySelectorList r = NRepositorySelectorList.of(s, NRepositoryDB.of()).get();
        NRepositoryLocation[] all = r.resolve(null, NRepositoryDB.of());
        if (all.length != 1) {
            throw new IllegalArgumentException("unexpected");
        }
        return createRepositoryOptions(all[0], requireName, tags);
    }

    public static NAddRepositoryOptions createRepositoryOptions(NRepositoryLocation loc, boolean requireName) {
        return createRepositoryOptions(loc, requireName, null);
    }
    public static NAddRepositoryOptions createRepositoryOptions(NRepositoryLocation loc, boolean requireName, String[] tags) {
        String defaultName = null;
        NRepositoryDB db = NRepositoryDB.of();
        if (!db.findAllNamesByName(loc.getName()).isEmpty()) {
            defaultName = loc.getName();
        } else {
            String nn = db.getRepositoryOptionsByLocation(loc.getPath()).map(x->x.getName()).orNull();
            if (nn != null) {
                defaultName = nn;
            }
        }
        if (defaultName != null) {
            NAddRepositoryOptions u = db.getRepositoryOptionsByName(defaultName).orNull();
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
                if (u.getConfig() != null) {
                    u.getConfig().setTags(
                            new NRepositoryTagsListHelper()
                                    .add(u.getConfig().getTags())
                                    .add(tags).toArray()
                    );
                }
                return u;
            }
        }
        return createCustomRepositoryOptions(loc.getName(), loc.getFullLocation(), requireName);
    }

    public static NAddRepositoryOptions createCustomRepositoryOptions(String name, String url, boolean requireName) {
        if ((name == null || name.isEmpty()) && requireName) {
            NAssert.requireNonBlank(name, "repository name (<name>=<url>)");
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
        NAssert.requireNonBlank(name, "repository name (<name>=<url>)");
        NAssert.requireNonBlank(url, "repository url (<name>=<url>)");

        NRepositoryLocation loc = NRepositoryLocation.of(url);
        String sloc = NPath.of(loc.getPath()).toAbsolute().toString();
        loc = loc.setPath(sloc);

        return new NAddRepositoryOptions().setName(name)
                .setFailSafe(false).setCreate(true)
                .setOrder((!NBlankable.isBlank(url) && NPath.of(url).isLocal())
                        ? NAddRepositoryOptions.ORDER_USER_LOCAL
                        : NAddRepositoryOptions.ORDER_USER_REMOTE
                )
                .setConfig(new NRepositoryConfig()
                        .setLocation(loc)
                );
    }

    public static NAddRepositoryOptions createDefaultRepositoryOptions(String nameOrURL) {
        switch (nameOrURL) {
//            case "local": {
//                return new NAddRepositoryOptions()
//                        .setName(NConstants.Names.DEFAULT_REPOSITORY_NAME)
//                        .setDeployWeight(10)
//                        .setFailSafe(false)
//                        .setCreate(true)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("nuts@" + NConstants.Names.DEFAULT_REPOSITORY_NAME))
//                        );
//            }
//            case "system": {
//                return new NAddRepositoryOptions()
//                        .setDeployWeight(100)
//                        .setName("system")
//                        .setFailSafe(true).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_SYSTEM_LOCAL)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("nuts@"
//                                                + NPath.of(
//                                                        NPlatformHome.SYSTEM.getWorkspaceLocation(
//                                                                NStoreType.LIB, NWorkspace.get().stored().getHomeLocations(),
//                                                                NConstants.Names.DEFAULT_WORKSPACE_NAME))
//                                                .resolve(NConstants.Folders.ID)
//                                                .toString())
//                                        )
//                        );
//            }
//            case ".m2":
//            case "m2":
//            case "maven-local": {
//                return new NAddRepositoryOptions().setName("maven-local")
//                        .setFailSafe(false).setCreate(true).setOrder(NAddRepositoryOptions.ORDER_USER_LOCAL)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@"
//                                                        + NPath.ofUserHome().resolve(".m2/repository").toString()
//                                                )
//                                        )
//                        );
//            }
//            case "central":
//            case "maven-central": {
//                return new NAddRepositoryOptions().setName("maven-central")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@htmlfs:https://repo.maven.apache.org/maven2"))
//                                        .setEnv(NMaps.of(
//                                                "maven.solrsearch.url", "https://search.maven.org/solrsearch/select",
//                                                "maven.solrsearch.enable", "true"
//                                        ))
//                        );
//            }
//            case "maven":
//            case "mvn": {
//                return new NAddRepositoryOptions().setName("maven")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@maven"))
//                                        .setEnv(NMaps.of(
//                                                "maven.solrsearch.url", "https://search.maven.org/solrsearch/select",
//                                                "maven.solrsearch.enable", "true"
//                                        ))
//                        );
//            }
//            case "jcenter": {
//                return new NAddRepositoryOptions().setName("jcenter")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@https://jcenter.bintray.com"))
//                        );
//            }
//            case "jboss": {
//                return new NAddRepositoryOptions().setName("jboss")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@https://repository.jboss.org/nexus/content/repositories/releases"))
//                        );
//            }
//            case "clojars": {
//                return new NAddRepositoryOptions().setName("clojars")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@https://repo.clojars.org"))
//                        );
//            }
//            case "atlassian": {
//                return new NAddRepositoryOptions().setName("atlassian")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@htmlfs:https://packages.atlassian.com/maven/public"))
//                        );
//            }
//            case "atlassian-snapshot": {
//                return new NAddRepositoryOptions().setName("atlassian-atlassian")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@https://packages.atlassian.com/maven/public-snapshot"))
//                        );
//            }
//            case "oracle": {
//                return new NAddRepositoryOptions().setName("oracle")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@https://maven.oracle.com"))
//                        );
//            }
//            case "google": {
//                return new NAddRepositoryOptions().setName("google")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@https://maven.google.com"))
//                        );
//            }
//            case "spring":
//            case "spring-framework": {
//                return new NAddRepositoryOptions().setName("spring")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@https://repo.spring.io/release"))
//                        );
//            }
//            case "maven-thevpc-git":
//            case "vpc-public-maven": {
//                return new NAddRepositoryOptions().setName("vpc-public-maven")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@dotfilefs:https://raw.githubusercontent.com/thevpc/vpc-public-maven/master"))
//                        );
//            }
//            case "nuts-thevpc-git":
//            case "nuts-public": {
//                return new NAddRepositoryOptions().setName("nuts-public")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("nuts@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master"))
//                        );
//            }
//            case "nuts-preview": {
//                return new NAddRepositoryOptions().setName("nuts-preview")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("nuts@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master"))
//                                        .setTags(new String[]{NConstants.RepoTags.PREVIEW})
//                        );
//            }
//            case "dev":
//            case "thevpc": {
//                return new NAddRepositoryOptions().setName("thevpc")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@htmlfs:https://thevpc.net/maven"))
//                                        .setTags(new String[]{NConstants.RepoTags.PREVIEW})
//                        );
//            }
//            case "thevpc-goodies":
//            case "goodies": {
//                return new NAddRepositoryOptions().setName("thevpc")
//                        .setFailSafe(false).setCreate(true)
//                        .setOrder(NAddRepositoryOptions.ORDER_USER_REMOTE)
//                        .setConfig(
//                                new NRepositoryConfig()
//                                        .setLocation(NRepositoryLocation.of("maven@htmlfs:https://thevpc.net/maven-goodies"))
//                        );
//            }
        }
        return null;
    }

}
