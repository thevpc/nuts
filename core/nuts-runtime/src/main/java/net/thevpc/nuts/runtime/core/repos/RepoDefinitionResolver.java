package net.thevpc.nuts.runtime.core.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.thevpc.nuts.runtime.standalone.NutsRepositorySelector;

public class RepoDefinitionResolver {

    public static NutsAddRepositoryOptions createRepositoryOptions(String s, boolean requireName, NutsSession session) {
        NutsRepositorySelector.SelectorList r = NutsRepositorySelector.parse(s);
        NutsRepositorySelector[] all = r.resolveSelectors(null);
        if (all.length != 1) {
            throw new IllegalArgumentException("unexpected");
        }
        return createRepositoryOptions(all[0], requireName, session);
    }

    public static NutsAddRepositoryOptions createRepositoryOptions(NutsRepositorySelector s, boolean requireName, NutsSession session) {
        switch (s.getUrl()) {
            case "local": {
                return new NutsAddRepositoryOptions()
                        .setName(NutsConstants.Names.DEFAULT_REPOSITORY_NAME)
                        .setDeployOrder(10)
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
                        .setDeployOrder(100)
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
                                                        Nuts.getPlatformHomeFolder(null,
                                                                NutsStoreLocation.CONFIG, null,
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
                                        .setLocation(System.getProperty("user.home") + CoreIOUtils.syspath("/.m2/repository")).setType(NutsConstants.RepoTypes.MAVEN)
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
                                        .setLocation("https://repo.maven.apache.org/maven2")
                                        .setType("maven+dirlist")
                        );
            }
            case "jcenter": {
                return new NutsAddRepositoryOptions().setName("jcenter")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://jcenter.bintray.com")
                                        .setType("maven+dirlist")
                        );
            }
            case "jboss": {
                return new NutsAddRepositoryOptions().setName("jboss")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repository.jboss.org/nexus/content/repositories/releases")
                                        .setType("maven+dirlist")
                        );
            }
            case "clojars": {
                return new NutsAddRepositoryOptions().setName("clojars")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.clojars.org")
                                        .setType("maven+dirlist")
                        );
            }
            case "atlassian": {
                return new NutsAddRepositoryOptions().setName("atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://packages.atlassian.com/maven/public")
                                        .setType("maven+dirlist")
                        );
            }
            case "atlassian-snapshot": {
                return new NutsAddRepositoryOptions().setName("atlassian-atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://packages.atlassian.com/maven/public-snapshot")
                                        .setType("maven+dirlist")
                        );
            }
            case "oracle": {
                return new NutsAddRepositoryOptions().setName("oracle")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://maven.oracle.com")
                                        .setType("maven+dirlist")
                        );
            }
            case "google": {
                return new NutsAddRepositoryOptions().setName("google")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("maven+dirlist+https://maven.google.com")
                                        .setType("maven+dirlist")
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
                                        .setType("maven+dirlist")
                        );
            }
            case "maven-thevpc-git":
            case "vpc-public-maven": {
                return new NutsAddRepositoryOptions().setName("vpc-public-maven")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://raw.githubusercontent.com/thevpc/vpc-public-maven/master")
                                        .setType("maven+dirtext")
                        );
            }
            case "nuts-thevpc-git":
            case "vpc-public-nuts": {
                return new NutsAddRepositoryOptions().setName("vpc-public-nuts")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master")
                                        .setType("nuts+dirtext")
                        );
            }
            case "dev":
            case "thevpc": {
                return new NutsAddRepositoryOptions().setName("thevpc")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("http://thevpc.net/maven")
                                        .setType("maven+dirlist")
                        );
            }
            default: {
                String ppath = s.getUrl();
                String name = s.getName();
                if (name == null && requireName) {
                    throw new IllegalArgumentException("missing repository name (<name>=<url>) for " + s);
                }
                if (name == null) {
                    name = ppath;
                    if (name.startsWith("http://")) {
                        name = name.substring("http://".length());
                    } else if (name.startsWith("https://")) {
                        name = name.substring("https://".length());
                    }

                    name = name.replaceAll("[/\\:?.]", "-");
                    while (name.endsWith("-")) {
                        name = name.substring(0, name.length() - 1);
                    }
                    while (name.startsWith("-")) {
                        name = name.substring(1);
                    }
                }
                if (name.isEmpty() || ppath.isEmpty()) {
                    throw new IllegalArgumentException("missing repository name (<name>=<url>) for " + s);
                }
//                boolean localRepo = CoreIOUtils.isPathFile(ppath);
                return new NutsAddRepositoryOptions().setName(name)
                        .setFailSafe(false).setCreate(true)
                        .setOrder(CoreIOUtils.isPathFile(ppath)
                                ? NutsAddRepositoryOptions.ORDER_USER_LOCAL
                                : NutsAddRepositoryOptions.ORDER_USER_REMOTE
                        )
                        .setConfig(new NutsRepositoryConfig()
                                .setLocation(ppath)
                        );
            }
        }
    }

    private static String[] extractKeyEqValue(String s) {
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(s);
        if (matcher.find()) {
            return new String[]{matcher.group("name"), matcher.group("value")};
        } else {
            return null;
        }
    }

}
