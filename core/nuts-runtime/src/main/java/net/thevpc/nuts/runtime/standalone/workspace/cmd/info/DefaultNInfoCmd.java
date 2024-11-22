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

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NDesktopIntegrationItem;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.dependency.solver.NDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NPredicates;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNInfoCmd extends DefaultFormatBase<NInfoCmd> implements NInfoCmd {

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
            for (NRepository repository : NRepositories.of().getRepositories()) {
                repositories.put(repository.getName(), buildRepoRepoMap(repository, true, null));
            }
            return repositories;
        }
        return null;
    };
    private Map<String, Supplier<Object>> mapSupplier;
    private Function<String, Object> fct = new Function<String, Object>() {
        @Override
        public Object apply(String s) {
            Supplier<Object> r = mapSupplier.get(s);
            if(r!=null){
                return r.get();
            }
            String v = extraProperties.get(s);
            if(v!=null){
                return v;
            }
            v = System.getProperty(s);
            if(v!=null){
                return v;
            }
            NRepository repo = NRepositories.of().findRepository(s).orNull();
            if (repo != null) {
                return buildRepoRepoMap(repo, true, null);
            }
            return null;
        }
    };

    public DefaultNInfoCmd(NWorkspace workspace) {
        super(workspace, "info");
        mapSupplier = buildMapSupplier();
    }

    private static String key(String prefix, String key) {
        if (NBlankable.isBlank(prefix)) {
            return key;
        }
        return prefix + "." + key;
    }

    @Override
    public NInfoCmd addProperty(String key, String value) {
        if (value == null) {
            extraProperties.remove(key);
        } else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NInfoCmd addProperties(Map<String, String> customProperties) {
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
    public NInfoCmd setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NInfoCmd setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public void print(NPrintStream w) {
        NSession session=workspace.currentSession();
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
                    throw new NIllegalArgumentException(NMsg.ofC("property not found : %s", key));
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
                        throw new NIllegalArgumentException(NMsg.ofC("property not found : %s", request));
                    }
                }
            }
        }
        session = session.copy();
        if (session.getOutputFormat().orDefault() == NContentType.PLAIN) {
            session.setOutputFormat(NContentType.PROPS);
        }
        Object fresult = result;
        session.runWith(()->{
            NObjectFormat.of().setValue(fresult).configure(true, args.toArray(new String[0])).print(w);
        });
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session = workspace.currentSession();
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "-r":
            case "--repos": {
                boolean val = cmdLine.nextFlag().get().getBooleanValue().get();
                if (enabled) {
                    this.setShowRepositories(val);
                }
                return true;
            }
            case "--fancy": {
                boolean val = cmdLine.nextFlag().get().getBooleanValue().get();
                if (enabled) {
                    this.setFancy(val);
                }
                return true;
            }
            case "-l":
            case "--lenient": {
                boolean val = cmdLine.nextFlag().get().getBooleanValue().get();
                if (enabled) {
                    this.setLenient(val);
                }
                return true;
            }
            case "--add": {
                String aa = cmdLine.nextEntry().get().getStringValue().get();
                NArg val = NArg.of(aa);
                if (enabled) {
                    extraProperties.put(val.key(), val.getStringValue().get());
                }
                return true;
            }
            case "-p":
            case "--path": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("nuts-workspace");
                    for (NStoreType folderType : NStoreType.values()) {
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
                String r = cmdLine.nextEntry().get().getStringValue().get();
                if (enabled) {
                    requests.add(r);
                }
                while (true) {
                    NArg p = cmdLine.peek().orNull();
                    if (p != null && !p.isOption()) {
                        cmdLine.skip();
                        if (enabled) {
                            requests.add(p.asString().get());
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
        return NOptional.ofNamed(fct.apply(propertyName), "property " + propertyName);
    }

    private Map<String, Supplier<Object>> buildMapSupplier() {
        Map<String, Supplier<Object>> props = new HashMap<>();
        props.put("name", () ->  stringValue(NWorkspace.of().get().getName()));
        props.put("nuts-api-version", () ->  NWorkspace.of().get().getApiVersion());
        props.put("nuts-api-id", () ->  NWorkspace.of().get().getApiId());
        props.put("nuts-runtime-id", () ->  NWorkspace.of().get().getRuntimeId());
        props.put("nuts-app-id", () ->  NApp.of().getId().orNull());

        props.put("nuts-runtime-classpath",
                () ->  {
                    NTexts txt = NTexts.of();
                    List<URL> cl = NBootManager.of().getBootClassWorldURLs();
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
                                runtimeClassPath.add(NPath.of(s));
                            }
                        }
                    }
                    return txt.ofBuilder().appendJoined(";", runtimeClassPath);
                }
        );
        props.put("nuts-workspace-id", () ->  NTexts.of().ofStyled(stringValue(NWorkspace.of().get().getUuid()), NTextStyle.path()));
        props.put("nuts-store-layout", () ->  NLocations.of().getStoreLayout());
        props.put("nuts-store-strategy", () ->  NLocations.of().getStoreStrategy());
        props.put("nuts-repo-store-strategy", () ->  NLocations.of().getRepositoryStoreStrategy());
        props.put("nuts-global", () ->  NBootManager.of().getBootOptions().getSystem().orNull());
        props.put("nuts-workspace", () ->  NLocations.of().getWorkspaceLocation());
        for (NStoreType folderType : NStoreType.values()) {
            props.put("nuts-workspace-" + folderType.id(), () ->  NLocations.of().getStoreLocation(folderType));
        }
        props.put("nuts-open-mode", () ->  NBootManager.of().getBootOptions().getOpenMode().orNull());
        props.put("nuts-isolation-level", () ->  NBootManager.of().getBootOptions().getIsolationLevel().orNull());
        props.put("nuts-secure", () ->  (NWorkspaceSecurityManager.of().isSecure()));
        props.put("nuts-gui", () ->  NBootManager.of().getBootOptions().getGui().orNull());
        props.put("nuts-inherited", () ->  NBootManager.of().getBootOptions().getInherited().orNull());
        props.put("nuts-recover", () ->  NBootManager.of().getBootOptions().getRecover().orNull());
        props.put("nuts-reset", () ->  NBootManager.of().getBootOptions().getReset().orNull());
        props.put("nuts-read-only", () ->  NBootManager.of().getBootOptions().getReadOnly().orNull());
        props.put("nuts-debug", () ->  NDebugString.of(NBootManager.of().getBootOptions().getDebug().orNull()));
        props.put("nuts-bot", () ->  NBootManager.of().getBootOptions().getBot().orNull());
        props.put("nuts-trace", () ->  NBootManager.of().getBootOptions().getTrace().orNull());
        props.put("nuts-indexed", () ->  NBootManager.of().getBootOptions().getIndexed().orNull());
        props.put("nuts-transitive", () ->  NBootManager.of().getBootOptions().getTransitive().orNull());
        props.put("nuts-fetch-strategy", () ->  NBootManager.of().getBootOptions().getFetchStrategy().orNull());
        props.put("nuts-execution-type", () ->  NBootManager.of().getBootOptions().getExecutionType().orNull());
        props.put("nuts-dry", () ->  NBootManager.of().getBootOptions().getDry().orNull());
        props.put("nuts-output-format", () ->  NBootManager.of().getBootOptions().getOutputFormat().orNull());
        props.put("nuts-confirm", () ->  NBootManager.of().getBootOptions().getConfirm().orNull());
        props.put("nuts-dependency-solver", () ->  NBootManager.of().getBootOptions().getDependencySolver().orNull());
        props.put("nuts-progress-options", () ->  NBootManager.of().getBootOptions().getProgressOptions().orNull());
        props.put("nuts-progress", () ->  NSession.of().get().isProgress());
        props.put("nuts-terminal-mode", () ->  NBootManager.of().getBootOptions().getTerminalMode().orNull());
        props.put("nuts-cached", () ->  NBootManager.of().getBootOptions().getCached().orNull());
        props.put("nuts-install-companions", () ->  NBootManager.of().getBootOptions().getInstallCompanions().orNull());
        props.put("nuts-skip-welcome", () ->  NBootManager.of().getBootOptions().getSkipWelcome().orNull());
        props.put("nuts-skip-boot", () ->  NBootManager.of().getBootOptions().getSkipBoot().orNull());
        props.put("nuts-init-platforms", () ->  NBootManager.of().getBootOptions().getInitPlatforms().orNull());
        props.put("nuts-init-java", () ->  NBootManager.of().getBootOptions().getInitJava().orNull());
        props.put("nuts-init-launchers", () ->  NBootManager.of().getBootOptions().getInitLaunchers().orNull());
        props.put("nuts-init-scripts", () ->  NBootManager.of().getBootOptions().getInitScripts().orNull());
        props.put("nuts-desktop-launcher", () ->  NBootManager.of().getBootOptions().getDesktopLauncher().orNull());
        props.put("nuts-menu-launcher", () ->  NBootManager.of().getBootOptions().getMenuLauncher().orNull());
        props.put("nuts-user-launcher", () ->  NBootManager.of().getBootOptions().getUserLauncher().orNull());
        props.put("nuts-locale", () ->  NBootManager.of().getBootOptions().getLocale().orNull());
        props.put("nuts-theme", () ->  NBootManager.of().getBootOptions().getTheme().orNull());
        props.put("nuts-username", () ->  NBootManager.of().getBootOptions().getUserName().orNull());
        props.put("nuts-solver",
                () ->  {
                    String ds = NDependencySolverUtils.resolveSolverName(NBootManager.of().getBootOptions().getDependencySolver().orNull());
                    List<String> allDs = NDependencySolver.getSolverNames();
                    return NTexts.of().ofStyled(
                            ds,
                            allDs.stream().map(NDependencySolverUtils::resolveSolverName)
                                    .anyMatch(x -> x.equals(ds))
                                    ? NTextStyle.keyword() : NTextStyle.error());
                }
        );
        props.put("nuts-solver-list",
                () ->  {
                    String ds = NDependencySolverUtils.resolveSolverName(NBootManager.of().getBootOptions().getDependencySolver().orNull());
                    List<String> allDs = NDependencySolver.getSolverNames();
                    NTexts txt = NTexts.of();
                    return txt.ofBuilder().appendJoined(";",
                            allDs.stream()
                                    .map(x -> txt.ofStyled(x, NTextStyle.keyword()))
                                    .collect(Collectors.toList())
                    );
                }
        );
        props.put("sys-terminal-flags", () ->  NBootManager.of().getBootTerminal().getFlags());
        props.put("sys-terminal-mode", () ->  NBootManager.of().getBootOptions().getTerminalMode().orElse(NTerminalMode.DEFAULT));
        props.put("java-version", () ->  NVersion.of(System.getProperty("java.version")).get());
        props.put("platform", () ->  NEnvs.of().getPlatform());
        props.put("java-home", () ->  NPath.of(System.getProperty("java.home")));
        props.put("java-executable", () ->  NPath.of(NJavaSdkUtils.of(NWorkspace.of().get()).resolveJavaCommandByHome(null)));
        props.put("java-classpath",
                () ->  NTexts.of().ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x))
                                .collect(Collectors.toList())
                )
        );
        props.put("java-library-path",
                () ->  NTexts.of().ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x))
                                .collect(Collectors.toList())
                )
        );
        props.put("os-name", () ->  NEnvs.of().getOs());
        props.put("os-family", () ->  NEnvs.of().getOsFamily());
        props.put("os-dist", () ->  NEnvs.of().getOsDist());
        props.put("os-arch", () ->  NEnvs.of().getArch());
        props.put("os-arch-family", () ->  NEnvs.of().getArchFamily());
        props.put("os-desktop", () ->  NEnvs.of().getDesktopEnvironment());
        props.put("os-desktops", () ->  NEnvs.of().getDesktopEnvironments());
        props.put("os-desktop-family", () ->  NEnvs.of().getDesktopEnvironmentFamily());
        props.put("os-desktop-families", () ->  NEnvs.of().getDesktopEnvironmentFamilies());
        props.put("os-desktop-path", () ->  NEnvs.of().getDesktopPath());
        props.put("os-desktop-launcher", () ->  NEnvs.of().getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP));
        props.put("os-menu-launcher", () ->  NEnvs.of().getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU));
        props.put("os-user-launcher", () ->  NEnvs.of().getDesktopIntegrationSupport(NDesktopIntegrationItem.USER));
        props.put("os-shell", () ->  NEnvs.of().getShellFamily());
        props.put("os-shells", () ->  NEnvs.of().getShellFamilies());
        props.put("os-username", () ->  stringValue(System.getProperty("user.name")));
        props.put("user-home", () -> NPath.ofUserHome());
        props.put("user-dir", () -> NPath.ofUserDirectory());
        props.put("command-line-long",
                () ->  NBootManager.of().getBootOptions().toCmdLine(new NWorkspaceOptionsConfig().setCompact(false))
        );
        props.put("command-line-short", () ->  NBootManager.of().getBootOptions().toCmdLine(new NWorkspaceOptionsConfig().setCompact(true)));
        props.put("inherited", () ->  NBootManager.of().getBootOptions().getInherited().orElse(false));
        // nuts-boot-args must always be parsed in bash format
        props.put("inherited-nuts-boot-args", () ->  NCmdLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH).format());
        props.put("inherited-nuts-args", () ->  NCmdLine.of(System.getProperty("nuts.args"), NShellFamily.SH)
                .format()
        );
        props.put("creation-started", () ->  NBootManager.of().getCreationStartTime());
        props.put("creation-finished", () ->  NBootManager.of().getCreationFinishTime());
        props.put("creation-within", () ->  CoreTimeUtils.formatPeriodMilli(NBootManager.of().getCreationDuration()).trim());
        props.put("repositories-count", () ->  (NRepositories.of().getRepositories().size()));
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
        NSession session = workspace.currentSession();
        NConfigs rt = NConfigs.of();
        NWorkspaceOptions options = NBootManager.of().getBootOptions();
        Set<String> extraKeys = new TreeSet<>(extraProperties.keySet());

        props.put("name", stringValue(session.getWorkspace().getName()));
        props.put("nuts-api-version", session.getWorkspace().getApiVersion());
//        NutsIdFormat idFormat = ws.id().formatter();
        props.put("nuts-api-id", session.getWorkspace().getApiId());
        props.put("nuts-runtime-id", session.getWorkspace().getRuntimeId());
        props.put("nuts-app-id", NApp.of().getId().orNull());
        List<URL> cl = NBootManager.of().getBootClassWorldURLs();
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
                    runtimeClassPath.add(NPath.of(s));
                }
            }
        }

        NTexts txt = NTexts.of();
        props.put("nuts-runtime-classpath",
                txt.ofBuilder().appendJoined(";", runtimeClassPath)
        );
        props.put("nuts-workspace-id", txt.ofStyled(stringValue(session.getWorkspace().getUuid()), NTextStyle.path()));
        props.put("nuts-store-layout", NLocations.of().getStoreLayout());
        props.put("nuts-store-strategy", NLocations.of().getStoreStrategy());
        props.put("nuts-repo-store-strategy", NLocations.of().getRepositoryStoreStrategy());
        props.put("nuts-global", options.getSystem().orNull());
        props.put("nuts-workspace", NLocations.of().getWorkspaceLocation());
        for (NStoreType folderType : NStoreType.values()) {
            props.put("nuts-workspace-" + folderType.id(), NLocations.of().getStoreLocation(folderType));
        }
        props.put("nuts-open-mode", options.getOpenMode().orNull());
        props.put("nuts-isolation-level", options.getIsolationLevel().orNull());
        props.put("nuts-secure", (NWorkspaceSecurityManager.of().isSecure()));
        props.put("nuts-gui", options.getGui().orNull());
        props.put("nuts-inherited", options.getInherited().orNull());
        props.put("nuts-recover", options.getRecover().orNull());
        props.put("nuts-reset", options.getReset().orNull());
        props.put("nuts-read-only", (options.getReadOnly().orNull()));
        props.put("nuts-debug", NDebugString.of(options.getDebug().orNull()));
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
        props.put("nuts-install-companions", options.getInstallCompanions().orNull());
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
        List<String> allDs = NDependencySolver.getSolverNames();
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
        NWorkspaceTerminalOptions b = NBootManager.of().getBootTerminal();
        props.put("sys-terminal-flags", b.getFlags());
        NTerminalMode terminalMode = NBootManager.of().getBootOptions().getTerminalMode().orElse(NTerminalMode.DEFAULT);
        props.put("sys-terminal-mode", terminalMode);
        props.put("java-version", NVersion.of(System.getProperty("java.version")).get());
        props.put("platform", NEnvs.of().getPlatform());
        props.put("java-home", NPath.of(System.getProperty("java.home")));
        props.put("java-executable", NPath.of(NJavaSdkUtils.of(NWorkspace.of().get()).resolveJavaCommandByHome(null)));
        props.put("java-classpath",
                txt.ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x))
                                .collect(Collectors.toList())
                )
        );
        props.put("java-library-path",
                txt.ofBuilder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x))
                                .collect(Collectors.toList())
                )
        );
        props.put("os-name", NEnvs.of().getOs());
        props.put("os-family", (NEnvs.of().getOsFamily()));
        if (NEnvs.of().getOsDist() != null) {
            props.put("os-dist", (NEnvs.of().getOsDist()));
        }
        NEnvs envs = NEnvs.of();
        props.put("os-arch", envs.getArch());
        props.put("os-arch-family", envs.getArchFamily());
        props.put("os-desktop", envs.getDesktopEnvironment());
        props.put("os-desktops", envs.getDesktopEnvironments());
        props.put("os-desktop-family", envs.getDesktopEnvironmentFamily());
        props.put("os-desktop-families", envs.getDesktopEnvironmentFamilies());
        props.put("os-desktop-path", envs.getDesktopPath());
        props.put("os-desktop-launcher", envs.getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP));
        props.put("os-menu-launcher", envs.getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU));
        props.put("os-user-launcher", envs.getDesktopIntegrationSupport(NDesktopIntegrationItem.USER));
        props.put("os-shell", envs.getShellFamily());
        props.put("os-shells", envs.getShellFamilies());
        props.put("os-username", stringValue(System.getProperty("user.name")));
        props.put("user-home", NPath.ofUserHome());
        props.put("user-dir", NPath.ofUserDirectory());
        props.put("command-line-long",
                NBootManager.of().getBootOptions().toCmdLine(new NWorkspaceOptionsConfig().setCompact(false))
        );
        props.put("command-line-short", NBootManager.of().getBootOptions().toCmdLine(new NWorkspaceOptionsConfig().setCompact(true)));
        props.put("inherited", NBootManager.of().getBootOptions().getInherited().orElse(false));
        // nuts-boot-args must always be parsed in bash format
        props.put("inherited-nuts-boot-args", NCmdLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH).format());
        props.put("inherited-nuts-args", NCmdLine.of(System.getProperty("nuts.args"), NShellFamily.SH)
                .format()
        );
        props.put("creation-started", NBootManager.of().getCreationStartTime());
        props.put("creation-finished", NBootManager.of().getCreationFinishTime());
        props.put("creation-within", CoreTimeUtils.formatPeriodMilli(NBootManager.of().getCreationDuration()).trim());
        props.put("repositories-count", (NRepositories.of().getRepositories().size()));
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        if (deep) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            props.put("repos", repositories);
            for (NRepository repository : NRepositories.of().getRepositories()) {
                repositories.put(repository.getName(), buildRepoRepoMap(repository, deep, prefix));
            }
        }

        return props.build();
    }

    private Map<String, Object> buildRepoRepoMap(NRepository repo, boolean deep, String prefix) {
        NSession session = workspace.currentSession();
        FilteredMap props = new FilteredMap(filter);
        props.put(key(prefix, "name"), stringValue(repo.getName()));
        props.put(key(prefix, "global-name"), repo.config().getGlobalName());
        props.put(key(prefix, "uuid"), stringValue(repo.getUuid()));
        props.put(key(prefix, "type"),
                //display as enum
                NTexts.of().ofStyled(repo.config().getType(), NTextStyle.option())
        );
        props.put(key(prefix, "speed"), (repo.config().getSpeed()));
        props.put(key(prefix, "enabled"), (repo.config().isEnabled()));
        props.put(key(prefix, "active"), (repo.isEnabled()));
        props.put(key(prefix, "index-enabled"), (repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), (repo.config().isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().getLocation());
        props.put(key(prefix, "deploy-order"), (repo.config().getDeployWeight()));
        props.put(key(prefix, "store-strategy"), (repo.config().getStoreStrategy()));
        props.put(key(prefix, "store-location"), repo.config().getStoreLocation());
        for (NStoreType value : NStoreType.values()) {
            props.put(key(prefix, "store-location-" + value.id()), repo.config().getStoreLocation(value));
        }
        props.put(key(prefix, "supported-mirroring"), (repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), ((!repo.config()
                    .isSupportedMirroring()) ? 0 : repo.config()
                    .getMirrors().size()));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                props.put("mirrors", mirrors);
                for (NRepository mirror : repo.config()
                        .getMirrors()) {
                    mirrors.put(mirror.getName(), buildRepoRepoMap(mirror, deep, null));
                }
            }
        }
        return props.build();
    }

    private String stringValue(Object s) {
        NSession session = workspace.currentSession();
        return NTexts.of().ofBuilder().append(CoreStringUtils.stringValue(s)).toString();
    }

    public boolean isLenient() {
        return lenient;
    }

    public NInfoCmd setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NInfoCmd setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NWorkspaceCmd run() {
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
