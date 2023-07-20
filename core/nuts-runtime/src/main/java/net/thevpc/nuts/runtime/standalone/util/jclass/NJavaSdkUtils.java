package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NClassLoaderNode;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.NSdkLocationComparator;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class NJavaSdkUtils {

    private final NWorkspace ws;
    private NLog LOG;

    private NJavaSdkUtils(NWorkspace ws) {
        this.ws = ws;
    }

    public static NJavaSdkUtils of(NSession session) {
        return of(session.getWorkspace());
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

    public static List<NClassLoaderNodeExt> loadNutsClassLoaderNodeExts(NClassLoaderNode[] n, boolean java9, NSession session) {
        List<NClassLoaderNodeExt> list = new ArrayList<>();
        for (NClassLoaderNode nn : n) {
            fillNodes(nn, list, java9, session);
        }
        return list;
    }

    private static void fillNodes(NClassLoaderNode n, List<NClassLoaderNodeExt> list, boolean java9, NSession session) {
        NClassLoaderNodeExt k = new NClassLoaderNodeExt();
        k.node = n;
        k.id = NId.of(n.getId()).get( session);
        k.path = NPath.of(n.getURL(), session);
        if (java9) {
            k.moduleInfo = JavaJarUtils.parseModuleInfo(k.path, session);
            if (k.moduleInfo != null) {
                k.moduleName = k.moduleInfo.module_name;
                for (JavaClassByteCode.ModuleInfoRequired r : k.moduleInfo.required) {
                    if (r.req_name.startsWith("javafx")) {
                        k.requiredJfx.add(r.req_name);
                    }
                }
            } else {
                k.moduleName = JavaJarUtils.parseDefaultModuleName(k.path, session);
            }
            k.jfx = k.moduleName != null && k.moduleName.startsWith("javafx");

        } else {
            k.jfx = k.id.getArtifactId().startsWith("javafx") &&
                    k.id.getGroupId().startsWith("org.openjfx");
        }
        list.add(k);
        for (NClassLoaderNode d : n.getDependencies()) {
            fillNodes(d, list, java9, session);
        }
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(NJavaSdkUtils.class, session);
        }
        return LOG;
    }

    public NPlatformLocation resolveJdkLocation(String requestedJavaVersion, NSession session) {
        String _requestedJavaVersion = requestedJavaVersion;
        requestedJavaVersion = NStringUtils.trim(requestedJavaVersion);
        NVersion vv = NVersion.of(requestedJavaVersion).get( session);
        String singleVersion = vv.asSingleValue().orNull();
        if (singleVersion!=null) {
            requestedJavaVersion = "[" + singleVersion + ",[";
        }
        NVersionFilter requestedVersionFilter = NVersionFilters.of(session).byValue(requestedJavaVersion).get();
        NPlatforms platforms = NPlatforms.of(session);
        NPlatformLocation bestJava = platforms
                .findPlatformByVersion(NPlatformFamily.JAVA, requestedVersionFilter).orNull();
        if (bestJava == null) {
            String appSuffix = NEnvs.of(session).getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
            String packaging = "jre";
            if (new File(System.getProperty("java.home"), "bin" + File.separator + "javac" + (appSuffix)).isFile()) {
                packaging = "jdk";
            }
            String product = "JDK";
            NPlatformLocation current = new NPlatformLocation(
                    NEnvs.of(session).getPlatform(),
                    product,
                    product + "-" + System.getProperty("java.version"),
                    System.getProperty("java.home"),
                    System.getProperty("java.version"),
                    packaging,
                    0
            );
            current.setConfigVersion(DefaultNWorkspace.VERSION_SDK_LOCATION);
            NVersionFilter requestedJavaVersionFilter = vv.filter(session);
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.acceptVersion(NVersion.of(current.getVersion()).get( session), session)) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!NBlankable.isBlank(requestedJavaVersion)) {
                    _LOGOP(session).level(Level.FINE).verb(NLogVerb.WARNING)
                            .log(NMsg.ofJ("no valid JRE found. recommended {0} . Using default java.home at {1}", requestedJavaVersion, System.getProperty("java.home")));
                } else {
                    _LOGOP(session).level(Level.FINE).verb(NLogVerb.WARNING)
                            .log(NMsg.ofJ("no valid JRE found. Using default java.home at {0}", System.getProperty("java.home")));
                }
                bestJava = current;
            }
        }
        String sVersion = bestJava.getVersion();
        if (requestedVersionFilter.acceptVersion(NVersion.of(sVersion).get( session), session)) {
            return bestJava;
        }
        // replace 1.6 by 6, and 1.8 by 8
        int a = sVersion.indexOf('.');
        if (a > 0) {
            NLiteral p = NLiteral.of(sVersion.substring(0, a));
            if (p.isInt() && p.asInt().get() == 1) {
                String sVersion2 = sVersion.substring(a + 1);
                NVersion version2 = NVersion.of(sVersion2).get(session);
                if (requestedVersionFilter.acceptVersion(version2, session)) {
                    return bestJava;
                }
            }
        }
        _LOGOP(session).level(Level.FINE).verb(NLogVerb.WARNING)
                .log(NMsg.ofJ("no valid JRE found for version {0}", _requestedJavaVersion));
        return null;
    }

    public NPlatformLocation[] searchJdkLocations(NSession session) {
        String[] conf = {};
        switch (NEnvs.of(session).getOsFamily()) {
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
            all.addAll(Arrays.asList(searchJdkLocations(s, session)));
        }
        return all.toArray(new NPlatformLocation[0]);
    }

    public Future<NPlatformLocation[]> searchJdkLocationsFuture(NSession session) {
        LinkedHashSet<String> conf = new LinkedHashSet<>();
        File file = new File(System.getProperty("java.home"));
        try {
            file = file.getCanonicalFile();
        } catch (IOException ex) {
            //
        }

        List<Future<NPlatformLocation[]>> all = new ArrayList<>();
        NPlatformLocation base = resolveJdkLocation(file.getPath(), null, session);
        if (base != null) {
            all.add(CompletableFuture.completedFuture(new NPlatformLocation[]{base}));
        }
        switch (NEnvs.of(session).getOsFamily()) {
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
            all.add(searchJdkLocationsFuture(Paths.get(s), session));
        }
        return NScheduler.of(session).executorService().submit(() -> {
            List<NPlatformLocation> locs = new ArrayList<>();
            for (Future<NPlatformLocation[]> nutsSdkLocationFuture : all) {
                NPlatformLocation[] e = nutsSdkLocationFuture.get();
                if (e != null) {
                    locs.addAll(Arrays.asList(e));
                }
            }
            locs.sort(new NSdkLocationComparator(session));
            return locs.toArray(new NPlatformLocation[0]);
        });
    }

    public NPlatformLocation[] searchJdkLocations(String loc, NSession session) {
        Path s = Paths.get(loc);
        List<NPlatformLocation> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    NPlatformLocation r = resolveJdkLocation(d.toString(), null, session);
                    if (r != null) {
                        all.add(r);
                        if (session != null && session.isPlainTrace()) {
//                            NTexts factory = NTexts.of(session);
//                            session.out().println(NMsg.ofC("detected java %s %s at %s", r.getPackaging(),
//                                    factory.ofStyled(r.getVersion(), NutsTextStyle.version()),
//                                    factory.ofStyled(r.getPath(), NutsTextStyle.path())
//                            );
                        }
                    }
                }
            } catch (IOException ex) {
                throw new NIOException(session, ex);
            }
        }
        all.sort(new NSdkLocationComparator(session));
        return all.toArray(new NPlatformLocation[0]);
    }

    public Future<NPlatformLocation[]> searchJdkLocationsFuture(Path s, NSession session) {
        List<Future<NPlatformLocation>> all = new ArrayList<>();
        if (s == null) {
            return CompletableFuture.completedFuture(new NPlatformLocation[0]);
        } else if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    all.add(
                            NScheduler.of(session).executorService().submit(() -> {
                                NPlatformLocation r = null;
                                try {
                                    r = resolveJdkLocation(d.toString(), null, session);
                                    if (r != null) {
                                        synchronized (session.getWorkspace()) {
                                            NTexts factory = NTexts.of(session);
                                            session.getTerminal().printProgress(
                                                    NMsg.ofC("detected java %s %s at %s", r.getPackaging(),
                                                            factory.ofStyled(r.getVersion(), NTextStyle.version()),
                                                            factory.ofStyled(r.getPath(), NTextStyle.path()))
                                            );
                                        }
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                return r;
                            })
                    );
                }
            } catch (IOException ex) {
                throw new NIOException(session, ex);
            }
        }
        return NScheduler.of(session).executorService().submit(() -> {
            List<NPlatformLocation> locs = new ArrayList<>();
            for (Future<NPlatformLocation> nutsSdkLocationFuture : all) {
                NPlatformLocation e = nutsSdkLocationFuture.get();
                if (e != null) {
                    locs.add(e);
                }
            }
            //just reset the line!
            session.getTerminal().printProgress(NMsg.ofPlain(""));
            return locs.toArray(new NPlatformLocation[0]);
        });
    }

    public NPlatformLocation resolveJdkLocation(String path, String preferredName, NSession session) {
        NSessionUtils.checkSession(ws, session);
        NAssert.requireNonBlank(path, "path", session);
        String appSuffix = NEnvs.of(session).getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
        Path bin = Paths.get(path).resolve("bin");
        Path javaExePath = bin.resolve("java" + appSuffix);
        if (!Files.isRegularFile(javaExePath)) {
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
                NExecCommand cmd = NExecCommand.of(session)
                        .setExecutionType(NExecutionType.SYSTEM)
                        .addCommand(javaExePath.toString(), "-version")
                        .redirectErrorStream()
                        .grabOutputString().setFailFast(true).run();
                cmdRresult = cmd.getResult();
                cmdOutputString = cmd.getOutputString();
                if (cmdOutputString.length() > 0) {
                    break;
                } else {
                    _LOGOP(session).level(i == (MAX_ITER - 1) ? Level.WARNING : Level.FINER).verb(NLogVerb.WARNING)
                            .log(NMsg.ofJ("unable to execute {0}. returned empty string ({1}/{2})", javaExePath, i + 1, MAX_ITER));
                }
            }
            if (cmdOutputString.length() > 0) {
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
            }
        } catch (Exception ex) {
            loggedError = true;
            _LOGOP(session).error(ex).level(Level.SEVERE).verb(NLogVerb.WARNING)
                    .log(NMsg.ofJ("unable to execute {0}. JDK Home ignored", javaExePath));
        }
        if (jdkVersion == null) {
            if (!loggedError) {
                _LOGOP(session).level(Level.SEVERE).verb(NLogVerb.WARNING)
                        .log(NMsg.ofJ("execute {0} failed with result code {1} and result string \"{2}\". JDK Home ignored", javaExePath.toString(), cmdRresult, cmdOutputString));
            }
            return null;
        }
        String packaging = "jre";
        if (Files.isRegularFile(bin.resolve("javac" + appSuffix)) && Files.isRegularFile(bin.resolve("jps" + appSuffix))) {
            packaging = "jdk";
        }
        if (NBlankable.isBlank(preferredName)) {
            preferredName = product + "-" + jdkVersion;
        } else {
            preferredName = NStringUtils.trim(preferredName);
        }
        NPlatformLocation r = new NPlatformLocation(
                NWorkspaceUtils.of(session).createSdkId("java", jdkVersion),
                product,
                preferredName,
                path,
                jdkVersion,
                packaging,
                0
        );
        r.setConfigVersion(DefaultNWorkspace.VERSION_SDK_LOCATION);
        return r;
    }

    public NId createJdkId(String version, NSession session) {
        NAssert.requireNonBlank(version, "version", session);
        NVersion jv = NVersion.of(version).get( session);
        long n1 = jv.getNumber(0).flatMap(NLiteral::asLong).orElse(0L);
        long n2 = jv.getNumber(1).flatMap(NLiteral::asLong).orElse(0L);
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

    public String resolveJavaCommandByVersion(String requestedJavaVersion, boolean javaw, NSession session) {
        NPlatformLocation nutsPlatformLocation = resolveJdkLocation(requestedJavaVersion, session);
        return resolveJavaCommandByVersion(nutsPlatformLocation, javaw, session);
    }

    public String resolveJavaCommandByVersion(NPlatformLocation nutsPlatformLocation, boolean javaw, NSession session) {
        if (nutsPlatformLocation == null) {
            return null;
        }
        String bestJavaPath = nutsPlatformLocation.getPath();
        //if (bestJavaPath.contains("/") || bestJavaPath.contains("\\") || bestJavaPath.equals(".") || bestJavaPath.equals("..")) {
        NPath file = NPath.of(bestJavaPath, session).toAbsolute(NLocations.of(session).getWorkspaceLocation());
        //if (file.isDirectory() && file.resolve("bin").isDirectory()) {
        boolean winOs = NEnvs.of(session).getOsFamily() == NOsFamily.WINDOWS;
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

    public String resolveJavaCommandByHome(String javaHome, NSession session) {
        String appSuffix = NEnvs.of(session).getOsFamily() == NOsFamily.WINDOWS ? ".exe" : "";
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
