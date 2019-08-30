package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsJavaSdkUtils {
    private static final Logger LOG = Logger.getLogger(NutsJavaSdkUtils.class.getName());

    public static NutsSdkLocation resolveJdkLocation(NutsWorkspace ws, String requestedJavaVersion) {
        requestedJavaVersion = CoreStringUtils.trim(requestedJavaVersion);
        NutsSdkLocation bestJava = ws.config().getSdk("java", requestedJavaVersion);
        if (bestJava == null) {
            String appSuffix = ws.config().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
            String packaging = "jre";
            if (new File(System.getProperty("java.home"), "bin" + File.separator + "javac" + (appSuffix)).isFile()) {
                packaging = "jdk";
            }
            String product = "JDK";
            NutsSdkLocation current = new NutsSdkLocation(
                    ws.config().getPlatform(),
                    product,
                    product + "-" + System.getProperty("java.version"),
                    System.getProperty("java.home"),
                    System.getProperty("java.version"),
                    packaging
            );
            NutsVersionFilter requestedJavaVersionFilter = ws.version().parse(requestedJavaVersion).filter();
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.accept(DefaultNutsVersion.valueOf(current.getVersion()), ws.createSession())) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!CoreStringUtils.isBlank(requestedJavaVersion)) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "No valid JRE found. recommended {0} . Using default java.home at {1}", new Object[]{requestedJavaVersion, System.getProperty("java.home")});
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "No valid JRE found. Using default java.home at {0}", System.getProperty("java.home"));
                    }
                }
                bestJava = current;
            }
        }
        return bestJava;
    }

    public static NutsSdkLocation[] searchJdkLocations(NutsWorkspace ws, NutsSession session) {
        String[] conf = {};
        switch (ws.config().getOsFamily()) {
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
            all.addAll(Arrays.asList(searchJdkLocations(ws, ws.io().path(s), session)));
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public static NutsSdkLocation[] searchJdkLocations(NutsWorkspace ws, Path s, NutsSession session) {
        List<NutsSdkLocation> all = new ArrayList<>();
        if (Files.isDirectory(s)) {
            try (final DirectoryStream<Path> it = Files.newDirectoryStream(s)) {
                for (Path d : it) {
                    NutsSdkLocation r = resolveJdkLocation(ws, d, null);
                    if (r != null) {
                        all.add(r);
                        if (session != null && session.isPlainTrace()) {
                            session.out().printf("detected java %s [[%s]] at ==%s==%n", r.getPackaging(), r.getVersion(), r.getPath());
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public static NutsSdkLocation resolveJdkLocation(NutsWorkspace ws, Path path, String preferredName) {
        if (path == null) {
            throw new NutsException(ws, "Missing path");
        }
        String appSuffix = ws.config().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
        Path bin = path.resolve("bin");
        Path javaExePath = bin.resolve("java" + appSuffix);
        if (!Files.isRegularFile(javaExePath)) {
            return null;
        }
        String product = null;
        String jdkVersion = null;
        try {
            String s = ws.exec().syscall().command(javaExePath.toString(), "-version")
                    .redirectErrorStream()
                    .grabOutputString().failFast().run().getOutputString();
            if (s.length() > 0) {
                String prefix = "java version \"";
                int i = s.indexOf(prefix);
                if (i >= 0) {
                    i = i + prefix.length();
                    int j = s.indexOf("\"", i);
                    if (i >= 0) {
                        jdkVersion = s.substring(i, j);
                        product = "JDK";
                    }
                }
                if (jdkVersion == null) {

                    prefix = "openjdk version \"";
                    i = s.indexOf(prefix);
                    if (i >= 0) {
                        i = i + prefix.length();
                        int j = s.indexOf("\"", i);
                        if (i > 0) {
                            jdkVersion = s.substring(i, j);
                            product = "OpenJDK";
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Unable to execute " + javaExePath.toString() + ". JDK Home ignored", ex);
        }
        if (jdkVersion == null) {
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
        return new NutsSdkLocation(
                NutsWorkspaceUtils.createSdkId(ws, "java", jdkVersion),
                product,
                preferredName,
                path.toString(),
                jdkVersion,
                packaging
        );
    }

    public static NutsId createJdkId(NutsWorkspace ws, String version) {
        if (CoreStringUtils.isBlank(version)) {
            throw new NutsException(ws, "Missing version");
        }
        NutsVersion jv = ws.version().parse(version);
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
        return ws.id().builder().artifactId("java")
                .property("s", standard)
                .property("c", String.valueOf(classFileId))
                .version(version)
                .build();
    }

    public static String resolveJavaCommandByVersion(String requestedJavaVersion, boolean javaw, NutsWorkspace workspace) {
        String bestJavaPath = resolveJdkLocation(workspace, requestedJavaVersion).getPath();
        if (bestJavaPath.contains("/") || bestJavaPath.contains("\\") || bestJavaPath.equals(".") || bestJavaPath.equals("..")) {
            Path file = workspace.config().getWorkspaceLocation().resolve(bestJavaPath);
            if (Files.isDirectory(file) && Files.isDirectory(file.resolve("bin"))) {
                boolean winOs = workspace.config().getOsFamily() == NutsOsFamily.WINDOWS;
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

    public static String resolveJavaCommandByHome(String javaHome, NutsWorkspace workspace) {
        String appSuffix = workspace.config().getOsFamily() == NutsOsFamily.WINDOWS ? ".exe" : "";
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
