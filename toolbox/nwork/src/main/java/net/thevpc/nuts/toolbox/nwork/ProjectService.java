package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nwork.config.ProjectConfig;
import net.thevpc.nuts.toolbox.nwork.config.RepositoryAddress;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ProjectService {

    private ProjectConfig config;
    private NutsApplicationContext appContext;
    private RepositoryAddress defaultRepositoryAddress;
    private Path sharedConfigFolder;

    public ProjectService(NutsApplicationContext context, RepositoryAddress defaultRepositoryAddress, Path file) throws IOException {
        this.appContext = context;
        this.defaultRepositoryAddress = defaultRepositoryAddress == null ? new RepositoryAddress() : defaultRepositoryAddress;
        config = context.getWorkspace().elem().setSession(appContext.getSession()).setContentType(NutsContentType.JSON).parse(file, ProjectConfig.class);
        sharedConfigFolder = Paths.get(context.getVersionFolderFolder(NutsStoreLocation.CONFIG, NWorkConfigVersions.CURRENT));
    }

    public ProjectService(NutsApplicationContext context, RepositoryAddress defaultRepositoryAddress, ProjectConfig config) {
        this.config = config;
        this.appContext = context;
        this.defaultRepositoryAddress = defaultRepositoryAddress;
        sharedConfigFolder = Paths.get(context.getVersionFolderFolder(NutsStoreLocation.CONFIG, NWorkConfigVersions.CURRENT));
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
        Path storeLocation = sharedConfigFolder.resolve("projects");
        return storeLocation.resolve(config.getId().replace(":", "-") + ".config");
    }

    public void save() throws IOException {
        Path configFile = getConfigFile();
        Files.createDirectories(configFile.getParent());
        appContext.getWorkspace().elem().setSession(appContext.getSession()).setContentType(NutsContentType.JSON).setValue(config).print(configFile);
    }

    public boolean load() {
        Path configFile = getConfigFile();
        if (Files.isRegularFile(configFile)) {
            ProjectConfig u = appContext.getWorkspace().elem().setSession(appContext.getSession()).setContentType(NutsContentType.JSON).parse(configFile, ProjectConfig.class);
            if (u != null) {
                config = u;
                return true;
            }
        }
        return false;
    }

    public NutsDescriptor getPom() {
        File f = new File(config.getPath());
        if (f.isDirectory()) {
            if (new File(f, "pom.xml").isFile()) {
                try {
                    return appContext.getWorkspace().descriptor()
                            .parser()
                            .setDescriptorFormat(NutsDescriptorParser.DescriptorFormat.MAVEN)
                            .parse(new File(f, "pom.xml"));
                } catch (Exception ex) {
                    //
                }
            }
        }
        return null;
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
                    NutsDescriptor g= appContext.getWorkspace().descriptor()
                            .parser()
                            .setDescriptorFormat(NutsDescriptorParser.DescriptorFormat.MAVEN)
                            .parse(new File(f, "pom.xml"));
                    if (g.getId().getGroupId() != null
                            && g.getId().getArtifactId() != null
                            && g.getId().getVersion() != null
                            && !g.getId().getGroupId().contains("$")
                            && !g.getId().getArtifactId().contains("$")
                            && !g.getId().getVersion().toString().contains("$")) {

                        String s = new String(Files.readAllBytes(new File(f, "pom.xml").toPath()));
                        //check if the s
                        int ok = 0;
                        if (s.contains("<artifactId>site-maven-plugin</artifactId>")) {
                            p2.getTechnologies().add("github-deploy");
                            ok++;
                        }
                        if (s.contains("<artifactId>nexus-staging-maven-plugin</artifactId>")) {
                            p2.getTechnologies().add("nexus-deploy");
                            ok++;
                        }
                        if (s.contains("<phase>deploy</phase>")) {
                            ok++;
                        }
                        if (ok > 0) {

                            if (p2.getId() == null) {
                                p2.setId(g.getId().getGroupId() + ":" + g.getId().getArtifactId());
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
        NutsId id = appContext.getWorkspace().id().parser().parse(sid);
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
                        return appContext.getWorkspace().descriptor()
                                .parser()
                                .setDescriptorFormat(NutsDescriptorParser.DescriptorFormat.MAVEN)
                                .parse(new File(f, "pom.xml")).getId().getVersion().toString();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return null;
    }

    public File detectRemoteVersionFile(String sid) {
        NutsId id = appContext.getWorkspace().id().parser().parse(sid);
        if (config.getTechnologies().contains("maven")) {
            RepositoryAddress a = config.getAddress();
            if (a == null) {
                a = defaultRepositoryAddress;
            }
            if (a == null) {
                a = new RepositoryAddress();
            }
            String nutsRepository = a.getNutsRepository();
            if (_StringUtils.isBlank(nutsRepository)) {
                throw new NutsExecutionException(appContext.getSession(), "missing repository. try 'nwork set -r vpc-public-maven' or something like that", 2);
            }
            try {
                NutsWorkspace ws2 = null;
                NutsSession s = null;
                if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(appContext.getWorkspace().locations().getWorkspaceLocation().toString())) {
                    ws2 = Nuts.openWorkspace(
                            Nuts.createOptions()
                                    .setOpenMode(NutsOpenMode.OPEN_OR_ERROR)
                                    .setReadOnly(true)
                                    .setWorkspace(a.getNutsWorkspace())
                    );
                    s = ws2.createSession().setTrace(false);
                    s.copyFrom(appContext.getSession());
                } else {
                    ws2 = appContext.getWorkspace();
                    s = appContext.getSession();
                }
                List<NutsDefinition> found = ws2.search()
                        .addId(sid)
                        .addRepositoryFilter(ws2.filters().repository().byName(nutsRepository))
                        .setLatest(true).setSession(s).setContent(true).getResultDefinitions().list();
                if (found.size() > 0) {
                    Path p = found.get(0).getContent().getFilePath();
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
                    if (_StringUtils.isBlank(nutsRepository)) {
                        throw new NutsExecutionException(appContext.getSession(), "missing repository. try 'nwork set -r vpc-public-maven' or something like that", 2);
                    }
                    try {
                        NutsDescriptor g= appContext.getWorkspace().descriptor()
                                .parser()
                                .setDescriptorFormat(NutsDescriptorParser.DescriptorFormat.MAVEN)
                                .parse(new File(f, "pom.xml"));
                        NutsWorkspace ws2 = null;
                        NutsSession s = null;
                        if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(appContext.getWorkspace().locations().getWorkspaceLocation().toString())) {
                            ws2 = Nuts.openWorkspace(
                                    appContext.getWorkspace().config().optionsBuilder()
                                            .setOpenMode(NutsOpenMode.OPEN_OR_ERROR)
                                            .setReadOnly(true)
                                            .setWorkspace(a.getNutsWorkspace())
                            );
                            s = ws2.createSession().setTrace(false);
                            s.copyFrom(appContext.getSession());
                        } else {
                            ws2 = appContext.getWorkspace();
                            s = appContext.getSession();
                        }
                        s.setTrace(false);
                        List<NutsId> found = ws2.search()
                                .addId(g.getId().getGroupId() + ":" + g.getId().getArtifactId())
                                .addRepositoryFilter(ws2.filters().repository().byName(nutsRepository))
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
