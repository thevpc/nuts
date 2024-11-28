package net.thevpc.nuts.boot.reserved;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.NAddRepositoryOptionsBoot;
import net.thevpc.nuts.boot.NRepositoryConfigBoot;
import net.thevpc.nuts.boot.NRepositoryLocationBoot;
import net.thevpc.nuts.boot.reserved.util.NPlatformHomeBoot;
import net.thevpc.nuts.boot.reserved.util.NReservedIOUtilsBoot;

import java.util.*;

public class NReservedBootRepositoryDB  {
    private final Map<String, NAddRepositoryOptionsBoot> defaultRepositoriesByName = new LinkedHashMap<>();
    private final Map<String, NAddRepositoryOptionsBoot> defaultRepositoriesByLocation = new LinkedHashMap<>();
    private final Map<String, String> aliasToBase = new LinkedHashMap<>();
    private final Map<String, Set<String>> baseToAliases = new LinkedHashMap<>();
    private static final NReservedBootRepositoryDB instance=new NReservedBootRepositoryDB();
    public static NReservedBootRepositoryDB of(){return instance;};

    public NReservedBootRepositoryDB() {
        reg(false, "system", NAddRepositoryOptionsBoot.ORDER_SYSTEM_LOCAL, "nuts@" + NReservedIOUtilsBoot.getNativePath(
                        NPlatformHomeBoot.SYSTEM.getWorkspaceStore(
                                "LIB",
                                NConstants.Names.DEFAULT_WORKSPACE_NAME) + "/" + NConstants.Folders.ID
                )
        );
        reg(false, "maven", NAddRepositoryOptionsBoot.ORDER_USER_LOCAL, "maven");
        reg(false, "maven-central", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://repo.maven.apache.org/maven2");
        reg(false, "jcenter", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://jcenter.bintray.com");
        reg(false, "jboss", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        reg(false, "clojars", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://repo.clojars.org");
        reg(false, "atlassian", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public");
        reg(false, "atlassian-snapshot", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public-snapshot");
        reg(false, "oracle", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://maven.oracle.com");
        reg(false, "google", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://maven.google.com");
        reg(false, "spring", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://repo.spring.io/release", "spring-framework");
        reg(false, "maven-thevpc-git", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "vpc-public-maven");
        reg(false, "nuts-public", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master", "vpc-public-nuts", "nuts-thevpc-git");
        reg(true, "nuts-preview", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master", "preview");
        reg(true, "thevpc-goodies", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "nuts@htmlfs:https://thevpc.net/maven-goodies", "goodies");
        reg(true, "thevpc", NAddRepositoryOptionsBoot.ORDER_USER_REMOTE, "maven@htmlfs:https://thevpc.net/maven", "dev");
    }


    public Set<String> findAllNamesByName(String name) {
        Set<String> a = baseToAliases.get(name);
        if (a != null) {
            return Collections.unmodifiableSet(a);
        }
        String base = aliasToBase.get(name);
        if (base != null) {
            a = baseToAliases.get(base);
            if (a != null) {
                return Collections.unmodifiableSet(a);
            }
        }
        return Collections.singleton(name);
    }

    public NAddRepositoryOptionsBoot getRepositoryOptionsByName(String name) {
        NAddRepositoryOptionsBoot o = defaultRepositoriesByName.get(name);
        if (o == null) {
            String base = aliasToBase.get(name);
            o = defaultRepositoriesByName.get(base);
        }
        if (o != null) {
            return o.copy();
        }
        return null;
    }


    public NAddRepositoryOptionsBoot getRepositoryOptionsByLocation(String name) {
        NAddRepositoryOptionsBoot o = defaultRepositoriesByLocation.get(name);
        if (o != null) {
            return o.copy();
        }
        return null;
    }

    private void reg(boolean preview, String name, int order, String url, String... names) {
        NAddRepositoryOptionsBoot options = new NAddRepositoryOptionsBoot()
                .setName(name)
                .setEnabled(true)
                .setCreate(true)
                .setFailSafe(false)
                .setOrder(order)
                .setConfig(
                        new NRepositoryConfigBoot()
                                .setLocation(
                                        NRepositoryLocationBoot.of(url)
                                )
                                .setTags(preview ? new String[]{NConstants.RepoTags.PREVIEW} : new String[0])
                );
        defaultRepositoriesByName.put(name, options);
        defaultRepositoriesByLocation.put(url, options);
        Set<String> all = new LinkedHashSet<>();
        all.add(name);
        for (String other : names) {
            aliasToBase.put(other, name);
            all.add(other);
        }
        baseToAliases.put(name, all);
    }
}
