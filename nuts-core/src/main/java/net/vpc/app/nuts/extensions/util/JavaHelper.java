package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsCommandExecBuilder;
import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.io.FileUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaHelper {

    public static NutsSdkLocation[] searchJdkLocations(NutsWorkspace ws, PrintStream out) {
        String[] conf = {};
        switch (ws.getConfigManager().getPlatformOs().getName()) {
            case "linux": {
                conf = new String[]{
                        "/usr/java",
                        "/usr/lib64/jvm",
                        "/usr/lib/jvm"
                };
                break;
            }
            case "windows": {
                conf = new String[]{
                        StringUtils.coalesce(System.getenv("ProgramFiles"), "C:\\Program Files") + "\\Java",
                        StringUtils.coalesce(System.getenv("ProgramFiles(x86)"), "C:\\Program Files (x86)") + "\\Java"
                };
                break;
            }
            case "mac": {
                conf = new String[]{
                        "/Library/Java/JavaVirtualMachines",
                        "/System/Library/Frameworks/JavaVM.framework"
                };
                break;
            }
        }
        List<NutsSdkLocation> all = new ArrayList<>();
        for (String s : conf) {
            all.addAll(Arrays.asList(searchJdkLocations(ws,s,out)));
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public static NutsSdkLocation[] searchJdkLocations(NutsWorkspace ws, String s, PrintStream out) {
        List<NutsSdkLocation> all = new ArrayList<>();
        File p = new File(s);
        if (p.isDirectory()) {
            for (File d : p.listFiles()) {
                NutsSdkLocation r = resolveJdkLocation(d.getPath(), ws);
                if (r != null) {
                    all.add(r);
                    if (out != null) {
                        out.printf("Detected SDK [[%s]] at ==%s==\n", r.getVersion(), r.getPath());
                    }
                }
            }
        }
        return all.toArray(new NutsSdkLocation[0]);
    }

    public static NutsSdkLocation resolveJdkLocation(String path, NutsWorkspace ws) {
        if (path == null) {
            return null;
        }
        File f = new File(path);
        if (!f.isDirectory()) {
            return null;
        }
        File javaExePath = new File(f, FileUtils.getNativePath("bin/java"));
        if (!javaExePath.exists()) {
            return null;
        }
        String type = null;
        String jdkVersion = null;
        try {
            NutsCommandExecBuilder b = ws.createExecBuilder()
                    .setNativeCommand(true)
                    .setCommand(javaExePath.getPath(), "-version")
                    .setRedirectErrorStream()
                    .grabOutputString()
                    .exec();
            if (b.getResult() == 0) {
                String s = b.getOutputString();
                if (s.length() > 0) {
                    String prefix = "java version \"";
                    int i = s.indexOf(prefix);
                    if (i >= 0) {
                        i = i + prefix.length();
                        int j = s.indexOf("\"", i);
                        if (i >= 0) {
                            jdkVersion = s.substring(i, j);
                            type = "JDK";
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
                                type = "OpenJDK";
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(JavaHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jdkVersion == null) {
            return null;
        }
        NutsSdkLocation loc = new NutsSdkLocation();
        loc.setName(type + " " + jdkVersion);
        loc.setVersion(jdkVersion);
        loc.setPath(path);
        return loc;
    }
}
