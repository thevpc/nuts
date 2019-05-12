package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.ByteArrayPrintStream;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsWorkspaceInfoFormat implements NutsWorkspaceInfoFormat {

    private final NutsWorkspace ws;
    private final Properties extraProperties = new Properties();
    private boolean showRepositories = false;
    private boolean fancy = false;
    private NutsSession session;

    public DefaultNutsWorkspaceInfoFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspaceInfoFormat session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsWorkspaceInfoFormat setSession(NutsSession session) {
        //should copy because will chage outputformat
        this.session = session == null ? null : session.copy();
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat showRepositories() {
        showRepositories(true);
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat showRepositories(boolean enable) {
        return setShowRepositories(enable);
    }

    @Override
    public NutsWorkspaceInfoFormat setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isShowRepositories() {
        return showRepositories;
    }

    @Override
    public NutsWorkspaceInfoFormat addProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat addProperties(Properties p) {
        if (p != null) {
            extraProperties.putAll(p);
        }
        return this;
    }

    public NutsSession getValidSession() {
        if (session == null) {
            session = ws.createSession();
        }
        return session;
    }

    @Override
    public NutsWorkspaceInfoFormat parseOptions(String... args) {
        NutsCommandLine cmd = ws.parser().parseCommandLine(args);
        NutsArgument a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                case "--show-repos": {
                    this.setShowRepositories(a.getBooleanValue());
                    break;
                }
                case "--fancy": {
                    this.setFancy(a.getBooleanValue());
                    break;
                }
                case "--add": {
                    NutsArgument r = cmd.getValueFor(a);
                    extraProperties.put(r.getKey().getString(), r.getValue().getString());
                    break;
                }
                default: {
                    cmd.pushBack(a);
                    if (!getValidSession().configureFirst(cmd)) {
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NutsWorkspaceInfoFormat setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public void print() {
        print(ws.getTerminal());
    }

    @Override
    public void println() {
        println(ws.getTerminal());
    }

    @Override
    public void print(NutsTerminal terminal) {
        print(terminal.out());
    }

    @Override
    public void println(NutsTerminal terminal) {
        println(terminal.out());
    }

    @Override
    public void print(File file) {
        print(file.toPath());
    }

    @Override
    public void println(File file) {
        println(file.toPath());
    }

    @Override
    public void print(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            print(w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void println(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            println(w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        return format();
    }

    @Override
    public String format() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        PrintWriter w = new PrintWriter(out);
        format0(w);
        w.flush();
        return out.toString();
    }

    @Override
    public void print(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        print(p);
        p.flush();
    }

    @Override
    public void println(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        println(p);
        p.flush();
    }

    @Override
    public void print(Writer w) {
        format0(w);
    }

    @Override
    public void println(Writer w) {
        try {
            format0(w);
            w.write("\n");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void format0(Writer w) {
        NutsOutputFormat t = getValidSession().getOutputFormat();
        if (t == null) {
            t = NutsOutputFormat.PLAIN;
        }
        NutsOutputFormatWriter m = ws.formatter().createOutputFormatWriter(t, buildWorkspaceMap(isShowRepositories()));
        if (isFancy()) {
            List<String> args=new ArrayList<>();
            args.add(MapStringObjectOutputFormatWriter.OPTION_MULTILINE_PROPERTY+ "=nuts-runtime-path=:|;");
            args.add(MapStringObjectOutputFormatWriter.OPTION_MULTILINE_PROPERTY+ "=nuts-boot-runtime-path=:|;");
            args.add(MapStringObjectOutputFormatWriter.OPTION_MULTILINE_PROPERTY+ "=java.class.path=" + File.pathSeparator);
            args.add(MapStringObjectOutputFormatWriter.OPTION_MULTILINE_PROPERTY+ "=java-class-path=" + File.pathSeparator);
            args.add(MapStringObjectOutputFormatWriter.OPTION_MULTILINE_PROPERTY+ "=java.library.path=" + File.pathSeparator);
            args.add(MapStringObjectOutputFormatWriter.OPTION_MULTILINE_PROPERTY+ "=java-library-path=" + File.pathSeparator);
            NutsCommandLine cmd = ws.parser().parseCommandLine(args.toArray(new String[0]));
            m.configure(cmd, true);
        }
        m.write(w);
    }

    private static String key(String prefix, String key) {
        if (CoreStringUtils.isBlank(prefix)) {
            return key;
        }
        return prefix + "." + key;
    }

//    @Override
    private LinkedHashMap<String, Object> buildWorkspaceMap(boolean deep) {
        String prefix = null;
        LinkedHashMap<String, Object> props = new LinkedHashMap<>();
        NutsWorkspaceConfigManager configManager = ws.config();
        NutsBootContext rtcontext = configManager.getContext(NutsBootContextType.RUNTIME);
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }

        props.put("nuts-api-version", rtcontext.getApiId().getVersion().toString());
        props.put("nuts-api-id", rtcontext.getApiId().toString());
        props.put("nuts-runtime-id", rtcontext.getRuntimeId().toString());
        URL[] cl = configManager.getBootClassWorldURLs();
        List<String> runtimeClassPath = new ArrayList<>();
        if (cl != null) {
            for (URL url : cl) {
                if (url != null) {
                    String s = url.toString();
                    try {
                        s = Paths.get(url.toURI()).toFile().getPath();
                    } catch (URISyntaxException ex) {
                        s = s.replace(":", "\\:");
                    }
                    runtimeClassPath.add(s);
                }
            }
        }

        props.put("nuts-runtime-path", CoreStringUtils.join(";", runtimeClassPath));
        props.put("nuts-workspace", configManager.getWorkspaceLocation().toString());
        props.put("nuts-workspace-id", configManager.getUuid());
        props.put("nuts-secure", stringValue(ws.security().isSecure()));
        props.put("nuts-store-layout", stringValue(configManager.getStoreLocationLayout()));
        props.put("nuts-store-strategy", stringValue(configManager.getStoreLocationStrategy()));
        props.put("nuts-repo-store-strategy", stringValue(configManager.getRepositoryStoreLocationStrategy()));
        props.put("nuts-option-open-mode", stringValue(configManager.getOptions().getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : configManager.getOptions().getOpenMode()));
        props.put("nuts-option-perf", stringValue(configManager.getOptions().isPerf()));
        props.put("nuts-option-init", stringValue(configManager.getOptions().getInitMode()));
        props.put("nuts-option-read-only", stringValue(configManager.getOptions().isReadOnly()));
        props.put("nuts-option-skip-companions", stringValue(configManager.getOptions().isSkipInstallCompanions()));
        for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
            props.put("nuts-workspace-" + folderType.name().toLowerCase(), configManager.getStoreLocation(folderType).toString());
        }
        props.put("java-version", System.getProperty("java.version"));
        props.put("java-executable", CoreIOUtils.resolveJavaCommand(null));
        props.put("java-classpath", System.getProperty("java.class.path"));
        props.put("java-library-path", System.getProperty("java.library.path"));
        props.put("os-name", ws.config().getPlatformOs().toString());
        props.put("os-family", stringValue(ws.config().getPlatformOsFamily()));
        if (ws.config().getPlatformOsDist() != null) {
            props.put("os-dist", ws.config().getPlatformOsDist().toString());
        }
        props.put("os-arch", ws.config().getPlatformArch().toString());
        props.put("user-name", System.getProperty("user.name"));
        props.put("user-home", System.getProperty("user.home"));
        props.put("user-dir", System.getProperty("user.dir"));
        props.put("creation-started", stringValue(new Date(ws.config().getCreationStartTimeMillis())));
        props.put("creation-finished", stringValue(new Date(ws.config().getCreationFinishTimeMillis())));
        props.put("creation-within", CoreCommonUtils.formatPeriodMilli(ws.config().getCreationTimeMillis()).trim());
        props.put("repositories-count", stringValue(ws.config().getRepositories().length));
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.getProperty(extraKey));
        }
        if (deep) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            props.put("repos", repositories);
            for (NutsRepository repository : ws.config().getRepositories()) {
                repositories.put(repository.config().name(), buildRepoRepoMap(repository, deep, prefix));
            }
        }

        return props;
    }

    private Map<String, Object> buildRepoRepoMap(NutsRepository repo, boolean deep, String prefix) {
        LinkedHashMap<String, Object> props = new LinkedHashMap<>();
        props.put(key(prefix, "name"), stringValue(repo.config().getName()));
        props.put(key(prefix, "global-name"), repo.config().getGlobalName());
        props.put(key(prefix, "uuid"), stringValue(repo.config().getUuid()));
        props.put(key(prefix, "type"), repo.config().getType());
        props.put(key(prefix, "speed"), stringValue(repo.config().getSpeed()));
        props.put(key(prefix, "enabled"), stringValue(repo.config().isEnabled()));
        props.put(key(prefix, "index-enabled"), stringValue(repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), stringValue(repo.config().isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().getLocation(false));
        if (repo.config().getLocation(false) != null) {
            props.put(key(prefix, "location-expanded"), repo.config().getLocation(true));
        }
        props.put(key(prefix, "deploy-order"), stringValue(repo.config().getDeployOrder()));
        props.put(key(prefix, "store-location-strategy"), stringValue(repo.config().getStoreLocationStrategy()));
        props.put(key(prefix, "store-location"), stringValue(repo.config().getStoreLocation()));
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            props.put(key(prefix, "store-location-" + value.name().toLowerCase()), stringValue(repo.config().getStoreLocation(value)));
        }
        props.put(key(prefix, "supported-mirroring"), stringValue(repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), stringValue((!repo.config().isSupportedMirroring()) ? 0 : repo.config().getMirrors().length));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                props.put("mirrors", mirrors);
                for (NutsRepository mirror : repo.config().getMirrors()) {
                    mirrors.put(mirror.config().name(), buildRepoRepoMap(mirror, deep, null));
                }
            }
        }
        return props;
    }

    private String stringValue(Object o) {
        if (o == null) {
            return "";
        }
        if (o.getClass().isEnum()) {
            return o.toString().toLowerCase().replace("_", "-");
        }
        if (o instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date) o);
        }
        return o.toString();
    }
}
