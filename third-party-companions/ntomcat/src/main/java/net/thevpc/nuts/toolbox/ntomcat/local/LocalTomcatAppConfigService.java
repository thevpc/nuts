package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPathOption;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.ntomcat.NTomcatConfigVersions;
import net.thevpc.nuts.toolbox.ntomcat.local.config.LocalTomcatAppConfig;
import net.thevpc.nuts.toolbox.ntomcat.util._StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LocalTomcatAppConfigService extends LocalTomcatServiceBase {

    private String name;
    private LocalTomcatAppConfig config;
    private LocalTomcatConfigService tomcat;
    private NutsApplicationContext context;
    private NutsPath sharedConfigFolder;

    public LocalTomcatAppConfigService(String name, LocalTomcatAppConfig config, LocalTomcatConfigService tomcat) {
        this.name = name;
        this.config = config;
        this.tomcat = tomcat;
        this.context = tomcat.getTomcatServer().getContext();
        sharedConfigFolder = tomcat.getContext().getVersionFolder(NutsStoreLocation.CONFIG, NTomcatConfigVersions.CURRENT);
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
            runningFolder = context.getSharedConfigFolder().resolve("archive").toString();
        }
        String packaging = "war";
        return Paths.get(runningFolder).resolve(name + "-" + version + "." + packaging);
    }

    public NutsPath getRunningFile() {
        String s = getConfig().getSourceFilePath();
        if (!NutsBlankable.isBlank(s)) {
            return NutsPath.of(s,getSession());
        }
        String _runningFolder = tomcat.getConfig().getRunningFolder();
        NutsPath runningFolder = (_runningFolder == null || _runningFolder.trim().isEmpty()) ? null : NutsPath.of(_runningFolder, getSession());
        if (runningFolder == null) {
            runningFolder = context.getSharedConfigFolder().resolve("running");
        }
        String packaging = "war";
        return runningFolder.resolve(name + "." + packaging);
    }

    private NutsSession getSession() {
        return context.getSession();
    }

    public NutsPath getVersionFile() {
        return sharedConfigFolder.resolve(name + ".version");
    }

    public String getCurrentVersion() {
        NutsPath f = getVersionFile();
        if (f.exists()) {
            return new String(f.readAllBytes());
        }
        return null;
    }
    public NutsString getFormattedPath(String str) {
        return NutsTexts.of(getSession()).ofStyled(str,NutsTextStyle.path());
    }
    public NutsString getFormattedVersion(String str) {
        return NutsTexts.of(getSession()).ofStyled(str, NutsTextStyle.version());
    }
    public NutsString getFormattedPrefix(String str) {
        return NutsTexts.of(getSession()).builder()
                .append("[")
                .append(str,NutsTextStyle.primary5())
                .append("]");
    }

    public LocalTomcatAppConfigService setCurrentVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            getSession().out().printf("%s unset version.\n", getFormattedPrefix(getFullName()));
            getVersionFile().delete();
            getSession().out().printf("%s [LOG] delete version file %s.\n", getFormattedPrefix(getFullName()), getFormattedPath(getVersionFile().toString()));
            getRunningFile().delete();
            getSession().out().printf("%s [LOG] delete running file %s.\n", getFormattedPrefix(getFullName()), getFormattedPath(getRunningFile().toString()));
        } else {
            getSession().out().printf("%s set version %s.\n", getFullName(), getFormattedVersion(version));
            getSession().out().printf("%s [LOG] updating version file %s to %s.\n", getFormattedPrefix(getFullName()), getFormattedVersion(_StringUtils.coalesce(version, "<DEFAULT>")), getFormattedPath(getVersionFile().toString()));
            getVersionFile().writeBytes(version.getBytes());
            getSession().out().printf("%s [LOG] updating archive file %s -> %s.\n", getFormattedPrefix(getFullName()), getFormattedPath(getArchiveFile(version).toString()), getFormattedPath(getRunningFile().toString()));
            NutsCp.of(getSession()).from(getArchiveFile(version))
                    .to(getRunningFile())
                    .run();
        }
        return this;
    }

    public NutsPath getDeployFile() {
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

    public NutsPath getDeployFolder() {
        NutsPath f = getDeployFile();
        String fn = f.getName().toString();
        return f.resolveSibling(fn.substring(0, fn.length() - ".war".length()));
    }

    public LocalTomcatAppConfigService resetDeployment() {
        NutsPath deployFile = getDeployFile();
        NutsPath deployFolder = getDeployFolder();
        getSession().out().printf("%s reset deployment (delete %s ).\n", getFormattedPrefix(getFullName()), getFormattedPath(deployFile.toString()));
        deployFile.delete();
        deployFolder.delete();
        return this;
    }

    public LocalTomcatAppConfigService deploy(String version) {
        if (NutsBlankable.isBlank(version)) {
            version = getCurrentVersion();
        }
        NutsPath runningFile = getRunningFile();
        NutsPath deployFile = getDeployFile();
        getSession().out().printf("%s deploy %s as file %s to %s.\n",
                getFormattedPrefix(getFullName()), getFormattedVersion(_StringUtils.coalesce(version, "<DEFAULT>")),
                getFormattedPath(runningFile.toString()), getFormattedPath(deployFile.toString()));
        NutsCp.of(getSession())
                .from(runningFile)
                .to(deployFile)
                .addOptions(NutsPathOption.REPLACE_EXISTING)
                .run();
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
            getSession().out().printf("%s install version %s : %s->%s.\n",
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
        getSession().out().printf("%s app removed.\n", getFormattedPrefix(getFullName()));
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
        NutsSession session = getSession();
        NutsElements.of(session).json().setValue(result).print(out);
        return this;
    }

}
