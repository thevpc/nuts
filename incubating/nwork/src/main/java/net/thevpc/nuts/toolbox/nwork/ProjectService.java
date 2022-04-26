package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.nuts.DefaultNutsWorkspaceBootOptionsBuilder;
import net.thevpc.nuts.toolbox.nwork.config.ProjectConfig;
import net.thevpc.nuts.toolbox.nwork.config.RepositoryAddress;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ProjectService {

    private ProjectConfig config;
    private final NutsApplicationContext appContext;
    private final RepositoryAddress defaultRepositoryAddress;
    private final NutsPath sharedConfigFolder;

    public ProjectService(NutsApplicationContext context, RepositoryAddress defaultRepositoryAddress, NutsPath file) throws IOException {
        this.appContext = context;
        this.defaultRepositoryAddress = defaultRepositoryAddress == null ? new RepositoryAddress() : defaultRepositoryAddress;
        NutsSession session = context.getSession();
        config = NutsElements.of(session).json().parse(file, ProjectConfig.class);
        sharedConfigFolder = context.getVersionFolder(NutsStoreLocation.CONFIG, NWorkConfigVersions.CURRENT);
    }

    public ProjectService(NutsApplicationContext context, RepositoryAddress defaultRepositoryAddress, ProjectConfig config) {
        this.config = config;
        this.appContext = context;
        this.defaultRepositoryAddress = defaultRepositoryAddress;
        sharedConfigFolder = context.getVersionFolder(NutsStoreLocation.CONFIG, NWorkConfigVersions.CURRENT);
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

    public NutsPath getConfigFile() {
        NutsPath storeLocation = sharedConfigFolder.resolve("projects");
        return storeLocation.resolve(config.getId().replace(":", "-") + ".config");
    }

    public void save() throws IOException {
        NutsPath configFile = getConfigFile();
        configFile.mkParentDirs();
        NutsSession session = appContext.getSession();
        NutsElements.of(session).json().setValue(config).print(configFile);
    }

    public boolean load() {
        NutsPath configFile = getConfigFile();
        if (configFile.isRegularFile()) {
            NutsSession session = appContext.getSession();
            ProjectConfig u = NutsElements.of(session).json().parse(configFile, ProjectConfig.class);
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
                    NutsSession session = appContext.getSession();
                    return NutsDescriptorParser.of(session)
                            .setDescriptorStyle(NutsDescriptorStyle.MAVEN)
                            .parse(new File(f, "pom.xml")).get(session);
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
                    NutsSession session = appContext.getSession();
                    NutsDescriptor g = NutsDescriptorParser.of(session)
                            .setDescriptorStyle(NutsDescriptorStyle.MAVEN)
                            .parse(new File(f, "pom.xml")).get(session);
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
        NutsSession session = appContext.getSession();
        NutsId id = NutsId.of(sid).get(session);
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
                        NutsSession session = appContext.getSession();
                        return NutsDescriptorParser.of(session)
                                .setDescriptorStyle(NutsDescriptorStyle.MAVEN)
                                .parse(new File(f, "pom.xml")).get(session).getId().getVersion().toString();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return null;
    }

    public File detectRemoteVersionFile(String sid) {
        NutsSession session = appContext.getSession();
        NutsId id = NutsId.of(sid).get(session);
        if (config.getTechnologies().contains("maven")) {
            RepositoryAddress a = config.getAddress();
            if (a == null) {
                a = defaultRepositoryAddress;
            }
            if (a == null) {
                a = new RepositoryAddress();
            }
            String nutsRepository = a.getNutsRepository();
            if (NutsBlankable.isBlank(nutsRepository)) {
                throw new NutsExecutionException(session, NutsMessage.cstyle("missing repository. try 'nwork set -r vpc-public-maven' or something like that"), 2);
            }
            try {
                NutsSession s = null;
                if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(session.locations().getWorkspaceLocation().toString())) {
                    s = Nuts.openWorkspace(
                            new DefaultNutsWorkspaceBootOptionsBuilder()
                                    .setOpenMode(NutsOpenMode.OPEN_OR_ERROR)
                                    .setReadOnly(true)
                                    .setWorkspace(a.getNutsWorkspace())
                    );
                    s.copyFrom(session);
                } else {
                    s = session;
                }
                List<NutsDefinition> found = s.search()
                        .addId(sid)
                        .addRepositoryFilter(NutsRepositoryFilters.of(s).byName(nutsRepository))
                        .setLatest(true).setSession(s).setContent(true).getResultDefinitions().toList();
                if (found.size() > 0) {
                    Path p = found.get(0).getContent().getFile();
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
                    NutsSession session = appContext.getSession();
                    if (NutsBlankable.isBlank(nutsRepository)) {
                        throw new NutsExecutionException(session, NutsMessage.cstyle("missing repository. try 'nwork set -r vpc-public-maven' or something like that"), 2);
                    }
                    try {
                        NutsDescriptor g = NutsDescriptorParser.of(session)
                                .setDescriptorStyle(NutsDescriptorStyle.MAVEN)
                                .parse(new File(f, "pom.xml")).get(session);
                        NutsSession s = null;
                        if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(session.locations().getWorkspaceLocation().toString())) {
                            s = Nuts.openWorkspace(
                                    new DefaultNutsWorkspaceBootOptionsBuilder()
                                            .setOpenMode(NutsOpenMode.OPEN_OR_ERROR)
                                            .setReadOnly(true)
                                            .setWorkspace(a.getNutsWorkspace())
                            );
                            s.copyFrom(session);
                        } else {
                            s = session;
                        }
                        List<NutsId> found = s.search()
                                .addId(g.getId().getGroupId() + ":" + g.getId().getArtifactId())
                                .addRepositoryFilter(NutsRepositoryFilters.of(s).byName(nutsRepository))
                                .setLatest(true).setSession(s).getResultIds().toList();
                        if (found.size() > 0) {
                            return found.get(0).getVersion().toString();
                        }
                    } catch (Exception e) {
                        throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("unable to process %s",f), e);
                    }
                }
            }
        }
        return null;
    }

}
