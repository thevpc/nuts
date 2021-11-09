package net.thevpc.nuts.runtime.core.format;

import net.thevpc.nuts.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.solvers.NutsDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreTimeUtils;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsInfoFormat extends DefaultFormatBase<NutsInfoFormat> implements NutsInfoFormat {

    private final Map<String, String> extraProperties = new LinkedHashMap<>();
    private boolean showRepositories = false;
    private boolean fancy = false;
    private List<String> requests = new ArrayList<>();
    private Predicate<String> filter = NutsPredicates.always();
    private boolean lenient = false;

    public DefaultNutsInfoFormat(NutsSession session) {
        super(session, "info");
    }

    @Override
    public NutsInfoFormat setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isShowRepositories() {
        return showRepositories;
    }

    @Override
    public NutsInfoFormat addProperty(String key, String value) {
        if (value == null) {
            extraProperties.remove(key);
        } else {
            extraProperties.put(key, value);
        }
        return this;
    }

    @Override
    public NutsInfoFormat addProperties(Map<String, String> customProperties) {
        if (customProperties != null) {
            for (Map.Entry<String, String> e : customProperties.entrySet()) {
                addProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NutsInfoFormat setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public void print(NutsPrintStream w) {
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
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("property not found : %s", key));
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
                        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("property not found : %s",request));
                    }
                }
            }
        }
        NutsObjectFormat.of(getSession()).setValue(result).configure(true, args.toArray(new String[0])).print(w);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getKey().getString()) {
            case "-r":
            case "--repos": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setShowRepositories(val);
                }
                return true;
            }
            case "--fancy": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setFancy(val);
                }
                return true;
            }
            case "-l":
            case "--lenient": {
                boolean val = cmdLine.nextBoolean().getValue().getBoolean();
                if (enabled) {
                    this.setLenient(val);
                }
                return true;
            }
            case "--add": {
                NutsVal aa = cmdLine.nextString().getValue();
                NutsArgument val = NutsArgument.of(aa.getString(),getSession());
                if (enabled) {
                    extraProperties.put(val.getKey().getString(), val.getValue().getString());
                }
                return true;
            }
            case "-p":
            case "--path": {
                cmdLine.skip();
                if (enabled) {
                    requests.add("nuts-workspace");
                    for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
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
                String r = cmdLine.nextString().getValue().getString();
                if (enabled) {
                    requests.add(r);
                }
                while (true) {
                    NutsArgument p = cmdLine.peek();
                    if (p != null && !p.isOption()) {
                        cmdLine.skip();
                        if (enabled) {
                            requests.add(p.getString());
                        }
                    } else {
                        break;
                    }
                }
                return true;
            }
            default: {
                if (getSession().configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String key(String prefix, String key) {
        if (NutsBlankable.isBlank(prefix)) {
            return key;
        }
        return prefix + "." + key;
    }

    //    @Override
    private Map<String, Object> buildWorkspaceMap(boolean deep) {
        String prefix = null;
        FilteredMap props = new FilteredMap(filter);
        NutsSession ws = getSession();
        NutsWorkspaceConfigManager rt = ws.config();
        NutsWorkspaceOptions options = ws.boot().getBootOptions();
        Set<String> extraKeys = new TreeSet<>(extraProperties.keySet());

        props.put("name", stringValue(ws.getWorkspace().getName()));
        props.put("nuts-api-version", ws.getWorkspace().getApiVersion());
//        NutsIdFormat idFormat = ws.id().formatter();
        props.put("nuts-api-id", ws.getWorkspace().getApiId());
        props.put("nuts-runtime-id", ws.getWorkspace().getRuntimeId());
        URL[] cl = ws.boot().getBootClassWorldURLs();
        List<NutsPath> runtimeClassPath = new ArrayList<>();
        if (cl != null) {
            for (URL url : cl) {
                if (url != null) {
                    String s = url.toString();
                    try {
                        s = Paths.get(url.toURI()).toFile().getPath();
                    } catch (URISyntaxException ex) {
                        s = s.replace(":", "\\:");
                    }
                    runtimeClassPath.add(NutsPath.of(s,ws));
                }
            }
        }

        NutsTexts txt = NutsTexts.of(ws);
        props.put("nuts-runtime-classpath",
                txt.builder().appendJoined(";",runtimeClassPath)
        );
        props.put("nuts-workspace-id", txt.ofStyled(stringValue(ws.getWorkspace().getUuid()),NutsTextStyle.path()));
        props.put("nuts-store-layout", ws.locations().getStoreLocationLayout());
        props.put("nuts-store-strategy", ws.locations().getStoreLocationStrategy());
        props.put("nuts-repo-store-strategy", ws.locations().getRepositoryStoreLocationStrategy());
        props.put("nuts-global", options.isGlobal());
        props.put("nuts-workspace", ws.locations().getWorkspaceLocation());
        for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
            props.put("nuts-workspace-" + folderType.id(), ws.locations().getStoreLocation(folderType));
        }
        props.put("nuts-open-mode", (options.getOpenMode() == null ? NutsOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
        props.put("nuts-secure", (ws.security().isSecure()));
        props.put("nuts-gui", options.isGui());
        props.put("nuts-inherited", options.isInherited());
        props.put("nuts-recover", options.isRecover());
        props.put("nuts-reset", options.isReset());
        props.put("nuts-debug", options.isDebug());
        props.put("nuts-trace", options.isTrace());
        props.put("nuts-read-only", (options.isReadOnly()));
        props.put("nuts-skip-companions", options.isSkipCompanions());
        props.put("nuts-skip-welcome", options.isSkipWelcome());
        props.put("nuts-skip-boot", options.isSkipBoot());
        String ds = NutsDependencySolverUtils.resolveSolverName(options.getDependencySolver());
        String[] allDs = NutsDependencySolver.getSolverNames(ws);
        props.put("nuts-solver",
                txt.ofStyled(
                        ds,
                        Arrays.stream(allDs).map(x-> NutsDependencySolverUtils.resolveSolverName(x))
                                .anyMatch(x->x.equals(ds))
                        ?NutsTextStyle.keyword() : NutsTextStyle.error())
                );
        props.put("nuts-solver-list",
                txt.builder().appendJoined(";",
                        Arrays.stream(allDs)
                                .map(x-> txt.ofStyled(x,NutsTextStyle.keyword()))
                                .collect(Collectors.toList())
                )

        );
        props.put("java-version", NutsVersion.of(System.getProperty("java.version"),ws));
        props.put("platform", ws.env().getPlatform());
        props.put("java-home", NutsPath.of(System.getProperty("java.home"),ws));
        props.put("java-executable", NutsPath.of(NutsJavaSdkUtils.of(ws).resolveJavaCommandByHome(null, getSession()),ws));
        props.put("java-classpath",
                txt.builder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                                .map(x->NutsPath.of(x,ws))
                                .collect(Collectors.toList())
                        )
        );
        props.put("java-library-path",
                txt.builder().appendJoined(";",
                        Arrays.stream(System.getProperty("java.library.path").split(File.pathSeparator))
                                .map(x->NutsPath.of(x,ws))
                                .collect(Collectors.toList())
                )
        );
        props.put("os-name", ws.env().getOs());
        props.put("os-family", (ws.env().getOsFamily()));
        if (ws.env().getOsDist() != null) {
            props.put("os-dist", (ws.env().getOsDist()));
        }
        props.put("os-arch", ws.env().getArchFamily());
        props.put("os-shell", ws.env().getShellFamily());
        props.put("user-name", stringValue(System.getProperty("user.name")));
        props.put("user-home", NutsPath.of(System.getProperty("user.home"),ws));
        props.put("user-dir", NutsPath.of(System.getProperty("user.dir"),ws));
        props.put("command-line-long",
                ws.boot().getBootOptions().formatter().setCompact(false).getBootCommandLine()
        );
        props.put("command-line-short", ws.boot().getBootOptions().formatter().setCompact(true).getBootCommandLine());
        props.put("inherited", ws.boot().getBootOptions().isInherited());
        // nuts-boot-args must always be parsed in bash format
        props.put("inherited-nuts-boot-args", NutsCommandLine.of(System.getProperty("nuts.boot.args"),NutsShellFamily.SH,ws).format());
        props.put("inherited-nuts-args", NutsCommandLine.of(System.getProperty("nuts.args"),NutsShellFamily.SH,ws)
                .format()
        );
        props.put("creation-started", Instant.ofEpochMilli(ws.boot().getCreationStartTimeMillis()));
        props.put("creation-finished", Instant.ofEpochMilli(ws.boot().getCreationFinishTimeMillis()));
        props.put("creation-within", CoreTimeUtils.formatPeriodMilli(ws.boot().getCreationTimeMillis()).trim());
        props.put("repositories-count", (ws.repos().setSession(getSession()).getRepositories().length));
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.get(extraKey));
        }
        if (deep) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            props.put("repos", repositories);
            for (NutsRepository repository : ws.repos().setSession(getSession()).getRepositories()) {
                repositories.put(repository.getName(), buildRepoRepoMap(repository, deep, prefix));
            }
        }

        return props.build();
    }

    private Map<String, Object> buildRepoRepoMap(NutsRepository repo, boolean deep, String prefix) {
        NutsSession ws = getSession();
        FilteredMap props = new FilteredMap(filter);
        props.put(key(prefix, "name"), stringValue(repo.getName()));
        props.put(key(prefix, "global-name"), repo.config().getGlobalName());
        props.put(key(prefix, "uuid"), stringValue(repo.getUuid()));
        props.put(key(prefix, "type"),
                //display as enum
                NutsTexts.of(ws).ofStyled(repo.config().getType(),NutsTextStyle.option())
                );
        props.put(key(prefix, "speed"), (repo.config().getSpeed()));
        props.put(key(prefix, "enabled"), (repo.config().isEnabled()));
        props.put(key(prefix, "index-enabled"), (repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), (repo.config().setSession(getSession()).isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().getLocation(false));
        if (repo.config().getLocation(false) != null) {
            props.put(key(prefix, "location-expanded"), repo.config().getLocation(true));
        }
        props.put(key(prefix, "deploy-order"), (repo.config().getDeployWeight()));
        props.put(key(prefix, "store-location-strategy"), (repo.config().getStoreLocationStrategy()));
        props.put(key(prefix, "store-location"), NutsPath.of(repo.config().getStoreLocation(),getSession()));
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            props.put(key(prefix, "store-location-" + value.id()), NutsPath.of(repo.config().getStoreLocation(value),getSession()));
        }
        props.put(key(prefix, "supported-mirroring"), (repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), ((!repo.config()
                    .setSession(getSession())
                    .isSupportedMirroring()) ? 0 : repo.config()
                                    .setSession(getSession())
                                    .getMirrors().length));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                props.put("mirrors", mirrors);
                for (NutsRepository mirror : repo.config()
                        .setSession(getSession())
                        .getMirrors()) {
                    mirrors.put(mirror.getName(), buildRepoRepoMap(mirror, deep, null));
                }
            }
        }
        return props.build();
    }

    private String stringValue(Object s) {
        return NutsTexts.of(getSession()).builder().append(CoreCommonUtils.stringValue(s)).toString();
    }

    public boolean isLenient() {
        return lenient;
    }

    public NutsInfoFormat setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NutsInfoFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
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
    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

}
