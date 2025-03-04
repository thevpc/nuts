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
    private static final NBootRepositoryDB instance = new NBootRepositoryDB();

    public static NBootRepositoryDB of() {
        return instance;
    }

    ;

    public NBootRepositoryDB() {
        reg(new String[]{NBootConstants.RepoTags.LOCAL}, "system", NBootAddRepositoryOptions.ORDER_SYSTEM_LOCAL, "nuts@" + NBootUtils.getNativePath(
                        NBootPlatformHome.SYSTEM.getWorkspaceStore(
                                "LIB",
                                NBootConstants.Names.DEFAULT_WORKSPACE_NAME) + "/" + NBootConstants.Folders.ID
                )
        );
        reg(new String[]{NBootConstants.RepoTags.MAIN}, "maven", NBootAddRepositoryOptions.ORDER_USER_LOCAL, "maven@");
        reg(new String[]{NBootConstants.RepoTags.MAIN}, "maven-central", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.maven.apache.org/maven2");
        reg(new String[]{}, "jcenter", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://jcenter.bintray.com");
        reg(new String[]{}, "jboss", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        reg(new String[]{}, "clojars", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.clojars.org");
        reg(new String[]{}, "atlassian", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public");
        reg(new String[]{}, "atlassian-snapshot", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public-snapshot");
        reg(new String[]{}, "oracle", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://maven.oracle.com");
        reg(new String[]{}, "google", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://maven.google.com");
        reg(new String[]{}, "spring", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.spring.io/release", "spring-framework");
        reg(new String[]{}, "maven-thevpc-git", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "vpc-public-maven");
        reg(new String[]{}, "nuts-public", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master", "vpc-public-nuts", "nuts-thevpc-git");
        reg(new String[]{NBootConstants.RepoTags.PREVIEW}, "thevpc", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@htmlfs:https://maven.thevpc.net", "dev");
        reg(new String[]{NBootConstants.RepoTags.PREVIEW}, "nuts-preview", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master", "preview");
        reg(new String[]{NBootConstants.RepoTags.PREVIEW}, "thevpc-goodies", NBootAddRepositoryOptions.ORDER_USER_REMOTE, "nuts@htmlfs:https://maven-goodies.thevpc.net", "goodies");
    }


    public Set<String> findByAnyTag(String... tags) {
        Set<String> ok = new LinkedHashSet<>();
        for (Map.Entry<String, NBootAddRepositoryOptions> e : defaultRepositoriesByName.entrySet()) {
            String[] u = e.getValue().getConfig().getTags();
            boolean found = tags.length==0;
            if (!found && u != null) {
                for (String u0 : u) {
                    for (String t : tags) {
                        if (t.equals(u0)) {
                            found = true;
                        }
                        if (found) {
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
            if (found) {
                ok.add(e.getKey());
            }
        }
        return ok;
    }

    public Set<String> findAllNamesByName(String name) {
        if (name == null || name.isEmpty()) {
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

    private void reg(String[] tags, String name, int order, String url, String... names) {
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
                                .setTags(tags == null ? new String[0] : tags)
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
