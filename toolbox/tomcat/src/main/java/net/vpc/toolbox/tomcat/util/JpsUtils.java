package net.vpc.toolbox.tomcat.util;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.io.JpsResult;
import net.vpc.common.io.PosApis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JpsUtils {
    private static String getJpsJavaHome(String base) {
        File jh = new File(base);
        if (new File(jh, ".."+File.separator+"bin"+File.separator+"jps").exists()) {
            return jh.getParent();
        }
        if (new File(jh, "bin"+File.separator+"jps").exists()) {
            return jh.getPath();
        }
        return null;
    }

    private static String getJpsJavaHome(NutsApplicationContext context) {
        List<String> detectedJavaHomes = new ArrayList<>();
        String jh = System.getProperty("java.home");
        detectedJavaHomes.add(jh);
        String v = getJpsJavaHome(jh);
        if (v != null) {
            return v;
        }
        NutsWorkspace ws = context.getWorkspace();
        for (NutsSdkLocation java : ws.config().getSdks("java")) {
            if ("jdk".equals(java.getPackaging())
                    && ws.version().parse(java.getVersion()).compareTo("1.8") >= 0) {
                detectedJavaHomes.add(java.getPath());
                v = getJpsJavaHome(java.getPath());
                if (v != null) {
                    return v;
                }
            }
        }
        throw new NutsExecutionException(ws, "Unable to resolve a valid jdk installation. " +
                "Either run nuts with a valid JDK/SDK (not JRE) or register a valid one using nadmin tool. " +
                "All the followings are invalid : \n"
                + String.join("\n", detectedJavaHomes)
                , 10);
    }

    public static JpsResult[] getRunningJava(NutsApplicationContext context, String className) throws IOException {
        return PosApis.get().findJavaProcessList(getJpsJavaHome(context), true, true,
                (p) -> p.getClassName().equals(className)
        );
    }
}
