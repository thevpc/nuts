package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NStoreLocation;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NPlatformUtils;
import net.thevpc.nuts.spi.NRepositoryDB;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.*;

public class NReservedBootRepositoryDB implements NRepositoryDB {
    private final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();
    private final Map<String, String> aliasToBase = new LinkedHashMap<>();
    private final Map<String, Set<String>> baseToAliases = new LinkedHashMap<>();

    public NReservedBootRepositoryDB(NLog logger) {
        reg("system", NReservedIOUtils.getNativePath(
                        NPlatformUtils.getDefaultPlatformHomeFolder(null,
                                NStoreLocation.LIB,
                                true,
                                NConstants.Names.DEFAULT_WORKSPACE_NAME) + "/" + NConstants.Folders.ID
                )
        );
        reg("maven", "maven", "central", "maven", "mvn",".m2", "m2");
        reg("jcenter", "maven@https://jcenter.bintray.com");
        reg("jboss", "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        reg("clojars", "maven@https://repo.clojars.org");
        reg("atlassian", "maven@https://packages.atlassian.com/maven/public");
        reg("atlassian-snapshot", "maven@https://packages.atlassian.com/maven/public-snapshot");
        reg("oracle", "maven@https://maven.oracle.com");
        reg("google", "maven@https://maven.google.com");
        reg("spring", "maven@https://repo.spring.io/release", "spring-framework");
        reg("maven-thevpc-git", "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "vpc-public-maven");
        reg("nuts-public", "maven@https://raw.githubusercontent.com/thevpc/nuts-public/master", "nuts-public", "vpc-public-nuts");
        reg("nuts-preview", "maven@https://raw.githubusercontent.com/thevpc/nuts-preview/master", "nuts-preview","preview");
        reg("thevpc", "maven@htmlfs:https://thevpc.net/maven", "dev");
    }

    @Override
    public Set<String> getAllNames(String name) {
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
    public String getRepositoryNameByLocation(String location) {
        NRepositoryLocation v0 = NRepositoryLocation.of(location).setName(null);
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            NRepositoryLocation v = NRepositoryLocation.of(entry.getValue()).setName(null);
            if (v.equals(v0)) {
                return entry.getKey();
            }
        }
        v0 = NRepositoryLocation.of(location).setName(null).setLocationType(null);
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            NRepositoryLocation v = NRepositoryLocation.of(entry.getValue()).setName(null).setLocationType(null);
            if (v.equals(v0)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isDefaultRepositoryName(String name) {
        return defaultRepositoriesByName.containsKey(name)
                || aliasToBase.containsKey(name);
    }

    @Override
    public String getRepositoryLocationByName(String name) {
        String a = defaultRepositoriesByName.get(name);
        if (a != null) {
            return a;
        }
        String base = aliasToBase.get(name);
        if (base != null) {
            return defaultRepositoriesByName.get(base);
        }
        return null;
    }

    private void reg(String name, String url, String... names) {
        defaultRepositoriesByName.put(name, url);
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
