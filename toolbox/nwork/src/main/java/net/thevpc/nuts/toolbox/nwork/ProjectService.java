package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.common.mvn.Pom;
import net.thevpc.common.mvn.PomXmlParser;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.toolbox.nwork.config.ProjectConfig;
import net.thevpc.nuts.toolbox.nwork.config.RepositoryAddress;

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
        config = context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(file, ProjectConfig.class);
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
        Path storeLocation = context.getSharedConfigFolder().resolve("projects");
        return storeLocation.resolve(config.getId().replace(":", "-") + ".config");
    }

    public void save() throws IOException {
        Path configFile = getConfigFile();
        Files.createDirectories(configFile.getParent());
        context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).setValue(config).print(configFile);
    }

    public boolean load() {
        Path configFile = getConfigFile();
        if (Files.isRegularFile(configFile)) {
            ProjectConfig u = context.getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(configFile, ProjectConfig.class);
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

                        String s = new String(Files.readAllBytes(new File(f, "pom.xml").toPath()));
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
                                p2.setId(g.getGroupId() + ":" + g.getArtifactId());
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

    public File detectLocalVersionFile(String sid) {
        NutsId id = context.getWorkspace().id().parser().parse(sid);
        if (config.getTechnologies().contains("maven")) {
            File f = new File(System.getProperty("user.home"), ".m2/repository/"
                    + id.getGroupId().replace('.', File.separatorChar)
                    + File.separatorChar
                    + id.getArtifactId()
                    + File.separatorChar
                    + id.getVersion()
                    + File.separatorChar
                    + id.getArtifactId()
                    + "-"
                    + id.getVersion()
                    + ".jar"
            );
            if (f.exists()) {
                return f;
            }
        }
        return null;
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

    public File detectRemoteVersionFile(String sid) {
        NutsId id = context.getWorkspace().id().parser().parse(sid);
        if (config.getTechnologies().contains("maven")) {
            RepositoryAddress a = config.getAddress();
            if (a == null) {
                a = defaultRepositoryAddress;
            }
            if (a == null) {
                a = new RepositoryAddress();
            }
            String nutsRepository = a.getNutsRepository();
            if (StringUtils.isBlank(nutsRepository)) {
                throw new NutsExecutionException(context.getWorkspace(), "Missing Repository. try 'worky set -r vpc-public-maven' or something like that", 2);
            }
            try {
                NutsWorkspace ws2 = null;
                NutsSession s = null;
                if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(context.getWorkspace().locations().getWorkspaceLocation().toString())) {
                    ws2 = Nuts.openWorkspace(
                            Nuts.createOptions()
                                    .setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING)
                                    .setReadOnly(true)
                                    .setWorkspace(a.getNutsWorkspace())
                    );
                    s = ws2.createSession().setSilent();
                    s.copyFrom(context.getSession());
                } else {
                    ws2 = context.getWorkspace();
                    s = context.getSession();
                }
                List<NutsDefinition> found = ws2.search()
                        .addId(sid)
                        .addRepository(nutsRepository)
                        .setLatest(true).setSession(s).setContent(true).getResultDefinitions().list();
                if (found.size() > 0) {
                    Path p = found.get(0).getContent().getPath();
                    if (p == null) {
                        return null;
                    }
                    return p.toFile();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
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
                        throw new NutsExecutionException(context.getWorkspace(), "Missing Repository. try 'worky set -r vpc-public-maven' or something like that", 2);
                    }
                    try {
                        Pom g = new PomXmlParser().parse(new File(f, "pom.xml"));
                        NutsWorkspace ws2 = null;
                        NutsSession s = null;
                        if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(context.getWorkspace().locations().getWorkspaceLocation().toString())) {
                            ws2 = Nuts.openWorkspace(
                                    context.getWorkspace().config().optionsBuilder()
                                            .setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING)
                                            .setReadOnly(true)
                                            .setWorkspace(a.getNutsWorkspace())
                            );
                            s = ws2.createSession().setSilent();
                            s.copyFrom(context.getSession());
                        } else {
                            ws2 = context.getWorkspace();
                            s = context.getSession();
                        }
                        s.setSilent();
                        List<NutsId> found = ws2.search()
                                .addId(g.getGroupId() + ":" + g.getArtifactId())
                                .addRepository(nutsRepository)
                                .setLatest(true).setSession(s).getResultIds().list();
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
