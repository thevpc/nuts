package net.vpc.toolbox.worky;

import net.vpc.app.nuts.*;
import net.vpc.common.io.IOUtils;
import net.vpc.common.mvn.Pom;
import net.vpc.common.mvn.PomXmlParser;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.worky.config.ProjectConfig;
import net.vpc.toolbox.worky.config.RepositoryAddress;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ProjectService {

    private ProjectConfig config;
    private NutsApplicationContext context;
    private RepositoryAddress defaultRepositoryAddress;

    public ProjectService(NutsApplicationContext context, RepositoryAddress defaultRepositoryAddress, Path file) throws IOException {
        this.context = context;
        this.defaultRepositoryAddress = defaultRepositoryAddress == null ? new RepositoryAddress() : defaultRepositoryAddress;
        config = context.getWorkspace().json().parse(file, ProjectConfig.class);
    }

    public ProjectService(NutsApplicationContext context, RepositoryAddress defaultRepositoryAddress, ProjectConfig config) {
        this.config = config;
        this.context = context;
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

    public Path getConfigFile() {
        Path storeLocation = context.getConfigFolder().resolve("projects");
        return storeLocation.resolve(config.getId().replace(":", "-") + ".config");
    }

    public void save() throws IOException {
        Path configFile = getConfigFile();
        Files.createDirectories(configFile.getParent());
        context.getWorkspace().json().set(config).print(configFile);
    }

    public boolean load() {
        Path configFile = getConfigFile();
        if (Files.isRegularFile(configFile)) {
            ProjectConfig u = context.getWorkspace().json().parse(configFile, ProjectConfig.class);
            if (u != null) {
                config = u;
                return true;
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
                    if (g.getGroupId() != null
                            && g.getArtifactId() != null
                            && g.getVersion() != null
                            && !g.getGroupId().contains("$")
                            && !g.getArtifactId().contains("$")
                            && !g.getVersion().contains("$")) {

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
                                p2.setId(g.getGroupId()+ ":" + g.getArtifactId());
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
                    } catch (Exception e) {
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
                    if (StringUtils.isBlank(nutsRepository)) {
                        throw new NutsExecutionException(context.getWorkspace(), "Missing Repository", 2);
                    }
                    try {
                        Pom g = new PomXmlParser().parse(new File(f, "pom.xml"));
                        NutsWorkspace ws2 = Nuts.openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING)
                                        .setReadOnly(true)
                                        .setWorkspace(a.getNutsWorkspace())
                        );
                        NutsSession s = ws2.createSession();
                        List<NutsId> found = ws2.search()
                                .id(g.getGroupId() + ":" + g.getArtifactId())
                                .repository(nutsRepository)
                                .latest().session(s).getResultIds().list();
                        if (found.size() > 0) {
                            return found.get(0).getVersion().toString();
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return null;
    }

}
