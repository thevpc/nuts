package net.thevpc.nuts.runtime.standalone.workspace.cmd.info;

import net.thevpc.nuts.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.dependency.solver.NDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NPredicates;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNInfoCommand extends DefaultFormatBase<NInfoCommand> implements NInfoCommand {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean showRepositories = false;
    private boolean fancy = false;
    private List<String> requests = new ArrayList<>();
    private List<Supplier<Map<String, Object>>> extraSuppliers = new ArrayList<>();
    private Predicate<String> filter = NPredicates.always();
    private boolean lenient = false;
    private Function<MapAndSession, Map<String, Object>> extraPropertiesSupplier = a -> (Map) extraProperties;

    Function<MapAndSession, Map<String, Object>> repoSupplier = s -> {
        Object oldRepos = s.map.get("repos");
        if (oldRepos instanceof String) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            for (NRepository repository : s.session.repos().setSession(getSession()).getRepositories()) {
                repositories.put(repository.getName(), buildRepoRepoMap(repository, true, null));
            }
            return repositories;
        }
        return null;
    };
    private Map<String, Function<NSession, Object>> mapSupplier;
    private Function<StringAndSession, Object> fct = new Function<StringAndSession, Object>() {
        @Override
        public Object apply(StringAndSession s) {
            Function<NSession, Object> r = mapSupplier.get(s.string);
            if(r!=null){
                return r.apply(s.session);
            }
            String v = extraProperties.get(s.string);
            if(v!=null){
                return v;
            }
            NRepository repo = s.session.repos().setSession(getSession()).getRepository(s.string);
            if(repo!=null){
                return buildRepoRepoMap(repo, true, null);
            }
            return null;
        }
    };

    public DefaultNInfoCommand(NSession session) {
        super(session, "info");
        mapSupplier = buildMapSupplier();
    }

    private static String key(String prefix, String key) {
        if (NBlankable.isBlank(prefix)) {
            return key;
        }
        return prefix + "." + key;
    }

    @Override
    public NInfoCommand addProperty(String key, String value) {
        if (value == null) {
            extraProperties.remove(key);
        } else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NInfoCommand addProperties(Map<String, String> customProperties) {
        if (customProperties != null) {
            for (Map.Entry<String, String> e : customProperties.entrySet()) {
                addProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public boolean isShowRepositories() {
        return showRepositories;
    }

    @Override
    public NInfoCommand setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NInfoCommand setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public NInfoCommand copySession() {
        NSession s = getSession();
        if (s != null) {
            s = s.copy();
        }
        return setSession(s);
    }

    @Override
    public void print(NOutStream w) {
        checkSession();
        List<String> args = new ArrayList<>();
        args.add("--escape-text=false");
        if (isFancy()) {
            args.add("--multiline-property=nuts-runtime-path=;");
            args.add("--multiline-property=java-classpath=" + File.pathSeparator);
            args.add("--multiline-property=java-library-path=" + File.pathSeparator);

            args.add("--multiline-property=nuts-boot-runtime-path=:|;");
            args.add("--multiline-property=java.class.path=" + File.pathSeparator);
            args.add("--multiline-property=java-class-path=" + File.pathSeparator);
            args.add("--multiline-property=java.library.path=" + File.pathSeparator);
        }
        Object result = null;
        if (requests.isEmpty()) {
            result = buildWorkspaceMap(isShowRepositories());
        } else if (requests.size() == 1) {
            final Map<String, Object> t = buildWorkspaceMap(true);
            String key = requests.get(0);
            Object v = t.get(key);
            if (v != null) {
                result = v;
            } else {
                if (!isLenient()) {
                    throw new NIllegalArgumentException(getSession(), NMsg.ofCstyle("property not found : %s", key));
                }
            }
        } else {
            final Map<String, Object> t = buildWorkspaceMap(true);
            Map<String, Object> e = new LinkedHashMap<>();
            result = e;
            for (String request : requests) {
                if (t.containsKey(request)) {
                    e.put(request, t.get(request));
                } else {
                    if (!isLenient()) {
                        throw new NIllegalArgumentException(getSession(), NMsg.ofCstyle("property not found : %s", request));
                    }
                }
            }
        }
        NSession session = getSession().copy();
        if (session.isPlainOut()) {
            session.setOutputFormat(NContentType.PROPS);
        }
        NObjectFormat.of(session).setValue(result).configure(true, args.toArray(new String[0])).print(w);
    }

    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        NSession session = getSession();
        NArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "-r":
            case "--repos": {
                boolean val = cmdLine.nextBoolean().get(session).getBooleanValue().get(session);
                if (enabled) {
                    this.setShowRepositories(val);
                }
                return true;
            }
            case "--fancy": {
                boolean val = cmdLine.nextBoolean().get(session).getBooleanValue().get(session);
                if (enabled) {
                    this.setFancy(val);
                }
                return true;
            }
            case "-l":
            case "--lenient": {
                boolean val = cmdLine.nextBoolean().get(session).getBooleanValue().get(session);
                if (enabled) {
                    this.setLenient(val);
                }
                return true;
            }
            case "--add": {
                String aa = cmdLine.nextString().get(session).getStringValue().get(session);
                NArgument val = NArgument.of(aa);
                if (enabled) {
                    extraProperties.put(val.key(), val.getStringValue().get(session));
                }
                return true;
            }
            case "-p":
            case "--path": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("nuts-workspace");
                    for (NStoreLocation folderType : NStoreLocation.values()) {
                        requests.add("nuts-workspace-" + folderType.id());
                    }
                    requests.add("user-home");
                    requests.add("user-dir");
                }
                return true;
            }
            case "-e":
            case "--env": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("platform");
                    requests.add("java-version");
                    requests.add("java-home");
                    requests.add("java-executable");
                    requests.add("java-classpath");
                    requests.add("os-name");
                    requests.add("os-family");
                    requests.add("os-dist");
                    requests.add("os-arch");
                    requests.add("user-name");
                }
                return true;
            }
            case "-c":
            case "--cmd": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("command-line-long");
                    requests.add("command-line-short");
                    requests.add("inherited");
                    requests.add("inherited-nuts-boot-args");
                    requests.add("inherited-nuts-args");
                }
                return true;
            }
            case "-g":
            case "--get": {
                String r = cmdLine.nextString().get(session).getStringValue().get(session);
                if (enabled) {
                    requests.add(r);
                }
                while (true) {
                    NArgument p = cmdLine.peek().orNull();
                    if (p != null && !p.isOption()) {
                        cmdLine.skip();
                        if (enabled) {
                            requests.add(p.asString().get(session));
                        }
                    } else {
                        break;
                    }
                }
                return true;
            }
            default: {
                if (session.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<String, Object> getPropertyValues() {
        return buildWorkspaceMap(true);
    }

    public NOptional<Object> getPropertyValue(String propertyName) {
        return NOptional.ofNamed(fct.apply(new StringAndSession(propertyName,getSession())), "property " + propertyName);
    }

    private Map<String, Function<NSession, Object>> buildMapSupplier() {
        Map<String, Function<NSession, Object>> props = new HashMap<>();
        props.put("name", session -> stringValue(session.getWorkspace().getName()));
        props.put("nuts-api-version", session -> session.getWorkspace().getApiVersion());
        props.put("nuts-api-id", session -> session.getWorkspace().getApiId());
        props.put("nuts-runtime-id", session -> session.getWorkspace().getRuntimeId());
        props.put("nuts-app-id", session -> session.getAppId());

        props.put("nuts-runtime-classpath",
                session -> {
                    NTexts txt = NTexts.of(session);
                    List<URL> cl = session.boot().getBootClassWorldURLs();
                    List<NPath> runtimeClassPath = new ArrayList<>();
                    if (cl != null) {
                        for (URL url : cl) {
                            if (url != null) {
                                String s = url.toString();
                                try {
                                    s = Paths.get(url.toURI()).toFile().getPath();
                                } catch (URISyntaxException ex) {
                                    s = s.replace(":", "\\:");
                                }
                                runtimeClassPath.add(NPath.of(s, session));
                            }
                        }
                    }
                    return txt.ofBuilder().appendJoined(";", runtimeClassPath);
                }
        );
        props.put("nuts-workspace-id", session -> NTexts.of(session).ofStyled(stringValue(session.getWorkspace().getUuid()), NTextStyle.path()));
        props.put("nuts-store-layout", session -> session.locations().getStoreLocationLayout());
        props.put("nuts-store-strategy", session -> session.locations().getStoreLocationStrategy());
        props.put("nuts-repo-store-strategy", session -> session.locations().getRepositoryStoreLocationStrategy());
        props.put("nuts-global", session -> session.boot().getBootOptions().getGlobal().orNull());
        props.put("nuts-workspace", session -> session.locations().getWorkspaceLocation());
        for (NStoreLocation folderType : NStoreLocation.values()) {
            props.put("nuts-workspace-" + folderType.id(), session -> session.locations().getStoreLocation(folderType));
        }
        props.put("nuts-open-mode", session -> session.boot().getBootOptions().getOpenMode().orNull());
        props.put("nuts-isolation-level", session -> session.boot().getBootOptions().getIsolationLevel().orNull());
        props.put("nuts-secure", session -> (session.security().isSecure()));
        props.put("nuts-gui", session -> session.boot().getBootOptions().getGui().orNull());
        props.put("nuts-inherited", session -> session.boot().getBootOptions().getInherited().orNull());
        props.put("nuts-recover", session -> session.boot().getBootOptions().getRecover().orNull());
        props.put("nuts-reset", session -> session.boot().getBootOptions().getReset().orNull());
        props.put("nuts-read-only", session -> session.boot().getBootOptions().getReadOnly().orNull());
        props.put("nuts-debug", session -> NDebugString.of(session.boot().getBootOptions().getDebug().orNull(), getSession()));
        props.put("nuts-bot", session -> session.boot().getBootOptions().getBot().orNull());
        props.put("nuts-trace", session -> session.boot().getBootOptions().getTrace().orNull());
        props.put("nuts-indexed", session -> session.boot().getBootOptions().getIndexed().orNull());
        props.put("nuts-transitive", session -> session.boot().getBootOptions().getTransitive().orNull());
        props.put("nuts-fetch-strategy", session -> session.boot().getBootOptions().getFetchStrategy().orNull());
        props.put("nuts-execution-type", session -> session.boot().getBootOptions().getExecutionType().orNull());
        props.put("nuts-dry", session -> session.boot().getBootOptions().getDry().orNull());
        props.put("nuts-output-format", session -> session.boot().getBootOptions().getOutputFormat().orNull());
        props.put("nuts-confirm", session -> session.boot().getBootOptions().getConfirm().orNull());
        props.put("nuts-dependency-solver", session -> session.boot().getBootOptions().getDependencySolver().orNull());
        props.put("nuts-progress-options", session -> session.boot().getBootOptions().getProgressOptions().orNull());
        props.put("nuts-progress", session -> session.isProgress());
        props.put("nuts-terminal-mode", session -> session.boot().getBootOptions().getTerminalMode().orNull());
        props.put("nuts-cached", session -> session.boot().getBootOptions().getCached().orNull());
        props.put("nuts-skip-companions", session -> session.boot().getBootOptions().getSkipCompanions().orNull());
        props.put("nuts-skip-welcome", session -> session.boot().getBootOptions().getSkipWelcome().orNull());
        props.put("nuts-skip-boot", session -> session.boot().getBootOptions().getSkipBoot().orNull());
        props.put("nuts-init-platforms", session -> session.boot().getBootOptions().getInitPlatforms().orNull());
        props.put("nuts-init-java", session -> session.boot().getBootOptions().getInitJava().orNull());
        props.put("nuts-init-launchers", session -> session.boot().getBootOptions().getInitLaunchers().orNull());
        props.put("nuts-init-scripts", session -> session.boot().getBootOptions().getInitScripts().orNull());
        props.put("nuts-desktop-launcher", session -> session.boot().getBootOptions().getDesktopLauncher().orNull());
        props.put("nuts-menu-launcher", session -> session.boot().getBootOptions().getMenuLauncher().orNull());
        props.put("nuts-user-launcher", session -> session.boot().getBootOptions().getUserLauncher().orNull());
        props.put("nuts-locale", session -> session.boot().getBootOptions().getLocale().orNull());
        props.put("nuts-theme", session -> session.boot().getBootOptions().getTheme().orNull());
        props.put("nuts-username", session -> session.boot().getBootOptions().getUserName().orNull());
        props.put("nuts-solver",
                session -> {
                    String ds = NDependencySolverUtils.resolveSolverName(session.boot().getBootOptions().getDependencySolver().orNull());
                    List<String> allDs = NDependencySolver.getSolverNames(session);
                    return NTexts.of(session).ofStyled(
                            ds,
                            allDs.stream().map(NDependencySolverUtils::resolveSolverName)
                                    .anyMatch(x -> x.equals(ds))
                                    ? NTextStyle.keyword() : NTextStyle.error());
                }
        );
        props.put("nuts-solver-list",
                session -> {
                    String ds = NDependencySolverUtils.resolveSolverName(session.boot().getBootOptions().getDependencySolver().orNull());
                    List<String> allDs = NDependencySolver.getSolverNames(session);
                    NTexts txt = NTexts.of(session);
                    return txt.ofBuilder().appendJoined(";",
                            allDs.stream()
                                    .map(x -> txt.ofStyled(x, NTextStyle.keyword()))
                                    .collect(Collectors.toList())
                    );
                }
        );
        props.put("sys-terminal-flags", session -> session.boot().getBootTerminal().getFlags());
        props.put("sys-terminal-mode", session -> session.boot().getBootOptions().getTerminalMode().orElse(NTerminalMode.DEFAULT));
        props.put("java-version", session -> NVersion.of(System.getProperty("java.version")).get(session));
        props.put("platform", session -> session.env().getPlatform());
        props.put("java-home", session -> NPath.of(System.getProperty("java.home"), session));
        props.put("java-executable", session -> NPath.of(NJavaSdkUtils.of(session).resolveJavaCommandByHome(null, getSession()), session));
        props.put("java-classpath",
                session -> NTexts.of(session).ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x, session))
                                .collect(Collectors.toList())
                )
        );
        props.put("java-library-path",
                session -> NTexts.of(session).ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x, session))
                                .collect(Collectors.toList())
                )
        );
        props.put("os-name", session -> session.env().getOs());
        props.put("os-family", session -> session.env().getOsFamily());
        props.put("os-dist", session -> session.env().getOsDist());
        props.put("os-arch", session -> session.env().getArch());
        props.put("os-arch-family", session -> session.env().getArchFamily());
        props.put("os-desktop", session -> session.env().getDesktopEnvironment());
        props.put("os-desktops", session -> session.env().getDesktopEnvironments());
        props.put("os-desktop-family", session -> session.env().getDesktopEnvironmentFamily());
        props.put("os-desktop-families", session -> session.env().getDesktopEnvironmentFamilies());
        props.put("os-desktop-path", session -> session.env().getDesktopPath());
        props.put("os-desktop-launcher", session -> session.env().getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP));
        props.put("os-menu-launcher", session -> session.env().getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU));
        props.put("os-user-launcher", session -> session.env().getDesktopIntegrationSupport(NDesktopIntegrationItem.USER));
        props.put("os-shell", session -> session.env().getShellFamily());
        props.put("os-shells", session -> session.env().getShellFamilies());
        props.put("os-username", session -> stringValue(System.getProperty("user.name")));
        props.put("user-home", NPath::ofUserHome);
        props.put("user-dir", NPath::ofUserDirectory);
        props.put("command-line-long",
                session -> session.boot().getBootOptions().toCommandLine(new NWorkspaceOptionsConfig().setCompact(false))
        );
        props.put("command-line-short", session -> session.boot().getBootOptions().toCommandLine(new NWorkspaceOptionsConfig().setCompact(true)));
        props.put("inherited", session -> session.boot().getBootOptions().getInherited().orElse(false));
        // nuts-boot-args must always be parsed in bash format
        props.put("inherited-nuts-boot-args", session -> NCommandLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH, session).format(session));
        props.put("inherited-nuts-args", session -> NCommandLine.of(System.getProperty("nuts.args"), NShellFamily.SH, session)
                .format(session)
        );
        props.put("creation-started", session -> session.boot().getCreationStartTime());
        props.put("creation-finished", session -> session.boot().getCreationFinishTime());
        props.put("creation-within", session -> CoreTimeUtils.formatPeriodMilli(session.boot().getCreationDuration()).trim());
        props.put("repositories-count", session -> (session.repos().setSession(getSession()).getRepositories().size()));
        return props;
    }

    private boolean inflate(MapAndSession m, Function<MapAndSession, Map<String, Object>> s) {
        boolean change = false;
        if (s != null) {
            Map<String, Object> i = s.apply(m);
            if (i != null) {
                for (Map.Entry<String, Object> e : i.entrySet()) {
                    String key = e.getKey();
                    Object value = e.getValue();
                    if (!change) {
                        Object old = m.map.get(key);
                        if (!Objects.equals(old, value)) {
                            change = true;
                        }
                    }
                    m.map.put(key, value);
                }
            }
        }
        return change;
    }

    static class StringAndSession {
        String string;
        NSession session;

        public StringAndSession(String string, NSession session) {
            this.string = string;
            this.session = session;
        }
    }

    static class MapAndSession {
        Map<String, Object> map;
        NSession session;

        public MapAndSession(Map<String, Object> map, NSession session) {
            this.map = map;
            this.session = session;
        }
    }

    private Map<String, Object> inflate(Map<String, Object> m, NSession session) {
        MapAndSession mm = new MapAndSession(m, session);
        inflate(mm, repoSupplier);
        inflate(mm, extraPropertiesSupplier);
        return mm.map;
    }

    private Map<String, Object> buildWorkspaceMap(boolean deep) {
        String prefix = null;
        FilteredMap props = new FilteredMap(filter);
        NSession session = getSession();
        NWorkspaceConfigManager rt = session.config();
        NWorkspaceOptions options = session.boot().getBootOptions();
        Set<String> extraKeys = new TreeSet<>(extraProperties.keySet());

        props.put("name", stringValue(session.getWorkspace().getName()));
        props.put("nuts-api-version", session.getWorkspace().getApiVersion());
//        NutsIdFormat idFormat = ws.id().formatter();
        props.put("nuts-api-id", session.getWorkspace().getApiId());
        props.put("nuts-runtime-id", session.getWorkspace().getRuntimeId());
        props.put("nuts-app-id", session.getAppId());
        List<URL> cl = session.boot().getBootClassWorldURLs();
        List<NPath> runtimeClassPath = new ArrayList<>();
        if (cl != null) {
            for (URL url : cl) {
                if (url != null) {
                    String s = url.toString();
                    try {
                        s = Paths.get(url.toURI()).toFile().getPath();
                    } catch (URISyntaxException ex) {
                        s = s.replace(":", "\\:");
                    }
                    runtimeClassPath.add(NPath.of(s, session));
                }
            }
        }

        NTexts txt = NTexts.of(session);
        props.put("nuts-runtime-classpath",
                txt.ofBuilder().appendJoined(";", runtimeClassPath)
        );
        props.put("nuts-workspace-id", txt.ofStyled(stringValue(session.getWorkspace().getUuid()), NTextStyle.path()));
        props.put("nuts-store-layout", session.locations().getStoreLocationLayout());
        props.put("nuts-store-strategy", session.locations().getStoreLocationStrategy());
        props.put("nuts-repo-store-strategy", session.locations().getRepositoryStoreLocationStrategy());
        props.put("nuts-global", options.getGlobal().orNull());
        props.put("nuts-workspace", session.locations().getWorkspaceLocation());
        for (NStoreLocation folderType : NStoreLocation.values()) {
            props.put("nuts-workspace-" + folderType.id(), session.locations().getStoreLocation(folderType));
        }
        props.put("nuts-open-mode", options.getOpenMode().orNull());
        props.put("nuts-isolation-level", options.getIsolationLevel().orNull());
        props.put("nuts-secure", (session.security().isSecure()));
        props.put("nuts-gui", options.getGui().orNull());
        props.put("nuts-inherited", options.getInherited().orNull());
        props.put("nuts-recover", options.getRecover().orNull());
        props.put("nuts-reset", options.getReset().orNull());
        props.put("nuts-read-only", (options.getReadOnly().orNull()));
        props.put("nuts-debug", NDebugString.of(options.getDebug().orNull(), getSession()));
        props.put("nuts-bot", options.getBot().orNull());
        props.put("nuts-trace", options.getTrace().orNull());
        props.put("nuts-indexed", options.getIndexed().orNull());
        props.put("nuts-transitive", options.getTransitive().orNull());
        props.put("nuts-fetch-strategy", options.getFetchStrategy().orNull());
        props.put("nuts-execution-type", options.getExecutionType().orNull());
        props.put("nuts-dry", options.getDry().orNull());
        props.put("nuts-output-format", options.getOutputFormat().orNull());
        props.put("nuts-confirm", options.getConfirm().orNull());
        props.put("nuts-dependency-solver", options.getDependencySolver().orNull());
        props.put("nuts-progress-options", options.getProgressOptions().orNull());
        props.put("nuts-progress", session.isProgress());
        props.put("nuts-terminal-mode", options.getTerminalMode().orNull());
        props.put("nuts-cached", options.getCached().orNull());
        props.put("nuts-skip-companions", options.getSkipCompanions().orNull());
        props.put("nuts-skip-welcome", options.getSkipWelcome().orNull());
        props.put("nuts-skip-boot", options.getSkipBoot().orNull());
        props.put("nuts-init-platforms", options.getInitPlatforms().orNull());
        props.put("nuts-init-java", options.getInitJava().orNull());
        props.put("nuts-init-launchers", options.getInitLaunchers().orNull());
        props.put("nuts-init-scripts", options.getInitScripts().orNull());
        props.put("nuts-desktop-launcher", options.getDesktopLauncher().orNull());
        props.put("nuts-menu-launcher", options.getMenuLauncher().orNull());
        props.put("nuts-user-launcher", options.getUserLauncher().orNull());
        props.put("nuts-locale", options.getLocale().orNull());
        props.put("nuts-theme", options.getTheme().orNull());
        props.put("nuts-username", options.getUserName().orNull());
        String ds = NDependencySolverUtils.resolveSolverName(options.getDependencySolver().orNull());
        List<String> allDs = NDependencySolver.getSolverNames(session);
        props.put("nuts-solver",
                txt.ofStyled(
                        ds,
                        allDs.stream().map(NDependencySolverUtils::resolveSolverName)
                                .anyMatch(x -> x.equals(ds))
                                ? NTextStyle.keyword() : NTextStyle.error())
        );
        props.put("nuts-solver-list",
                txt.ofBuilder().appendJoined(";",
                        allDs.stream()
                                .map(x -> txt.ofStyled(x, NTextStyle.keyword()))
                                .collect(Collectors.toList())
                )

        );
        NWorkspaceTerminalOptions b = session.boot().getBootTerminal();
        props.put("sys-terminal-flags", b.getFlags());
        NTerminalMode terminalMode = session.boot().getBootOptions().getTerminalMode().orElse(NTerminalMode.DEFAULT);
        props.put("sys-terminal-mode", terminalMode);
        props.put("java-version", NVersion.of(System.getProperty("java.version")).get(session));
        props.put("platform", session.env().getPlatform());
        props.put("java-home", NPath.of(System.getProperty("java.home"), session));
        props.put("java-executable", NPath.of(NJavaSdkUtils.of(session).resolveJavaCommandByHome(null, getSession()), session));
        props.put("java-classpath",
                txt.ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x, session))
                                .collect(Collectors.toList())
                )
        );
        props.put("java-library-path",
                txt.ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x, session))
                                .collect(Collectors.toList())
                )
        );
        props.put("os-name", session.env().getOs());
        props.put("os-family", (session.env().getOsFamily()));
        if (session.env().getOsDist() != null) {
            props.put("os-dist", (session.env().getOsDist()));
        }
        props.put("os-arch", session.env().getArch());
        props.put("os-arch-family", session.env().getArchFamily());
        props.put("os-desktop", session.env().getDesktopEnvironment());
        props.put("os-desktops", session.env().getDesktopEnvironments());
        props.put("os-desktop-family", session.env().getDesktopEnvironmentFamily());
        props.put("os-desktop-families", session.env().getDesktopEnvironmentFamilies());
        props.put("os-desktop-path", session.env().getDesktopPath());
        props.put("os-desktop-launcher", session.env().getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP));
        props.put("os-menu-launcher", session.env().getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU));
        props.put("os-user-launcher", session.env().getDesktopIntegrationSupport(NDesktopIntegrationItem.USER));
        props.put("os-shell", session.env().getShellFamily());
        props.put("os-shells", session.env().getShellFamilies());
        props.put("os-username", stringValue(System.getProperty("user.name")));
        props.put("user-home", NPath.ofUserHome(session));
        props.put("user-dir", NPath.ofUserDirectory(session));
        props.put("command-line-long",
                session.boot().getBootOptions().toCommandLine(new NWorkspaceOptionsConfig().setCompact(false))
        );
        props.put("command-line-short", session.boot().getBootOptions().toCommandLine(new NWorkspaceOptionsConfig().setCompact(true)));
        props.put("inherited", session.boot().getBootOptions().getInherited().orElse(false));
        // nuts-boot-args must always be parsed in bash format
        props.put("inherited-nuts-boot-args", NCommandLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH, session).format(session));
        props.put("inherited-nuts-args", NCommandLine.of(System.getProperty("nuts.args"), NShellFamily.SH, session)
                .format(session)
        );
        props.put("creation-started", session.boot().getCreationStartTime());
        props.put("creation-finished", session.boot().getCreationFinishTime());
        props.put("creation-within", CoreTimeUtils.formatPeriodMilli(session.boot().getCreationDuration()).trim());
        props.put("repositories-count", (session.repos().setSession(getSession()).getRepositories().size()));
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        if (deep) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            props.put("repos", repositories);
            for (NRepository repository : session.repos().setSession(getSession()).getRepositories()) {
                repositories.put(repository.getName(), buildRepoRepoMap(repository, deep, prefix));
            }
        }

        return props.build();
    }

    private Map<String, Object> buildRepoRepoMap(NRepository repo, boolean deep, String prefix) {
        NSession ws = getSession();
        FilteredMap props = new FilteredMap(filter);
        props.put(key(prefix, "name"), stringValue(repo.getName()));
        props.put(key(prefix, "global-name"), repo.config().getGlobalName());
        props.put(key(prefix, "uuid"), stringValue(repo.getUuid()));
        props.put(key(prefix, "type"),
                //display as enum
                NTexts.of(ws).ofStyled(repo.config().getType(), NTextStyle.option())
        );
        props.put(key(prefix, "speed"), (repo.config().getSpeed()));
        props.put(key(prefix, "enabled"), (repo.config().isEnabled()));
        props.put(key(prefix, "index-enabled"), (repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), (repo.config().setSession(getSession()).isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().getLocation());
        props.put(key(prefix, "deploy-order"), (repo.config().getDeployWeight()));
        props.put(key(prefix, "store-location-strategy"), (repo.config().getStoreLocationStrategy()));
        props.put(key(prefix, "store-location"), repo.config().getStoreLocation());
        for (NStoreLocation value : NStoreLocation.values()) {
            props.put(key(prefix, "store-location-" + value.id()), repo.config().getStoreLocation(value));
        }
        props.put(key(prefix, "supported-mirroring"), (repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), ((!repo.config()
                    .setSession(getSession())
                    .isSupportedMirroring()) ? 0 : repo.config()
                    .setSession(getSession())
                    .getMirrors().size()));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                props.put("mirrors", mirrors);
                for (NRepository mirror : repo.config()
                        .setSession(getSession())
                        .getMirrors()) {
                    mirrors.put(mirror.getName(), buildRepoRepoMap(mirror, deep, null));
                }
            }
        }
        return props.build();
    }

    private String stringValue(Object s) {
        return NTexts.of(getSession()).ofBuilder().append(CoreStringUtils.stringValue(s)).toString();
    }

    public boolean isLenient() {
        return lenient;
    }

    public NInfoCommand setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NInfoCommand setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NWorkspaceCommand run() {
        println();
        return this;
    }

    private static class FilteredMap {

        private Predicate<String> filter;
        private LinkedHashMap<String, Object> data = new LinkedHashMap<>();

        public FilteredMap(Predicate<String> filter) {
            this.filter = filter;
        }

        public boolean accept(String s) {
            return filter.test(s);
        }

        public void put(String s, Supplier<Object> v) {
            if (filter.test(s)) {
                data.put(s, v.get());
            }
        }

        public void putAnyway(String s, Object v) {
            data.put(s, v);
        }

        public void put(String s, Object v) {
            if (filter.test(s)) {
                data.put(s, v);
            }
        }

        public Map<String, Object> build() {
            return data;
        }
    }
}
