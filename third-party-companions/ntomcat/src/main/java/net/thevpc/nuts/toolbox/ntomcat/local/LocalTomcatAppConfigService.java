package net.thevpc.nuts.toolbox.ntomcat.local;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
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
    private NSession session;
    private NPath sharedConfigFolder;

    public LocalTomcatAppConfigService(String name, LocalTomcatAppConfig config, LocalTomcatConfigService tomcat) {
        this.name = name;
        this.config = config;
        this.tomcat = tomcat;
        this.session = tomcat.getTomcatServer().getSession();
        sharedConfigFolder = tomcat.getSession().getAppVersionFolder(NStoreType.CONF, NTomcatConfigVersions.CURRENT);
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
            runningFolder = session.getAppSharedConfFolder().resolve("archive").toString();
        }
        String packaging = "war";
        return Paths.get(runningFolder).resolve(name + "-" + version + "." + packaging);
    }

    public NPath getRunningFile() {
        String s = getConfig().getSourceFilePath();
        if (!NBlankable.isBlank(s)) {
            return NPath.of(s,getSession());
        }
        String _runningFolder = tomcat.getConfig().getRunningFolder();
        NPath runningFolder = (_runningFolder == null || _runningFolder.trim().isEmpty()) ? null : NPath.of(_runningFolder, getSession());
        if (runningFolder == null) {
            runningFolder = session.getAppSharedConfFolder().resolve("running");
        }
        String packaging = "war";
        return runningFolder.resolve(name + "." + packaging);
    }

    private NSession getSession() {
        return session;
    }

    public NPath getVersionFile() {
        return sharedConfigFolder.resolve(name + ".version");
    }

    public String getCurrentVersion() {
        NPath f = getVersionFile();
        if (f.exists()) {
            return new String(f.readBytes());
        }
        return null;
    }
    public NString getFormattedPath(String str) {
        return NTexts.of(getSession()).ofStyled(str, NTextStyle.path());
    }
    public NString getFormattedVersion(String str) {
        return NTexts.of(getSession()).ofStyled(str, NTextStyle.version());
    }
    public NString getFormattedPrefix(String str) {
        return NTexts.of(getSession()).ofBuilder()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }

    public LocalTomcatAppConfigService setCurrentVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            getSession().out().println(NMsg.ofC("%s unset version.", getFormattedPrefix(getFullName())));
            getVersionFile().delete();
            getSession().out().println(NMsg.ofC("%s [LOG] delete version file %s.", getFormattedPrefix(getFullName()), getFormattedPath(getVersionFile().toString())));
            getRunningFile().delete();
            getSession().out().println(NMsg.ofC("%s [LOG] delete running file %s.", getFormattedPrefix(getFullName()), getFormattedPath(getRunningFile().toString())));
        } else {
            getSession().out().println(NMsg.ofC("%s set version %s.", getFullName(), getFormattedVersion(version)));
            getSession().out().println(NMsg.ofC("%s [LOG] updating version file %s to %s.", getFormattedPrefix(getFullName()), getFormattedVersion(_StringUtils.coalesce(version, "<DEFAULT>")), getFormattedPath(getVersionFile().toString())));
            getVersionFile().writeString(version);
            getSession().out().println(NMsg.ofC("%s [LOG] updating archive file %s -> %s.", getFormattedPrefix(getFullName()), getFormattedPath(getArchiveFile(version).toString()), getFormattedPath(getRunningFile().toString())));
            NCp.of(getSession()).from(getArchiveFile(version))
                    .to(getRunningFile())
                    .run();
        }
        return this;
    }

    public NPath getDeployFile() {
        LocalTomcatDomainConfigService d = tomcat.getDomain(getConfig().getDomain(), NOpenMode.OPEN_OR_ERROR);
        String deployName = getConfig().getDeployName();
        if (NBlankable.isBlank(deployName)) {
            deployName = name + ".war";
        }
        if (!deployName.endsWith(".war")) {
            deployName += ".war";
        }
        return d.getDomainDeployPath().resolve(deployName);
    }

    public NPath getDeployFolder() {
        NPath f = getDeployFile();
        String fn = f.getName().toString();
        return f.resolveSibling(fn.substring(0, fn.length() - ".war".length()));
    }

    public LocalTomcatAppConfigService resetDeployment() {
        NPath deployFile = getDeployFile();
        NPath deployFolder = getDeployFolder();
        getSession().out().println(NMsg.ofC("%s reset deployment (delete %s ).", getFormattedPrefix(getFullName()), getFormattedPath(deployFile.toString())));
        deployFile.delete();
        deployFolder.delete();
        return this;
    }

    public LocalTomcatAppConfigService deploy(String version) {
        if (NBlankable.isBlank(version)) {
            version = getCurrentVersion();
        }
        NPath runningFile = getRunningFile();
        NPath deployFile = getDeployFile();
        getSession().out().println(NMsg.ofC("%s deploy %s as file %s to %s.",
                getFormattedPrefix(getFullName()), getFormattedVersion(_StringUtils.coalesce(version, "<DEFAULT>")),
                getFormattedPath(runningFile.toString()), getFormattedPath(deployFile.toString())));
        NCp.of(getSession())
                .from(runningFile)
                .to(deployFile)
                .addOptions(NPathOption.REPLACE_EXISTING)
                .run();
        return this;
    }

    public LocalTomcatAppConfigService install(String version, String file, boolean setVersion) {
        try {
            Path f = Paths.get(file);
            if (!Files.isRegularFile(f)) {
                throw new UncheckedIOException(new IOException("File not found " + f));
            }
            if (NBlankable.isBlank(version)) {
                version = getCurrentVersion();
            }
            Path domainDeployPath = getArchiveFile(version);
            Files.createDirectories(domainDeployPath.getParent());
            getSession().out().println(NMsg.ofC("%s install version %s : %s->%s.",
                    getFormattedPrefix(getFullName()), getFormattedVersion(version), getFormattedPath(f.toString()), getFormattedPath(domainDeployPath.toString())));
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
        getSession().out().println(NMsg.ofC("%s app removed.", getFormattedPrefix(getFullName())));
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
    public LocalTomcatAppConfigService print(NPrintStream out) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", getFullName());
        result.put("config", getConfig());
        result.put("version", getCurrentVersion());
        result.put("deployFile", getDeployFile());
        result.put("deployfolder", getDeployFolder());
        result.put("runningfolder", getRunningFile());
        result.put("versionFolder", getVersionFile());
        NSession session = getSession();
        NElements.of(session).json().setValue(result).print(out);
        return this;
    }

}
