package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.NPlatformFamily;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.util.NIteratorBuilder;

import java.io.File;
import java.util.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;

import net.thevpc.nuts.runtime.standalone.util.stream.NStreamEmpty;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamFromNIterator;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

public class DefaultNPs implements NPs {

    private String processType;
    private boolean failFast;

    public DefaultNPs() {
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NPs setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public boolean isSupportedKillProcess() {
        NOsFamily f = NWorkspace.of().getOsFamily();
        return f == NOsFamily.LINUX || f == NOsFamily.MACOS || f == NOsFamily.UNIX;
    }

    @Override
    public boolean killProcess(String processId) {
        return NExecCmd.of()
                .addCommand("kill", "-9", processId)
                .getResultCode() == 0;
    }

    @Override
    public NPs failFast(boolean failFast) {
        return setFailFast(failFast);
    }

    @Override
    public NPs failFast() {
        return failFast(true);
    }


    @Override
    public String getType() {
        return processType;
    }

    public NPs setType(String processType) {
        this.processType = processType;
        return this;
    }

    @Override
    public NPs type(String processType) {
        return setType(processType);
    }

    private static String getJpsJavaHome2(String version) {
        List<String> detectedJavaHomes = new ArrayList<>();
        String jh = System.getProperty("java.home");
        detectedJavaHomes.add(jh);
        String v = getJpsJavaHome(jh);
        if (v != null) {
            return v;
        }
        NWorkspace workspace = NWorkspace.of();
        NVersionFilter nvf = NBlankable.isBlank(version) ? null : NVersion.get(version).get().filter();
        NPlatformLocation[] availableJava = workspace.findPlatforms(NPlatformFamily.JAVA,
                java -> "jdk".equals(java.getPackaging()) && (nvf == null || nvf.acceptVersion(NVersion.get(java.getVersion()).get()))
        ).toArray(NPlatformLocation[]::new);
        for (NPlatformLocation java : availableJava) {
            detectedJavaHomes.add(java.getPath());
            v = getJpsJavaHome(java.getPath());
            if (v != null) {
                return v;
            }
        }
        throw new NExecutionException(
                NMsg.ofC("unable to resolve a valid jdk installation. "
                        + "Either run nuts with a valid JDK/SDK (not JRE) or register a valid one using 'nuts settings' command. "
                        + "All the followings are invalid : \n%s",
                        String.join("\n", detectedJavaHomes)
                ),
                NExecutionException.ERROR_2);
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
    public NStream<NPsInfo> getResultList() {
        String processType = NStringUtils.trim(getType());
        if (processType.toLowerCase().startsWith("java#")) {
            return getResultListJava(processType.substring("java#".length()));
        } else if (processType.equalsIgnoreCase("java")) {
            return getResultListJava("");
        } else {
            if (isFailFast()) {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported list processes of type : %s", processType));
            }
            return new NStreamEmpty<>( "process-" + processType);
        }
    }

    private NStream<NPsInfo> getResultListJava(String version) {
        NWorkspace workspace = NWorkspace.of();
        NIterator<NPsInfo> it = NIteratorBuilder.ofSupplier(() -> {
            String cmd = "jps";
            NExecCmd b = null;
            boolean mainArgs = true;
            boolean vmArgs = true;
            String jdkHome = getJpsJavaHome2(version);
            if (jdkHome != null) {
                cmd = jdkHome + File.separator + "bin" + File.separator + cmd;
            }
            b = NExecCmd.of()
                    .system()
                    .addCommand(cmd)
                    .addCommand("-l" + (mainArgs ? "m" : "") + (vmArgs ? "v" : ""))
                    .grabAll()
                    .setFailFast(isFailFast());
            b.getResultCode();
            if (b.getResultCode() == 0) {
                String out = b.getGrabbedOutString();
                String[] split = out.split("\n");
                return Arrays.asList(split).iterator();
            }
            return NIteratorBuilder.emptyIterator();
        }, () -> NElements.of().ofString("jps")).map(
                NFunction.of(
                        (String line) -> {
                            int s1 = line.indexOf(' ');
                            int s2 = line.indexOf(' ', s1 + 1);
                            String pid = line.substring(0, s1).trim();
                            String cls = line.substring(s1 + 1, s2 < 0 ? line.length() : s2).trim();
                            String cmdLineString = s2 >= 0 ? line.substring(s2 + 1).trim() : "";
                            String[] parsedCmdLine = betterArgs(pid);
                            if (parsedCmdLine == null) {
                                parsedCmdLine= NCmdLine.of(cmdLineString,null).toStringArray();
                            }
                            return (NPsInfo) new DefaultNPsInfo(
                                    pid, cls, null, cmdLineString,parsedCmdLine
                            );
                        }).withDesc(NEDesc.of("processInfo"))).build();
        return new NStreamFromNIterator<>("process-" + getType(), it);
    }

    private String[] betterArgs(String pid) {
        switch (NWorkspace.of().getOsFamily()) {
            case LINUX:
            case UNIX:
            case MACOS: {
                try {
                    NPath procFile = NPath.of("/proc/" + pid + "/cmdline");
                    if (procFile.exists()) {
                        return procFile.readString().split("\0");
                    }
                } catch (Exception ex) {

                }
                break;
            }
        }
        return null;
    }
}
