package net.thevpc.nuts.runtime.core.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepoDefinitionResolver {
    public static NutsAddRepositoryOptions createRepositoryOptions(String s, boolean requireName, NutsWorkspace ws) {
        switch (s) {
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
                        )
                        ;
            }
            case "system": {
                return new NutsAddRepositoryOptions()
                        .setDeployOrder(100)
                        .setName("system")
                        .setLocation(
                                CoreIOUtils.getNativePath(
                                        Nuts.getPlatformHomeFolder(null,
                                                NutsStoreLocation.CONFIG, null,
                                                true,
                                                NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                                                + "/" + NutsConstants.Folders.REPOSITORIES
                                                + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
                                ))
                        .setFailSafe(true).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_SYSTEM_LOCAL)
                        ;
            }
            case ".m2":
            case "m2":
            case "maven-local": {
                return new NutsAddRepositoryOptions().setName("maven-local")
                        .setFailSafe(false).setCreate(true).setOrder(NutsAddRepositoryOptions.ORDER_USER_LOCAL)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation(System.getProperty("user.home") + CoreIOUtils.syspath("/.m2/repository")).setType(NutsConstants.RepoTypes.MAVEN)
                        )
                        ;
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
                        )
                        ;
            }
            case "jcenter": {
                return new NutsAddRepositoryOptions().setName("jcenter")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://jcenter.bintray.com")
                                        .setType("maven+dirlist")
                        )
                        ;
            }
            case "jboss": {
                return new NutsAddRepositoryOptions().setName("jboss")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repository.jboss.org/nexus/content/repositories/releases")
                                        .setType("maven+dirlist")
                        )
                        ;
            }
            case "clojars": {
                return new NutsAddRepositoryOptions().setName("clojars")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.clojars.org")
                                        .setType("maven+dirlist")
                        )
                        ;
            }
            case "atlassian": {
                return new NutsAddRepositoryOptions().setName("atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://packages.atlassian.com/maven/public")
                                        .setType("maven+dirlist")
                        )
                        ;
            }
            case "atlassian-snapshot": {
                return new NutsAddRepositoryOptions().setName("atlassian-atlassian")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://packages.atlassian.com/maven/public-snapshot")
                                        .setType("maven+dirlist")
                        )
                        ;
            }
            case "oracle": {
                return new NutsAddRepositoryOptions().setName("oracle")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://maven.oracle.com")
                                        .setType("maven+dirlist")
                        )
                        ;
            }
            case "google": {
                return new NutsAddRepositoryOptions().setName("google")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("maven+dirlist+https://maven.google.com")
                                        .setType("maven+dirlist")
                        )
                        ;
            }
            case "spring":
            case "spring-framework":{
                return new NutsAddRepositoryOptions().setName("spring")
                        .setFailSafe(false).setCreate(true)
                        .setOrder(NutsAddRepositoryOptions.ORDER_USER_REMOTE)
                        .setConfig(
                                new NutsRepositoryConfig()
                                        .setLocation("https://repo.spring.io/release")
                                        .setType("maven+dirlist")
                        )
                        ;
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
                        )
                        ;
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
                        )
                        ;
            }
            default: {
                String[] kv = extractKeyEqValue(s);
                if (kv == null) {
                    if(requireName) {
                        throw new IllegalArgumentException("missing repository name (<name>=<url>) for " + s);
                    }
                    boolean localRepo = CoreIOUtils.isPathFile(s);
                    return new NutsAddRepositoryOptions().setName(null)
                            .setFailSafe(false).setCreate(true)
                            .setOrder(
                                    localRepo ?
                                            NutsAddRepositoryOptions.ORDER_USER_LOCAL
                                            : NutsAddRepositoryOptions.ORDER_USER_REMOTE
                            )
                            .setConfig(
                                    new NutsRepositoryConfig()
                                            .setLocation(s)
                            )
                            ;
                }else {
                    boolean localRepo = CoreIOUtils.isPathFile(kv[1]);
                    return new NutsAddRepositoryOptions().setName(kv[0])
                            .setFailSafe(false).setCreate(true)
                            .setOrder(
                                    CoreIOUtils.isPathFile(kv[1]) ?
                                            NutsAddRepositoryOptions.ORDER_USER_LOCAL
                                            : NutsAddRepositoryOptions.ORDER_USER_REMOTE
                            )
                            .setConfig(
                                    new NutsRepositoryConfig()
                                            .setLocation(kv[1])
                            )
                            ;
                }
            }
        }
    }

    private static String[] extractKeyEqValue(String s) {
        Matcher matcher = Pattern.compile("(?<name>[a-zA-Z-_]+)=(?<value>.+)").matcher(s);
        if (matcher.find()) {
            return new String[]{matcher.group("key"), matcher.group("value")};
        } else {
            return null;
        }
    }
}
