package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootAddRepositoryOptions;
import net.thevpc.nuts.boot.NBootRepositoryConfig;
import net.thevpc.nuts.boot.NBootRepositoryLocation;

import java.util.*;

public class NBootRepositoryDB {
    private final Map<String, NBootAddRepositoryOptions> defaultRepositoriesByName = new LinkedHashMap<>();
    private final Map<String, NBootAddRepositoryOptions> defaultRepositoriesByLocation = new LinkedHashMap<>();
    private final Map<String, String> aliasToBase = new LinkedHashMap<>();
    private final Map<String, Set<String>> baseToAliases = new LinkedHashMap<>();
    private static final NBootRepositoryDB instance=new NBootRepositoryDB();
    public static NBootRepositoryDB of(){return instance;};

    public NBootRepositoryDB() {
        reg(false, "system", NBootAddRepositoryOptions.ORDER_SYSTEM_LOCAL, "nuts@" + NBootUtils.getNativePath(
                        NBootPlatformHome.SYSTEM.getWorkspaceStore(
                                "LIB",
                                NBootConstants.Names.DEFAULT_WORKSPACE_NAME) + "/" + NBootConstants.Folders.ID
                )
        );
        reg(false, "maven", NBootAddRepositoryOptions.ORDER_USER_LOCAL, "maven@");
        reg(false, "maven-central", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.maven.apache.org/maven2");
        reg(false, "jcenter", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://jcenter.bintray.com");
        reg(false, "jboss", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        reg(false, "clojars", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.clojars.org");
        reg(false, "atlassian", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public");
        reg(false, "atlassian-snapshot", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public-snapshot");
        reg(false, "oracle", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://maven.oracle.com");
        reg(false, "google", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://maven.google.com");
        reg(false, "spring", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.spring.io/release", "spring-framework");
        reg(false, "maven-thevpc-git", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "vpc-public-maven");
        reg(false, "nuts-public", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master", "vpc-public-nuts", "nuts-thevpc-git");
        reg(true, "nuts-preview", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master", "preview");
        reg(true, "thevpc-goodies", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "nuts@htmlfs:https://thevpc.net/maven-goodies", "goodies");
        reg(true, "thevpc", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@htmlfs:https://thevpc.net/maven", "dev");
    }


    public Set<String> findAllNamesByName(String name) {
        if(name==null || name.isEmpty()){
            return Collections.emptySet();
        }
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

    public NBootAddRepositoryOptions getRepositoryOptionsByName(String name) {
        NBootAddRepositoryOptions o = defaultRepositoriesByName.get(name);
        if (o == null) {
            String base = aliasToBase.get(name);
            o = defaultRepositoriesByName.get(base);
        }
        if (o != null) {
            return o.copy();
        }
        return null;
    }


    public NBootAddRepositoryOptions getRepositoryOptionsByLocation(String location) {
        NBootAddRepositoryOptions o = defaultRepositoriesByLocation.get(location);
        if (o != null) {
            return o.copy();
        }
        return null;
    }

    private void reg(boolean preview, String name, int order, String url, String... names) {
        NBootRepositoryLocation location = NBootRepositoryLocation.of(url);
        NBootUtils.requireNonBlank(location.getLocationType(), "locationType");
        NBootAddRepositoryOptions options = new NBootAddRepositoryOptions()
                .setName(name)
                .setEnabled(true)
                .setCreate(true)
                .setFailSafe(false)
                .setOrder(order)
                .setConfig(
                        new NBootRepositoryConfig()
                                .setLocation(
                                        location
                                )
                                .setTags(preview ? new String[]{NBootConstants.RepoTags.PREVIEW} : new String[0])
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
