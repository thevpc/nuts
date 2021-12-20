package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsUtilPlatforms;
import net.thevpc.nuts.spi.NutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class PrivateNutsBootRepositoryDB implements NutsRepositoryDB {
    public static final NutsRepositoryDB INSTANCE = new PrivateNutsBootRepositoryDB();

    private final Map<String, String> defaultRepositoriesByName = new LinkedHashMap<>();

    {
        defaultRepositoriesByName.put("system", PrivateNutsUtilIO.getNativePath(
                        NutsUtilPlatforms.getDefaultPlatformHomeFolder(null,
                                NutsStoreLocation.LIB,
                                true,
                                NutsConstants.Names.DEFAULT_WORKSPACE_NAME) + "/" + NutsConstants.Folders.ID
                )
        );
        //
        defaultRepositoriesByName.put("maven-local", "maven@" + System.getProperty("user.home") + PrivateNutsUtilIO.getNativePath("/.m2/repository"));
        defaultRepositoriesByName.put(".m2", defaultRepositoriesByName.get("maven-local"));
        defaultRepositoriesByName.put("m2", defaultRepositoriesByName.get("maven-local"));
        //
        defaultRepositoriesByName.put("maven-central", "maven@https://repo.maven.apache.org/maven2");
        defaultRepositoriesByName.put("central", defaultRepositoriesByName.get("maven-central"));
        defaultRepositoriesByName.put("maven", defaultRepositoriesByName.get("maven-central"));
        defaultRepositoriesByName.put("mvn", defaultRepositoriesByName.get("maven-central"));
        //
        defaultRepositoriesByName.put("jcenter", "maven@https://jcenter.bintray.com");
        //
        defaultRepositoriesByName.put("jboss", "maven@https://repository.jboss.org/nexus/content/repositories/releases");
        //
        defaultRepositoriesByName.put("clojars", "maven@https://repo.clojars.org");
        //
        defaultRepositoriesByName.put("atlassian", "maven@https://packages.atlassian.com/maven/public");
        //
        defaultRepositoriesByName.put("atlassian-snapshot", "maven@https://packages.atlassian.com/maven/public-snapshot");
        //
        defaultRepositoriesByName.put("oracle", "maven@https://maven.oracle.com");
        //
        defaultRepositoriesByName.put("google", "maven@https://maven.google.com");
        //
        defaultRepositoriesByName.put("spring", "maven@https://repo.spring.io/release");
        defaultRepositoriesByName.put("spring-framework", defaultRepositoriesByName.get("spring"));
        //
        defaultRepositoriesByName.put("maven-thevpc-git", "maven@https://raw.githubusercontent.com/thevpc/vpc-public-maven/master");
        defaultRepositoriesByName.put("vpc-public-maven", defaultRepositoriesByName.get("maven-thevpc-git"));
        //
        defaultRepositoriesByName.put("nuts-thevpc-git", "maven@https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
        defaultRepositoriesByName.put("vpc-public-nuts", defaultRepositoriesByName.get("nuts-thevpc-git"));
        //
        defaultRepositoriesByName.put("thevpc", "maven@htmlfs:http://thevpc.net/maven");
        defaultRepositoriesByName.put("dev", defaultRepositoriesByName.get("thevpc"));
        defaultRepositoriesByName.put("preview", defaultRepositoriesByName.get("thevpc"));
    }

    @Override
    public String getRepositoryNameByURL(String url) {
        NutsRepositoryLocation nru = NutsRepositoryLocation.of(url);
        for (Map.Entry<String, String> entry : defaultRepositoriesByName.entrySet()) {
            String v = entry.getValue();
            if (v.equals(nru.toString())
                    || v.equals(nru.setName(null).toString())
                    || v.equals(nru.setName(null).setType(null).toString())
            ) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isDefaultRepositoryName(String name) {
        return defaultRepositoriesByName.containsKey(name);
    }

    @Override
    public String getRepositoryURLByName(String name) {
        return defaultRepositoriesByName.get(name);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return 1;
    }
}
