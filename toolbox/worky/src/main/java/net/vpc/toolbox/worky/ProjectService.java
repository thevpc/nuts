package net.vpc.toolbox.worky;

import net.vpc.app.nuts.*;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.mvn.Pom;
import net.vpc.common.mvn.PomXmlParser;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.worky.config.ProjectConfig;
import net.vpc.toolbox.worky.config.RepositoryAddress;

import java.io.*;
import java.util.List;

public class ProjectService {
    private ProjectConfig config;
    private NutsWorkspace ws;
    private RepositoryAddress defaultRepositoryAddress;

    public ProjectService(NutsWorkspace ws, RepositoryAddress defaultRepositoryAddress, File file) throws IOException {
        this.ws = ws;
        this.defaultRepositoryAddress = defaultRepositoryAddress;
        try (FileReader f = new FileReader(file)) {
            config = ws.getExtensionManager().getJsonIO().read(f, ProjectConfig.class);
        }
    }

    public ProjectService(NutsWorkspace ws, RepositoryAddress defaultRepositoryAddress, ProjectConfig config) {
        this.config = config;
        this.ws = ws;
        this.defaultRepositoryAddress = defaultRepositoryAddress;
    }

    public ProjectConfig getConfig() {
        return config;
    }

    public boolean updateProjectMetadata() throws IOException {
        ProjectConfig p2 = rebuildProjectMetadata();
        if (!p2.equals(config)) {
            save();
            return true;
        }
        return false;
    }

    public File getConfigFile() {
        String storeRoot = ws.getStoreRoot("net.vpc.app.nuts.toolbox:worky#CURRENT", RootFolderType.CONFIG);
        return new File(storeRoot, config.getId() + ".config");
    }

    public void save() throws IOException {
        File configFile = getConfigFile();
        FileUtils.createParents(configFile);
        try (FileWriter f = new FileWriter(configFile)) {
            ws.getExtensionManager().getJsonIO().write(config, f, true);
        }
    }

    public boolean load() throws IOException {
        File configFile = getConfigFile();
        if (configFile.isFile()) {
            try (FileReader f = new FileReader(configFile)) {
                ProjectConfig u = ws.getExtensionManager().getJsonIO().read(f, ProjectConfig.class);
                if (u != null) {
                    config = u;
                    return true;
                }
            }
        }
        return false;
    }

    public ProjectConfig rebuildProjectMetadata() {
        ProjectConfig p2 = new ProjectConfig();
        p2.setId(config.getId());
        p2.setAddress(config.getAddress());
        p2.setPath(config.getPath());
        File f = new File(config.getPath());
        if (f.isDirectory()) {
            if (new File(f, "pom.xml").isFile()) {
                try {
                    Pom g = new PomXmlParser().parse(new File(f, "pom.xml"));
                    if (
                            g.getGroupId() != null
                                    && g.getArtifactId() != null
                                    && g.getVersion() != null
                                    && !g.getGroupId().contains("$")
                                    && !g.getArtifactId().contains("$")
                                    && !g.getVersion().contains("$")
                            ) {

                        String s = IOUtils.loadString(new File(f, "pom.xml"));
                        //check if the s
                        int ok = 0;
                        if (s.contains("<artifactId>site-maven-plugin</artifactId>")) {
                            ok++;
                        }
                        if (s.contains("<phase>deploy</phase>")) {
                            ok++;
                        }
                        if (ok > 0) {

                            if (p2.getId() == null) {
                                p2.setId(g.getGroupId() + "-" + g.getArtifactId());
                            }
                            if (new File(f, "src/main").isDirectory()) {
                                p2.getTechnologies().add("maven");
                            }
                            if (new File(f, "src/main/java").isDirectory()) {
                                p2.getTechnologies().add("java");
                                //should
                            }
                            if (new File(f, "src/main/webapp").isDirectory()) {
                                p2.getTechnologies().add("web");
                                //should
                            }
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }
        } else {
            p2.setZombie(true);
        }
        return p2;
    }

    public String detectLocalVersion() {
        if (config.getTechnologies().contains("maven")) {
            File f = new File(config.getPath());
            if (f.isDirectory()) {
                if (new File(f, "pom.xml").isFile()) {
                    try {
                        return new PomXmlParser().parse(new File(f, "pom.xml"))
                                .getVersion();
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return null;
    }

    public String detectRemoteVersion() {
        if (config.getTechnologies().contains("maven")) {
            File f = new File(config.getPath());
            if (f.isDirectory()) {
                if (new File(f, "pom.xml").isFile()) {
                    RepositoryAddress a = config.getAddress();
                    if (a == null) {
                        a = defaultRepositoryAddress;
                    }
                    if (a == null) {
                        a = new RepositoryAddress();
                    }
                    String nutsRepository = a.getNutsRepository();
                    if (StringUtils.isEmpty(nutsRepository)) {
                        throw new IllegalArgumentException("Missing Repository");
                    }
                    try {
                        Pom g = new PomXmlParser().parse(new File(f, "pom.xml"));
                        NutsWorkspace ws2 = Nuts.openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setCreateIfNotFound(false)
                                        .setReadOnly(true)
                                        .setWorkspace(a.getNutsWorkspace()),
                                new NutsBootOptions().setHome(a.getNutsHome())
                        );
                        NutsSession s = ws2.createSession();
                        List<NutsId> found = ws2.find(new NutsSearch()
                                        .setIds(g.getGroupId() + ":" + g.getArtifactId())
                                        .setRepositoryFilter(nutsRepository)
                                        .setLastestVersions(true)
                                , s);
                        if (found.size() > 0) {
                            return found.get(0).getVersion().toString();
                        }
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return null;
    }


}
