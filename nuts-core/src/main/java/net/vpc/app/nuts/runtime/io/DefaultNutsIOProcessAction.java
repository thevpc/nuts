package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.io.DefaultNutsProcessInfo;
import net.vpc.app.nuts.runtime.util.NutsCollectionResult;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.ProcessBuilder2;
import net.vpc.app.nuts.runtime.util.iter.IteratorBuilder;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public class DefaultNutsIOProcessAction implements NutsIOProcessAction {
    private String processType;
    private NutsWorkspace ws;
    private NutsSession session;
    private boolean failFast;

    public DefaultNutsIOProcessAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsIOProcessAction setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NutsIOProcessAction failFast(boolean failFast) {
        return setFailFast(failFast);
    }

    @Override
    public NutsIOProcessAction failFast() {
        return failFast(true);
    }

    @Override
    public NutsIOProcessAction session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOProcessAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public String getType() {
        return processType;
    }

    public NutsIOProcessAction setType(String processType) {
        this.processType = processType;
        return this;
    }

    @Override
    public NutsIOProcessAction type(String processType) {
        return setType(processType);
    }

    private static String getJpsJavaHome(NutsWorkspace ws, String version) {
        List<String> detectedJavaHomes = new ArrayList<>();
        String jh = System.getProperty("java.home");
        detectedJavaHomes.add(jh);
        String v = getJpsJavaHome(jh);
        if (v != null) {
            return v;
        }
        NutsSession session = ws.createSession();
        NutsVersionFilter nvf = CoreStringUtils.isBlank(version) ? null : ws.version().parse(version).filter();
        List<NutsSdkLocation> availableJava = new ArrayList<>();
        for (NutsSdkLocation java : ws.config().getSdks("java", session)) {
            if ("jdk".equals(java.getPackaging()) && (nvf == null || nvf.accept(ws.version().parse(java.getVersion()), session))) {
                availableJava.add(java);
            }
        }
        availableJava.sort((o1, o2) -> -ws.version().parse(o1.getVersion()).compareTo(o2.getVersion()));
        for (NutsSdkLocation java : availableJava) {
            detectedJavaHomes.add(java.getPath());
            v = getJpsJavaHome(java.getPath());
            if (v != null) {
                return v;
            }
        }
        throw new NutsExecutionException(ws, "Unable to resolve a valid jdk installation. " +
                "Either run nuts with a valid JDK/SDK (not JRE) or register a valid one using nadmin tool. " +
                "All the followings are invalid : \n"
                + String.join("\n", detectedJavaHomes)
                , 10);
    }

    private static String getJpsJavaHome(String base) {
        File jh = new File(base);
        if (new File(jh, ".." + File.separator + "bin" + File.separator + "jps").exists()) {
            return jh.getParent();
        }
        if (new File(jh, "bin" + File.separator + "jps").exists()) {
            return jh.getPath();
        }
        return null;
    }

    @Override
    public NutsResultList<NutsProcessInfo> getResultList() {
        String processType = CoreStringUtils.trim(getType());
        if (processType.toLowerCase().startsWith("java#")) {
            return getResultListJava(processType.substring("java#".length()));
        } else if (processType.toLowerCase().equals("java")) {
            return getResultListJava("");
        } else {
            if (isFailFast()) {
                throw new NutsIllegalArgumentException(ws, "Unsupported list processes of type : " + processType);
            }
            return new NutsCollectionResult<>(ws, "process-" + processType, Collections.emptyList());
        }
    }

    private NutsResultList<NutsProcessInfo> getResultListJava(String version) {
        Iterator<NutsProcessInfo> it = IteratorBuilder.ofLazy(() -> {
            String cmd = "jps";
            ProcessBuilder2 b = null;
            boolean mainArgs = true;
            boolean vmArgs = true;
            try {
                String jdkHome = getJpsJavaHome(ws,version);
                if (jdkHome != null) {
                    cmd = jdkHome + File.separator + "bin" + File.separator + cmd;
                }
                b = new ProcessBuilder2(ws)
                        .addCommand(cmd)
                        .addCommand("-l" + (mainArgs ? "m" : "") + (vmArgs ? "v" : ""))
                        .setRedirectErrorStream(true)
                        .grabOutputString();
                b.waitFor();
            } catch (IOException ex) {
                if (isFailFast()) {
                    throw new UncheckedIOException(ex);
                }
            }
            if (b != null && b.getResult() == 0) {
                String out = b.getOutputString();
                String[] split = out.split("\n");
                return Arrays.asList(split).iterator();
            }
            return IteratorUtils.emptyIterator();
        }).map(line -> {
            int s1 = line.indexOf(' ');
            int s2 = line.indexOf(' ', s1 + 1);
            String pid = line.substring(0, s1).trim();
            String cls = line.substring(s1 + 1, s2 < 0 ? line.length() : s2).trim();
            String args = s2 >= 0 ? line.substring(s2 + 1).trim() : "";
            return (NutsProcessInfo) new DefaultNutsProcessInfo(
                    pid, cls, null, args
            );
        }).build();
        return new NutsCollectionResult<NutsProcessInfo>(ws, "process-" + getType(), it);
    }
}
