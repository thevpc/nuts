package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.core.NClassLoaderNode;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NPlatformFamily;
import net.thevpc.nuts.platform.NPlatformHome;
import net.thevpc.nuts.platform.NPlatformLocation;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NSdkLocationComparator;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.logging.Level;

public class NJavaSdkUtils {

    private final NWorkspace workspace;

    private NJavaSdkUtils(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public static NJavaSdkUtils of(NWorkspace ws) {
        return new NJavaSdkUtils(ws);
//        NutsJavaSdkUtils wp = (NutsJavaSdkUtils) ws.env().getProperty(NutsJavaSdkUtils.class.getName());
//        if (wp == null) {
//            wp = new NutsJavaSdkUtils(ws);
//            ws.env().setProperty(NutsJavaSdkUtils.class.getName(), wp);
//        }
//        return wp;
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
            return NPlatformFamily.JAVA == NPlatformFamily.parse(id.getArtifactId()).orNull();
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

    public NVersionFilter createVersionFilter(String requestedJavaVersion) {
        requestedJavaVersion = NStringUtils.trim(requestedJavaVersion);
        NVersion vv = NVersion.get(requestedJavaVersion).get();
        String singleVersion = vv.asSingleValue().orNull();
        if (singleVersion != null) {
            requestedJavaVersion = "[" + singleVersion + ",[";
        }
        return NVersionFilters.of().byValue(requestedJavaVersion).get();
    }

    public NPlatformLocation resolveJdkLocation(String requestedJavaVersion) {
        NVersionFilter requestedVersionFilter = createVersionFilter(requestedJavaVersion);
        NPlatformLocation bestJava = workspace
                .findPlatformByVersion(NPlatformFamily.JAVA, requestedVersionFilter).orNull();
        if (bestJava == null) {
            String appSuffix = NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
            String packaging = "jre";
            if (new File(System.getProperty("java.home"), "bin" + File.separator + "javac" + (appSuffix)).isFile()) {
                packaging = "jdk";
            }
            String product = "JDK";
            NPlatformLocation current = new NPlatformLocation(
                    NWorkspace.of().getPlatform(),
                    product,
                    product + "-" + System.getProperty("java.version"),
                    System.getProperty("java.home"),
                    System.getProperty("java.version"),
                    packaging,
                    0
            );
            current.setConfigVersion(DefaultNWorkspace.VERSION_SDK_LOCATION);
            NVersionFilter requestedJavaVersionFilter = NVersion.get(NStringUtils.trim(requestedJavaVersion)).get().filter();
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.acceptVersion(NVersion.get(current.getVersion()).get())) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!NBlankable.isBlank(requestedJavaVersion)) {
                    _LOG()
                            .log(NMsg.ofJ("no valid JRE found. recommended {0} . Using default java.home at {1}", requestedJavaVersion, System.getProperty("java.home"))
                                    .asFineAlert());
                } else {
                    _LOG()
                            .log(NMsg.ofJ("no valid JRE found. Using default java.home at {0}", System.getProperty("java.home"))
                                    .asFineAlert()
                            );
                }
                bestJava = current;
            }
        }
        String sVersion = bestJava.getVersion();
        if (createVersionFilterPredicate(requestedVersionFilter).test(sVersion)) {
            return bestJava;
        }
        _LOG()
                .log(NMsg.ofJ("no valid JRE found for version {0}", requestedJavaVersion)
                        .asFineAlert()
                );
        return null;
    }

    public NPlatformLocation[] searchJdkLocations() {
        String[] conf = {};
        switch (NWorkspace.of().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"};
                break;
            }
            case WINDOWS: {
                conf = new String[]{
                        NPlatformHome.USER.getWindowsProgramFiles() + "\\Java",
                        NPlatformHome.USER.getWindowsProgramFilesX86() + "\\Java"
                };
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"};
                break;
            }
        }
        List<NPlatformLocation> all = new ArrayList<>();
        for (String s : conf) {
            all.addAll(Arrays.asList(searchJdkLocations(NPath.of(s))));
        }
        return all.toArray(new NPlatformLocation[0]);
    }

    public Future<NPlatformLocation[]> searchJdkLocationsFuture() {
        LinkedHashSet<String> conf = new LinkedHashSet<>();
        NPath file = NPath.of(System.getProperty("java.home")).normalize();
        List<Future<NPlatformLocation[]>> all = new ArrayList<>();
        NPlatformLocation base = resolveJdkLocation(file, null);
        if (base != null) {
            all.add(CompletableFuture.completedFuture(new NPlatformLocation[]{base}));
        }
        switch (NWorkspace.of().getOsFamily()) {
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
            List<NPlatformLocation> locs = new ArrayList<>();
            for (Future<NPlatformLocation[]> nutsSdkLocationFuture : all) {
                NPlatformLocation[] e = nutsSdkLocationFuture.get();
                if (e != null) {
                    locs.addAll(Arrays.asList(e));
                }
            }
            locs.sort(new NSdkLocationComparator());
            return locs.toArray(new NPlatformLocation[0]);
        });
    }

    public NPlatformLocation[] searchJdkLocations(NPath loc) {
        List<NPlatformLocation> all = new ArrayList<>();
        if (loc.isDirectory()) {
            for (NPath d : loc.list()) {
                NPlatformLocation r = resolveJdkLocation(d, null);
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
        return all.toArray(new NPlatformLocation[0]);
    }

    public Future<NPlatformLocation[]> searchJdkLocationsFuture(NPath s) {
        List<Future<NPlatformLocation>> all = new ArrayList<>();
        if (s == null) {
            return CompletableFuture.completedFuture(new NPlatformLocation[0]);
        } else if (s.isDirectory()) {
            for (NPath d : s.list()) {
                all.add(
                        NConcurrent.of().executorService().submit(() -> {
                            NPlatformLocation r = null;
                            try {
                                r = resolveJdkLocation(d, null);
                                if (r != null) {
                                    synchronized (workspace) {
                                        NTexts factory = NTexts.of();
                                        workspace.currentSession().getTerminal().printProgress(
                                                NMsg.ofC("detected java %s %s at %s", r.getPackaging(),
                                                        factory.ofStyled(r.getVersion(), NTextStyle.version()),
                                                        factory.ofStyled(r.getPath(), NTextStyle.path()))
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
            List<NPlatformLocation> locs = new ArrayList<>();
            for (Future<NPlatformLocation> nutsSdkLocationFuture : all) {
                NPlatformLocation e = nutsSdkLocationFuture.get();
                if (e != null) {
                    locs.add(e);
                }
            }
            //just reset the line!
            workspace.currentSession().getTerminal().printProgress(NMsg.ofPlain(""));
            return locs.toArray(new NPlatformLocation[0]);
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

    public NPlatformLocation resolveJdkLocation(NPath path, String preferredName) {
        NAssert.requireNonBlank(path, "path");
        String appSuffix = NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
        NPath bin = path.resolve("bin");
        NPath javaExePath = bin.resolve("java" + appSuffix);
        if (!javaExePath.isRegularFile()) {
            return null;
        }
        String product = null;
        String jdkVersion = null;
        String cmdOutputString = null;
        int cmdRresult = 0;
        boolean loggedError = false;
        try {
            //I do not know why but sometimes, the process exists before receiving stdout result!!
            final int MAX_ITER = 5;
            for (int i = 0; i < MAX_ITER; i++) {
                NExecCmd cmd = NExecCmd.of()
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
                        product = "JDK";
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
                            product = "OpenJDK";
                        }
                    }
                }
                String uu = detectJdkProvider(path.getName());
                if (uu != null) {
                    product += " " + uu.trim();
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
        String packaging = "jre";
        if (bin.resolve("javac" + appSuffix).isRegularFile() && bin.resolve("jps" + appSuffix).isRegularFile()) {
            packaging = "jdk";
        }
        if (NBlankable.isBlank(preferredName)) {
            preferredName = product + "-" + jdkVersion;
        } else {
            preferredName = NStringUtils.trim(preferredName);
        }
        NPlatformLocation r = new NPlatformLocation(
                NWorkspaceUtils.of(workspace).createSdkId("java", jdkVersion),
                product,
                preferredName,
                path.toString(),
                jdkVersion,
                packaging,
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

    public String resolveJavaCommandByVersion(String requestedJavaVersion, boolean javaw) {
        NPlatformLocation nutsPlatformLocation = resolveJdkLocation(requestedJavaVersion);
        return resolveJavaCommandByVersion(nutsPlatformLocation, javaw);
    }

    public String resolveJavaCommandByVersion(NPlatformLocation nutsPlatformLocation, boolean javaw) {
        if (nutsPlatformLocation == null) {
            return null;
        }
        String bestJavaPath = nutsPlatformLocation.getPath();
        //if (bestJavaPath.contains("/") || bestJavaPath.contains("\\") || bestJavaPath.equals(".") || bestJavaPath.equals("..")) {
        NPath file = NPath.of(bestJavaPath).toAbsolute(NWorkspace.of().getWorkspaceLocation());
        //if (file.isDirectory() && file.resolve("bin").isDirectory()) {
        boolean winOs = NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS;
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
        return bestJavaPath;
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
        String appSuffix = NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
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
}
