package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.local.config.LocalTomcatAppConfig;
import net.thevpc.nuts.toolbox.ntomcat.util.TomcatUtils;
import net.thevpc.nuts.toolbox.ntomcat.util._StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class LocalTomcatAppConfigService extends LocalTomcatServiceBase {

    private String name;
    private LocalTomcatAppConfig config;
    private LocalTomcatConfigService tomcat;
    private NutsApplicationContext context;
    private Path sharedConfigFolder;

    public LocalTomcatAppConfigService(String name, LocalTomcatAppConfig config, LocalTomcatConfigService tomcat) {
        this.name = name;
        this.config = config;
        this.tomcat = tomcat;
        this.context = tomcat.getTomcatServer().getContext();
        sharedConfigFolder = Paths.get(tomcat.getContext().getVersionFolderFolder(NutsStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT));
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
            runningFolder = Paths.get(context.getSharedConfigFolder()).resolve("archive").toString();
        }
        String packaging = "war";
        return Paths.get(runningFolder).resolve(name + "-" + version + "." + packaging);
    }

    public Path getRunningFile() {
        String s = getConfig().getSourceFilePath();
        if (!NutsBlankable.isBlank(s)) {
            return Paths.get(s);
        }
        String _runningFolder = tomcat.getConfig().getRunningFolder();
        Path runningFolder = (_runningFolder == null || _runningFolder.trim().isEmpty()) ? null : Paths.get(_runningFolder);
        if (runningFolder == null) {
            runningFolder = Paths.get(context.getSharedConfigFolder()).resolve("running");
        }
        String packaging = "war";
        return runningFolder.resolve(name + "." + packaging);
    }

    public Path getVersionFile() {
        return sharedConfigFolder.resolve(name + ".version");
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
    public NutsString getFormattedPath(String str) {
        return context.getWorkspace()
                .text().forStyled(str,NutsTextStyle.path());
    }
    public NutsString getFormattedVersion(String str) {
        return context.getWorkspace()
                .text().forStyled(str,NutsTextStyle.version());
    }
    public NutsString getFormattedPrefix(String str) {
        return context.getWorkspace()
                .text().builder()
                .append("[")
                .append(str,NutsTextStyle.primary5())
                .append("]");
    }

    public LocalTomcatAppConfigService setCurrentVersion(String version) {
        try {
            if (version == null || version.trim().isEmpty()) {
                context.getSession().out().printf("%s unset version.\n", getFormattedPrefix(getFullName()));
                Files.delete(getVersionFile());
                context.getSession().out().printf("%s [LOG] delete version file %s.\n", getFormattedPrefix(getFullName()), getFormattedPath(getVersionFile().toString()));
                Files.delete(getRunningFile());
                context.getSession().out().printf("%s [LOG] delete running file %s.\n", getFormattedPrefix(getFullName()), getFormattedPath(getRunningFile().toString()));
            } else {
                context.getSession().out().printf("%s set version %s.\n", getFullName(), getFormattedVersion(version));
                context.getSession().out().printf("%s [LOG] updating version file %s to %s.\n", getFormattedPrefix(getFullName()), getFormattedVersion(_StringUtils.coalesce(version, "<DEFAULT>")), getFormattedPath(getVersionFile().toString()));
                Files.write(getVersionFile(), version.getBytes());
                context.getSession().out().printf("%s [LOG] updating archive file %s -> %s.\n", getFormattedPrefix(getFullName()), getFormattedPath(getArchiveFile(version).toString()), getFormattedPath(getRunningFile().toString()));
                Files.copy(getArchiveFile(version), getRunningFile());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public Path getDeployFile() {
        LocalTomcatDomainConfigService d = tomcat.getDomain(getConfig().getDomain(), NutsOpenMode.OPEN_OR_ERROR);
        String deployName = getConfig().getDeployName();
        if (NutsBlankable.isBlank(deployName)) {
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
        context.getSession().out().printf("%s reset deployment (delete %s ).\n", getFormattedPrefix(getFullName()), getFormattedPath(deployFile.toString()));
        try {
            Files.delete(deployFile);
            Files.delete(deployFolder);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return this;
    }

    public LocalTomcatAppConfigService deploy(String version) {
        if (NutsBlankable.isBlank(version)) {
            version = getCurrentVersion();
        }
        Path runningFile = getRunningFile();
        Path deployFile = getDeployFile();
        context.getSession().out().printf("%s deploy %s as file %s to %s.\n",
                getFormattedPrefix(getFullName()), getFormattedVersion(_StringUtils.coalesce(version, "<DEFAULT>")),
                getFormattedPath(runningFile.toString()), getFormattedPath(deployFile.toString()));
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
            if (NutsBlankable.isBlank(version)) {
                version = getCurrentVersion();
            }
            Path domainDeployPath = getArchiveFile(version);
            Files.createDirectories(domainDeployPath.getParent());
            context.getSession().out().printf("%s install version %s : %s->%s.\n",
                    getFormattedPrefix(getFullName()), getFormattedVersion(version), getFormattedPath(f.toString()), getFormattedPath(domainDeployPath.toString()));
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
        context.getSession().out().printf("%s app removed.\n", getFormattedPrefix(getFullName()));
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
    public LocalTomcatAppConfigService print(NutsPrintStream out) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", getFullName());
        result.put("config", getConfig());
        result.put("version", getCurrentVersion());
        result.put("deployFile", getDeployFile());
        result.put("deployfolder", getDeployFolder());
        result.put("runningfolder", getRunningFile());
        result.put("versionFolder", getVersionFile());
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setValue(result).print(out);
        return this;
    }

}
