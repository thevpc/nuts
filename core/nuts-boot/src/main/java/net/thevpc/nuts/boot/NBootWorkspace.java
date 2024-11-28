/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.reserved.cmdline.NCmdLineBoot;
import net.thevpc.nuts.boot.reserved.cmdline.NWorkspaceCmdLineFormatterBoot;
import net.thevpc.nuts.boot.reserved.cmdline.NWorkspaceCmdLineParserBoot;
import net.thevpc.nuts.boot.reserved.cmdline.NWorkspaceOptionsConfigBoot;
import net.thevpc.nuts.boot.reserved.*;
import net.thevpc.nuts.boot.reserved.util.*;
import net.thevpc.nuts.boot.reserved.NReservedErrorInfo;
import net.thevpc.nuts.boot.reserved.NReservedErrorInfoList;
import net.thevpc.nuts.boot.reserved.util.NReservedIOUtilsBoot;
import net.thevpc.nuts.boot.reserved.maven.NReservedMavenUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NReservedJsonParser;
import net.thevpc.nuts.boot.reserved.NReservedPath;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * NutsBootWorkspace is responsible of loading initial nuts-runtime.jar and its
 * dependencies and for creating workspaces using the method
 * {@link #openWorkspace()} . NutsBootWorkspace is also responsible of managing
 * local jar cache folder located at ~/.cache/nuts/default-workspace/boot
 * <br>
 * Default Bootstrap implementation. This class is responsible of loading
 * initial nuts-runtime.jar and its dependencies and for creating workspaces
 * using the method {@link #openWorkspace()}.
 * <br>
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public final class NBootWorkspace {
    private static final String version = "0.8.5";

    public static String getVersion() {
        return version;
    }

    private final Instant creationTime = Instant.now();
    private final NBootOptionsBoot userOptions;
    private final NLogBoot bLog;
    private final NBootOptionsBoot computedOptions = new NBootOptionsBoot();
    private final NReservedBootRepositoryDB repositoryDB=new NReservedBootRepositoryDB();
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "workspace":
                    return computedOptions.getWorkspace();
                case "user.home":
                    return System.getProperty("user.home");
                case "home.apps":
                case "home.config":
                case "home.lib":
                case "home.temp":
                case "home.var":
                case "home.cache":
                case "home.run":
                case "home.log":
                    return NUtilsBoot.getHome(from.substring("home.".length()).toUpperCase(), computedOptions);
                case "apps":
                case "config":
                case "lib":
                case "cache":
                case "run":
                case "temp":
                case "log":
                case "var": {
                    Map<String, String> s = NUtilsBoot.firstNonNull(computedOptions.getStoreLocations(),Collections.emptyMap());
                    String v = s.get(from);
                    if (v == null) {
                        return "${" + from + "}";
                    }
                    return v;
                }
            }
            return "${" + from + "}";
        }
    };
    private int newInstanceRequirements = 0;
    private NBootOptionsBoot lastWorkspaceOptions;
    //private Set<NRepositoryLocationBoot> parsedBootRuntimeDependenciesRepositories;
    private Set<NRepositoryLocationBoot> parsedBootRuntimeRepositories;
    private boolean preparedWorkspace;
    private Scanner scanner;
    private NBootCache cache = new NBootCache();
    Boolean runtimeLoaded;
    NIdBoot runtimeLoadedId;

    public NBootWorkspace(NBootArguments userOptionsUnparsed) {
        if(userOptionsUnparsed==null){
            userOptionsUnparsed=new NBootArguments();
        }
        NBootOptionsBoot userOptions = new NBootOptionsBoot();
        userOptions.setStdin(userOptionsUnparsed.getIn());
        userOptions.setStdout(userOptionsUnparsed.getOut());
        userOptions.setStderr(userOptionsUnparsed.getErr());
        userOptions.setCreationTime(userOptionsUnparsed.getStartTime());
        InputStream in = userOptions.getStdin();
        scanner = new Scanner(in == null ? System.in : in);
        this.bLog = new NLogBoot(userOptions);
        String[] args = userOptionsUnparsed.getArgs();
        NWorkspaceCmdLineParserBoot.parseNutsArguments(args==null?new String[0] : args,userOptions);
        if (NUtilsBoot.firstNonNull(userOptions.getSkipErrors(),false)) {
            StringBuilder errorMessage = new StringBuilder();
            if(userOptions.getErrors()!=null) {
                for (String s : userOptions.getErrors()) {
                    errorMessage.append(s).append("\n");
                }
            }
            errorMessage.append("Try 'nuts --help' for more information.");
            bLog.log(Level.WARNING, "WARNING", NMsgBoot.ofC("Error : %s", errorMessage));
        }
        this.userOptions = userOptions.copy();
        this.postInit();
    }
    public NBootWorkspace(NBootOptionsBoot userOptions) {
        if (userOptions == null) {
            userOptions = new NBootOptionsBoot();
        }
        this.bLog = new NLogBoot(userOptions);
        this.userOptions = userOptions;
        this.postInit();
    }

    private void postInit() {
        this.computedOptions.setAll(userOptions);
        this.computedOptions.setUserOptions(this.userOptions);
//        this.computedOptions.setIsolationLevel(this.computedOptions.getIsolationLevel().orElse(NIsolationLevel.SYSTEM));
//        this.computedOptions.setExecutionType(this.computedOptions.getExecutionType().orElse(NExecutionType.SPAWN));
//        this.computedOptions.setConfirm(this.computedOptions.getConfirm().orElse(NConfirmationMode.ASK));
//        this.computedOptions.setFetchStrategy(this.computedOptions.getFetchStrategy().orElse(NFetchStrategy.ONLINE));
//        this.computedOptions.setOpenMode(this.computedOptions.getOpenMode().orElse(NOpenMode.OPEN_OR_CREATE));
//        this.computedOptions.setRunAs(this.computedOptions.getRunAs().orElse(NRunAs.CURRENT_USER));
//        this.computedOptions.setLogConfig(this.computedOptions.getLogConfig().orElseGet(NLogConfig::new));
//        this.computedOptions.setStdin(this.computedOptions.getStdin().orElse(System.in));
//        this.computedOptions.setStdout(this.computedOptions.getStdout().orElse(System.out));
//        this.computedOptions.setStderr(this.computedOptions.getStderr().orElse(System.err));
//        this.computedOptions.setLocale(this.computedOptions.getLocale().orElse(Locale.getDefault().toString()));
//        this.computedOptions.setOutputFormat(this.computedOptions.getOutputFormat().orElse(NContentType.PLAIN));
//        this.computedOptions.setCreationTime(this.computedOptions.getCreationTime().orElse(creationTime));
        if (this.computedOptions.getApplicationArguments().isEmpty()) {
            this.computedOptions.setApplicationArguments(new ArrayList<>());
        }
        this.bLog.setOptions(this.computedOptions);
    }

    private static void revalidateLocations(NBootOptionsBoot bootOptions, String workspaceName, boolean immediateLocation, String sandboxMode) {
        if (NStringUtilsBoot.isBlank(bootOptions.getName())) {
            bootOptions.setName(workspaceName);
        }
        boolean system = NUtilsBoot.firstNonNull(bootOptions.getSystem(),false);
        if (NNameFormatBoot.CONST_NAME.equals(sandboxMode, "SANDBOX")) {
            bootOptions.setStoreStrategy("STANDALONE");
            bootOptions.setRepositoryStoreStrategy("EXPLODED");
            system = false;
        } else {
            if (bootOptions.getStoreStrategy()==null) {
                bootOptions.setStoreStrategy(immediateLocation ? "EXPLODED" : "STANDALONE");
            }
            if (bootOptions.getRepositoryStoreStrategy()==null) {
                bootOptions.setRepositoryStoreStrategy("EXPLODED");
            }
        }
        Map<String, String> storeLocations =
                NPlatformHomeBoot.of(bootOptions.getStoreLayout(), system)
                        .buildLocations(bootOptions.getStoreStrategy(), bootOptions.getStoreLocations(), bootOptions.getHomeLocations(), bootOptions.getWorkspace() //no session!
                        );
        if (new HashSet<>(storeLocations.values()).size() != storeLocations.size()) {
            Map<String, List<String>> conflicts = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : storeLocations.entrySet()) {
                conflicts.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }
            StringBuilder error = new StringBuilder();
            error.append("invalid store locations. Two or more stores point to the same location:");
            List<Object> errorParams = new ArrayList<>();
            for (Map.Entry<String, List<String>> e : conflicts.entrySet()) {
                List<String> ev = e.getValue();
                if (ev.size() > 1) {
                    String ek = e.getKey();
                    error.append("\n");
                    error.append("all of (").append(ev.stream().map(x -> "%s").collect(Collectors.joining(","))).append(") point to %s");
                    errorParams.addAll(ev);
                    errorParams.add(ek);
                }
            }
            throw new NBootException(NMsgBoot.ofC(error.toString(), errorParams));
        }
        bootOptions.setStoreLocations(storeLocations);
    }

    private static final class ApiDigestHolder {
        static final String apiDigest = NUtilsBoot.resolveNutsIdDigest();
    }

    /**
     * current nuts version, loaded from pom file
     *
     * @return current nuts version
     */
    private static String getApiDigestOrInternal() {
        return ApiDigestHolder.apiDigest == null ? "<internal>" : ApiDigestHolder.apiDigest;
    }

    public boolean hasUnsatisfiedRequirements() {
        prepareWorkspace();
        return newInstanceRequirements != 0;
    }

    public void runNewProcess() {
        String[] processCmdLine = createProcessCmdLine();
        int result;
        try {
//            if (nLog != null) {
//                nLog.with().level(Level.FINE).verbStart().log(NMsgBoot.ofC("start new process : %s", NCmdLine.of(processCmdLine)));
//            } else {
            bLog.log(Level.FINE, "START", NMsgBoot.ofC("start new process : %s", new NCmdLineBoot(processCmdLine)));
//            }
            result = new ProcessBuilder(processCmdLine).inheritIO().start().waitFor();
        } catch (IOException | InterruptedException ex) {
            throw new NBootException(NMsgBoot.ofPlain("failed to run new nuts process"), ex);
        }
        if (result != 0) {
            throw new NBootException(NMsgBoot.ofC("failed to exec new process. returned %s", result));
        }
    }

    /**
     * repositories used to locale nuts-runtime artifact or its dependencies
     *
     * @param dependencies when true search for runtime dependencies, when
     *                     false, search for runtime
     * @return repositories
     */
    public Set<NRepositoryLocationBoot> resolveBootRuntimeRepositories(boolean dependencies) {
//        if (dependencies) {
//            if (parsedBootRuntimeDependenciesRepositories != null) {
//                return parsedBootRuntimeDependenciesRepositories;
//            }
//            bLog.log(Level.FINE, "START", NMsgBoot.ofC("resolve boot repositories to load nuts-runtime dependencies from options : %s and config: %s", computedOptions.getRepositories().orElseGet(Collections::emptyList).toString(), computedOptions.getBootRepositories().ifBlankEmpty().orElse("[]")));
//        } else {
        if (parsedBootRuntimeRepositories != null) {
            return parsedBootRuntimeRepositories;
        }
        bLog.log(Level.FINE, "START", NMsgBoot.ofC("resolve boot repositories to load nuts-runtime from options : %s and config: %s",
                computedOptions.getRepositories()==null?"[]":computedOptions.getRepositories().toString(),
                computedOptions.getBootRepositories()==null?"[]":computedOptions.getBootRepositories().toString()));
//        }
        NRepositorySelectorListBoot bootRepositoriesSelector = NRepositorySelectorListBoot.of(computedOptions.getRepositories(), repositoryDB);
        NRepositorySelectorBoot[] old = NRepositorySelectorListBoot.of(Arrays.asList(computedOptions.getBootRepositories()), repositoryDB).toArray();
        NRepositoryLocationBoot[] result;
        if (old.length == 0) {
            //no previous config, use defaults!
            result = bootRepositoriesSelector.resolve(new NRepositoryLocationBoot[]{
                    Boolean.getBoolean("nomaven") ? null : new NRepositoryLocationBoot("maven", "maven", "maven")
            }, repositoryDB);
        } else {
            result = bootRepositoriesSelector.resolve(Arrays.stream(old).map(x -> NRepositoryLocationBoot.of(x.getName(), x.getUrl())).toArray(NRepositoryLocationBoot[]::new), repositoryDB);
        }
        result = Arrays.stream(result).map(
                r -> {
                    if (NStringUtilsBoot.isBlank(r.getLocationType()) || NStringUtilsBoot.isBlank(r.getName())) {
                        boolean fileExists = false;
                        if (r.getPath() != null) {
                            NReservedPath r1 = new NReservedPath(r.getPath()).toAbsolute();
                            if (!r.getPath().equals(r1.getPath())) {
                                r = r.setPath(r1.getPath());
                            }
                            NReservedPath r2 = r1.resolve(".nuts-repository");
                            NReservedJsonParser parser = null;
                            try {
                                byte[] bytes = r2.readAllBytes(bLog);
                                if (bytes != null) {
                                    fileExists = true;
                                    parser = new NReservedJsonParser(new InputStreamReader(new ByteArrayInputStream(bytes)));
                                    Map<String, Object> jsonObject = parser.parseObject();
                                    if (NStringUtilsBoot.isBlank(r.getLocationType())) {
                                        Object o = jsonObject.get("repositoryType");
                                        if (o instanceof String && !NStringUtilsBoot.isBlank((String) o)) {
                                            r = r.setLocationType(String.valueOf(o));
                                        }
                                    }
                                    if (NStringUtilsBoot.isBlank(r.getName())) {
                                        Object o = jsonObject.get("repositoryName");
                                        if (o instanceof String && !NStringUtilsBoot.isBlank((String) o)) {
                                            r = r.setName(String.valueOf(o));
                                        }
                                    }
                                    if (NStringUtilsBoot.isBlank(r.getName())) {
                                        r = r.setName(r.getName());
                                    }
                                }
                            } catch (Exception e) {
                                bLog.log(Level.CONFIG, "WARNING", NMsgBoot.ofC("unable to load %s", r2));
                            }
                        }
                        if (fileExists) {
                            if (NStringUtilsBoot.isBlank(r.getLocationType())) {
                                r = r.setLocationType(NConstants.RepoTypes.NUTS);
                            }
                        }
                    }
                    return r;
                }
        ).toArray(NRepositoryLocationBoot[]::new);
        Set<NRepositoryLocationBoot> rr = Arrays.stream(result).collect(Collectors.toCollection(LinkedHashSet::new));
//        if (dependencies) {
//            parsedBootRuntimeDependenciesRepositories = rr;
//        } else {
        parsedBootRuntimeRepositories = rr;
//        }
        return rr;
    }

    public String[] createProcessCmdLine() {
        prepareWorkspace();
        bLog.log(Level.FINE, "START", NMsgBoot.ofC("running version %s.  %s", computedOptions.getApiVersion(), getRequirementsHelpString(true)));
        String defaultWorkspaceLibFolder = computedOptions.getStoreType("LIB")+"/"+NConstants.Folders.ID;
        List<NRepositoryLocationBoot> repos = new ArrayList<>();
        repos.add(NRepositoryLocationBoot.of("nuts@" + defaultWorkspaceLibFolder));
        Collection<NRepositoryLocationBoot> bootRepositories = resolveBootRuntimeRepositories(true);
        repos.addAll(bootRepositories);
        NReservedErrorInfoList errorList = new NReservedErrorInfoList();
        File file = NReservedMavenUtilsBoot.resolveOrDownloadJar(NIdBoot.ofApi(computedOptions.getApiVersion()), repos.toArray(new NRepositoryLocationBoot[0]),
                NRepositoryLocationBoot.of("nuts@" + computedOptions.getStoreType("LIB") + File.separator + NConstants.Folders.ID), bLog, false, computedOptions.getExpireTime(), errorList);
        if (file == null) {
            errorList.insert(0, new NReservedErrorInfo(null, null, null, "unable to load nuts " + computedOptions.getApiVersion(), null));
            logError(null, errorList);
            throw new NBootException(NMsgBoot.ofC("unable to load %s#%s", NConstants.Ids.NUTS_API, computedOptions.getApiVersion()));
        }

        List<String> cmd = new ArrayList<>();
        String jc = computedOptions.getJavaCommand();
        if (jc == null || jc.trim().isEmpty()) {
            jc = NUtilsBoot.resolveJavaCommand(null);
        }
        cmd.add(jc);
        boolean showCommand = false;
        for (String c : NCmdLineBoot.parseDefaultList(computedOptions.getJavaOptions())) {
            if (!c.isEmpty()) {
                if (c.equals("--show-command")) {
                    showCommand = true;
                } else {
                    cmd.add(c);
                }
            }
        }
        if (computedOptions.getJavaOptions()==null) {
            Collections.addAll(cmd, NCmdLineBoot.parseDefaultList(computedOptions.getJavaOptions()));
        }
        cmd.add("-jar");
        cmd.add(file.getPath());
        cmd.addAll(asCmdLine(computedOptions,new NWorkspaceOptionsConfigBoot().setCompact(true).setApiVersion(computedOptions.getApiVersion())).toStringList());
        if (showCommand) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cmd.size(); i++) {
                String s = cmd.get(i);
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(s);
            }
            bLog.log(Level.FINE, "START", NMsgBoot.ofC("[exec] %s", sb));
        }
        return cmd.toArray(new String[0]);
    }

    private NCmdLineBoot asCmdLine(NBootOptionsBoot cc,NWorkspaceOptionsConfigBoot conf){
        if(conf==null){
            conf=new NWorkspaceOptionsConfigBoot();
        }
        NWorkspaceCmdLineFormatterBoot a=new NWorkspaceCmdLineFormatterBoot(
                conf
                ,
                cc
                );
        return a.toCmdLine();
    }

    public NBootOptionsBoot getOptions() {
        return computedOptions;
    }


    private NIdCache getFallbackCache(NIdBoot baseId, boolean lastWorkspace, boolean copyTemp) {
        NIdCache old = cache.fallbackIdMap.get(baseId);
        if (old != null) {
            return old;
        }

        NIdCache fid = new NIdCache();
        fid.baseId = baseId;
        cache.fallbackIdMap.put(fid.baseId, fid);
        String s = (lastWorkspace ? lastWorkspaceOptions : computedOptions).getStoreLocations().get("LIB") + "/id/"
                + NIdUtilsBoot.resolveIdPath(baseId.getShortId());
        //
        Path ss = Paths.get(s);
        NIdBoot bestId = null;
        NVersionBoot bestVersion = null;
        Path bestPath = null;

        if (Files.isDirectory(ss)) {
            try (Stream<Path> stream = Files.list(ss)) {
                for (Path path : stream.collect(Collectors.toList())) {
                    NVersionBoot version = NVersionBoot.of(path.getFileName().toString());
                    if (version != null) {
                        if (Files.isDirectory(path)) {
                            NIdBoot rId = baseId.copy().setVersion(version.getValue());
                            Path jar = ss.resolve(version.toString()).resolve(NIdUtilsBoot.resolveFileName(
                                    rId,
                                    "jar"
                            ));
                            if (Files.isRegularFile(jar)) {
                                if (bestVersion == null || bestVersion.compareTo(version) < 0) {
                                    bestVersion = version;
                                    bestPath = jar;
                                    bestId = rId;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore any error
            }
        }
        if (bestVersion != null) {
            Path descNutsPath = bestPath.resolveSibling(NIdUtilsBoot.resolveFileName(bestId, "nuts"));
            Set<NIdBoot> dependencies = NReservedMavenUtilsBoot.loadDependenciesFromNutsUrl(descNutsPath.toString(), bLog);
            if (dependencies != null) {
                fid.deps = dependencies.stream()
                        .filter(x -> NUtilsBoot.isAcceptDependency(x.toDependency(), computedOptions))
                        .collect(Collectors.toSet());
                fid.depsData = fid.deps.stream()
                        .map(x -> {
                            try {
                                return getFallbackCache(x, lastWorkspace, copyTemp);
                            } catch (RuntimeException e) {
                                if (!x.toDependency().isOptional()) {
                                    throw e;
                                }
                                //
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            fid.id = bestId;
            if (copyTemp) {
                Path temp = null;
                try {
                    temp = Files.createTempFile("old-", bestPath.getFileName().toString());
                    Files.copy(bestPath, temp, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new NBootException(NMsgBoot.ofPlain("error storing nuts-runtime.jar"), e);
                }
                fid.jar = temp.toString();
                fid.expected = bestPath.toString();
                fid.temp = true;

            } else {
                fid.jar = bestPath.toString();
            }
        }

        return fid;
    }

    private boolean isRuntimeLoaded() {
        if (runtimeLoaded == null) {
            runtimeLoaded = false;
            try {
                Class<?> c = Class.forName("net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace");
                runtimeLoadedId = (NIdBoot) c.getField("RUNTIME_ID").get(null);
                runtimeLoaded = true;
            } catch (Exception ex) {
                //
            }
        }
        return runtimeLoaded;
    }

    @SuppressWarnings("unchecked")
    private boolean prepareWorkspace() {
        if (!preparedWorkspace) {

            preparedWorkspace = true;
            String isolationLevel = NStringUtilsBoot.firstNonBlank(computedOptions.getIsolationLevel(), "SYSTEM");
            if (bLog.isLoggable(Level.CONFIG)) {
                bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("bootstrap Nuts version %s %s digest %s...", getVersion(),
                        NNameFormatBoot.equalsIgnoreFormat(isolationLevel, "SYSTEM") ? "" :
                                NNameFormatBoot.equalsIgnoreFormat(isolationLevel, "USER") ? " (user mode)" :
                                        NNameFormatBoot.equalsIgnoreFormat(isolationLevel, "CONFINED") ? " (confined mode)" :
                                                NNameFormatBoot.equalsIgnoreFormat(isolationLevel, "SANDBOX") ? " (sandbox mode)" : " (unsupported mode " + isolationLevel + ")",
                        getApiDigestOrInternal()));
                bLog.log(Level.CONFIG, "START", NMsgBoot.ofPlain("boot-class-path:"));
                for (String s : NStringUtilsBoot.split(System.getProperty("java.class.path"), File.pathSeparator, true, true)) {
                    bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("                  %s", s));
                }
                ClassLoader thisClassClassLoader = getClass().getClassLoader();
                bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("class-loader: %s", thisClassClassLoader));
                for (URL url : NReservedLangUtilsBoot.resolveClasspathURLs(thisClassClassLoader, false)) {
                    bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("                 %s", url));
                }
                ClassLoader tctxloader = Thread.currentThread().getContextClassLoader();
                if (tctxloader != null && tctxloader != thisClassClassLoader) {
                    bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("thread-class-loader: %s", tctxloader));
                    for (URL url : NReservedLangUtilsBoot.resolveClasspathURLs(tctxloader, false)) {
                        bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("                 %s", url));
                    }
                }
                ClassLoader contextClassLoader = getContextClassLoader();
                bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("ctx-class-loader: %s", contextClassLoader));
                if (contextClassLoader != null && contextClassLoader != thisClassClassLoader) {
                    for (URL url : NReservedLangUtilsBoot.resolveClasspathURLs(contextClassLoader, false)) {
                        bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("                 %s", url));
                    }
                }
                bLog.log(Level.CONFIG, "START", NMsgBoot.ofPlain("system-properties:"));
                Map<String, String> m = (Map) System.getProperties();
                int max = m.keySet().stream().mapToInt(String::length).max().getAsInt();
                for (String k : new TreeSet<String>(m.keySet())) {
                    bLog.log(Level.CONFIG, "START", NMsgBoot.ofC("    %s = %s", NStringUtilsBoot.formatAlign(k, max, NPositionTypeBoot.FIRST), NStringUtilsBoot.formatStringLiteral(m.get(k))));
                }
            }
            String workspaceName = null;
            NBootOptionsBoot lastConfigLoaded = null;
            String lastNutsWorkspaceJsonConfigPath = null;
            boolean immediateLocation = false;
            boolean resetFlag = NUtilsBoot.firstNonNull(computedOptions.getReset(),false);
            boolean dryFlag = NUtilsBoot.firstNonNull(computedOptions.getDry(),false);
            String _ws = computedOptions.getWorkspace();
            if (NNameFormatBoot.CONST_NAME.equals(isolationLevel, "SANDBOX")) {
                Path t = null;
                try {
                    t = Files.createTempDirectory("nuts-sandbox-" + Instant.now().toString().replace(':', '-'));
                } catch (IOException e) {
                    throw new NBootException(NMsgBoot.ofPlain("unable to create temporary/sandbox folder"), e);
                }
                lastNutsWorkspaceJsonConfigPath = t.toString();
                immediateLocation = true;
                workspaceName = t.getFileName().toString();
                resetFlag = false; //no need for reset
                if (NUtilsBoot.firstNonNull(computedOptions.getSystem(),false)) {
                    throw new NBootException(NMsgBoot.ofPlain("you cannot specify option '--global' in sandbox mode"));
                }
                if (!NStringUtilsBoot.isBlank(computedOptions.getWorkspace())) {
                    throw new NBootException(NMsgBoot.ofPlain("you cannot specify '--workspace' in sandbox mode"));
                }
                if (!NUtilsBoot.sameEnum(computedOptions.getStoreStrategy(),"STANDALONE")) {
                    throw new NBootException(NMsgBoot.ofPlain("you cannot specify '--exploded' in sandbox mode"));
                }
                if (NUtilsBoot.firstNonNull(computedOptions.getSystem(),false)) {
                    throw new NBootException(NMsgBoot.ofPlain("you cannot specify '--global' in sandbox mode"));
                }
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            } else {
                if (!NNameFormatBoot.equalsIgnoreFormat(isolationLevel,"SYSTEM") && NUtilsBoot.firstNonNull(userOptions.getSystem(),false)) {
                    if (NUtilsBoot.firstNonNull(userOptions.getReset(),false)) {
                        throw new NBootException(NMsgBoot.ofC("invalid option 'global' in %s mode", isolationLevel));
                    }
                }
                if (_ws != null && _ws.matches("[a-z-]+://.*")) {
                    //this is a protocol based workspace
                    //String protocol=ws.substring(0,ws.indexOf("://"));
                    workspaceName = "remote-bootstrap";
                    lastNutsWorkspaceJsonConfigPath = NPlatformHomeBoot.of(null, NUtilsBoot.firstNonNull(computedOptions.getSystem(),false)).getWorkspaceLocation(NUtilsBoot.resolveValidWorkspaceName(workspaceName));
                    lastConfigLoaded = NReservedBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                    immediateLocation = true;

                } else {
                    immediateLocation = NUtilsBoot.isValidWorkspaceName(_ws);
                    int maxDepth = 36;
                    for (int i = 0; i < maxDepth; i++) {
                        lastNutsWorkspaceJsonConfigPath = NUtilsBoot.isValidWorkspaceName(_ws) ? NPlatformHomeBoot.of(null, NUtilsBoot.firstNonNull(computedOptions.getSystem(),false)).getWorkspaceLocation(NUtilsBoot.resolveValidWorkspaceName(_ws)) : NReservedIOUtilsBoot.getAbsolutePath(_ws);

                        NBootOptionsBoot configLoaded = NReservedBootConfigLoader.loadBootConfig(lastNutsWorkspaceJsonConfigPath, bLog);
                        if (configLoaded == null) {
                            //not loaded
                            break;
                        }
                        if (NStringUtilsBoot.isBlank(configLoaded.getWorkspace())) {
                            lastConfigLoaded = configLoaded;
                            break;
                        }
                        _ws = configLoaded.getWorkspace();
                        if (i >= maxDepth - 1) {
                            throw new NBootException(NMsgBoot.ofPlain("cyclic workspace resolution"));
                        }
                    }
                    workspaceName = NUtilsBoot.resolveValidWorkspaceName(computedOptions.getWorkspace());
                }
            }
            computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
            if (lastConfigLoaded != null) {
                computedOptions.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                computedOptions.setName(lastConfigLoaded.getName());
                computedOptions.setUuid(lastConfigLoaded.getUuid());
                NBootOptionsBoot curr;
                if (!resetFlag) {
                    curr = computedOptions;
                } else {
                    lastWorkspaceOptions = new NBootOptionsBoot();
                    curr = lastWorkspaceOptions;
                    curr.setWorkspace(lastNutsWorkspaceJsonConfigPath);
                    curr.setName(lastConfigLoaded.getName());
                    curr.setUuid(lastConfigLoaded.getUuid());
                }
                curr.setBootRepositories(lastConfigLoaded.getBootRepositories());
                curr.setJavaCommand(lastConfigLoaded.getJavaCommand());
                curr.setJavaOptions(lastConfigLoaded.getJavaOptions());
                curr.setExtensionsSet(NReservedLangUtilsBoot.nonNullSet(lastConfigLoaded.getExtensionsSet()));
                curr.setStoreStrategy(lastConfigLoaded.getStoreStrategy());
                curr.setRepositoryStoreStrategy(lastConfigLoaded.getRepositoryStoreStrategy());
                curr.setStoreLayout(lastConfigLoaded.getStoreLayout());
                curr.setStoreLocations(NReservedLangUtilsBoot.nonNullMap(lastConfigLoaded.getStoreLocations()));
                curr.setHomeLocations(NReservedLangUtilsBoot.nonNullMap(lastConfigLoaded.getHomeLocations()));
            }
            revalidateLocations(computedOptions, workspaceName, immediateLocation, isolationLevel);
            long countDeleted = 0;
            //now that config is prepared proceed to any cleanup
            if (resetFlag) {
                //force loading version early, it will be used later-on
                if (lastWorkspaceOptions != null) {
                    revalidateLocations(lastWorkspaceOptions, workspaceName, immediateLocation, isolationLevel);
                    if (dryFlag) {
                        bLog.log(Level.INFO, "DEBUG", NMsgBoot.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, "WARNING", NMsgBoot.ofPlain("reset workspace"));
                        getFallbackCache(NIdBoot.RUNTIME_ID, true, true);
                        countDeleted = NReservedIOUtilsBoot.deleteStoreLocations(lastWorkspaceOptions, getOptions(), true, bLog, NPlatformHomeBoot.storeTypes(), () -> scanner.nextLine());
                        NUtilsBoot.ndiUndo(bLog);
                    }
                } else {
                    if (dryFlag) {
                        bLog.log(Level.INFO, "DEBUG", NMsgBoot.ofPlain("[dry] [reset] delete ALL workspace folders and configurations"));
                    } else {
                        bLog.log(Level.CONFIG, "WARNING", NMsgBoot.ofPlain("reset workspace"));
                        getFallbackCache(NIdBoot.RUNTIME_ID, false, true);
                        countDeleted = NReservedIOUtilsBoot.deleteStoreLocations(computedOptions, getOptions(), true, bLog, NPlatformHomeBoot.storeTypes(), () -> scanner.nextLine());
                        NUtilsBoot.ndiUndo(bLog);
                    }
                }
            } else if (NUtilsBoot.firstNonNull(computedOptions.getRecover(),false)) {
                if (dryFlag) {
                    bLog.log(Level.INFO, "DEBUG", NMsgBoot.ofPlain("[dry] [recover] delete CACHE/TEMP workspace folders"));
                } else {
                    bLog.log(Level.CONFIG, "WARNING", NMsgBoot.ofPlain("recover workspace."));
                    List<Object> folders = new ArrayList<>();
                    folders.add("CACHE");
                    folders.add("TEMP");
                    //delete nuts.jar and nuts-runtime.jar in the lib folder. They will be re-downloaded.
                    String p = NReservedIOUtilsBoot.getStoreLocationPath(computedOptions, "LIB");
                    if (p != null) {
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts"));
                        folders.add(Paths.get(p).resolve("id/net/thevpc/nuts/nuts-runtime"));
                    }
                    countDeleted = NReservedIOUtilsBoot.deleteStoreLocations(computedOptions, getOptions(), false, bLog, folders.toArray(), () -> scanner.nextLine());
                }
            }
            if (computedOptions.getExtensionsSet()==null) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setExtensionsSet(NUtilsBoot.firstNonNull(lastWorkspaceOptions.getExtensionsSet(),Collections.emptySet()));
                } else {
                    computedOptions.setExtensionsSet(Collections.emptySet());
                }
            }
            if (computedOptions.getHomeLocations()==null) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setHomeLocations(NUtilsBoot.firstNonNull(lastWorkspaceOptions.getHomeLocations(),Collections.emptyMap()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }
            if (computedOptions.getStoreLayout()==null) {
                if (lastWorkspaceOptions != null && !resetFlag) {
                    computedOptions.setStoreLayout(NUtilsBoot.firstNonNull(lastWorkspaceOptions.getStoreLayout(),NPlatformHomeBoot.currentOsFamily()));
                } else {
                    computedOptions.setHomeLocations(Collections.emptyMap());
                }
            }

            //if recover or reset mode with -Q option (SkipBoot)
            //as long as there are no applications to run, will exit before creating workspace
            if (computedOptions.getApplicationArguments().size() == 0 && NUtilsBoot.firstNonNull(computedOptions.getSkipBoot(),false) && (NUtilsBoot.firstNonNull(computedOptions.getRecover(),false) || resetFlag)) {
                if (isPlainTrace()) {
                    if (countDeleted > 0) {
                        bLog.log(Level.WARNING, "WARNING", NMsgBoot.ofC("workspace erased : %s", computedOptions.getWorkspace()));
                    } else {
                        bLog.log(Level.WARNING, "WARNING", NMsgBoot.ofC("workspace is not erased because it does not exist : %s", computedOptions.getWorkspace()));
                    }
                }
                throw new NBootException(NMsgBoot.ofPlain(""), 0);
            }
            //after eventual clean up
            if (NUtilsBoot.firstNonNull(computedOptions.getInherited(),false)) {
                //when Inherited, always use the current Api version!
                computedOptions.setApiVersion(getVersion().toString());
            } else {
                NVersionBoot nutsVersion = NVersionBoot.of(computedOptions.getApiVersion());
                if (nutsVersion.isLatestVersion() || nutsVersion.isReleaseVersion()) {
                    NIdBoot s = NReservedMavenUtilsBoot.resolveLatestMavenId(NIdBoot.ofApi(""), null, bLog, resolveBootRuntimeRepositories(true), computedOptions);
                    if (s == null) {
                        throw new NBootException(NMsgBoot.ofPlain("unable to load latest nuts version"));
                    }
                    computedOptions.setApiVersion(s.getVersion());
                }
                if (nutsVersion.isBlank()) {
                    computedOptions.setApiVersion(getVersion().toString());
                }
            }

            NIdBoot bootApiId = NIdBoot.ofApi(computedOptions.getApiVersion());
            Path nutsApiConfigBootPath = Paths.get(computedOptions.getStoreType("CONF") + File.separator + NConstants.Folders.ID).resolve(NIdUtilsBoot.resolveIdPath(bootApiId)).resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
            boolean loadedApiConfig = false;

            //This is not cache, but still, if recover or reset, config will be ignored!
            if (isLoadFromCache() && NReservedIOUtilsBoot.isFileAccessible(nutsApiConfigBootPath, computedOptions.getExpireTime(), bLog)) {
                try {
                    Map<String, Object> obj = NReservedJsonParser.parse(nutsApiConfigBootPath);
                    if (!obj.isEmpty()) {
                        bLog.log(Level.CONFIG, "READ", NMsgBoot.ofC("loaded %s file : %s", nutsApiConfigBootPath.getFileName(), nutsApiConfigBootPath.toString()));
                        loadedApiConfig = true;
                        if (computedOptions.getRuntimeId()==null) {
                            String runtimeId = (String) obj.get("runtimeId");
                            if (NStringUtilsBoot.isBlank(runtimeId)) {
                                bLog.log(Level.CONFIG, "FAIL", NMsgBoot.ofC("%s does not contain runtime-id", nutsApiConfigBootPath));
                            }
                            computedOptions.setRuntimeId(runtimeId);
                        }
                        if (computedOptions.getJavaCommand()==null) {
                            computedOptions.setJavaCommand((String) obj.get("javaCommand"));
                        }
                        if (computedOptions.getJavaOptions()==null) {
                            computedOptions.setJavaOptions((String) obj.get("javaOptions"));
                        }
                    }
                } catch (UncheckedIOException e) {
                    bLog.log(Level.CONFIG, "READ", NMsgBoot.ofC("unable to read %s : %s", nutsApiConfigBootPath, e));
                }
            }
            if (!loadedApiConfig || computedOptions.getRuntimeId()==null || computedOptions.getRuntimeBootDescriptor()==null || computedOptions.getExtensionBootDescriptors()==null || computedOptions.getBootRepositories()==null) {

                NVersionBoot apiVersion = NVersionBoot.of(computedOptions.getApiVersion());
                if (isRuntimeLoaded() && (apiVersion.isBlank() || getVersion().equals(apiVersion))) {
                    if (computedOptions.getRuntimeId()==null) {
                        computedOptions.setRuntimeId(runtimeLoadedId==null?null:runtimeLoadedId.toString());
                        computedOptions.setRuntimeBootDescriptor(null);
                    }
                }
                //resolve runtime id
                if (computedOptions.getRuntimeId()==null) {
                    //load from local lib folder
                    NIdBoot runtimeId = null;
                    if (!resetFlag && !NUtilsBoot.firstNonNull(computedOptions.getRecover(),false)) {
                        runtimeId = NReservedMavenUtilsBoot.resolveLatestMavenId(NIdBoot.of(NConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, Collections.singletonList(NRepositoryLocationBoot.of("nuts@" + computedOptions.getStoreType("LIB") + File.separatorChar + NConstants.Folders.ID)), computedOptions);
                    }
                    if (runtimeId == null) {
                        runtimeId = NReservedMavenUtilsBoot.resolveLatestMavenId(NIdBoot.of(NConstants.Ids.NUTS_RUNTIME), (rtVersion) -> rtVersion.getValue().startsWith(apiVersion + "."), bLog, resolveBootRuntimeRepositories(true), computedOptions);
                    }
                    if (runtimeId == null) {
                        runtimeId = getFallbackCache(NIdBoot.RUNTIME_ID, false, false).id;
                    }
                    if (runtimeId == null) {
                        bLog.log(Level.FINEST, "FAIL", NMsgBoot.ofPlain("unable to resolve latest runtime-id version (is connection ok?)"));
                    }
                    computedOptions.setRuntimeId(runtimeId==null?null:runtimeId.toString());
                    computedOptions.setRuntimeBootDescriptor(null);
                }
                if (computedOptions.getRuntimeId()==null) {
                    computedOptions.setRuntimeId((resolveDefaultRuntimeId(computedOptions.getApiVersion())));
                    bLog.log(Level.CONFIG, "READ", NMsgBoot.ofC("consider default runtime-id : %s", computedOptions.getRuntimeId()));
                }
                NIdBoot runtimeIdObject = NIdBoot.of(computedOptions.getRuntimeId());
                if (NStringUtilsBoot.isBlank(runtimeIdObject.getVersion())) {
                    computedOptions.setRuntimeId(resolveDefaultRuntimeId(computedOptions.getApiVersion()));
                }

                //resolve runtime libraries
                if (computedOptions.getRuntimeBootDescriptor()==null && !isRuntimeLoaded()) {
                    Set<NIdBoot> loadedDeps = null;
                    String rid = computedOptions.getRuntimeId();
                    Path nutsRuntimeCacheConfigPath = Paths.get(computedOptions.getStoreType("CONF") + File.separator + NConstants.Folders.ID).resolve(NIdUtilsBoot.resolveIdPath(bootApiId)).resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
                    try {
                        boolean cacheLoaded = false;
                        if (!NUtilsBoot.firstNonNull(computedOptions.getRecover(),false) && !resetFlag && NReservedIOUtilsBoot.isFileAccessible(nutsRuntimeCacheConfigPath, computedOptions.getExpireTime(), bLog)) {
                            try {
                                Map<String, Object> obj = NReservedJsonParser.parse(nutsRuntimeCacheConfigPath);
                                bLog.log(Level.CONFIG, "READ", NMsgBoot.ofC("loaded %s file : %s", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString()));
                                loadedDeps = NIdBoot.ofSet((String) obj.get("dependencies"));
                                if(loadedDeps==null){
                                    loadedDeps=new LinkedHashSet<>();
                                }
                            } catch (Exception ex) {
                                bLog.log(Level.FINEST, "FAIL", NMsgBoot.ofC("unable to load %s file : %s : %s", nutsRuntimeCacheConfigPath.getFileName(), nutsRuntimeCacheConfigPath.toString(), ex.toString()));
                                //ignore...
                            }
                            cacheLoaded = true;
                        }

                        if (!cacheLoaded || loadedDeps == null) {
                            loadedDeps = NReservedMavenUtilsBoot.loadDependenciesFromId(NIdBoot.of(computedOptions.getRuntimeId()), bLog, resolveBootRuntimeRepositories(false), cache);
                            bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofC("detect runtime dependencies : %s", loadedDeps));
                        }
                    } catch (Exception ex) {
                        bLog.log(Level.FINEST, "FAIL", NMsgBoot.ofC("unable to load %s file : %s", nutsRuntimeCacheConfigPath.getFileName(), ex.toString()));
                        //
                    }
                    if (loadedDeps == null) {
                        NIdCache r = getFallbackCache(NIdBoot.RUNTIME_ID, false, false);
                        loadedDeps = r.deps;
                    }

                    if (loadedDeps == null) {
                        throw new NBootException(NMsgBoot.ofC("unable to load dependencies for %s", rid));
                    }
                    computedOptions.setRuntimeBootDescriptor(new NDescriptorBoot().setId(computedOptions.getRuntimeId()).setDependencies(loadedDeps.stream().map(NIdBoot::toDependency).collect(Collectors.toList())));
                    Set<NRepositoryLocationBoot> bootRepositories = resolveBootRuntimeRepositories(false);
                    if (bLog.isLoggable(Level.CONFIG)) {
                        if (bootRepositories.size() == 0) {
                            bLog.log(Level.CONFIG, "FAIL", NMsgBoot.ofPlain("workspace bootRepositories could not be resolved"));
                        } else if (bootRepositories.size() == 1) {
                            bLog.log(Level.CONFIG, "INFO", NMsgBoot.ofC("workspace bootRepositories resolved to : %s", bootRepositories.toArray()[0]));
                        } else {
                            bLog.log(Level.CONFIG, "INFO", NMsgBoot.ofPlain("workspace bootRepositories resolved to : "));
                            for (NRepositoryLocationBoot repository : bootRepositories) {
                                bLog.log(Level.CONFIG, "INFO", NMsgBoot.ofC("    %s", repository));
                            }
                        }
                    }
                    computedOptions.setBootRepositories(bootRepositories.stream().map(NRepositoryLocationBoot::toString).collect(Collectors.joining(";")));
                }

                //resolve extension libraries
                if (computedOptions.getExtensionBootDescriptors()==null) {
                    LinkedHashSet<String> excludedExtensions = new LinkedHashSet<>();
                    if (computedOptions.getExcludedExtensions()!=null) {
                        for (String excludedExtensionGroup : computedOptions.getExcludedExtensions()) {
                            for (String excludedExtension : NStringUtilsBoot.split(excludedExtensionGroup, ";,", true, true)) {
                                excludedExtensions.add(NIdBoot.of(excludedExtension).getShortName());
                            }
                        }
                    }
                    if (computedOptions.getExtensionsSet()!=null) {
                        List<NDescriptorBoot> all = new ArrayList<>();
                        for (String extension : computedOptions.getExtensionsSet()) {
                            NIdBoot eid = NIdBoot.of(extension);
                            if (!excludedExtensions.contains(eid.getShortName()) && !excludedExtensions.contains(eid.getArtifactId())) {
                                Path extensionFile = Paths.get(computedOptions.getStoreType("CONF") + File.separator + NConstants.Folders.ID).resolve(NIdUtilsBoot.resolveIdPath(bootApiId)).resolve(NConstants.Files.EXTENSION_BOOT_CONFIG_FILE_NAME);
                                Set<NIdBoot> loadedDeps = null;
                                if (isLoadFromCache() && NReservedIOUtilsBoot.isFileAccessible(extensionFile, computedOptions.getExpireTime(), bLog)) {
                                    try {
                                        Properties obj = NReservedIOUtilsBoot.loadURLProperties(extensionFile, bLog);
                                        bLog.log(Level.CONFIG, "READ", NMsgBoot.ofC("loaded %s file : %s", extensionFile.getFileName(), extensionFile.toString()));
                                        List<NIdBoot> loadedDeps0 = NIdBoot.ofList((String) obj.get("dependencies"));
                                        loadedDeps = loadedDeps0==null?new LinkedHashSet<>():new LinkedHashSet<>(loadedDeps0);
                                    } catch (Exception ex) {
                                        bLog.log(Level.CONFIG, "FAIL", NMsgBoot.ofC("unable to load %s file : %s : %s", extensionFile.getFileName(), extensionFile.toString(), ex.toString()));
                                        //ignore
                                    }
                                }
                                if (loadedDeps == null) {
                                    loadedDeps = NReservedMavenUtilsBoot.loadDependenciesFromId(eid, bLog, resolveBootRuntimeRepositories(true), cache);
                                }
                                if (loadedDeps == null) {
                                    throw new NBootException(NMsgBoot.ofC("unable to load dependencies for %s", eid));
                                }
                                all.add(new NDescriptorBoot().setId(NIdBoot.of(extension)).setDependencies(loadedDeps.stream().map(NIdBoot::toDependency).collect(Collectors.toList())));
                            }
                        }
                        computedOptions.setExtensionBootDescriptors(all);
                    } else {
                        computedOptions.setExtensionBootDescriptors(new ArrayList<>());
                    }
                }
            }
            newInstanceRequirements = checkRequirements(true);
            if (newInstanceRequirements == 0) {
                computedOptions.setJavaCommand(null);
                computedOptions.setJavaOptions(null);
            }
            return true;
        }
        return false;
    }

    private boolean isPlainTrace() {
        return NUtilsBoot.firstNonNull(computedOptions.getTrace(),true)
                && !NUtilsBoot.firstNonNull(computedOptions.getBot(),false)
                && (NUtilsBoot.sameEnum(computedOptions.getOutputFormat(),"PLAIN") || NStringUtilsBoot.isBlank(computedOptions.getOutputFormat()));
    }

    private boolean isLoadFromCache() {
        return !NUtilsBoot.firstNonNull(computedOptions.getRecover(),false) && !NUtilsBoot.firstNonNull(computedOptions.getReset(),false);
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     *
     * @return NWorkspace instance as object
     */
    public Object openWorkspace() {
        return openOrRunWorkspace(false);
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     *
     * @return NWorkspace instance as object
     */
    private Object openOrRunWorkspace(boolean run) {
        prepareWorkspace();
        if (hasUnsatisfiedRequirements()) {
            throw new NUnsatisfiedWorkspaceRequirementsException(NMsgBoot.ofC("unable to open a distinct version : %s from nuts#%s", getRequirementsHelpString(true), getVersion()));
        }
        //if recover or reset mode with -K option (SkipWelcome)
        //as long as there are no applications to run, will exit before creating workspace
        if (computedOptions.getApplicationArguments().size() == 0 && NUtilsBoot.firstNonNull(computedOptions.getSkipBoot(),false) && (NUtilsBoot.firstNonNull(computedOptions.getRecover(),false) || NUtilsBoot.firstNonNull(computedOptions.getReset(),false))) {
            if (isPlainTrace()) {
                bLog.log(Level.WARNING, "WARNING", NMsgBoot.ofC("workspace erased : %s", computedOptions.getWorkspace()));
            }
            throw new NBootException(null, 0);
        }
        URL[] bootClassWorldURLs = null;
        ClassLoader workspaceClassLoader;
        Object wsInstance = null;
        NReservedErrorInfoList errorList = new NReservedErrorInfoList();
        try {
            Path configFile = Paths.get(computedOptions.getWorkspace()).resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            if (NUtilsBoot.sameEnum(computedOptions.getOpenMode() ,"OPEN_OR_ERROR")) {
                //add fail fast test!!
                if (!Files.isRegularFile(configFile)) {
                    throw new NWorkspaceNotFoundException(computedOptions.getWorkspace());
                }
            } else if (NUtilsBoot.sameEnum(computedOptions.getOpenMode(),"CREATE_OR_ERROR")) {
                if (Files.exists(configFile)) {
                    throw new NWorkspaceAlreadyExistsException(computedOptions.getWorkspace());
                }
            }
            if (NStringUtilsBoot.isBlank(computedOptions.getApiVersion())
                    || NStringUtilsBoot.isBlank(computedOptions.getRuntimeId())
                    || (!isRuntimeLoaded() && computedOptions.getRuntimeBootDescriptor()==null)
                    || computedOptions.getExtensionBootDescriptors()==null
//                    || (!runtimeLoaded && (computedOptions.getBootRepositories().isBlank()))
            ) {
                throw new NBootException(NMsgBoot.ofPlain("invalid workspace state"));
            }
            boolean recover = NUtilsBoot.firstNonNull(computedOptions.getRecover(),false) || NUtilsBoot.firstNonNull(computedOptions.getReset(),false);

            List<NClassLoaderNode> deps = new ArrayList<>();

            String workspaceBootLibFolder = computedOptions.getStoreType("LIB") + File.separator + NConstants.Folders.ID;

            NRepositoryLocationBoot[] repositories = NStringUtilsBoot.split(computedOptions.getBootRepositories(), "\n;", true, true).stream().map(NRepositoryLocationBoot::of).toArray(NRepositoryLocationBoot[]::new);

            NRepositoryLocationBoot workspaceBootLibFolderRepo = NRepositoryLocationBoot.of("nuts@" + workspaceBootLibFolder);
            computedOptions.setRuntimeBootDependencyNode(
                    isRuntimeLoaded() ? null :
                            createClassLoaderNode(computedOptions.getRuntimeBootDescriptor(), repositories, workspaceBootLibFolderRepo, recover, errorList, true)
            );

            if(computedOptions.getExtensionBootDescriptors()!=null) {
                for (NDescriptorBoot nutsBootDescriptor : computedOptions.getExtensionBootDescriptors()) {
                    deps.add(createClassLoaderNode(nutsBootDescriptor, repositories, workspaceBootLibFolderRepo, recover, errorList, false));
                }
            }
            computedOptions.setExtensionBootDependencyNodes(deps);
            deps.add(0, computedOptions.getRuntimeBootDependencyNode());

            bootClassWorldURLs = NReservedLangUtilsBoot.resolveClassWorldURLs(deps.toArray(new NClassLoaderNode[0]), getContextClassLoader(), bLog);
            workspaceClassLoader = /*bootClassWorldURLs.length == 0 ? getContextClassLoader() : */ new NReservedBootClassLoader(deps.toArray(new NClassLoaderNode[0]), getContextClassLoader());
            computedOptions.setClassWorldLoader(workspaceClassLoader);
            if (bLog.isLoggable(Level.CONFIG)) {
                if (bootClassWorldURLs.length == 0) {
                    bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofPlain("empty nuts class world. All dependencies are already loaded in classpath, most likely"));
                } else if (bootClassWorldURLs.length == 1) {
                    bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofC("resolve nuts class world to : %s %s", NReservedIOUtilsBoot.getURLDigest(bootClassWorldURLs[0], bLog), bootClassWorldURLs[0]));
                } else {
                    bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofPlain("resolve nuts class world to : "));
                    for (URL u : bootClassWorldURLs) {
                        bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofC("    %s : %s", NReservedIOUtilsBoot.getURLDigest(u, bLog), u));
                    }
                }
            }
            computedOptions.setClassWorldURLs(Arrays.asList(bootClassWorldURLs));
            bLog.log(Level.CONFIG, "INFO", NMsgBoot.ofPlain("search for NutsBootWorkspaceFactory service implementations"));
            ServiceLoader<NBootWorkspaceFactory> serviceLoader = ServiceLoader.load(NBootWorkspaceFactory.class, workspaceClassLoader);
            List<NBootWorkspaceFactory> factories = new ArrayList<>(5);
            for (NBootWorkspaceFactory a : serviceLoader) {
                factories.add(a);
            }
            factories.sort(new NReservedBootWorkspaceFactoryComparator(computedOptions));
            if (bLog.isLoggable(Level.CONFIG)) {
                switch (factories.size()) {
                    case 0: {
                        bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofPlain("unable to detect NutsBootWorkspaceFactory service implementations"));
                        break;
                    }
                    case 1: {
                        bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofC("detect NutsBootWorkspaceFactory service implementation : %s", factories.get(0).getClass().getName()));
                        break;
                    }
                    default: {
                        bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofPlain("detect NutsBootWorkspaceFactory service implementations are :"));
                        for (NBootWorkspaceFactory u : factories) {
                            bLog.log(Level.CONFIG, "SUCCESS", NMsgBoot.ofC("    %s", u.getClass().getName()));
                        }
                    }
                }
            }
            NBootWorkspaceFactory factoryInstance;
            List<Throwable> exceptions = new ArrayList<>();
            for (NBootWorkspaceFactory a : factories) {
                factoryInstance = a;
                try {
                    if (bLog.isLoggable(Level.CONFIG)) {
                        bLog.log(Level.CONFIG, "INFO", NMsgBoot.ofC("create workspace using %s", factoryInstance.getClass().getName()));
                    }
                    computedOptions.setBootWorkspaceFactory(factoryInstance);
                    if (run) {
                        wsInstance = a.runWorkspace(computedOptions);
                    } else {
                        wsInstance = a.createWorkspace(computedOptions);
                    }
                } catch (UnsatisfiedLinkError | Exception ex) {
                    exceptions.add(ex);
                    bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("unable to create workspace using factory %s", a), ex);
                    // if the creation generates an error
                    // just stop
                    break;
                }
                if (wsInstance != null) {
                    break;
                }
            }
            if (wsInstance == null) {
                //should never happen
                bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("unable to load Workspace \"%s\" from ClassPath :", computedOptions.getName()));
                for (URL url : bootClassWorldURLs) {
                    bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("\t %s", NReservedIOUtilsBoot.formatURL(url)));
                }
                for (Throwable exception : exceptions) {
                    bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("%s", exception), exception);
                }
                bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("unable to load Workspace Component from ClassPath : %s", Arrays.asList(bootClassWorldURLs)));
                throw new NInvalidWorkspaceException(this.computedOptions.getWorkspace(), NMsgBoot.ofC("unable to load Workspace Component from ClassPath : %s%n  caused by:%n\t%s", Arrays.asList(bootClassWorldURLs), exceptions.stream().map(Throwable::toString).collect(Collectors.joining("\n\t"))));
            }
            return wsInstance;
//        } catch (NReadOnlyException | NCancelException | NNoSessionCancelException ex) {
//            throw ex;
        } catch (UnsatisfiedLinkError | AbstractMethodError ex) {
            NMsgBoot errorMessage = NMsgBoot.ofC("unable to boot nuts workspace because the installed binaries are incompatible with the current nuts bootstrap version %s\nusing '-N' command line flag should fix the problem", getVersion());
            errorList.insert(0, new NReservedErrorInfo(null, null, null, errorMessage + ": " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            throw new NBootException(errorMessage, ex);
        } catch (Throwable ex) {
            NMsgBoot message = NMsgBoot.ofPlain("unable to locate valid nuts-runtime package");
            errorList.insert(0, new NReservedErrorInfo(null, null, null, message + " : " + ex, ex));
            logError(bootClassWorldURLs, errorList);
            if (ex instanceof NBootException) {
                throw (NBootException) ex;
            }
            throw new NBootException(message, ex);
        }
    }

    private ClassLoader getContextClassLoader() {
        Supplier<ClassLoader> classLoaderSupplier = computedOptions.getClassLoaderSupplier();
        if(classLoaderSupplier!=null){
            ClassLoader classLoader = classLoaderSupplier.get();
            if(classLoader!=null) {
                return classLoader;
            }
        }
        return Thread.currentThread().getContextClassLoader();
    }

    private void runCommandHelp() {
        String f = NUtilsBoot.firstNonNull(computedOptions.getOutputFormat(),"PLAIN");
        if (NUtilsBoot.firstNonNull(computedOptions.getDry(),false)) {
            printDryCommand("help");
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (NUtilsBoot.enumName(f)) {
                case "JSON": {
                    bLog.outln("{");
                    bLog.outln("  \"help\": \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case "TSON": {
                    bLog.outln("{");
                    bLog.outln("  help: \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case "YAML": {
                    bLog.outln("help: %s", msg);
                    return;
                }
                case "TREE": {
                    bLog.outln("- help: %s", msg);
                    return;
                }
                case "TABLE": {
                    bLog.outln("help  %s", msg);
                    return;
                }
                case "XML": {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<string>");
                    bLog.outln(" %s", msg);
                    bLog.outln("</string>");
                    return;
                }
                case "PROPS": {
                    bLog.outln("help=%s", msg);
                    return;
                }
            }
            bLog.outln("%s", msg);
        }
    }

    private void printDryCommand(String cmd) {
        String f = NUtilsBoot.firstNonNull(computedOptions.getOutputFormat(),"PLAIN");
        if (NUtilsBoot.firstNonNull(computedOptions.getDry(),false)) {
            switch (NUtilsBoot.enumName(f)) {
                case "JSON": {
                    bLog.outln("{");
                    bLog.outln("  \"dryCommand\": \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case "TSON": {
                    bLog.outln("{");
                    bLog.outln("  dryCommand: \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case "YAML": {
                    bLog.outln("dryCommand: %s", cmd);
                    return;
                }
                case "TREE": {
                    bLog.outln("- dryCommand: %s", cmd);
                    return;
                }
                case "TABLE": {
                    bLog.outln("dryCommand  %s", cmd);
                    return;
                }
                case "XML": {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<object>");
                    bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "dryCommand", cmd);
                    bLog.outln("</object>");
                    return;
                }
                case "PROPS": {
                    bLog.outln("dryCommand=%s", cmd);
                    return;
                }
            }
            bLog.outln("[Dry] %s", getVersion());
        }
    }

    private void runCommandVersion() {
        String f = NUtilsBoot.firstNonNull(computedOptions.getOutputFormat(),"PLAIN");
        if (NUtilsBoot.firstNonNull(computedOptions.getDry(),false)) {
            printDryCommand("version");
            return;
        }
        switch (NUtilsBoot.enumName(f)) {
            case "JSON": {
                bLog.outln("{");
                bLog.outln("  \"version\": \"%s\",", getVersion());
                bLog.outln("  \"digest\": \"%s\"", getApiDigestOrInternal());
                bLog.outln("}");
                return;
            }
            case "TSON": {
                bLog.outln("{");
                bLog.outln("  version: \"%s\",", getVersion());
                bLog.outln("  digest: \"%s\"", getApiDigestOrInternal());
                bLog.outln("}");
                return;
            }
            case "YAML": {
                bLog.outln("version: %s", getVersion());
                bLog.outln("digest: %s", getApiDigestOrInternal());
                return;
            }
            case "TREE": {
                bLog.outln("- version: %s", getVersion());
                bLog.outln("- digest: %s", getApiDigestOrInternal());
                return;
            }
            case "TABLE": {
                bLog.outln("version      %s", getVersion());
                bLog.outln("digest  %s", getApiDigestOrInternal());
                return;
            }
            case "XML": {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("<object>");
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "version", getVersion());
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", getApiDigestOrInternal());
                bLog.outln("</object>");
                return;
            }
            case "PROPS": {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("version=%s", getVersion());
                bLog.outln("digest=%s", getApiDigestOrInternal());
                bLog.outln("</object>");
                return;
            }
        }
        bLog.outln("%s", getVersion());
    }

    /**
     * return type is Object to remove static dependency on NWorkspace class
     * so than versions of API can evolve independently of Boot
     *
     * @return NWorkspace instance as object
     */
    public Object runWorkspace() {
        if (NUtilsBoot.firstNonNull(computedOptions.getCommandHelp(),false)) {
            runCommandHelp();
            return null;
        } else if (NUtilsBoot.firstNonNull(computedOptions.getCommandVersion(),false)) {
            runCommandVersion();
            return null;
        }
        if (hasUnsatisfiedRequirements()) {
            runNewProcess();
            return null;
        }
        return this.openOrRunWorkspace(true);
    }

    private void fallbackInstallActionUnavailable(String message) {
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain(message));
    }

    private void logError(URL[] bootClassWorldURLs, NReservedErrorInfoList ths) {
        String workspace = computedOptions.getWorkspace();
        Map<String, String> rbc_locations = computedOptions.getStoreLocations();
        if(rbc_locations==null){
            rbc_locations=new HashMap<>();
        }
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("unable to bootstrap nuts (digest %s):", getApiDigestOrInternal()));
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("%s", ths.list().get(0)));
        }
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("here after current environment info:"));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-boot-api-version            : %s", NUtilsBoot.firstNonNull(computedOptions.getApiVersion(),"<?> Not Found!")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-boot-runtime                : %s", NUtilsBoot.firstNonNull(computedOptions.getRuntimeId(),"<?> Not Found!")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-boot-repositories           : %s", NUtilsBoot.firstNonNull(computedOptions.getBootRepositories(),"<?> Not Found!")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  workspace-location               : %s", NUtilsBoot.firstNonNull(workspace,"<default-location>")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-bin                   : %s", rbc_locations.get("BIN")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-conf                  : %s", rbc_locations.get("CONF")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-var                   : %s", rbc_locations.get("VAR")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-log                   : %s", rbc_locations.get("LOG")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-temp                  : %s", rbc_locations.get("TEMP")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-cache                 : %s", rbc_locations.get("CACHE")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-run                   : %s", rbc_locations.get("RUN")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-lib                   : %s", rbc_locations.get("LIB")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-strategy              : %s", NUtilsBoot.desc(computedOptions.getStoreStrategy())));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-store-layout                : %s", NUtilsBoot.desc(computedOptions.getStoreLayout())));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-boot-args                   : %s", asCmdLine(this.computedOptions,null)));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-app-args                    : %s", this.computedOptions.getApplicationArguments()));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  option-read-only                 : %s", NUtilsBoot.firstNonNull(this.computedOptions.getReadOnly(),false)));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  option-trace                     : %s", NUtilsBoot.firstNonNull(this.computedOptions.getTrace(),false)));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  option-progress                  : %s", NUtilsBoot.desc(this.computedOptions.getProgressOptions())));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  option-open-mode                 : %s", NUtilsBoot.desc(NUtilsBoot.firstNonNull(this.computedOptions.getOpenMode(),"OPEN_OR_CREATE"))));

        NClassLoaderNode rtn = this.computedOptions.getRuntimeBootDependencyNode();
        String rtHash = "";
        if (rtn != null) {
            rtHash = NReservedIOUtilsBoot.getURLDigest(rtn.getURL(), bLog);
        }
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-runtime-digest                : %s", rtHash));

        if (bootClassWorldURLs == null || bootClassWorldURLs.length == 0) {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-runtime-classpath           : %s", "<none>"));
        } else {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-runtime-hash                : %s", "<none>"));
            for (int i = 0; i < bootClassWorldURLs.length; i++) {
                URL bootClassWorldURL = bootClassWorldURLs[i];
                if (i == 0) {
                    bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  nuts-runtime-classpath           : %s", NReservedIOUtilsBoot.formatURL(bootClassWorldURL)));
                } else {
                    bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("                                     %s", NReservedIOUtilsBoot.formatURL(bootClassWorldURL)));
                }
            }
        }
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  java-version                     : %s", System.getProperty("java.version")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  java-executable                  : %s", NUtilsBoot.resolveJavaCommand(null)));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  java-class-path                  : %s", System.getProperty("java.class.path")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  java-library-path                : %s", System.getProperty("java.library.path")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  os-name                          : %s", System.getProperty("os.name")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  os-arch                          : %s", System.getProperty("os.arch")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  os-version                       : %s", System.getProperty("os.version")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  user-name                        : %s", System.getProperty("user.name")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  user-home                        : %s", System.getProperty("user.home")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC("  user-dir                         : %s", System.getProperty("user.dir")));
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain(""));
        if (this.computedOptions.getLogConfig().getLogTermLevel() == null || this.computedOptions.getLogConfig().getLogFileLevel().intValue() > Level.FINEST.intValue()) {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("If the problem persists you may want to get more debug info by adding '--verbose' arguments."));
        }
        if (!NUtilsBoot.firstNonNull(this.computedOptions.getReset(),false) && !NUtilsBoot.firstNonNull(this.computedOptions.getRecover(),false) && this.computedOptions.getExpireTime()==null) {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("You may also enable recover mode to ignore existing cache info with '--recover' and '--expire' arguments."));
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("  java -jar nuts.jar --verbose --recover --expire [...]"));
        } else if (!NUtilsBoot.firstNonNull(this.computedOptions.getReset(),false) && NUtilsBoot.firstNonNull(this.computedOptions.getRecover(),false) && this.computedOptions.getExpireTime()==null) {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("You may also enable full reset mode to ignore existing configuration with '--reset' argument."));
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("ATTENTION: this will delete all your nuts configuration. Use it at your own risk."));
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("Here is the proper command : "));
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("  java -jar nuts.jar --verbose --reset [...]"));
        }
        if (!ths.list().isEmpty()) {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("error stack trace is:"));
            for (NReservedErrorInfo th : ths.list()) {
                StringBuilder msg = new StringBuilder();
                List<Object> msgParams = new ArrayList<>();
                msg.append("[error]");
                if (th.getNutsId() != null) {
                    msg.append(" <id>=%s");
                    msgParams.add(th.getNutsId());
                }
                if (th.getRepository() != null) {
                    msg.append(" <repo>=%s");
                    msgParams.add(th.getRepository());
                }
                if (th.getUrl() != null) {
                    msg.append(" <url>=%s");
                    msgParams.add(th.getUrl());
                }
                if (th.getThrowable() != null) {
                    msg.append(" <error>=%s");
                    msgParams.add(th.getThrowable().toString());
                } else {
                    msg.append(" <error>=%s");
                    msgParams.add("unexpected error");
                }
                bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofC(msg.toString(), msgParams.toArray()));
                bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain(th.toString()), th.getThrowable());
            }
        } else {
            bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("no stack trace is available."));
        }
        bLog.log(Level.SEVERE, "FAIL", NMsgBoot.ofPlain("now exiting nuts, Bye!"));
    }

    /**
     * build and return unsatisfied requirements
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return unsatisfied requirements
     */
    private int checkRequirements(boolean unsatisfiedOnly) {
        int req = 0;
        if (!NStringUtilsBoot.isBlank(computedOptions.getApiVersion())) {
            if (!unsatisfiedOnly || !computedOptions.getApiVersion().equals(getVersion())) {
                req += 1;
            }
        }
        if (!unsatisfiedOnly || !NUtilsBoot.isActualJavaCommand(computedOptions.getJavaCommand())) {
            req += 2;
        }
        if (!unsatisfiedOnly || !NUtilsBoot.isActualJavaOptions(computedOptions.getJavaOptions())) {
            req += 4;
        }
        return req;
    }

    /**
     * return a string representing unsatisfied constraints
     *
     * @param unsatisfiedOnly when true return requirements for new instance
     * @return a string representing unsatisfied constraints
     */
    public String getRequirementsHelpString(boolean unsatisfiedOnly) {
        int req = unsatisfiedOnly ? newInstanceRequirements : checkRequirements(false);
        StringBuilder sb = new StringBuilder();
        if ((req & 1) != 0) {
            sb.append("nuts version ").append(NIdBoot.ofApi(computedOptions.getApiVersion()));
        }
        if ((req & 2) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java command ").append(computedOptions.getJavaCommand());
        }
        if ((req & 4) != 0) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append("java options ").append(computedOptions.getJavaOptions());
        }
        if (sb.length() > 0) {
            sb.insert(0, "required ");
            return sb.toString();
        }
        return null;
    }

    private NClassLoaderNode createClassLoaderNode(NDescriptorBoot descr, NRepositoryLocationBoot[] repositories, NRepositoryLocationBoot workspaceBootLibFolder, boolean recover, NReservedErrorInfoList errorList, boolean runtimeDep) throws MalformedURLException {
        NIdBoot id = descr.getId();
        List<NDependencyBoot> deps = descr.getDependencies();
        NClassLoaderNodeBuilder rt = new NClassLoaderNodeBuilder();
        String name = runtimeDep ? "runtime" : ("extension " + id.toString());
        File file = NReservedMavenUtilsBoot.getBootCacheJar(NIdBoot.of(computedOptions.getRuntimeId()), repositories, workspaceBootLibFolder, !recover, name, computedOptions.getExpireTime(), errorList, computedOptions, pathExpansionConverter, bLog, cache);
        rt.setId(id.toString());
        rt.setUrl(file.toURI().toURL());
        rt.setIncludedInClasspath(NReservedLangUtilsBoot.isLoadedClassPath(rt.getURL(), getContextClassLoader(), bLog));

        if (bLog.isLoggable(Level.CONFIG)) {
            String rtHash = "";
            if (computedOptions.getRuntimeId()!=null) {
                rtHash = NReservedIOUtilsBoot.getFileOrDirectoryDigest(file.toPath());
                if (rtHash == null) {
                    rtHash = "";
                }
            }
            bLog.log(Level.CONFIG, "INFO", NMsgBoot.ofC("detect %s version %s - digest %s from %s", name, id.toString(), rtHash, file));
        }

        for (NDependencyBoot s : deps) {
            NClassLoaderNodeBuilder x = new NClassLoaderNodeBuilder();
            if (NUtilsBoot.isAcceptDependency(s, computedOptions)) {
                x.setId(s.toString()).setUrl(NReservedMavenUtilsBoot.getBootCacheJar(s.toId(), repositories, workspaceBootLibFolder, !recover, name + " dependency", computedOptions.getExpireTime(), errorList, computedOptions, pathExpansionConverter, bLog, cache).toURI().toURL());
                x.setIncludedInClasspath(NReservedLangUtilsBoot.isLoadedClassPath(x.getURL(), getContextClassLoader(), bLog));
                rt.addDependency(x.build());
            }
        }
        return rt.build();
    }

    private String resolveDefaultRuntimeId(String sApiVersion) {
        // check fo qualifier
        int q = sApiVersion.indexOf('-');
        if (q > 0) {
            return NIdBoot.ofRuntime((sApiVersion.substring(0, q) + ".0" + sApiVersion.substring(q))).toString();
        }
        return NIdBoot.ofRuntime(sApiVersion + ".0").toString();
    }
}
