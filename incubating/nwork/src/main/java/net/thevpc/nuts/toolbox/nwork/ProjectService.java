package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nwork.config.ProjectConfig;
import net.thevpc.nuts.toolbox.nwork.config.RepositoryAddress;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIdUtils;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ProjectService {

    private ProjectConfig config;
    private final RepositoryAddress defaultRepositoryAddress;
    private final NPath sharedConfigFolder;

    public ProjectService(RepositoryAddress defaultRepositoryAddress, NPath file) throws IOException {
        this.defaultRepositoryAddress = defaultRepositoryAddress == null ? new RepositoryAddress() : defaultRepositoryAddress;
        config = NElements.of().json().parse(file, ProjectConfig.class);
        sharedConfigFolder = NSession.get().getAppVersionFolder(NStoreType.CONF, NWorkConfigVersions.CURRENT);
    }

    public ProjectService(RepositoryAddress defaultRepositoryAddress, ProjectConfig config) {
        this.config = config;
        this.defaultRepositoryAddress = defaultRepositoryAddress;
        sharedConfigFolder = NSession.get().getAppVersionFolder(NStoreType.CONF, NWorkConfigVersions.CURRENT);
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

    public NPath getConfigFile() {
        NPath storeLocation = sharedConfigFolder.resolve("projects");
        return storeLocation.resolve(config.getId().replace(":", "-") + ".config");
    }

    public void save() throws IOException {
        NPath configFile = getConfigFile();
        configFile.mkParentDirs();
        NElements.of().json().setValue(config).print(configFile);
    }

    public boolean load() {
        NPath configFile = getConfigFile();
        if (configFile.isRegularFile()) {
            ProjectConfig u = NElements.of().json().parse(configFile, ProjectConfig.class);
            if (u != null) {
                config = u;
                return true;
            }
        }
        return false;
    }

    public NDescriptor getPom() {
        File f = new File(config.getPath());
        if (f.isDirectory()) {
            if (new File(f, "pom.xml").isFile()) {
                try {
                    return NDescriptorParser.of()
                            .setDescriptorStyle(NDescriptorStyle.MAVEN)
                            .parse(new File(f, "pom.xml")).get();
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
                    NDescriptor g = NDescriptorParser.of()
                            .setDescriptorStyle(NDescriptorStyle.MAVEN)
                            .parse(new File(f, "pom.xml")).get();
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
        NId id = NId.of(sid).get();
        if (config.getTechnologies().contains("maven")) {
            File f = new File(System.getProperty("user.home"), ".m2/repository/" + NIdUtils.resolveJarPath(id));
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
                        return NDescriptorParser.of()
                                .setDescriptorStyle(NDescriptorStyle.MAVEN)
                                .parse(new File(f, "pom.xml")).get().getId().getVersion().toString();
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }
        return null;
    }

    public File detectRemoteVersionFile(String sid) {
        NId id = NId.of(sid).get();
        if (config.getTechnologies().contains("maven")) {
            RepositoryAddress a = config.getAddress();
            if (a == null) {
                a = defaultRepositoryAddress;
            }
            if (a == null) {
                a = new RepositoryAddress();
            }
            String nutsRepository = a.getNutsRepository();
            if (NBlankable.isBlank(nutsRepository)) {
                throw new NExecutionException(NMsg.ofPlain("missing repository. try 'nwork set -r vpc-public-maven' or something like that"), NExecutionException.ERROR_2);
            }
            try {
                NSession s = null;
                if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(NLocations.of().getWorkspaceLocation().toString())) {
                    s = Nuts.openWorkspace(
                            new DefaultNBootOptionsBuilder()
                                    .setOpenMode(NOpenMode.OPEN_OR_ERROR)
                                    .setReadOnly(true)
                                    .setWorkspace(a.getNutsWorkspace())
                    ).currentSession();
                    s.setAll(NSession.get());
                } else {
                    s = NSession.get();
                }

                List<NDefinition> found = s.callWith(() -> NSearchCmd.of()
                        .addId(sid)
                        .addRepositoryFilter(NRepositoryFilters.of().byName(nutsRepository))
                        .setLatest(true).setContent(true).getResultDefinitions().toList()
                );
                if (found.size() > 0) {
                    NPath p = found.get(0).getContent().orNull();
                    if (p == null) {
                        return null;
                    }
                    return p.toFile().get();
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
                    if (NBlankable.isBlank(nutsRepository)) {
                        throw new NExecutionException(NMsg.ofPlain("missing repository. try 'nwork set -r vpc-public-maven' or something like that"), NExecutionException.ERROR_2);
                    }
                    try {
                        NDescriptor g = NDescriptorParser.of()
                                .setDescriptorStyle(NDescriptorStyle.MAVEN)
                                .parse(new File(f, "pom.xml")).get();
                        NSession s = null;
                        if (a.getNutsWorkspace() != null && a.getNutsWorkspace().trim().length() > 0 && !a.getNutsWorkspace().equals(NLocations.of().getWorkspaceLocation().toString())) {
                            s = Nuts.openWorkspace(
                                    new DefaultNBootOptionsBuilder()
                                            .setOpenMode(NOpenMode.OPEN_OR_ERROR)
                                            .setReadOnly(true)
                                            .setWorkspace(a.getNutsWorkspace())
                            ).currentSession();
                            s.setAll(NSession.get());
                        } else {
                            s = NSession.get();
                        }
                        List<NId> found = s.callWith(()->NSearchCmd.of()
                                .addId(g.getId().getGroupId() + ":" + g.getId().getArtifactId())
                                .addRepositoryFilter(NRepositoryFilters.of().byName(nutsRepository))
                                .setLatest(true).getResultIds().toList());
                        if (found.size() > 0) {
                            return found.get(0).getVersion().toString();
                        }
                    } catch (Exception e) {
                        throw new NIllegalArgumentException(NMsg.ofC("unable to process %s", f), e);
                    }
                }
            }
        }
        return null;
    }

}
