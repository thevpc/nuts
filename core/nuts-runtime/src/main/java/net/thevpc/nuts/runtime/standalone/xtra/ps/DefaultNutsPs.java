package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPsInfo;
import net.thevpc.nuts.io.NutsPs;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;

import java.io.File;
import java.util.*;

import net.thevpc.nuts.runtime.standalone.stream.NutsEmptyStream;
import net.thevpc.nuts.runtime.standalone.stream.NutsIteratorStream;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsFunction;
import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.util.NutsStream;
import net.thevpc.nuts.util.NutsUtilStrings;

public class DefaultNutsPs implements NutsPs {

    private String processType;
    private NutsWorkspace ws;
    private NutsSession session;
    private boolean failFast;

    public DefaultNutsPs(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    protected void checkSession() {
        NutsSessionUtils.checkSession(ws, session);
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsPs setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public boolean isSupportedKillProcess(){
        checkSession();
        NutsOsFamily f = getSession().env().getOsFamily();
        return f ==NutsOsFamily.LINUX || f ==NutsOsFamily.MACOS || f ==NutsOsFamily.UNIX;
    }


    @Override
    public boolean killProcess(String processId) {
        checkSession();
        return getSession().exec()
                .addCommand("kill", "-9", processId)
                .getResult()==0;
    }

    @Override
    public NutsPs failFast(boolean failFast) {
        return setFailFast(failFast);
    }

    @Override
    public NutsPs failFast() {
        return failFast(true);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPs setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public String getType() {
        return processType;
    }

    public NutsPs setType(String processType) {
        this.processType = processType;
        return this;
    }

    @Override
    public NutsPs type(String processType) {
        return setType(processType);
    }

    private static String getJpsJavaHome(String version, NutsSession session) {
        List<String> detectedJavaHomes = new ArrayList<>();
        String jh = System.getProperty("java.home");
        detectedJavaHomes.add(jh);
        String v = getJpsJavaHome(jh);
        if (v != null) {
            return v;
        }
        NutsVersionFilter nvf = NutsBlankable.isBlank(version) ? null : NutsVersion.of(version).get(session).filter(session);
        NutsPlatformLocation[] availableJava = session.env().platforms().setSession(session).findPlatforms(NutsPlatformFamily.JAVA,
                java -> "jdk".equals(java.getPackaging()) && (nvf == null || nvf.acceptVersion(NutsVersion.of(java.getVersion()).get(session), session))
        ).toArray(NutsPlatformLocation[]::new);
        for (NutsPlatformLocation java : availableJava) {
            detectedJavaHomes.add(java.getPath());
            v = getJpsJavaHome(java.getPath());
            if (v != null) {
                return v;
            }
        }
        throw new NutsExecutionException(session,
                NutsMessage.cstyle("unable to resolve a valid jdk installation. "
                + "Either run nuts with a valid JDK/SDK (not JRE) or register a valid one using 'nuts settings' command. "
                + "All the followings are invalid : \n%s",
                String.join("\n", detectedJavaHomes)
                ),
                 10);
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
    public NutsStream<NutsPsInfo> getResultList() {
        checkSession();
        String processType = NutsUtilStrings.trim(getType());
        if (processType.toLowerCase().startsWith("java#")) {
            return getResultListJava(processType.substring("java#".length()));
        } else if (processType.equalsIgnoreCase("java")) {
            return getResultListJava("");
        } else {
            if (isFailFast()) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported list processes of type : %s" , processType));
            }
            return new NutsEmptyStream<>(getSession(), "process-" + processType);
        }
    }

    private NutsStream<NutsPsInfo> getResultListJava(String version) {
        checkSession();
        NutsIterator<NutsPsInfo> it = IteratorBuilder.ofSupplier(() -> {
            String cmd = "jps";
            NutsExecCommand b = null;
            boolean mainArgs = true;
            boolean vmArgs = true;
            String jdkHome = getJpsJavaHome(version, session);
            if (jdkHome != null) {
                cmd = jdkHome + File.separator + "bin" + File.separator + cmd;
            }
            b = getSession().exec()
                    .setExecutionType(NutsExecutionType.SYSTEM)
                    .addCommand(cmd)
                    .addCommand("-l" + (mainArgs ? "m" : "") + (vmArgs ? "v" : ""))
                    .setRedirectErrorStream(true)
                    .grabOutputString()
                    .setFailFast(isFailFast());
            b.getResult();
            if (b.getResult() == 0) {
                String out = b.getOutputString();
                String[] split = out.split("\n");
                return Arrays.asList(split).iterator();
            }
            return IteratorBuilder.emptyIterator();
        },e-> NutsElements.of(e).ofString("jps"), session).map(
                NutsFunction.of(
                line -> {
            int s1 = line.indexOf(' ');
            int s2 = line.indexOf(' ', s1 + 1);
            String pid = line.substring(0, s1).trim();
            String cls = line.substring(s1 + 1, s2 < 0 ? line.length() : s2).trim();
            String args = s2 >= 0 ? line.substring(s2 + 1).trim() : "";
            return (NutsPsInfo) new DefaultNutsPsInfo(
                    pid, cls, null, args
            );
        },"processInfo")).build();
        return new NutsIteratorStream<>(getSession(), "process-" + getType(), it);
    }
}
