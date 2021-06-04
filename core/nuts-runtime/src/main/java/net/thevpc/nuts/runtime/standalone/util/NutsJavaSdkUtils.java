package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.NutsSdkLocationComparator;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;

public class NutsJavaSdkUtils {

    private NutsLogger LOG;

    private NutsWorkspace ws;

    public static NutsJavaSdkUtils of(NutsSession ws) {
        return of(ws.getWorkspace());
    }

    public static NutsJavaSdkUtils of(NutsWorkspace ws) {
        return new NutsJavaSdkUtils(ws);
//        NutsJavaSdkUtils wp = (NutsJavaSdkUtils) ws.env().getProperty(NutsJavaSdkUtils.class.getName());
//        if (wp == null) {
//            wp = new NutsJavaSdkUtils(ws);
//            ws.env().setProperty(NutsJavaSdkUtils.class.getName(), wp);
//        }
//        return wp;
    }

    private NutsJavaSdkUtils(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.getWorkspace().log().of(NutsJavaSdkUtils.class);
        }
        return LOG;
    }

    public NutsSdkLocation resolveJdkLocation(String requestedJavaVersion, NutsSession session) {
        requestedJavaVersion = CoreStringUtils.trim(requestedJavaVersion);
        NutsSdkLocation bestJava = session.getWorkspace().sdks().setSession(session).findByVersion("java",
                session.getWorkspace().version().filter().byValue(requestedJavaVersion)
        );
        if (bestJava == null) {
            String appSuffix = session.getWorkspace().env().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
            String packaging = "jre";
            if (new File(System.getProperty("java.home"), "bin" + File.separator + "javac" + (appSuffix)).isFile()) {
                packaging = "jdk";
            }
            String product = "JDK";
            NutsSdkLocation current = new NutsSdkLocation(
                    session.getWorkspace().env().getPlatform(),
                    product,
                    product + "-" + System.getProperty("java.version"),
                    System.getProperty("java.home"),
                    System.getProperty("java.version"),
                    packaging,
                    0
            );
            current.setConfigVersion(DefaultNutsWorkspace.VERSION_SDK_LOCATION);
            NutsVersionFilter requestedJavaVersionFilter = session.getWorkspace().version().parser().parse(requestedJavaVersion).filter();
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.acceptVersion(session.getWorkspace().version().parser().parse(current.getVersion()), session)) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!CoreStringUtils.isBlank(requestedJavaVersion)) {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.WARNING).log("No valid JRE found. recommended {0} . Using default java.home at {1}", requestedJavaVersion, System.getProperty("java.home"));
                } else {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.WARNING).log("No valid JRE found. Using default java.home at {0}", System.getProperty("java.home"));
                }
                bestJava = current;
            }
        }
        return bestJava;
    }

    public NutsSdkLocation[] searchJdkLocations(NutsSession session) {
        String[] conf = {};
        switch (session.getWorkspace().env().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"};
                break;
            }
            case WINDOWS: {
                conf = new String[]{CoreStringUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files") + "\\Java", CoreStringUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)") + "\\Java"};
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"};
                break;
            }
        }
        List<NutsSdkLocation> all = new ArrayList<>();
        for (String s : conf) {
            all.addAll(Arrays.asList(searchJdkLocations(s, session)));
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public Future<NutsSdkLocation[]> searchJdkLocationsFuture(NutsSession session) {
        String[] conf = {};
        switch (session.getWorkspace().env().getOsFamily()) {
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                conf = new String[]{"/usr/java", "/usr/lib64/jvm", "/usr/lib/jvm"};
                break;
            }
            case WINDOWS: {
                conf = new String[]{CoreStringUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files") + "\\Java", CoreStringUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)") + "\\Java"};
                break;
            }
            case MACOS: {
                conf = new String[]{"/Library/Java/JavaVirtualMachines", "/System/Library/Frameworks/JavaVM.framework"};
                break;
            }
        }
        List<Future<NutsSdkLocation[]>> all = new ArrayList<>();
        for (String s : conf) {
            all.add(searchJdkLocationsFuture(Paths.get(s), session));
        }
        return session.getWorkspace().concurrent().executorService().submit(new Callable<NutsSdkLocation[]>() {
            @Override
            public NutsSdkLocation[] call() throws Exception {
                List<NutsSdkLocation> locs = new ArrayList<>();
                for (Future<NutsSdkLocation[]> nutsSdkLocationFuture : all) {
                    NutsSdkLocation[] e = nutsSdkLocationFuture.get();
                    if (e != null) {
                        locs.addAll(Arrays.asList(e));
                    }
                }
                locs.sort(new NutsSdkLocationComparator(session.getWorkspace()));
                return locs.toArray(new NutsSdkLocation[0]);
            }
        });
    }

    public NutsSdkLocation[] searchJdkLocations(String loc, NutsSession session) {
        Path s = Paths.get(loc);
        List<NutsSdkLocation> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    NutsSdkLocation r = resolveJdkLocation(d.toString(), null, session);
                    if (r != null) {
                        all.add(r);
                        if (session != null && session.isPlainTrace()) {
                            NutsTextManager factory = session.getWorkspace().text();
                            session.out().printf("detected java %s %s at %s%n", r.getPackaging(),
                                    factory.forStyled(r.getVersion(), NutsTextStyle.version()),
                                    factory.forStyled(r.getPath(), NutsTextStyle.path())
                            );
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        all.sort(new NutsSdkLocationComparator(session.getWorkspace()));
        return all.toArray(new NutsSdkLocation[0]);
    }

    public Future<NutsSdkLocation[]> searchJdkLocationsFuture(Path s, NutsSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        List<Future<NutsSdkLocation>> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    all.add(
                            session.getWorkspace().concurrent().executorService().submit(new Callable<NutsSdkLocation>() {
                                @Override
                                public NutsSdkLocation call() throws Exception {
                                    NutsSdkLocation r = null;
                                    try {
                                        r = resolveJdkLocation(d.toString(), null, session);
                                        if (r != null) {
                                            if (session.isPlainTrace()) {
                                                synchronized (session.getWorkspace()) {
                                                    NutsTextManager factory = session.getWorkspace().text();
                                                    session.out().printf("detected java %s %s at %s%n", r.getPackaging(),
                                                            factory.forStyled(r.getVersion(), NutsTextStyle.version()),
                                                            factory.forStyled(r.getPath(), NutsTextStyle.path())
                                                    );
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    return r;
                                }
                            })
                    );
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return session.getWorkspace().concurrent().executorService().submit(new Callable<NutsSdkLocation[]>() {
            @Override
            public NutsSdkLocation[] call() throws Exception {
                List<NutsSdkLocation> locs = new ArrayList<>();
                for (Future<NutsSdkLocation> nutsSdkLocationFuture : all) {
                    NutsSdkLocation e = nutsSdkLocationFuture.get();
                    if (e != null) {
                        locs.add(e);
                    }
                }
                return locs.toArray(new NutsSdkLocation[0]);
            }
        });
    }

    public NutsSdkLocation resolveJdkLocation(String path, String preferredName, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        if (path == null) {
            throw new NutsException(session, "missing path");
        }
        String appSuffix = session.getWorkspace().env().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
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
                NutsExecCommand cmd = session.getWorkspace().exec()
                        .setSession(session)
                        .userCmd().addCommand(javaExePath.toString(), "-version")
                        .setRedirectErrorStream(true)
                        .grabOutputString().setFailFast(true).run();
                cmdRresult = cmd.getResult();
                cmdOutputString = cmd.getOutputString();
                if (cmdOutputString.length() > 0) {
                    break;
                } else {
                    _LOGOP(session).level(i == (MAX_ITER - 1) ? Level.WARNING : Level.FINER).verb(NutsLogVerb.WARNING).log("unable to execute {0}. returned empty string ({1}/{2})", javaExePath, i + 1, MAX_ITER);
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
            _LOGOP(session).error(ex).level(Level.SEVERE).verb(NutsLogVerb.WARNING).log("unable to execute {0}. JDK Home ignored", javaExePath);
        }
        if (jdkVersion == null) {
            if (!loggedError) {
                _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.WARNING).log("execute {0} failed with result code {1} and result string \"{2}\". JDK Home ignored", javaExePath.toString(), cmdRresult, cmdOutputString);
            }
            return null;
        }
        String packaging = "jre";
        if (Files.isRegularFile(bin.resolve("javac" + appSuffix)) && Files.isRegularFile(bin.resolve("jps" + appSuffix))) {
            packaging = "jdk";
        }
        if (CoreStringUtils.isBlank(preferredName)) {
            preferredName = product + "-" + jdkVersion;
        } else {
            preferredName = CoreStringUtils.trim(preferredName);
        }
        NutsSdkLocation r = new NutsSdkLocation(
                NutsWorkspaceUtils.of(session).createSdkId("java", jdkVersion),
                product,
                preferredName,
                path.toString(),
                jdkVersion,
                packaging,
                0
        );
        r.setConfigVersion(DefaultNutsWorkspace.VERSION_SDK_LOCATION);
        return r;
    }

    public NutsId createJdkId(String version, NutsSession session) {
        if (CoreStringUtils.isBlank(version)) {
            throw new NutsException(session, "missing version");
        }
        NutsVersion jv = session.getWorkspace().version().parser().parse(version);
        int n1 = jv.getNumber(0, 0);
        int n2 = jv.getNumber(1, 0);
        int classFileId = 0;
        String standard = n1 + "." + n2;
        if (n1 == 1) {
            classFileId = 44 + n2;
        }
        if (classFileId == 0) {
            classFileId = 52;
        }
        return session.getWorkspace().id().builder().setArtifactId("java")
                .setProperty("s", standard)
                .setProperty("c", String.valueOf(classFileId))
                .setVersion(version)
                .build();
    }

    public String resolveJavaCommandByVersion(String requestedJavaVersion, boolean javaw, NutsSession session) {
        String bestJavaPath = resolveJdkLocation(requestedJavaVersion, session).getPath();
        if (bestJavaPath.contains("/") || bestJavaPath.contains("\\") || bestJavaPath.equals(".") || bestJavaPath.equals("..")) {
            Path file = Paths.get(session.getWorkspace().locations().getWorkspaceLocation()).resolve(bestJavaPath);
            if (Files.isDirectory(file) && Files.isDirectory(file.resolve("bin"))) {
                boolean winOs = session.getWorkspace().env().getOsFamily() == NutsOsFamily.WINDOWS;
                if (winOs) {
                    if (javaw) {
                        bestJavaPath = file.resolve("bin" + File.separatorChar + "javaw.exe").toString();
                    } else {
                        bestJavaPath = file.resolve("bin" + File.separatorChar + "java.exe").toString();
                    }
                } else {
                    bestJavaPath = file.resolve("bin" + File.separatorChar + "java").toString();
                }
            }
        }
        return bestJavaPath;
    }

    public String resolveJavaCommandByHome(String javaHome, NutsSession session) {
        String appSuffix = session.getWorkspace().env().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
        String exe = "java" + appSuffix;
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (CoreStringUtils.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }
}
