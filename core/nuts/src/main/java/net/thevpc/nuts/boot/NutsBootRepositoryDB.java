package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsUtilPlatforms;
import net.thevpc.nuts.spi.NutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryURL;

import java.util.LinkedHashMap;
import java.util.Map;

public class NutsBootRepositoryDB implements NutsRepositoryDB {
    public static final NutsRepositoryDB INSTANCE=new NutsBootRepositoryDB();

    private final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();

    {
        defaultRepositoriesByName.put("system", PrivateNutsUtilIO.getNativePath(
                NutsUtilPlatforms.getDefaultPlatformHomeFolder(null,
                        NutsStoreLocation.CONFIG,
                        true,
                        NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                        + "/" + NutsConstants.Folders.REPOSITORIES
                        + "/" + NutsConstants.Names.DEFAULT_REPOSITORY_NAME
        ));
        //
        defaultRepositoriesByName.put("maven-local", System.getProperty("user.home") + PrivateNutsUtilIO.getNativePath("/.m2/repository"));
        defaultRepositoriesByName.put(".m2", defaultRepositoriesByName.get("maven-local"));
        defaultRepositoriesByName.put("m2", defaultRepositoriesByName.get("maven-local"));
        //
        defaultRepositoriesByName.put("maven-central", "https://repo.maven.apache.org/maven2");
        defaultRepositoriesByName.put("m2", defaultRepositoriesByName.get("maven-central"));
        defaultRepositoriesByName.put("central", defaultRepositoriesByName.get("maven-central"));
        //
        defaultRepositoriesByName.put("jcenter", "https://jcenter.bintray.com");
        //
        defaultRepositoriesByName.put("jboss", "https://repository.jboss.org/nexus/content/repositories/releases");
        //
        defaultRepositoriesByName.put("clojars", "https://repo.clojars.org");
        //
        defaultRepositoriesByName.put("atlassian", "https://packages.atlassian.com/maven/public");
        //
        defaultRepositoriesByName.put("atlassian-snapshot", "https://packages.atlassian.com/maven/public-snapshot");
        //
        defaultRepositoriesByName.put("oracle", "https://maven.oracle.com");
        //
        defaultRepositoriesByName.put("google", "https://maven.google.com");
        //
        defaultRepositoriesByName.put("spring", "https://repo.spring.io/release");
        defaultRepositoriesByName.put("spring-framework", defaultRepositoriesByName.get("spring"));
        //
        defaultRepositoriesByName.put("maven-thevpc-git", "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master");
        defaultRepositoriesByName.put("vpc-public-maven", defaultRepositoriesByName.get("maven-thevpc-git"));
        //
        defaultRepositoriesByName.put("nuts-thevpc-git", "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
        defaultRepositoriesByName.put("vpc-public-nuts", defaultRepositoriesByName.get("nuts-thevpc-git"));
        //
        defaultRepositoriesByName.put("thevpc", "htmlfs:http://thevpc.net/maven");
        defaultRepositoriesByName.put("dev", defaultRepositoriesByName.get("thevpc"));
    }

    @Override
    public String getRepositoryNameByURL(String url) {
        NutsRepositoryURL nru = NutsRepositoryURL.of(url);
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            String v = entry.getValue();
            if (v.equals(nru.getURLString()) || v.equals(nru.getLocation())) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public String getRepositoryURLByName(String name) {
        return defaultRepositoriesByName.get(name);
    }

    public boolean isDefaultRepository(String name) {
        return defaultRepositoriesByName.containsKey(name);
    }
}
