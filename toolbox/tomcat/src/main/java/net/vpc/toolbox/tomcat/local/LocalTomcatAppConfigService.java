package net.vpc.toolbox.tomcat.local;

import net.vpc.app.nuts.NutsWorkspaceOpenMode;
import net.vpc.common.strings.StringUtils;
import net.vpc.toolbox.tomcat.local.config.LocalTomcatAppConfig;
import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsApplicationContext;

public class LocalTomcatAppConfigService extends LocalTomcatServiceBase {

    private String name;
    private LocalTomcatAppConfig config;
    private LocalTomcatConfigService tomcat;
    private NutsApplicationContext context;

    public LocalTomcatAppConfigService(String name, LocalTomcatAppConfig config, LocalTomcatConfigService tomcat) {
        this.name = name;
        this.config = config;
        this.tomcat = tomcat;
        this.context = tomcat.getTomcatServer().getContext();
    }

    @Override
    public LocalTomcatAppConfig getConfig() {
        return config;
    }

    public LocalTomcatConfigService getTomcat() {
        return tomcat;
    }

    public Path getArchiveFile(String version) {
        String runningFolder = tomcat.getConfig().getArchiveFolder();
        if (runningFolder == null || runningFolder.trim().isEmpty()) {
            runningFolder = context.getVarFolder().resolve("archive").toString();
        }
        String packaging = "war";
        return Paths.get(runningFolder).resolve(name + "-" + version + "." + packaging);
    }

    public Path getRunningFile() {
        String s = getConfig().getSourceFilePath();
        if (!TomcatUtils.isBlank(s)) {
            return Paths.get(s);
        }
        String _runningFolder = tomcat.getConfig().getRunningFolder();
        Path runningFolder = (_runningFolder == null || _runningFolder.trim().isEmpty()) ? null : Paths.get(_runningFolder);
        if (runningFolder == null) {
            runningFolder = context.getVarFolder().resolve("running");
        }
        String packaging = "war";
        return runningFolder.resolve(name + "." + packaging);
    }

    public Path getVersionFile() {
        return context.getSharedConfigFolder().resolve(name + ".version");
    }

    public String getCurrentVersion() {
        Path f = getVersionFile();
        if (Files.exists(f)) {
            try {
                return new String(Files.readAllBytes(f));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return null;
    }

    public LocalTomcatAppConfigService setCurrentVersion(String version) {
        try {
            if (version == null || version.trim().isEmpty()) {
                context.session().out().printf("==[%s]== unset version.\n", getFullName());
                Files.delete(getVersionFile());
                context.session().out().printf("==[%s]== [LOG] delete version file [[%s]].\n", getFullName(), getVersionFile());
                Files.delete(getRunningFile());
                context.session().out().printf("==[%s]== [LOG] delete running file [[%s]].\n", getFullName(), getRunningFile());
            } else {
                context.session().out().printf("==[%s]== set version [[%s]].\n", getFullName(), version);
                context.session().out().printf("==[%s]== [LOG] updating version file [[%s]] to [[%s]].\n", getFullName(), StringUtils.coalesce(version, "<DEFAULT>"), getVersionFile());
                Files.write(getVersionFile(), version.getBytes());
                context.session().out().printf("==[%s]== [LOG] updating archive file [[%s]] -> [[%s]].\n", getFullName(), getArchiveFile(version), getRunningFile());
                Files.copy(getArchiveFile(version), getRunningFile());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public Path getDeployFile() {
        LocalTomcatDomainConfigService d = tomcat.getDomain(getConfig().getDomain(), NutsWorkspaceOpenMode.OPEN_EXISTING);
        String deployName = getConfig().getDeployName();
        if (TomcatUtils.isBlank(deployName)) {
            deployName = name + ".war";
        }
        if (!deployName.endsWith(".war")) {
            deployName += ".war";
        }
        return d.getDomainDeployPath().resolve(deployName);
    }

    public Path getDeployFolder() {
        Path f = getDeployFile();
        String fn = f.getFileName().toString();
        return f.resolveSibling(fn.substring(0, fn.length() - ".war".length()));
    }

    public LocalTomcatAppConfigService resetDeployment() {
        Path deployFile = getDeployFile();
        Path deployFolder = getDeployFolder();
        context.session().out().printf("==[%s]== reset deployment (delete [[%s]] ).\n", getFullName(), deployFile);
        try {
            Files.delete(deployFile);
            Files.delete(deployFolder);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public LocalTomcatAppConfigService deploy(String version) {
        if (TomcatUtils.isBlank(version)) {
            version = getCurrentVersion();
        }
        Path runningFile = getRunningFile();
        Path deployFile = getDeployFile();
        context.session().out().printf("==[%s]== deploy [[%s]] as file [[%s]] to [[%s]].\n", getFullName(), StringUtils.coalesce(version, "<DEFAULT>"), runningFile, deployFile);
        try {
            Files.copy(runningFile, deployFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public LocalTomcatAppConfigService install(String version, String file, boolean setVersion) {
        try {
            Path f = Paths.get(file);
            if (!Files.isRegularFile(f)) {
                throw new UncheckedIOException(new IOException("File not found " + f));
            }
            if (StringUtils.isBlank(version)) {
                version = getCurrentVersion();
            }
            Path domainDeployPath = getArchiveFile(version);
            Files.createDirectories(domainDeployPath.getParent());
            context.session().out().printf("==[%s]== install version [[%s]] : [[%s]]->[[%s]].\n", getFullName(), version, f, domainDeployPath);
            Files.copy(f, domainDeployPath);
            if (setVersion) {
                setCurrentVersion(version);
            }
            return this;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

//
//    public void deploy(String configName, String appName, String version, String file) {
//        LocalTomcatConfig c = loadConfig(configName);
//        if (c.getApps().containsKey(appName)) {
//            LocalTomcatAppConfig a = getTomcatApp(c, appName);
//            c.getRunningFolder()
//            String domainDeployPath = getDomainDeployPath(configName, a.getDomain());
//            Files.copy(ws.getStoreLocation())
//        }
//    }
    @Override
    public LocalTomcatAppConfigService remove() {
        tomcat.getConfig().getApps().remove(name);
        context.session().out().printf("==[%s]== app removed.\n", getFullName());
        return this;
    }

    public String getFullName() {
        return tomcat.getName() + "/" + getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LocalTomcatAppConfigService print(PrintStream out) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", getFullName());
        result.put("config", getConfig());
        result.put("version", getCurrentVersion());
        result.put("deployFile", getDeployFile());
        result.put("deployfolder", getDeployFolder());
        result.put("runningfolder", getRunningFile());
        result.put("versionFolder", getVersionFile());
        context.getWorkspace().json().value(result).print(out);
        return this;
    }

}
