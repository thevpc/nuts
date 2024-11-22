package net.thevpc.nuts.reserved.boot;

import net.thevpc.nuts.NAddRepositoryOptions;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NRepositoryConfig;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.reserved.io.NReservedIOUtils;
import net.thevpc.nuts.spi.NRepositoryDB;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NPlatformHome;

import java.util.*;

public class NReservedBootRepositoryDB implements NRepositoryDB {
    private final Map<String, NAddRepositoryOptions> defaultRepositoriesByName = new LinkedHashMap<>();
    private final Map<String, NAddRepositoryOptions> defaultRepositoriesByLocation = new LinkedHashMap<>();
    private final Map<String, String> aliasToBase = new LinkedHashMap<>();
    private final Map<String, Set<String>> baseToAliases = new LinkedHashMap<>();

    public NReservedBootRepositoryDB() {
        reg(false, "system", NAddRepositoryOptions.ORDER_SYSTEM_LOCAL, "nuts@" + NReservedIOUtils.getNativePath(
                        NPlatformHome.SYSTEM.getWorkspaceStore(
                                NStoreType.LIB,
                                NConstants.Names.DEFAULT_WORKSPACE_NAME) + "/" + NConstants.Folders.ID
                )
        );
        reg(false, "maven", NAddRepositoryOptions.ORDER_USER_LOCAL, "maven");
        reg(false, "maven-central", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.maven.apache.org/maven2");
        reg(false, "jcenter", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://jcenter.bintray.com");
        reg(false, "jboss", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        reg(false, "clojars", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.clojars.org");
        reg(false, "atlassian", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public");
        reg(false, "atlassian-snapshot", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://packages.atlassian.com/maven/public-snapshot");
        reg(false, "oracle", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://maven.oracle.com");
        reg(false, "google", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://maven.google.com");
        reg(false, "spring", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://repo.spring.io/release", "spring-framework");
        reg(false, "maven-thevpc-git", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "vpc-public-maven");
        reg(false, "nuts-public", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master", "vpc-public-nuts", "nuts-thevpc-git");
        reg(true, "nuts-preview", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master", "preview");
        reg(true, "thevpc-goodies", NAddRepositoryOptions.ORDER_USER_REMOTE, "nuts@htmlfs:https://thevpc.net/maven-goodies", "goodies");
        reg(true, "thevpc", NAddRepositoryOptions.ORDER_USER_REMOTE, "maven@htmlfs:https://thevpc.net/maven", "dev");
    }

    @Override
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

    @Override
    public NOptional<NAddRepositoryOptions> getRepositoryOptionsByName(String name) {
        NAddRepositoryOptions o = defaultRepositoriesByName.get(name);
        if (o == null) {
            String base = aliasToBase.get(name);
            o = defaultRepositoriesByName.get(base);
        }
        if (o != null) {
            return NOptional.of(o.copy());
        }
        return NOptional.ofEmpty(NMsg.ofC("repository %s", name));
    }

    @Override
    public NOptional<NAddRepositoryOptions> getRepositoryOptionsByLocation(String name) {
        NAddRepositoryOptions o = defaultRepositoriesByLocation.get(name);
        if (o != null) {
            return NOptional.of(o.copy());
        }
        return NOptional.ofEmpty(NMsg.ofC("repository %s", name));
    }

    private void reg(boolean preview, String name, int order, String url, String... names) {
        NAddRepositoryOptions options = new NAddRepositoryOptions()
                .setName(name)
                .setEnabled(true)
                .setCreate(true)
                .setFailSafe(false)
                .setOrder(order)
                .setConfig(
                        new NRepositoryConfig()
                                .setLocation(
                                        NRepositoryLocation.of(url)
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


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return 1;
    }
}
