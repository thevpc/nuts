package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.*;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.platform.NRuntimeDistribution;
import net.thevpc.nuts.runtime.standalone.atrifact.DefaultNClasspathEntry;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NClassLoaderNodeExt;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;

import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class JavaExecutorOptions {

    private final boolean mainClassApp = false;
    private Boolean acceptOptional = null;
    private final List<String> execArgs;
    private final List<String> jvmArgs = new ArrayList<String>();
    private final List<String> extraExecutorOptions = new ArrayList<String>();
    private final List<String> extraNutsOptions = new ArrayList<String>();
    private final List<String> j9_addModules = new ArrayList<String>();
    private final List<String> j9_modulePath = new ArrayList<String>();
    private final List<String> j9_upgradeModulePath = new ArrayList<String>();
    private final List<String> prependArgs = new ArrayList<>();
    private final List<String> appArgs = new ArrayList<>();
    private final List<String> appendArgs = new ArrayList<>();
    //    private NutsDefinition nutsMainDef;
    private final List<NClasspathEntry> resolvedCP = new ArrayList<>();
    private final List<String> classPath = new ArrayList<>();
    private String javaVersion = null;//runnerProps.getProperty("java.parseVersion");
    private String javaEffVersion = null;
    private boolean java9;
    private String javaCommand = null;//runnerProps.getProperty("java.parseVersion");
    private String mainClass = null;
    private NPath dir = null;
    private boolean javaw = false;
    private boolean excludeBase = false;
    private boolean showCommand;
    private boolean jar = false;
    private String splash;
    private String j9_module;
    private NDependencyFilter dependencyFilter;

    public JavaExecutorOptions(NDefinition def, boolean tempId, List<String> args,
                               List<String> executorOptions, NPath dir) {
        this(def, tempId, args, executorOptions, dir, null);
    }

    public JavaExecutorOptions(NDefinition def, boolean tempId, List<String> args,
                               List<String> executorOptions, NPath dir, net.thevpc.nuts.platform.NEnv targetEnv) {
        showCommand = CoreNUtils.isShowCommand();
        NId id = def.id();
        Path path = def.content().flatMap(NPath::toPath).orNull();
        this.dir = dir;
        this.execArgs = executorOptions;

        NCmdLine cmdLine = NCmdLine.of(getExecArgs()).expandSimpleOptions(false);
        NArg a;
        NClasspathBuilder currentCP = NClasspathBuilder.of();
        List<NArg> extraMayBeJvmOptions = new ArrayList<>();

        cmdLine.matcher()
                .when("--java-version","-java-version").asEntry((v) -> javaVersion = v.stringValue())
                .when("--java-home","-java-home").asEntry((v) -> javaCommand = v.stringValue())
                .when("--class-path","-class-path","--classpath","-classpath","--cp","-cp").asEntry((v) -> addCp(currentCP, v.stringValue()))
                .when("--nuts-path","-nuts-path","--nutspath","-nutspath","--np","-np").asEntry((v) -> addNp(currentCP, v.stringValue()))
                .when("--main-class","-main-class","--class","-class").asEntry((v) -> mainClass = v.stringValue())
                .when("--dir","-dir").asEntry((v) -> this.dir = NPath.of(v.stringValue()))
                .when("--win","-javaw").asFlag((v) -> javaw = v.booleanValue())
                .when("--jar","-jar").asFlag((v) -> jar = v.booleanValue())
                .when("--show-command","-show-command").asFlag((v) -> showCommand = v.booleanValue())
                .when("--exclude-base","-exclude-base").asFlag((v) -> excludeBase = v.booleanValue())
                .when("--add-module","-add-module").asEntry((v) -> this.j9_addModules.add(v.stringValue()))
                .when("--m","-m","--module","-module").asEntry((v) -> this.j9_module = v.stringValue())
                .when("--module-path","-module-path").asEntry((v) -> this.j9_modulePath.add(v.stringValue()))
                .when("--splash","-splash").asEntry((v) -> this.splash=v.stringValue())
                .when("--upgrade-module-path","-upgrade-module-path").asEntry((v) -> this.j9_upgradeModulePath.add(v.stringValue()))
                .when("--prepend-arg","-prepend-arg").asEntry((v) -> this.prependArgs.add(v.stringValue()))
                .when("--prepend-arg","-prepend-arg").asEntry((v) -> this.prependArgs.add(v.stringValue()))
                .when("--append-arg","-append-arg").asEntry((v) -> this.appendArgs.add(v.stringValue()))
                .when("--optional","-optional").asFlag((v) -> this.acceptOptional=v.booleanValue())
                .when("-s").asFlag((v) -> {
                    getJvmArgs().add("-Dswing.aatext=true");
                    getJvmArgs().add("-Dawt.useSystemAAFontSettings=on");
                    getJvmArgs().add("-Dapple.laf.useScreenMenuBar=true");
                    getJvmArgs().add("-Dapple.awt.graphics.UseQuartz=true");
                })
                .whenOption().asRaw(v->{
                    NArg aa = v.peek().get();
                    List<NArg> nArgs = NWorkspaceCmdLineParser.nextNutsArgument(v, null).orNull();
                    if (nArgs != null) {
                        for (NArg nArg : nArgs) {
                            extraNutsOptions.add(nArg.toString());
                        }
                    } else if (aa.toString().startsWith("--jvm-")) {
                        getJvmArgs().add(v.next().get().toString().substring("--jvm".length()));
                    } else if (aa.toString().startsWith("--nuts-")) {
                        extraNutsOptions.add(v.next().get().toString().substring("--nuts".length()));
                    } else {
                        extraMayBeJvmOptions.add(v.next().get());
                    }
                })
                .requireAll();

        dependencyFilter = NDependencyFilter.ofScope(NDependencyScopePattern.RUN)
                .and(NDependencyFilter.ofOptional(acceptOptional));

        boolean cached = NSession.of().isCached() && NSession.of().fetchStrategy().orNull() != NFetchStrategy.REMOTE;
        NPath cacheFile = null;
        if (cached) {
            cacheFile = loadCachedClassPath(id, currentCP, resolvedCP);
        }

        if (resolvedCP.isEmpty()) {
            List<NDefinition> nDefinitions = new ArrayList<>();
            NSearch se = NSearch.of();
            if (targetEnv != null) {
                se.targetEnv(targetEnv);
            }
            if (tempId) {
                for (NDependency dependency : def.dependencies().get().immediate().toList()) {
                    if (dependencyFilter.acceptDependency(dependency, null)) {
                        se.addId(dependency.toId());
                    }
                }
            } else {
                se.addId(id);
            }
            if (se.ids().size() > 0) {
                nDefinitions.addAll(
                        se
                                .transitive(true)
                                .distinct(true)
                                .latest(true)
                                .inlineDependencies(true)
                                .dependencyFilter(dependencyFilter)
                                .getResultDefinitions().toList()
                );
            }
            resolveJavaSdk(def, path, null, args);
            if (this.jar) {
                NSession session = NSession.of();
                if (this.mainClass != null) {
                    if (NOut.isPlain()) {
                        session.terminal().err().println((NMsg.ofC("ignored main-class=%s. running jar!", getMainClass())));
                    }
                }
                if (!currentCP.isEmpty()) {
                    if (NOut.isPlain()) {
                        session.terminal().err().println(NMsg.ofC("ignored class-path=%s. running jar!", currentCP
                                .stream()
                                .map(x -> x.toString()).collect(Collectors.joining(","))
                        ));
                    }
                }
                if (this.excludeBase) {
                    throw new NIllegalArgumentException(NMsg.ofP("cannot exclude base with jar modifier"));
                }
            } else {
                resolveMainClassFromPath(path);
                NId finalId = id;
                NAssert.requireNonNull(mainClass, () -> NMsg.ofC("missing Main Class for %s", finalId));
                boolean baseDetected = false;
                for (NDefinition nDefinition : nDefinitions) {
                    if (nDefinition.content().isPresent()) {
                        if (id.longName().equals(nDefinition.id().longName())) {
                            baseDetected = true;
                            if (!isExcludeBase()) {
                                currentCP.add(nDefinition);
                            }
                        } else {
                            currentCP.add(nDefinition);
                        }
                    }
                }
                if (!isExcludeBase() && !baseDetected) {
                    NAssert.requireNonNull(path, () -> NMsg.ofC("missing path %s", finalId));
                    //do append, not prepend, because use cp shall prevail
                    currentCP.add(def);
                }
                resolvedCP.addAll(currentCP.resolve());
                if (cached && cacheFile != null) {
                    writeCache(cacheFile, resolvedCP);
                }
            }
        } else {
            // Cache hit! We need to ensure some fields that would have been set in the else block are set here too.
            // Specifically: javaVersion, javaEffVersion, javaCommand, java9.
            // These depend on NJavaSdkUtils and might not be cached.
            // However, they are relatively fast to compute compared to NSearch.
            // We should probably extract the Java setup logic to run regardless of cache hit, 
            // OR cache these values too.
            // The logic above put javaVersion in the cache key, so it is assumed known or passed.
            // But javaEffVersion and javaCommand are computed.
            // Let's copy the Java setup logic here or refactor.
            // For safety and simplicity in this patch, I will duplicate the Java setup logic.

            // ... Copying Java setup logic ...
            resolveJavaSdk(def, path, null, args);
            if (!this.jar) {
                resolveMainClassFromPath(path);
            }
        }

        if (!resolvedCP.isEmpty()) {
            List<NClassLoaderNodeExt> ln =
                    NJavaSdkUtils.loadNutsClassLoaderNodeExts(
                            resolvedCP.toArray(new NClasspathEntry[0]),
                            java9
                    );
            if (java9) {
                List<NClassLoaderNodeExt> ln_javaFx = new ArrayList<>();
                List<NClassLoaderNodeExt> ln_others = new ArrayList<>();
                for (NClassLoaderNodeExt n : ln) {
                    if (n.jfx) {
                        ln_javaFx.add(n);
                    } else {
                        ln_others.add(n);
                    }
                }
                ln_javaFx.sort(
                        (a1, a2) -> {
                            NId b1 = a1.id;
                            NId b2 = a2.id;
                            // give precedence to classifiers
                            String c1 = b1.classifier();
                            String c2 = b2.classifier();
                            if (b1.builder().classifier(null).build().shortName().equals(b2.builder().classifier(null).build().shortName())) {
                                if (NBlankable.isBlank(c1)) {
                                    return 1;
                                }
                                if (NBlankable.isBlank(c2)) {
                                    return -1;
                                }
                                return b1.compareTo(b2);
                            }
                            return b1.compareTo(b2);
                        }
                );
                ln.clear();
                ln.addAll(ln_javaFx);
                ln.addAll(ln_others);
            }
            for (NClassLoaderNodeExt s : ln) {
                if (java9 && s.moduleName != null && s.jfx) {
                    if (!s.moduleName.endsWith("Empty")) {
                        j9_addModules.add(s.moduleName);
                    }
                    j9_modulePath.add(s.path.toPath().get().toString());
                    for (String requiredJfx : s.requiredJfx) {
                        if (!requiredJfx.endsWith("Empty")) {
                            j9_addModules.add(requiredJfx);
                        }
                    }
                } else {
                    classPath.add(s.path.toPath().get().toString());
                }
            }

            if (this.mainClass != null && this.mainClass.contains(":")) {
                List<String> possibleClasses = StringTokenizerUtils.split(getMainClass(), ":");
                switch (possibleClasses.size()) {
                    case 0:
                        throw new NIllegalArgumentException(NMsg.ofC("missing Main-Class in Manifest for %s", id));
                    case 1:
                        //
                        break;
                    default: {
                        if (!NOut.isPlain()
                                || NSession.of().isBot()
//                                    || !session.isAsk()
                        ) {
                            throw new NExecutionException(NMsg.ofC("multiple runnable classes detected : %s", possibleClasses), NExecutionException.ERROR_1);
                        }
                        NTextBuilder msgString = NTextBuilder.of();

                        msgString.append("multiple runnable classes detected  - actually ")
                                .append(NText.ofStyled("" + possibleClasses.size(), NTextStyle.primary5()))
                                .append(" . Select one :\n");
                        int x = ((int) Math.log(possibleClasses.size())) + 2;
                        for (int i = 0; i < possibleClasses.size(); i++) {
                            StringBuilder clsIndex = new StringBuilder();
                            clsIndex.append((i + 1));
                            while (clsIndex.length() < x) {
                                clsIndex.append(' ');
                            }
                            msgString.append(clsIndex.toString(), NTextStyle.primary4());
                            msgString.append(possibleClasses.get(i), NTextStyle.primary4());
                            msgString.append("\n");
                        }
                        msgString.append("enter class ")
                                .append("#", NTextStyle.primary5()).append(" or ").append("name", NTextStyle.primary5())
                                .append(" to run it. type ").append("cancel!", NTextStyle.error())
                                .append(" to cancel : ");

                        mainClass = NIn.ask()
                                .forString(NMsg.ofNtf(msgString))
                                .validator((value, question) -> {
                                    Integer anyInt = NLiteral.of(value).asInt().orNull();
                                    if (anyInt != null) {
                                        int i = anyInt;
                                        if (i >= 1 && i <= possibleClasses.size()) {
                                            return possibleClasses.get(i - 1);
                                        }
                                    } else {
                                        for (String possibleClass : possibleClasses) {
                                            if (possibleClass.equals(value)) {
                                                return possibleClass;
                                            }
                                        }
                                    }
                                    throw new NValidationException();
                                }).value();
                        break;
                    }
                }
            }
        }
    }

    private void writeCache(NPath cacheFile, List<NClasspathEntry> classPathNodes) {
        try {
            cacheFile.mkParentDirs();
            try (BufferedWriter bw = cacheFile.getBufferedWriter(NPathOption.CREATE, NPathOption.TRUNCATE_EXISTING)) {
                for (NClasspathEntry node : classPathNodes) {
                    bw.write((node.id() == null ? "" : node.id().toString()) + "|" + (node.path() == null ? "" : node.path().toString()));
                    bw.newLine();
                }
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    private NPath loadCachedClassPath(NId id, NClasspathBuilder currentCP, List<NClasspathEntry> resolvedCP) {
        CoreDigestHelper dh = new CoreDigestHelper();
        dh.append(String.valueOf(id).getBytes());
        dh.append(currentCP.stream().map(Object::toString).collect(Collectors.joining(":")).getBytes());
        dh.append(String.valueOf(javaVersion).getBytes());
        dh.append(String.valueOf(excludeBase).getBytes());
        dh.append(String.valueOf(jar).getBytes());
        dh.append(j9_addModules.stream().sorted().collect(Collectors.joining(":")).getBytes());
        dh.append(j9_modulePath.stream().sorted().collect(Collectors.joining(":")).getBytes());
        dh.append(j9_upgradeModulePath.stream().sorted().collect(Collectors.joining(":")).getBytes());
        String cacheKey = dh.getDigest();
        NPath cacheFile = NPath.of(NStoreKey.ofCache(NWorkspace.of().apiId())).resolve("classpaths").resolve(cacheKey);
        if (cacheFile != null && cacheFile.exists()) {
            try (BufferedReader br = cacheFile.asBufferedReader()) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!NStringUtils.isBlank(line)) {
                        String[] split = line.split("\\|");
                        if (split.length >= 2) {
                            String idStr = split[0];
                            String urlStr = split[1];
                            NId nid = NId.get(idStr).orNull();
                            if (NStringUtils.isBlank(urlStr)) {
                                if (!NStringUtils.isBlank(idStr)) {
                                    resolvedCP.add(new DefaultNClasspathEntry(
                                            NFetch.of(nid).getResultDefinition()
                                    ));
                                } else {
                                    resolvedCP.clear();
                                    return cacheFile;
                                }
                            } else {
                                if(NPath.of(urlStr).exists()) {
                                    resolvedCP.add(new DefaultNClasspathEntry(nid, NPath.of(urlStr)));
                                }else{
                                    resolvedCP.clear();
                                    return cacheFile;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore and re-compute
                resolvedCP.clear();
            }
        }
        return cacheFile;
    }

    private boolean isJvmOption(NArg extraMayBeJvmOption, NVersion nVersion) {
        String s = extraMayBeJvmOption.toString();
        if (s.startsWith("-d32")) {
            return true;
        }
        if (s.startsWith("-d64")) {
            return true;
        }
        if (s.startsWith("-server")) {
            return true;
        }
        if (s.startsWith("-cp")) {
            return true;
        }
        if (s.startsWith("-classpath")) {
            return true;
        }
        if (s.startsWith("-D")) {
            return true;
        }
        if (s.startsWith("-verbose:")) {
            return true;
        }
        if (s.startsWith("-ea:")) {
            return true;
        }
        if (s.startsWith("-enableassertions:")) {
            return true;
        }
        if (s.startsWith("-da:")) {
            return true;
        }
        if (s.startsWith("-disableassertions:")) {
            return true;
        }
        if (s.startsWith("-esa")) {
            return true;
        }
        if (s.startsWith("-dsa")) {
            return true;
        }
        if (s.startsWith("-enablesystemassertions")) {
            return true;
        }
        if (s.startsWith("-disablesystemassertions")) {
            return true;
        }
        if (s.startsWith("-splash:")) {
            return true;
        }
        if (s.startsWith("-agentlib:")) {
            return true;
        }
        return s.startsWith("-javaagent:");
    }

    private String resolveMainClass(String name, List<String> possibleClasses) {
        if (name != null) {
            Integer v = NLiteral.of(name).asInt().orNull();
            if (v != null) {
                if (v >= 1 && v <= possibleClasses.size()) {
                    return possibleClasses.get(v - 1);
                } else if (v < 0) {
                    int i = possibleClasses.size() + v;
                    if (i >= 0 && i < possibleClasses.size()) {
                        return possibleClasses.get(i);
                    }
                }
            } else {
                if (possibleClasses.contains(name)) {
                    return name;
                } else {
                    List<String> extraPossibilities = new ArrayList<>();
                    for (String possibleClass : possibleClasses) {
                        int x = possibleClass.lastIndexOf('.');
                        if (x > 0) {
                            if (possibleClass.substring(x + 1).equals(name)) {
                                extraPossibilities.add(possibleClass);
                            }
                        }
                    }
                    if (extraPossibilities.size() == 1) {
                        return extraPossibilities.get(0);
                    }
                    if (extraPossibilities.size() > 1) {
                        throw new NIllegalArgumentException(NMsg.ofC("ambiguous main-class %s matches all of %s",
                                name, extraPossibilities.toString()
                        ));
                    }
                    for (String possibleClass : possibleClasses) {
                        int x = possibleClass.lastIndexOf('.');
                        if (x > 0) {
                            if (possibleClass.substring(x + 1).equalsIgnoreCase(name)) {
                                extraPossibilities.add(possibleClass);
                            }
                        }
                    }
                    if (extraPossibilities.size() == 1) {
                        return extraPossibilities.get(0);
                    }
                    if (extraPossibilities.size() > 1) {
                        throw new NIllegalArgumentException(NMsg.ofC("ambiguous main-class %s matches all of from %s",
                                name, extraPossibilities.toString()
                        ));
                    }
                }
            }
        }
        return null;
    }

    private void addCp(NClasspathBuilder classPath, String value) {
        if (value == null) {
            value = "";
        }
        boolean files = value.matches("([^:]+\\.jar[:;]?.*)");//|(.*[/\\\\].*)
        boolean nutsIds = value.matches("(.*[:#?].*)");
        if (nutsIds && !files) {
            addNp(classPath, value);
        } else {
            for (String n : StringTokenizerUtils.splitColon(value)) {
                if (!NBlankable.isBlank(n)) {
                    classPath.add(NPath.of(n));
                }
            }
        }

    }

    private void addNp(NClasspathBuilder classPath, String value) {
        NSearch ns = NSearch.of().latest(true);
        for (String n : StringTokenizerUtils.splitDefault(value)) {
            if (!NBlankable.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NId nutsId : ns.getResultIds()) {
            NDefinition d = NSearch.of().addId(nutsId)
                    .latest(true).getResultDefinitions().findFirst().get();
            classPath.add(d);
        }
    }

    private void resolveJavaSdk(NDefinition def, Path path, NVersion explicitJavaVersion, List<String> args) {
        if (path != null) {
            NVersion binJavaVersion = JavaJarUtils.parseJarClassVersion(
                    NPath.of(path)
            );
            if (!NBlankable.isBlank(binJavaVersion) && (NBlankable.isBlank(javaVersion) || binJavaVersion.compareTo(javaVersion) > 0)) {
                javaVersion = binJavaVersion.toString();
            }
        }
        if (explicitJavaVersion == null) {
            explicitJavaVersion = def.descriptor().condition().platform().stream().map(x -> NId.get(x).get())
                    .filter(x -> x.shortName().equals("java"))
                    .map(NId::version)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
        }
        if (!NBlankable.isBlank(explicitJavaVersion) && (NBlankable.isBlank(javaVersion) || explicitJavaVersion.compareTo(javaVersion) > 0)) {
            javaVersion = explicitJavaVersion.toString();
        }
        NJavaSdkUtils nJavaSdkUtils = NJavaSdkUtils.of();
        NOptional<NRuntimeDistribution> nutsPlatformLocation = nJavaSdkUtils.resolveJdkLocation(getJavaVersion(), false, true, true,null);
        if (!nutsPlatformLocation.isPresent()) {
            throw new NExecutionException(NMsg.ofC("no java version %s was found", NStringUtils.strip(getJavaVersion())), NExecutionException.ERROR_1);
        }
        javaEffVersion = nutsPlatformLocation.get().version();
        javaCommand = nJavaSdkUtils.resolveJavaCommandByVersion(nutsPlatformLocation.get(), javaw).orNull();
        if (javaCommand == null) {
            throw new NExecutionException(NMsg.ofC("no java version %s was found", getJavaVersion()), NExecutionException.ERROR_1);
        }
        // Also need to populate appArgs
        //this.appArgs = new ArrayList<>();
        appArgs.addAll(prependArgs);
        appArgs.addAll(args);
        appArgs.addAll(appendArgs);

        // extra options
        List<NArg> extraMayBeJvmOptions = new ArrayList<>();
        NCmdLine cmdLine = NCmdLine.of(getExecArgs()).expandSimpleOptions(false);
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get();
            if (a.isOption()) {
                List<NArg> nArgs = NWorkspaceCmdLineParser.nextNutsArgument(cmdLine, null).orNull();
                if (nArgs == null && !a.toString().startsWith("--jvm-") && !a.toString().startsWith("--nuts-")) {
                    switch (a.key()) {
                        case "--java-version":
                        case "-java-version":
                        case "--java-home":
                        case "-java-home":
                        case "--class-path":
                        case "-class-path":
                        case "--classpath":
                        case "-classpath":
                        case "--cp":
                        case "-cp":
                        case "--nuts-path":
                        case "-nuts-path":
                        case "--nutspath":
                        case "-nutspath":
                        case "--np":
                        case "-np":
                        case "--main-class":
                        case "-main-class":
                        case "--class":
                        case "-class":
                        case "--dir":
                        case "-dir":
                        case "--win":
                        case "--javaw":
                        case "--jar":
                        case "-jar":
                        case "--show-command":
                        case "-show-command":
                        case "--exclude-base":
                        case "-exclude-base":
                        case "--add-module":
                        case "-m":
                        case "--module":
                        case "--module-path":
                        case "-splash":
                        case "--upgrade-module-path":
                        case "--prepend-arg":
                        case "--append-arg":
                        case "-s": {
                            //ignore
                            cmdLine.skip();
                            break;
                        }
                        default: {
                            extraMayBeJvmOptions.add(cmdLine.next().get());
                        }
                    }
                } else {
                    cmdLine.skip();
                }
            } else {
                cmdLine.skip();
            }
        }

        for (NArg varg : extraMayBeJvmOptions) {
            if (isJvmOption(varg, explicitJavaVersion)) {
                getJvmArgs().add(varg.toString());
            } else {
                extraExecutorOptions.add(varg.toString());
            }
        }
        java9 = NVersion.get(javaVersion).get().compareTo("9") >= 0;
        for (NArg extraMayBeJvmOption : extraMayBeJvmOptions) {
            if (extraMayBeJvmOption.toString().startsWith("--jvm-")) {
                getJvmArgs().add(extraMayBeJvmOption.toString().substring("--jvm".length()));
            } else if (isJvmOption(extraMayBeJvmOption, NVersion.get(javaVersion).get())) {
                getJvmArgs().add(extraMayBeJvmOption.toString());
            }
        }
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public String getMainClass() {
        return mainClass;
    }

    public NPath getDir() {
        return dir;
    }

    public boolean isMainClassApp() {
        return mainClassApp;
    }

    public boolean isExcludeBase() {
        return excludeBase;
    }

    public boolean isShowCommand() {
        return showCommand;
    }

    public boolean isJar() {
        return jar;
    }

    //    public List<String> getClassPath() {
//        return classPath;
//    }
//
//    public List<String> getNutsPath() {
//        return nutsPath;
//    }
    public List<String> getExecArgs() {
        return execArgs;
    }

    public List<String> getJvmArgs() {
        return jvmArgs;
    }


    public List<String> getAppArgs() {
        return appArgs;
    }

//    public void fillStrings(NClassLoaderNode n, List<String> list) {
//        URL f = n.getURL();
//        list.add(NPath.of(f).toPath().get().toString());
//        for (NClassLoaderNode d : n.getDependencies()) {
//            fillStrings(d, list);
//        }
//    }


    public List<String> getClassPathNidStrings() {
        List<String> li = new ArrayList<>();
        for (NClasspathEntry n : getResolvedCP()) {
            li.add(n.toString());
        }
        return li;
    }

    public List<NClasspathEntry> getResolvedCP() {
        return resolvedCP;
    }

    public String getJavaEffVersion() {
        return javaEffVersion;
    }

    public boolean isJava9() {
        return java9;
    }

    public boolean isJavaw() {
        return javaw;
    }

    public String getSplash() {
        return splash;
    }

    public List<String> getJ9_addModules() {
        return j9_addModules;
    }

    public List<String> getJ9_modulePath() {
        return j9_modulePath;
    }

    public String getJ9_module() {
        return j9_module;
    }

    public List<String> getJ9_upgradeModulePath() {
        return j9_upgradeModulePath;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getExtraNutsOptions() {
        return extraNutsOptions;
    }

    public List<String> getExtraExecutorOptions() {
        return extraExecutorOptions;
    }

    private void resolveMainClassFromPath(Path path) {
        if (mainClass == null) {
            if (path != null) {
                //check manifest!
                List<NExecutionEntry> classes = NExecutionEntry.parse(NPath.of(path));
                NExecutionEntry[] primary = classes.stream().filter(NExecutionEntry::isDefaultEntry).toArray(NExecutionEntry[]::new);
                if (primary.length > 0) {
                    mainClass = Arrays.stream(primary).map(NExecutionEntry::name)
                            .collect(Collectors.joining(":"));
                } else if (classes.size() > 0) {
                    mainClass = classes.stream().map(NExecutionEntry::name)
                            .collect(Collectors.joining(":"));
                }
            }
        } else if (!mainClass.contains(".")) {
            List<NExecutionEntry> classes = NExecutionEntry.parse(NPath.of(path));
            List<String> possibleClasses = classes.stream().map(NExecutionEntry::name)
                    .collect(Collectors.toList());
            String r = resolveMainClass(mainClass, possibleClasses);
            if (r != null) {
                mainClass = r;
            }
        }
    }
}
