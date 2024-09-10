package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NRepositoryDB;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NPlatformHome;

import java.util.*;

public class DefaultNRepositoryDB implements NRepositoryDB {
    private final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();
    private final Map<String, String> aliasToBase = new LinkedHashMap<>();
    private final Map<String, Set<String>> baseToAliases = new LinkedHashMap<>();
    private final Map<String, Boolean> previewByName = new LinkedHashMap<>();

    private DefaultNRepositoryDB(NSession session) {
        reg(false, "system", "nuts@" + NPath.of(
                NPlatformHome.SYSTEM.getWorkspaceStore(NStoreType.LIB, NConstants.Names.DEFAULT_WORKSPACE_NAME), session
        ).resolve(NConstants.Folders.ID).toString());
        reg(false, "maven", "maven");
        reg(false, "maven-central", "maven@https://repo.maven.apache.org/maven2");
        reg(false, "jcenter", "maven@https://jcenter.bintray.com");
        reg(false, "jboss", "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        reg(false, "clojars", "maven@https://repo.clojars.org");
        reg(false, "atlassian", "maven@https://packages.atlassian.com/maven/public");
        reg(false, "atlassian-snapshot", "maven@https://packages.atlassian.com/maven/public-snapshot");
        reg(false, "oracle", "maven@https://maven.oracle.com");
        reg(false, "google", "maven@https://maven.google.com");
        reg(false, "spring", "maven@https://repo.spring.io/release", "spring-framework");
        reg(false, "maven-thevpc-git", "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "vpc-public-maven");
        reg(false, "nuts-public", "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-public/master", "vpc-public-nuts", "nuts-thevpc-git");
        reg(true, "nuts-preview", "maven@dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master", "preview");
        reg(true, "thevpc-goodies", "nuts@htmlfs:https://thevpc.net/maven-goodies", "goodies");
        reg(true, "thevpc", "maven@htmlfs:https://thevpc.net/maven", "dev");
        reg(false, "local", "nuts@local");
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
    public boolean isPreviewRepository(String nameOrUrl) {
        for (String n : getAllNames(nameOrUrl)) {
            Boolean u = previewByName.get(n);
            if (u != null) {
                return u;
            }
        }
        String i = getRepositoryNameByLocation(nameOrUrl);
        if (i != null) {
            for (String n : getAllNames(i)) {
                Boolean u = previewByName.get(n);
                if (u != null) {
                    return u;
                }
            }
        }
        return false;
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

    @Override
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

    private void reg(boolean preview, String name, String url, String... names) {
        defaultRepositoriesByName.put(name, url);
        previewByName.put(name, preview);
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
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
