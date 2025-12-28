package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.core.NClassLoaderNode;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NIn;
import net.thevpc.nuts.io.NTrace;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.util.NCoreLogUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NSdkLocationComparator;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Stream;

public class NJavaSdkUtils {

    private final NWorkspace workspace;
    private List<JavaProvider> javaProviders = new ArrayList<>();
    private NExecutionEngineLocation hostVm;

    private NJavaSdkUtils(NWorkspace workspace) {
        this.workspace = workspace;
        javaProviders.add(new TemurinProvider());
    }

    public static NJavaSdkUtils of() {
        return of(NWorkspace.of());
    }

    public static NJavaSdkUtils of(NWorkspace ws) {
        return ws.getOrComputeProperty(NJavaSdkUtils.class.getName(), () -> new NJavaSdkUtils(ws));
    }

    public static Integer defaultJavaMajorVersion() {
        // ?? why 25?
        return 25;
    }

    public static Integer validateJavaMajorVersionOrDefault(String version) {
        return validateJavaMajorVersion(version).orElse(defaultJavaMajorVersion());
    }

    public static NOptional<Integer> validateJavaMajorVersion(String version) {
        if (!NBlankable.isBlank(version)) {
            NVersion v = NVersion.get(version).orNull();
            if (v != null) {
                if (v.isSingleValue()) {
                    Integer e = v.getIntegerAt(0).orElse(null);
                    if (e != null) {
                        if (e.intValue() == 1) {
                            Integer b = v.getIntegerAt(1).orElse(null);
                            if (b == null) {
                                return NOptional.of(1);
                            }
                            return NOptional.of(b);
                        }
                        return NOptional.of(e);
                    }
                }
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("invalid java version %s", version));
    }

    public static Integer validateJavaMajorVersionOrDefault(NVersion version) {
        return validateJavaMajorVersion(version).orElse(defaultJavaMajorVersion());
    }

    public NOptional<NExecutionEngineLocation> resolveAndInstall(String product, NVersion version, NOsFamily os, NArchFamily arch) {
        for (JavaProvider kavaProvider : javaProviders) {
            String product2 = NJavaSdkUtils.validateJavaProduct(product).orElse(NExecutionEngineLocation.JAVA_PRODUCT_JDK);
            int version2 = NJavaSdkUtils.validateJavaMajorVersionOrDefault(version);
            NOsFamily os2 = os == null ? NOsFamily.getCurrent() : os;
            NArchFamily arch2 = arch == null ? NArchFamily.getCurrent() : arch;
            NOptional<NPath> z = kavaProvider.resolveAndInstall(product2, version2, os2, arch2);
            if (z.isPresent()) {
                NExecutionEngineLocation r = resolveJdkLocation(z.get(), kavaProvider.getName() + "-" + product2 + "-" + version2 + "-" + arch2);
                if (r != null) {
                    return NOptional.of(r);
                }
            }
        }
        Map<String, Object> env = new LinkedHashMap<>();
        if (!NBlankable.isBlank(product)) {
            env.put("product", product);
        }
        if (!NBlankable.isBlank(version)) {
            env.put("version", version);
        }
        if (!NBlankable.isBlank(os)) {
            env.put("os", os);
        }
        if (!NBlankable.isBlank(arch)) {
            env.put("arch", arch);
        }
        return NOptional.ofEmpty(NMsg.ofC("java not found : %s", env));
    }

    public static NOptional<Integer> validateJavaMajorVersion(NVersion version) {
        if (!NBlankable.isBlank(version)) {
            if (version.isSingleValue()) {
                Integer e = version.getIntegerAt(0).orElse(null);
                if (e != null) {
                    if (e.intValue() == 1) {
                        Integer b = version.getIntegerAt(1).orElse(null);
                        if (b == null) {
                            return NOptional.of(1);
                        }
                        return NOptional.of(b);
                    }
                    return NOptional.of(e);
                }
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("invalid java version %s", version));
    }

    public static NOptional<String> validateJavaProduct(String product) {
        if (!NBlankable.isBlank(product)) {
            switch (NStringUtils.trim(product).toLowerCase()) {
                case NExecutionEngineLocation.JAVA_PRODUCT_JDK: {
                    return NOptional.of(NExecutionEngineLocation.JAVA_PRODUCT_JDK);
                }
                case NExecutionEngineLocation.JAVA_PRODUCT_JRE: {
                    return NOptional.of(NExecutionEngineLocation.JAVA_PRODUCT_JRE);
                }
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("java product : %s", product));
    }

    public static List<NClassLoaderNodeExt> loadNutsClassLoaderNodeExts(NClassLoaderNode[] n, boolean java9) {
        List<NClassLoaderNodeExt> list = new ArrayList<>();
        for (NClassLoaderNode nn : n) {
            fillNodes(nn, list, java9);
        }
        return list;
    }

    private static void fillNodes(NClassLoaderNode n, List<NClassLoaderNodeExt> list, boolean java9) {
        NClassLoaderNodeExt k = new NClassLoaderNodeExt();
        k.node = n;
        k.id = n.getId();
        k.path = NPath.of(n.getURL());
        if (java9) {
            k.moduleInfo = JavaJarUtils.parseModuleInfo(k.path);
            if (k.moduleInfo != null) {
                k.moduleName = k.moduleInfo.module_name;
                for (JavaClassByteCode.ModuleInfoRequired r : k.moduleInfo.required) {
                    if (r.req_name.startsWith("javafx")) {
                        k.requiredJfx.add(r.req_name);
                    }
                }
            } else {
                k.moduleName = JavaJarUtils.parseDefaultModuleName(k.path);
            }
            k.jfx = k.moduleName != null && k.moduleName.startsWith("javafx");

        } else {
            k.jfx = k.id.getArtifactId().startsWith("javafx") &&
                    k.id.getGroupId().startsWith("org.openjfx");
        }
        list.add(k);
        for (NClassLoaderNode d : n.getDependencies()) {
            fillNodes(d, list, java9);
        }
    }

    public static boolean isJava(NId id) {
        if (id != null) {
            return NExecutionEngineFamily.JAVA == NExecutionEngineFamily.parse(id.getArtifactId()).orNull();
        }
        return false;
    }

    protected NLog _LOG() {
        return NLog.of(NJavaSdkUtils.class);
    }

    public Predicate<String> createVersionFilterPredicate(String requestedJavaVersion) {
        NVersionFilter versionFilter = createVersionFilter(requestedJavaVersion);
        return createVersionFilterPredicate(versionFilter);
    }

    public Predicate<String> createVersionFilterPredicate(NVersionFilter versionFilter) {
        return new Predicate<String>() {
            @Override
            public boolean test(String sVersion) {
                NVersion version = NVersion.get(sVersion).get();
                if (versionFilter.acceptVersion(version)) {
                    return true;
                }
                // replace 1.6 by 6, and 1.8 by 8
                int a = sVersion.indexOf('.');
                if (a > 0) {
                    NLiteral p = NLiteral.of(sVersion.substring(0, a));
                    if (p.asInt().isPresent() && p.asInt().get() == 1) {
                        String sVersion2 = sVersion.substring(a + 1);
                        NVersion version2 = NVersion.get(sVersion2).get();
                        if (versionFilter.acceptVersion(version2)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    public NVersionFilter createVersionFilterExact(String requestedJavaVersion) {
        requestedJavaVersion = NStringUtils.trim(requestedJavaVersion);
        NVersion vv = NVersion.get(requestedJavaVersion).get();
        String singleVersion = vv.asSingleValue().orNull();
        if (singleVersion != null) {
            requestedJavaVersion = singleVersion;
        }
        return NVersionFilters.of().byValue(requestedJavaVersion).get();
    }

    public NVersionFilter createVersionFilter(String requestedJavaVersion) {
        requestedJavaVersion = NStringUtils.trim(requestedJavaVersion);
        NVersion vv = NVersion.get(requestedJavaVersion).get();
        String singleVersion = vv.asSingleValue().orNull();
        if (singleVersion != null) {
            requestedJavaVersion = "[" + singleVersion + ",[";
        }
        return NVersionFilters.of().byValue(requestedJavaVersion).get();
    }

    public NExecutionEngineLocation getHostJvm() {
        if (hostVm == null) {
            String appSuffix = NEnv.of().getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
            String product = NExecutionEngineLocation.JAVA_PRODUCT_JRE;
            if (new File(System.getProperty("java.home"), "bin" + File.separator + "javac" + (appSuffix)).isFile()) {
                product = NExecutionEngineLocation.JAVA_PRODUCT_JDK;
            }
            String vmName = System.getProperty("java.vm.name");          // HotSpot / GraalVM CE
            String variant = vmName.toLowerCase().contains("graalvm") ? "GraalVM CE" :
                    vmName.toLowerCase().contains("hotspot") ? "HotSpot" :
                    vmName;
            NExecutionEngineLocation current = new NExecutionEngineLocation(
                    NEnv.of().getJava(),
                    System.getProperty("java.vendor"),
                    product,
                    variant,
                    product + "-" + System.getProperty("java.version"),
                    System.getProperty("java.home"),
                    System.getProperty("java.version"),
                    product,
                    0
            );
            current.setConfigVersion(DefaultNWorkspace.VERSION_SDK_LOCATION);
            hostVm = current;
        }
        return hostVm;
    }

    public NOptional<NExecutionEngineLocation> resolveJdkLocation(String javaVersion, boolean jdk, boolean ifNotFoundSearchLocally, boolean ifNotFoundSearchRemotely) {
        // [1] look if locally this version is installed (1.8)
        // [2] look if host JVM 1.8
        // [3] look if locally this version could be installed (1.8 after confirmation)
        // [4] look if remotely this version could be installed (1.8 after confirmation)
        // [5] look if locally this version is installed >1.8)
        // [6] look if host JVM >1.8
        // [7] look if locally this version could be installed (>1.8 after confirmation)
        // [8] look if remotely this version could be installed (>1.8 after confirmation)

        Predicate<NVersion> requestedVersionFilterExact = validateJavaVersion(createVersionFilterExact(javaVersion));

        NExecutionEngines nExecutionEngines = NExecutionEngines.of();
        NExecutionEngineLocation[] allRegisteredInstallations = nExecutionEngines.findExecutionEngines().toArray(NExecutionEngineLocation[]::new);
        NExecutionEngineLocationComparatorVersionFirstThenJdkFirst highestVersionFirst = new NExecutionEngineLocationComparatorVersionFirstThenJdkFirst(false);
        {
            // [1] look if locally this version is installed (1.8)
            // [2] look if host JVM 1.8
            NExecutionEngineLocation[] found = Arrays.stream(allRegisteredInstallations).filter(x ->
                    (!jdk || NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.getProduct())) &&
                            requestedVersionFilterExact.test(NVersion.get(x.getVersion()).orNull())).toArray(NExecutionEngineLocation[]::new);
            if (found.length > 0) {
                NExecutionEngineLocation[] sorted = Arrays.stream(found).sorted(highestVersionFirst).toArray(NExecutionEngineLocation[]::new);
                return NOptional.of(sorted[0]);
            }
        }
        boolean searchSystemInstallations = ifNotFoundSearchLocally && NIn.ask().forBoolean(
                        NMsg.ofC("No Java %s found. will search for system installed locations. Would you want me to?", javaVersion)
                ).setDefaultValue(true)
                .getBooleanValue();
        NExecutionEngineLocation[] searchedJdkLocations = new NExecutionEngineLocation[0];
        if (searchSystemInstallations) {
            searchedJdkLocations = searchJdkLocations();
            NExecutionEngineLocation[] found = Stream.of(searchedJdkLocations).filter(
                    x ->
                            (!jdk || NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.getProduct())) &&
                                    requestedVersionFilterExact.test(NVersion.get(x.getVersion()).orNull())).toArray(NExecutionEngineLocation[]::new);
            if (found.length > 0) {
                NExecutionEngineLocation[] sorted = Arrays.stream(found).sorted(highestVersionFirst).toArray(NExecutionEngineLocation[]::new);
                for (NExecutionEngineLocation selected : sorted) {
                    if (NIn.ask().forBoolean(
                                    NMsg.ofC("Found in your system this %s installation : %s (located at %s). Would you like to auto-configure and use it?", javaVersion, selected.getName(), selected.getPath())
                            ).setDefaultValue(true)
                            .getBooleanValue()) {
                        nExecutionEngines.addExecutionEngine(selected);
                        return NOptional.of(selected);
                    }
                }
            }
        }
        boolean searchRemoteInstallations = ifNotFoundSearchRemotely && NSession.of().getFetchStrategy().orElse(NFetchStrategy.ONLINE).accept(NFetchMode.REMOTE) && NIn.ask().forBoolean(
                        NMsg.ofC("No Java %s found. will download one and install it. Would you want me to?", javaVersion)
                ).setDefaultValue(true)
                .getBooleanValue();

        if (searchRemoteInstallations) {
            // [4] look if remotely this version could be installed (1.8 after confirmation)
            NExecutionEngineLocation[] found = Stream.of(searchRemoteLocationsAndInstall(jdk ? NExecutionEngineLocation.JAVA_PRODUCT_JDK : NExecutionEngineLocation.JAVA_PRODUCT_JRE, NBlankable.isBlank(javaVersion) ? NVersion.BLANK : NVersion.of(javaVersion))).filter(
                    x ->
                            (!jdk || NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.getProduct())) &&
                                    requestedVersionFilterExact.test(NVersion.get(x.getVersion()).orNull())).toArray(NExecutionEngineLocation[]::new);
            if (found.length > 0) {
                NExecutionEngineLocation[] sorted = Arrays.stream(found).sorted(highestVersionFirst).toArray(NExecutionEngineLocation[]::new);
                for (NExecutionEngineLocation selected : sorted) {
                    if (NIn.ask().forBoolean(
                                    NMsg.ofC("Found in your system this %s installation : %s (located at %s). Would you like to auto-configure and use it?", javaVersion, selected.getName(), selected.getPath())
                            ).setDefaultValue(true)
                            .getBooleanValue()) {
                        nExecutionEngines.addExecutionEngine(selected);
                        return NOptional.of(selected);
                    }
                }
            }
        }
        NTrace.println(NMsg.ofC("Still no exact Java %s found. will search for most appropriate one.", javaVersion));
        Predicate<NVersion> requestedVersionFilterBigger = validateJavaVersion(createVersionFilter(javaVersion));
        NExecutionEngineLocationComparatorVersionFirstThenJdkFirst lowerVersionFirst = new NExecutionEngineLocationComparatorVersionFirstThenJdkFirst(true);
        {
            // [5] look if locally this version is installed >1.8)
            // [6] look if host JVM >1.8
            NExecutionEngineLocation[] found = Arrays.stream(allRegisteredInstallations).filter(x ->
                    (!jdk || NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.getProduct())) &&
                            requestedVersionFilterBigger.test(NVersion.get(x.getVersion()).orNull())).toArray(NExecutionEngineLocation[]::new);
            if (found.length > 0) {
                NExecutionEngineLocation[] sorted = Arrays.stream(found).sorted(lowerVersionFirst).toArray(NExecutionEngineLocation[]::new);
                for (NExecutionEngineLocation selected : sorted) {
                    if (NIn.ask().forBoolean(
                                    NMsg.ofC("Found this registered installation : %s. Would you like to use it instead of %s?", selected.getName(), javaVersion)
                            ).setDefaultValue(true)
                            .getBooleanValue()) {
                        nExecutionEngines.addExecutionEngine(selected);
                        return NOptional.of(selected);
                    }
                }
            }
        }
        if (searchSystemInstallations) {
            // [7] look if locally this version could be installed (>1.8 after confirmation)
            NExecutionEngineLocation[] found = Stream.of(searchedJdkLocations).filter(
                    x ->
                            (!jdk || NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.getProduct())) &&
                                    requestedVersionFilterBigger.test(NVersion.get(x.getVersion()).orNull())).toArray(NExecutionEngineLocation[]::new);
            if (found.length > 0) {
                NExecutionEngineLocation[] sorted = Arrays.stream(found).sorted(lowerVersionFirst).toArray(NExecutionEngineLocation[]::new);
                for (NExecutionEngineLocation selected : sorted) {
                    if (NIn.ask().forBoolean(
                                    NMsg.ofC("Found this system installation : %s (located at %s). Would you like to auto-configure and use it?", selected.getName(), selected.getPath())
                            ).setDefaultValue(true)
                            .getBooleanValue()) {
                        nExecutionEngines.addExecutionEngine(selected);
                        return NOptional.of(selected);
                    }
                }
            }
        }
        if (searchRemoteInstallations) {
            // [4] look if remotely this version could be installed (1.8 after confirmation)
            NExecutionEngineLocation[] found = Stream.of(searchRemoteLocationsAndInstall(
                    jdk ? NExecutionEngineLocation.JAVA_PRODUCT_JDK : NExecutionEngineLocation.JAVA_PRODUCT_JRE
                    , NBlankable.isBlank(javaVersion) ? NVersion.BLANK : NVersion.of(javaVersion))).filter(
                    x ->
                            (!jdk || NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(x.getProduct())) &&
                                    requestedVersionFilterBigger.test(NVersion.get(x.getVersion()).orNull())).toArray(NExecutionEngineLocation[]::new);
            if (found.length > 0) {
                NExecutionEngineLocation[] sorted = Arrays.stream(found).sorted(lowerVersionFirst).toArray(NExecutionEngineLocation[]::new);
                for (NExecutionEngineLocation selected : sorted) {
                    if (NIn.ask().forBoolean(
                                    NMsg.ofC("Found this installation : %s (located at %s). Would you like to auto-configure and use it?", javaVersion, selected.getName(), selected.getPath())
                            ).setDefaultValue(true)
                            .getBooleanValue()) {
                        nExecutionEngines.addExecutionEngine(selected);
                        return NOptional.of(selected);
                    }
                }
            }
        }
        NTrace.println(NMsg.ofC("Java %s not found", javaVersion));
        return NOptional.ofNamedEmpty(NMsg.ofC("Java %s", javaVersion));
    }

    public Predicate<NVersion> validateJavaVersion(NVersionFilter versionFilter) {
        return new SpecialJavaVersionPredicate(versionFilter);
    }

    public NExecutionEngineLocation[] searchRemoteLocationsAndInstall(String product, NVersion version) {
        NOptional<NExecutionEngineLocation> e = resolveAndInstall(product, version, null, null);
        if (e.isPresent()) {
            return new NExecutionEngineLocation[]{e.get()};
        }
        return new NExecutionEngineLocation[0];
    }

    public NExecutionEngineLocation[] searchJdkLocations() {
        String[] conf = {};
        switch (NEnv.of().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm", (System.getProperty("java.home") + "/.jdk")};
                break;
            }
            case WINDOWS: {
                conf = new String[]{
                        NPlatformHome.USER.getWindowsProgramFiles() + "\\Java",
                        NPlatformHome.USER.getWindowsProgramFilesX86() + "\\Java",
                        (System.getProperty("java.home") + "\\.jdk")
                };
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework",
                        (System.getProperty("java.home") + "/.jdk")
                };
                break;
            }
        }
        List<NExecutionEngineLocation> all = new ArrayList<>();
        for (String s : conf) {
            all.addAll(Arrays.asList(searchJdkLocations(NPath.of(s))));
        }
        return all.toArray(new NExecutionEngineLocation[0]);
    }

    public Future<NExecutionEngineLocation[]> searchJdkLocationsFuture() {
        LinkedHashSet<String> conf = new LinkedHashSet<>();
        NPath file = NPath.of(System.getProperty("java.home")).normalize();
        List<Future<NExecutionEngineLocation[]>> all = new ArrayList<>();
        NExecutionEngineLocation base = resolveJdkLocation(file, null);
        if (base != null) {
            all.add(CompletableFuture.completedFuture(new NExecutionEngineLocation[]{base}));
        }
        switch (NEnv.of().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf.addAll(Arrays.asList("/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"));
                break;
            }
            case WINDOWS: {
                conf.addAll(Arrays.asList(
                        NPlatformHome.USER.getWindowsProgramFiles() + "\\Java",
                        NPlatformHome.USER.getWindowsProgramFilesX86() + "\\Java"
                ));
                break;
            }
            case MACOS: {
                conf.addAll(Arrays.asList("/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"));
                break;
            }
        }
        for (String s : conf) {
            all.add(searchJdkLocationsFuture(NPath.of(s)));
        }
        return NConcurrent.of().executorService().submit(() -> {
            List<NExecutionEngineLocation> locs = new ArrayList<>();
            for (Future<NExecutionEngineLocation[]> nutsSdkLocationFuture : all) {
                NExecutionEngineLocation[] e = nutsSdkLocationFuture.get();
                if (e != null) {
                    locs.addAll(Arrays.asList(e));
                }
            }
            locs.sort(new NSdkLocationComparator());
            return locs.toArray(new NExecutionEngineLocation[0]);
        });
    }

    public NExecutionEngineLocation[] searchJdkLocations(NPath loc) {
        List<NExecutionEngineLocation> all = new ArrayList<>();
        if (loc.isDirectory()) {
            for (NPath d : loc.list()) {
                NExecutionEngineLocation r = resolveJdkLocation(d, null);
                if (r != null) {
                    all.add(r);
//                    if (session != null && session.isPlainTrace()) {
//                            NTexts factory = NTexts.of();
//                            NOut.println(NMsg.ofC("detected java %s %s at %s", r.getPackaging(),
//                                    factory.ofStyled(r.getVersion(), NutsTextStyle.version()),
//                                    factory.ofStyled(r.getPath(), NutsTextStyle.path())
//                            );
//                    }
                }
            }
        }
        all.sort(new NSdkLocationComparator());
        return all.toArray(new NExecutionEngineLocation[0]);
    }

    public Future<NExecutionEngineLocation[]> searchJdkLocationsFuture(NPath s) {
        List<Future<NExecutionEngineLocation>> all = new ArrayList<>();
        if (s == null) {
            return CompletableFuture.completedFuture(new NExecutionEngineLocation[0]);
        } else if (s.isDirectory()) {
            for (NPath d : s.list()) {
                all.add(
                        NConcurrent.of().executorService().submit(() -> {
                            NExecutionEngineLocation r = null;
                            try {
                                r = resolveJdkLocation(d, null);
                                if (r != null) {
                                    synchronized (workspace) {
                                        NTexts factory = NTexts.of();
                                        workspace.currentSession().getTerminal().printProgress(
                                                NMsg.ofC("detected java %s %s at %s", r.getProduct(),
                                                        factory.ofStyled(r.getVersion(), NTextStyle.version()),
                                                        NCoreLogUtils.forProgressPathString(r.getPath()))
                                        );
                                    }
                                }
                            } catch (Exception ex) {
                                _LOG().log(NMsg.ofC("error: %s", ex).asError(ex));
                            }
                            return r;
                        })
                );
            }
        }
        return NConcurrent.of().executorService().submit(() -> {
            List<NExecutionEngineLocation> locs = new ArrayList<>();
            for (Future<NExecutionEngineLocation> nutsSdkLocationFuture : all) {
                NExecutionEngineLocation e = nutsSdkLocationFuture.get();
                if (e != null) {
                    locs.add(e);
                }
            }
            //just reset the line!
            workspace.currentSession().getTerminal().printProgress(NMsg.ofPlain(""));
            return locs.toArray(new NExecutionEngineLocation[0]);
        });
    }

    public String detectJdkProvider(String name) {
        for (String s : new String[]{
                "bellsoft:Bellsoft",
                "oracle:Oracle",
                "openj9:Eclipse OpenJ9",
                "liberica:Bellsoft Liberica",
                "sap:SAP",
                "graalvm:GraalVM",
                "temurin:IBM Temurin",
                "sapmachine:SAP Machine",
                "corretto:Amazon Corretto",
                "jbr:Jetbrains",
                "azul:Azul Zulu",
                "zulu:Azul Zulu",
                "semeru:IBM Semeru",


        }) {
            String[] t = s.split(":");
            if (name.toLowerCase().matches(".*\\b" + t[0].toLowerCase() + "\\b.*")) {
                return t[1];
            }
        }
        return null;
    }

    public NExecutionEngineLocation resolveJdkLocation(NPath path, String preferredName) {
        NAssert.requireNonBlank(path, "path");
        String appSuffix = NEnv.of().getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
        NPath bin = path.resolve("bin");
        NPath javaExePath = bin.resolve("java" + appSuffix);
        if (!javaExePath.isRegularFile()) {
            return null;
        }
        String vendor = null;
//        String product = null;
        String variant = null;
        String jdkVersion = null;
        String cmdOutputString = null;
        int cmdRresult = 0;
        boolean loggedError = false;
        try {
            //I do not know why but sometimes, the process exists before receiving stdout result!!
            final int MAX_ITER = 5;
            for (int i = 0; i < MAX_ITER; i++) {
                NExec cmd = NExec.of()
                        .system()
                        .addCommand(javaExePath.toString(), "-version")
                        .grabAll().failFast().run();
                cmdRresult = cmd.getResultCode();
                cmdOutputString = cmd.getGrabbedOutString();
                if (!cmdOutputString.isEmpty()) {
                    break;
                } else {
                    _LOG()
                            .log(NMsg.ofJ("unable to execute {0}. returned empty string ({1}/{2})", javaExePath, i + 1, MAX_ITER)
                                    .withLevel(i == (MAX_ITER - 1) ? Level.WARNING : Level.FINER).withIntent(NMsgIntent.ALERT)
                            );
                }
            }
            if (!cmdOutputString.isEmpty()) {
                String prefix = "java version \"";
                int i = cmdOutputString.indexOf(prefix);
                if (i >= 0) {
                    i = i + prefix.length();
                    int j = cmdOutputString.indexOf("\"", i);
                    if (i >= 0) {
                        jdkVersion = cmdOutputString.substring(i, j);
                        vendor = "Oracle";
                    }
                }
                if (jdkVersion == null) {
                    prefix = "openjdk version \"";
                    i = cmdOutputString.indexOf(prefix);
                    if (i >= 0) {
                        i = i + prefix.length();
                        int j = cmdOutputString.indexOf("\"", i);
                        if (i > 0) {
                            jdkVersion = cmdOutputString.substring(i, j);
                            if (cmdOutputString.contains("Temurin")) {
                                vendor = "Temurin";
                            } else {
                                vendor = "OpenJDK";
                            }
                        }
                    }
                }
                if (cmdOutputString.contains("Server VM") && cmdOutputString.contains("mixed mode")) {
                    variant = "hotspot";
                }
                if (cmdOutputString.contains("Eclipse OpenJ9")) {
                    variant = "openj9";
                }
                String uu = detectJdkProvider(path.getName());
                if (uu != null) {
                    vendor += " " + uu.trim();
                }
            }
        } catch (Exception ex) {
            loggedError = true;
            _LOG()
                    .log(NMsg.ofJ("unable to execute {0}. JDK Home ignored", javaExePath).asErrorAlert(ex));
        }
        if (jdkVersion == null) {
            if (!loggedError) {
                _LOG()
                        .log(NMsg.ofJ("execute {0} failed with result code {1} and result string \"{2}\". JDK Home ignored", javaExePath.toString(), cmdRresult, cmdOutputString)
                                .withLevel(Level.SEVERE).withIntent(NMsgIntent.ALERT));
            }
            return null;
        }
        String product = NExecutionEngineLocation.JAVA_PRODUCT_JRE;
        if (bin.resolve("javac" + appSuffix).isRegularFile() && bin.resolve("jps" + appSuffix).isRegularFile()) {
            product = NExecutionEngineLocation.JAVA_PRODUCT_JDK;
        }
        if (NBlankable.isBlank(preferredName)) {
            preferredName = product + "-" + vendor + "-" + jdkVersion;
        } else {
            preferredName = NStringUtils.trim(preferredName);
        }
        NExecutionEngineLocation r = new NExecutionEngineLocation(
                NWorkspaceUtils.of(workspace).createSdkId("java", jdkVersion),
                vendor,
                product,
                variant,
                preferredName,
                path.toString(),
                jdkVersion,
                product,
                0
        );
        r.setConfigVersion(DefaultNWorkspace.VERSION_SDK_LOCATION);
        return r;
    }

    public NId createJdkId(String version) {
        NAssert.requireNonBlank(version, "version");
        NVersion jv = NVersion.get(version).get();
        long n1 = jv.getNumberLiteralAt(0).flatMap(NLiteral::asLong).orElse(0L);
        long n2 = jv.getNumberLiteralAt(1).flatMap(NLiteral::asLong).orElse(0L);
        long classFileId = 0;
        String standard = n1 + "." + n2;
        if (n1 == 1) {
            classFileId = 44 + n2;
        }
        if (classFileId == 0) {
            classFileId = 52;
        }
        return NIdBuilder.of().setArtifactId("java")
                .setProperty("s", standard)
                .setProperty("c", String.valueOf(classFileId))
                .setVersion(version)
                .build();
    }

    public NOptional<String> resolveJavaCommandByVersion(String requestedJavaVersion, boolean javaw, boolean jdk, boolean ifNotFoundSearchLocally, boolean ifNotFoundSearchRemotely) {
        NOptional<NExecutionEngineLocation> nutsPlatformLocation = resolveJdkLocation(requestedJavaVersion, jdk, ifNotFoundSearchLocally, ifNotFoundSearchRemotely);
        if (nutsPlatformLocation.isPresent()) {
            return resolveJavaCommandByVersion(nutsPlatformLocation.get(), javaw);
        } else {
            return NOptional.ofNamedEmpty(NMsg.ofC("java '%s'", requestedJavaVersion));
        }
    }

    public NOptional<String> resolveJavaCommandByVersion(NExecutionEngineLocation nutsPlatformLocation, boolean javaw) {
        if (nutsPlatformLocation == null) {
            return NOptional.of("command");
        }
        String bestJavaPath = nutsPlatformLocation.getPath();
        //if (bestJavaPath.contains("/") || bestJavaPath.contains("\\") || bestJavaPath.equals(".") || bestJavaPath.equals("..")) {
        NPath file = NPath.of(bestJavaPath).toAbsolute(NWorkspace.of().getWorkspaceLocation());
        //if (file.isDirectory() && file.resolve("bin").isDirectory()) {
        boolean winOs = NEnv.of().getOsFamily() == NOsFamily.WINDOWS;
        if (winOs) {
            if (javaw) {
                bestJavaPath = file.resolve("bin").resolve("javaw.exe").toString();
            } else {
                bestJavaPath = file.resolve("bin").resolve("java.exe").toString();
            }
        } else {
            bestJavaPath = file.resolve("bin").resolve("java").toString();
        }
        //}
        //}
        return NOptional.ofNamed(bestJavaPath, "command");
    }

    public static int normalizeJavaVersionAsInt(NVersion version) {
        if (version == null) {
            return -1;
        }
        int min = -1;
        for (NVersionInterval nVersionInterval : version.filter().intervals().orElse(new ArrayList<>())) {
            String lowerBound = nVersionInterval.getLowerBound();
            String upperBound = nVersionInterval.getLowerBound();
            int m = normalizeJavaVersionAsInt0(lowerBound);
            if (m > 0) {
                if (min < m) {
                    min = m;
                }
            }
            m = normalizeJavaVersionAsInt0(upperBound);
            if (m > 0) {
                if (min < m) {
                    min = m;
                }
            }
        }
        return min;
    }

    private static int normalizeJavaVersionAsInt0(String sVersion) {
        if (sVersion == null) {
            return -1;
        }
        NVersion v = NVersion.of(sVersion);
        int i1 = v.getIntegerAt(0).orElse(0);
        int i2 = v.getIntegerAt(1).orElse(0);
        if (i1 <= 0) {
            return -1;
        }
        if (i1 == 1) {
            if (i2 <= 1) {
                return i1;
            }
            return i2;
        }
        return -1;
    }

    public String resolveJavaCommandByHome(String javaHome) {
        String appSuffix = NEnv.of().getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
        String exe = "java" + appSuffix;
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (NBlankable.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    private static class ByVersionSorter implements Function<NExecutionEngineLocation, NVersion> {
        @Override
        public NVersion apply(NExecutionEngineLocation x) {
            return NVersion.get(x.getVersion()).orNull();
        }
    }

    private static class SpecialJavaVersionPredicate implements Predicate<NVersion> {
        private final NVersionFilter versionFilter;
        private final boolean alwaysTrue;

        public SpecialJavaVersionPredicate(NVersionFilter versionFilter) {
            this.versionFilter = versionFilter;
            this.alwaysTrue = NBlankable.isBlank(versionFilter);
        }

        @Override
        public boolean test(NVersion found) {
            if (alwaysTrue) {
                return true;
            }
            if (NBlankable.isBlank(found)) {
                return false;
            }
            if (versionFilter.acceptVersion(found)) {
                return true;
            }
            List<NVersionInterval> intervalls = versionFilter.intervals().orElse(new ArrayList<>());
            for (NVersionInterval nVersionInterval : intervalls) {
                if (nVersionInterval.isFixedValue()) {
                    NVersion expected = NVersion.get(nVersionInterval.getLowerBound()).orNull();
                    int expected_0 = expected.getIntegerAt(0).orElse(0);
                    int expected_1 = expected.getIntegerAt(1).orElse(0);
                    int expected_2 = expected.getIntegerAt(2).orElse(0);
                    if (expected_0 == 1) {
                        expected_0 = expected_1;
                        expected_1 = expected_2;
                        expected_2 = 0;
                    }
                    int found_0 = found.getIntegerAt(0).orElse(0);
                    int found_1 = found.getIntegerAt(1).orElse(0);
                    int found_2 = found.getIntegerAt(2).orElse(0);
                    if (found_0 == 1) {
                        found_0 = found_1;
                        found_1 = found_2;
                        found_2 = 0;
                    }
                    if (expected_0 != found_0) {
                        return false;
                    }
                    if (expected_1 != 0 && expected_1 != found_1) {
                        return false;
                    }
                    if (expected_2 != 0 && expected_2 != found_2) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    private static class NExecutionEngineLocationComparatorVersionFirstThenJdkFirst implements Comparator<NExecutionEngineLocation> {
        private final boolean lowestVersionFirst;

        public NExecutionEngineLocationComparatorVersionFirstThenJdkFirst(boolean lowestVersionFirst) {
            this.lowestVersionFirst = lowestVersionFirst;
        }

        @Override
        public int compare(NExecutionEngineLocation a, NExecutionEngineLocation b) {
            int c;
            c = Integer.compare(b.getPriority(), a.getPriority()); // higher priority first
            if (c != 0) {
                return c;
            }
            NVersion v1 = NVersion.get(a.getVersion()).orElse(NVersion.BLANK);
            NVersion v2 = NVersion.get(b.getVersion()).orElse(NVersion.BLANK);
            if (lowestVersionFirst) {
                c = v1.compareTo(v2);
            } else {
                c = v2.compareTo(v1);
            }
            if (c != 0) {
                return c;
            }
            c = Integer.compare(
                    (NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(a.getProduct()) ? 0 : 1),
                    (NExecutionEngineLocation.JAVA_PRODUCT_JDK.equalsIgnoreCase(b.getProduct()) ? 0 : 1)
            );
            if (c != 0) {
                return c;
            }
            c = NStringUtils.trim(a.getName()).compareTo(NStringUtils.trim(b.getName()));
            if (c != 0) {
                return c;
            }
            c = NStringUtils.trim(a.getVariant()).compareTo(NStringUtils.trim(b.getVariant()));
            if (c != 0) {
                return c;
            }
            c = NStringUtils.trim(a.getPath()).compareTo(NStringUtils.trim(b.getPath()));
            if (c != 0) {
                return c;
            }
            c = NStringUtils.trim(a.getPackaging()).compareTo(NStringUtils.trim(b.getPackaging()));
            if (c != 0) {
                return c;
            }
            return 0;
        }
    }
}
