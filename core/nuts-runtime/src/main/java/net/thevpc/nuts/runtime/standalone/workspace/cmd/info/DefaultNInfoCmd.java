package net.thevpc.nuts.runtime.standalone.workspace.cmd.info;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.boot.NWorkspaceTerminalOptions;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NInfoCmd;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.dependency.solver.NDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.util.*;

/**
 * type: Command Class
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNInfoCmd implements NInfoCmd {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean showRepositories = false;
    private boolean fancy = false;
    private boolean includeSysEnv = false;
    private boolean includeSysProps = false;
    private List<String> requests = new ArrayList<>();
    private List<Supplier<Map<String, Object>>> extraSuppliers = new ArrayList<>();
    private Predicate<String> filter = NPredicates.always();
    private boolean lenient = false;
    private boolean ntf = false;
    private Function<MapAndSession, Map<String, Object>> extraPropertiesSupplier = a -> (Map) extraProperties;

    Function<MapAndSession, Map<String, Object>> repoSupplier = s -> {
        Object oldRepos = s.map.get("repos");
        if (oldRepos instanceof String) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            for (NRepository repository : NWorkspace.of().repositories()) {
                repositories.put(repository.name(), buildRepoRepoMap(repository, true, null));
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
            if (r != null) {
                return r.get();
            }
            String v = extraProperties.get(s);
            if (v != null) {
                return v;
            }
            v = System.getProperty(s);
            if (v != null) {
                return v;
            }
            NRepository repo = NWorkspace.of().getRepository(s).orNull();
            if (repo != null) {
                return buildRepoRepoMap(repo, true, null);
            }
            return null;
        }
    };

    public DefaultNInfoCmd() {
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
    public NInfoCmd showRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NInfoCmd fancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    public NInfoCmd println(NPrintStream w) {
        print(w);
        w.println();
        return this;
    }

    public NInfoCmd print(NPrintStream w) {
        NSession session = NSession.of();
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
        if (session.outputFormat().orDefault() == NContentType.PLAIN) {
            session.outputFormat(NContentType.PROPS);
        }
        Object fresult = result;
        session.runWith(() -> {
            NObjectObjectWriter.of().configure(true, args.toArray(new String[0])).print(fresult, w);
        });
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session = NSession.of();
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isUncommented();
        switch (a.key()) {
            case "-r":
            case "--repos": {
                boolean val = cmdLine.nextFlag().get().getBooleanValue().get();
                if (enabled) {
                    this.showRepositories(val);
                }
                return true;
            }
            case "--fancy": {
                boolean val = cmdLine.nextFlag().get().getBooleanValue().get();
                if (enabled) {
                    this.fancy(val);
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
            case "--sys-env": {
                cmdLine.skip();
                if (enabled) {
                    this.includeSysEnv=true;
                }
                return true;
            }
            case "--sys-props": {
                cmdLine.skip();
                if (enabled) {
                    this.includeSysProps=true;
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

    public Map<String, Object> propertyValues() {
        return buildWorkspaceMap(true);
    }

    public NOptional<Object> getPropertyValue(String propertyName) {
        return NOptional.ofNamed(fct.apply(propertyName), "property " + propertyName);
    }

    private Map<String, Supplier<Object>> buildMapSupplier() {
        Map<String, Supplier<Object>> props = new LinkedHashMap<>();
        props.put("name", () -> stringValue(NWorkspace.of().name()));
        props.put("nuts-api-version", () -> NWorkspace.of().apiVersion());
        props.put("nuts-api-id", () -> NWorkspace.of().apiId());
        props.put("nuts-runtime-id", () -> NWorkspace.of().runtimeId());
        props.put("nuts-app-id", () -> NApp.of().id().orNull());

        props.put("nuts-runtime-classpath",
                () -> {
                    NTexts txt = NTexts.of();
                    List<URL> cl = NWorkspace.of().bootClassWorldURLs();
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
        props.put("nuts-workspace-id", () -> NText.ofStyledPath(stringValue(NWorkspace.of().uuid())));
        props.put("nuts-store-layout", () -> NWorkspace.of().storeLayout());
        props.put("nuts-store-strategy", () -> NWorkspace.of().storeStrategy());
        props.put("nuts-repo-store-strategy", () -> NWorkspace.of().repositoryStoreStrategy());
        props.put("nuts-global", () -> NWorkspace.of().bootOptions().system().orNull());
        props.put("nuts-workspace", () -> NWorkspace.of().workspaceLocation());
        for (NStoreType storeType : NStoreType.values()) {
            props.put("nuts-workspace-" + storeType.id(), () -> NPath.of(NStoreKey.of(storeType)));
        }
        props.put("nuts-open-mode", () -> NWorkspace.of().bootOptions().openMode().orNull());
        props.put("nuts-isolation-level", () -> NWorkspace.of().bootOptions().isolationLevel().orNull());
        props.put("nuts-secure", () -> (NSecurityManager.of().isSecureMode()));
        props.put("nuts-gui", () -> NWorkspace.of().bootOptions().gui().orNull());
        props.put("nuts-inherited", () -> NWorkspace.of().bootOptions().inherited().orNull());
        props.put("nuts-recover", () -> NWorkspace.of().bootOptions().recover().orNull());
        props.put("nuts-reset", () -> NWorkspace.of().bootOptions().reset().orNull());
        props.put("nuts-read-only", () -> NWorkspace.of().bootOptions().readOnly().orNull());
        props.put("nuts-debug", () -> NDebugString.of(NWorkspace.of().bootOptions().debug().orNull()));
        props.put("nuts-bot", () -> NWorkspace.of().bootOptions().bot().orNull());
        props.put("nuts-trace", () -> NWorkspace.of().bootOptions().trace().orNull());
        props.put("nuts-indexed", () -> NWorkspace.of().bootOptions().indexed().orNull());
        props.put("nuts-transitive", () -> NWorkspace.of().bootOptions().transitive().orNull());
        props.put("nuts-fetch-strategy", () -> NWorkspace.of().bootOptions().fetchStrategy().orNull());
        props.put("nuts-execution-type", () -> NWorkspace.of().bootOptions().executionType().orNull());
        props.put("nuts-dry", () -> NWorkspace.of().bootOptions().dry().orNull());
        props.put("nuts-output-format", () -> NWorkspace.of().bootOptions().outputFormat().orNull());
        props.put("nuts-confirm", () -> NWorkspace.of().bootOptions().confirm().orNull());
        props.put("nuts-dependency-solver", () -> NWorkspace.of().bootOptions().dependencySolver().orNull());
        props.put("nuts-progress-options", () -> NWorkspace.of().bootOptions().progressOptions().orNull());
        props.put("nuts-progress", () -> NSession.of().isProgress());
        props.put("nuts-terminal-mode", () -> NWorkspace.of().bootOptions().terminalMode().orNull());
        props.put("nuts-cached", () -> NWorkspace.of().bootOptions().cached().orNull());
        props.put("nuts-install-companions", () -> NWorkspace.of().bootOptions().installCompanions().orNull());
        props.put("nuts-skip-welcome", () -> NWorkspace.of().bootOptions().skipWelcome().orNull());
        props.put("nuts-skip-boot", () -> NWorkspace.of().bootOptions().skipBoot().orNull());
        props.put("nuts-init-platforms", () -> NWorkspace.of().bootOptions().initPlatforms().orNull());
        props.put("nuts-init-java", () -> NWorkspace.of().bootOptions().initJava().orNull());
        props.put("nuts-init-launchers", () -> NWorkspace.of().bootOptions().initLaunchers().orNull());
        props.put("nuts-init-scripts", () -> NWorkspace.of().bootOptions().initScripts().orNull());
        props.put("nuts-desktop-launcher", () -> NWorkspace.of().bootOptions().desktopLauncher().orNull());
        props.put("nuts-menu-launcher", () -> NWorkspace.of().bootOptions().menuLauncher().orNull());
        props.put("nuts-user-launcher", () -> NWorkspace.of().bootOptions().userLauncher().orNull());
        props.put("nuts-locale", () -> NWorkspace.of().bootOptions().locale().orNull());
        props.put("nuts-theme", () -> NWorkspace.of().bootOptions().theme().orNull());
        props.put("nuts-username", () -> NWorkspace.of().bootOptions().userName().orNull());
        props.put("nuts-solver",
                () -> {
                    String ds = NDependencySolverUtils.resolveSolverName(NWorkspace.of().bootOptions().dependencySolver().orNull());
                    List<String> allDs = NDependencySolver.solverNames();
                    return NText.ofStyled(
                            ds,
                            allDs.stream().map(NDependencySolverUtils::resolveSolverName)
                                    .anyMatch(x -> x.equals(ds))
                                    ? NTextStyle.keyword() : NTextStyle.error());
                }
        );
        props.put("nuts-solver-list",
                () -> {
                    String ds = NDependencySolverUtils.resolveSolverName(NWorkspace.of().bootOptions().dependencySolver().orNull());
                    List<String> allDs = NDependencySolver.solverNames();
                    NTexts txt = NTexts.of();
                    return txt.ofBuilder().appendJoined(";",
                            allDs.stream()
                                    .map(x -> txt.ofStyled(x, NTextStyle.keyword()))
                                    .collect(Collectors.toList())
                    );
                }
        );
        props.put("sys-terminal-flags", () -> NWorkspace.of().bootTerminal().getFlags());
        props.put("sys-terminal-mode", () -> NWorkspace.of().bootOptions().terminalMode().orElse(NTerminalMode.DEFAULT));
        props.put("java-version", () -> NVersion.get(System.getProperty("java.version")).get());
        props.put("platform", () -> NEnv.of().java());
        props.put("java-home", () -> NPath.of(System.getProperty("java.home")));
        props.put("java-executable", () -> NPath.of(NJavaSdkUtils.of().resolveJavaCommandByHome(null)));
        props.put("java-classpath",
                () -> NTextBuilder.of().appendJoined(";",
                        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x))
                                .collect(Collectors.toList())
                )
        );
        props.put("java-library-path",
                () -> NTextBuilder.of().appendJoined(";",
                        Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                                .map(x -> NPath.of(x))
                                .collect(Collectors.toList())
                )
        );
        props.put("os-name", () -> NEnv.of().os());
        props.put("os-family", () -> NEnv.of().osFamily());
        props.put("os-dist", () -> NEnv.of().osDist());
        props.put("os-arch", () -> NEnv.of().arch());
        props.put("os-arch-family", () -> NEnv.of().archFamily());
        props.put("os-desktop", () -> NEnv.of().desktopEnvironment());
        props.put("os-desktops", () -> NEnv.of().desktopEnvironments());
        props.put("os-desktop-family", () -> NEnv.of().desktopEnvironmentFamily());
        props.put("os-desktop-families", () -> NEnv.of().desktopEnvironmentFamilies());
        props.put("os-desktop-path", () -> NEnv.of().desktopPath());
        props.put("os-desktop-launcher", () -> NEnv.of().getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP));
        props.put("os-menu-launcher", () -> NEnv.of().getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU));
        props.put("os-user-launcher", () -> NEnv.of().getDesktopIntegrationSupport(NDesktopIntegrationItem.USER));
        props.put("os-shell", () -> NEnv.of().shellFamily());
        props.put("os-shells", () -> NEnv.of().shellFamilies());
        props.put("os-username", () -> stringValue(System.getProperty("user.name")));
        props.put("user-home", () -> NPath.ofUserHome());
        props.put("user-dir", () -> NPath.ofUserDirectory());
        props.put("command-line-long",
                () -> NWorkspace.of().bootOptions().toCmdLine(new NWorkspaceOptionsConfig().compact(false))
        );
        props.put("command-line-short", () -> NWorkspace.of().bootOptions().toCmdLine(new NWorkspaceOptionsConfig().compact(true)));
        props.put("inherited", () -> NWorkspace.of().bootOptions().inherited().orElse(false));
        // nuts-boot-args must always be parsed in bash format
        props.put("inherited-nuts-boot-args", () -> NCmdLineWriter.of().format(NCmdLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH)));
        props.put("inherited-nuts-args", () -> NCmdLineWriter.of().format(NCmdLine.of(System.getProperty("nuts.args"), NShellFamily.SH)));
        props.put("creation-started", () -> NWorkspace.of().creationStartTime());
        props.put("creation-finished", () -> NWorkspace.of().creationFinishTime());
        props.put("creation-within", () -> NWorkspace.of().creationDuration().normalize());
        props.put("repositories-count", () -> (NWorkspace.of().repositories().size()));
        if(includeSysProps){
            System.getProperties().forEach((k,v)->{
                if(!props.containsKey(k)){
                    props.put((String) k,()-> v);
                }
            });
        }
        if(includeSysEnv){
            System.getenv().forEach((k,v)->{
                if(!props.containsKey(k)){
                    props.put((String) k,()-> v);
                }
            });
        }
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

        public MapAndSession(Map<String, Object> map) {
            this.map = map;
        }
    }

    private Map<String, Object> inflate(Map<String, Object> m) {
        MapAndSession mm = new MapAndSession(m);
        inflate(mm, repoSupplier);
        inflate(mm, extraPropertiesSupplier);
        return mm.map;
    }

    private Map<String, Object> buildWorkspaceMap(boolean deep) {
        String prefix = null;
        FilteredMap props = new FilteredMap(filter);
        NSession session = NSession.of();
        NWorkspace workspace = NWorkspace.of();
        NEnv environment = NEnv.of();
        NBootOptions options = workspace.bootOptions();
        Set<String> extraKeys = new TreeSet<>(extraProperties.keySet());

        props.put("name", stringValue(session.workspace().name()));
        props.put("nuts-api-version", session.workspace().apiVersion());
//        NutsIdFormat idFormat = ws.id().formatter();
        props.put("nuts-api-id", session.workspace().apiId());
        props.put("nuts-runtime-id", session.workspace().runtimeId());
        props.put("nuts-app-id", NApp.of().id().orNull());
        List<URL> cl = workspace.bootClassWorldURLs();
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
        props.put("nuts-workspace-id", txt.ofStyled(stringValue(session.workspace().uuid()), NTextStyle.path()));
        props.put("nuts-store-layout", workspace.storeLayout());
        props.put("nuts-store-strategy", workspace.storeStrategy());
        props.put("nuts-repo-store-strategy", workspace.repositoryStoreStrategy());
        props.put("nuts-global", options.system().orNull());
        props.put("nuts-workspace", workspace.workspaceLocation());
        for (NStoreType storeType : NStoreType.values()) {
            props.put("nuts-workspace-" + storeType.id(), NPath.of(NStoreKey.of(storeType)));
        }
        for (NStoreType storeType : NStoreType.values()) {
            props.put("nuts-system-" + storeType.id(), NPath.of(NStoreKey.ofSystem(storeType)));
        }
        for (NStoreType storeType : NStoreType.values()) {
            props.put("nuts-user-" + storeType.id(), NPath.of(NStoreKey.ofUser(storeType)));
        }
        for (NStoreType storeType : NStoreType.values()) {
            props.put("nuts-base-" + storeType.id(), NPath.of(NStoreKey.ofBase(storeType)));
        }
        props.put("nuts-open-mode", options.openMode().orNull());
        props.put("nuts-isolation-level", options.isolationLevel().orNull());
        props.put("nuts-secure", (NSecurityManager.of().isSecureMode()));
        props.put("nuts-gui", options.gui().orNull());
        props.put("nuts-inherited", options.inherited().orNull());
        props.put("nuts-recover", options.recover().orNull());
        props.put("nuts-reset", options.reset().orNull());
        props.put("nuts-read-only", (options.readOnly().orNull()));
        props.put("nuts-debug", NDebugString.of(options.debug().orNull()));
        props.put("nuts-bot", options.bot().orNull());
        props.put("nuts-trace", options.trace().orNull());
        props.put("nuts-indexed", options.indexed().orNull());
        props.put("nuts-transitive", options.transitive().orNull());
        props.put("nuts-fetch-strategy", options.fetchStrategy().orNull());
        props.put("nuts-execution-type", options.executionType().orNull());
        props.put("nuts-dry", options.dry().orNull());
        props.put("nuts-output-format", options.outputFormat().orNull());
        props.put("nuts-confirm", options.confirm().orNull());
        props.put("nuts-dependency-solver", options.dependencySolver().orNull());
        props.put("nuts-progress-options", options.progressOptions().orNull());
        props.put("nuts-progress", session.isProgress());
        props.put("nuts-terminal-mode", options.terminalMode().orNull());
        props.put("nuts-cached", options.cached().orNull());
        props.put("nuts-install-companions", options.installCompanions().orNull());
        props.put("nuts-skip-welcome", options.skipWelcome().orNull());
        props.put("nuts-skip-boot", options.skipBoot().orNull());
        props.put("nuts-init-platforms", options.initPlatforms().orNull());
        props.put("nuts-init-java", options.initJava().orNull());
        props.put("nuts-init-launchers", options.initLaunchers().orNull());
        props.put("nuts-init-scripts", options.initScripts().orNull());
        props.put("nuts-desktop-launcher", options.desktopLauncher().orNull());
        props.put("nuts-menu-launcher", options.menuLauncher().orNull());
        props.put("nuts-user-launcher", options.userLauncher().orNull());
        props.put("nuts-locale", options.locale().orNull());
        props.put("nuts-theme", options.theme().orNull());
        props.put("nuts-username", options.userName().orNull());
        String ds = NDependencySolverUtils.resolveSolverName(options.dependencySolver().orNull());
        List<String> allDs = NDependencySolver.solverNames();
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
        NWorkspaceTerminalOptions b = workspace.bootTerminal();
        props.put("sys-terminal-flags", b.getFlags());
        NTerminalMode terminalMode = workspace.bootOptions().terminalMode().orElse(NTerminalMode.DEFAULT);
        props.put("sys-terminal-mode", terminalMode);
        props.put("java-version", NVersion.get(System.getProperty("java.version")).get());
        props.put("platform", environment.java());
        props.put("java-home", NPath.of(System.getProperty("java.home")));
        props.put("java-native", environment.isNativeImage());
        props.put("java-executable", NPath.of(NJavaSdkUtils.of().resolveJavaCommandByHome(null)));
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
        props.put("os-name", environment.os());
        props.put("os-family", (environment.osFamily()));
        if (environment.osDist() != null) {
            props.put("os-dist", (environment.osDist()));
        }
        props.put("os-arch", environment.arch());
        props.put("os-arch-family", environment.archFamily());
        props.put("os-desktop", environment.desktopEnvironment());
        props.put("os-desktops", environment.desktopEnvironments());
        props.put("os-desktop-family", environment.desktopEnvironmentFamily());
        props.put("os-desktop-families", environment.desktopEnvironmentFamilies());
        props.put("os-desktop-path", environment.desktopPath());
        props.put("os-desktop-launcher", environment.getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP));
        props.put("os-menu-launcher", environment.getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU));
        props.put("os-user-launcher", environment.getDesktopIntegrationSupport(NDesktopIntegrationItem.USER));
        props.put("os-shell", environment.shellFamily());
        props.put("os-shells", environment.shellFamilies());
        props.put("os-username", stringValue(System.getProperty("user.name")));
        props.put("user-home", NPath.ofUserHome());
        props.put("user-dir", NPath.ofUserDirectory());
        props.put("command-line-long",
                workspace.bootOptions().toCmdLine(new NWorkspaceOptionsConfig().compact(false))
        );
        props.put("command-line-short", workspace.bootOptions().toCmdLine(new NWorkspaceOptionsConfig().compact(true)));
        props.put("inherited", workspace.bootOptions().inherited().orElse(false));
        // nuts-boot-args must always be parsed in bash format
        props.put("inherited-nuts-boot-args", NCmdLineWriter.of().format(NCmdLine.of(System.getProperty("nuts.boot.args"), NShellFamily.SH)));
        props.put("inherited-nuts-args", NCmdLineWriter.of().format(NCmdLine.of(System.getProperty("nuts.args"), NShellFamily.SH)));
        props.put("creation-started", workspace.creationStartTime());
        props.put("creation-finished", workspace.creationFinishTime());
        props.put("creation-within", workspace.creationDuration().normalize());
        props.put("repositories-count", (workspace.repositories().size()));
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        if(includeSysProps){
            System.getProperties().forEach((k,v)->{
                String sk = (String) k;
                if(!props.containsKey(sk)){
                    props.put(sk,()-> v);
                }
            });
        }
        if(includeSysEnv){
            System.getenv().forEach((k,v)->{
                if(!props.containsKey(k)){
                    props.put((String) k,()-> v);
                }
            });
        }
        if (deep) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            props.put("repos", repositories);
            for (NRepository repository : workspace.repositories()) {
                repositories.put(repository.name(), buildRepoRepoMap(repository, deep, prefix));
            }
        }

        return props.build();
    }

    private Map<String, Object> buildRepoRepoMap(NRepository repo, boolean deep, String prefix) {
        FilteredMap props = new FilteredMap(filter);
        props.put(key(prefix, "name"), stringValue(repo.name()));
        props.put(key(prefix, "global-name"), repo.config().globalName());
        props.put(key(prefix, "uuid"), stringValue(repo.uuid()));
        props.put(key(prefix, "type"),
                //display as enum
                NText.ofStyled(repo.config().type(), NTextStyle.option())
        );
        props.put(key(prefix, "speed"), (repo.config().speed()));
        props.put(key(prefix, "enabled"), (repo.config().isEnabled()));
        props.put(key(prefix, "active"), (repo.isEnabled()));
        props.put(key(prefix, "index-enabled"), (repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), (repo.config().isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().location());
        props.put(key(prefix, "deploy-order"), (repo.config().deployWeight()));
        props.put(key(prefix, "store-strategy"), (repo.config().storeStrategy()));
        props.put(key(prefix, "store-location"), repo.config().storeLocation());
        for (NStoreType value : NStoreType.values()) {
            props.put(key(prefix, "store-location-" + value.id()), repo.config().getStoreLocation(value));
        }
        props.put(key(prefix, "supported-mirroring"), (repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), ((!repo.config()
                    .isSupportedMirroring()) ? 0 : repo.config()
                    .mirrors().size()));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                props.put("mirrors", mirrors);
                for (NRepository mirror : repo.config()
                        .mirrors()) {
                    mirrors.put(mirror.name(), buildRepoRepoMap(mirror, deep, null));
                }
            }
        }
        return props.build();
    }

    private String stringValue(Object s) {
        return NTextBuilder.of().append(CoreStringUtils.stringValue(s)).toString();
    }

    public boolean isLenient() {
        return lenient;
    }

    public NInfoCmd setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NInfoCmd ntf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }

    @Override
    public NWorkspaceCmd run() {
        println(NOut.out());
        return this;
    }

    @Override
    public NInfoCmd configure(boolean skipUnsupported, String... args) {
        configure(skipUnsupported, NCmdLine.of(args).commandName("info"));
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

        public boolean containsKey(String k) {
            return data.containsKey(k);
        }
    }
}
