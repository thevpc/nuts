package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.util.NIteratorBuilder;

import java.io.File;
import java.io.StringReader;
import java.util.*;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;

import net.thevpc.nuts.runtime.standalone.util.stream.NStreamEmpty;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamFromNIterator;
import net.thevpc.nuts.util.*;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNPs implements NPs {

    private NExecutionEngineFamily platformFamily;
    private NConnectionString connectionString;
    private boolean failFast;

    public DefaultNPs() {
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NConnectionString getConnectionString() {
        return connectionString;
    }

    @Override
    public NPs setConnectionString(String host) {
        this.connectionString = host == null ? null : NConnectionString.of(host);
        return this;
    }

    @Override
    public NPs at(String host) {
        return setConnectionString(host);
    }

    @Override
    public NPs setConnectionString(NConnectionString host) {
        this.connectionString = host;
        return this;
    }

    @Override
    public NPs at(NConnectionString host) {
        return setConnectionString(host);
    }

    @Override
    public NPs setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public boolean isSupportedKillProcess() {
        NEnv target = NEnv.of(connectionString);
        NOsFamily f = target.getOsFamily();

        return f == NOsFamily.LINUX || f == NOsFamily.MACOS || f == NOsFamily.UNIX || f == NOsFamily.WINDOWS;
    }

    @Override
    public boolean killProcess(String processId) {
        NEnv target = NEnv.of(connectionString);
        NOsFamily f = target.getOsFamily();
        switch (f) {
            case LINUX:
            case MACOS:
            case UNIX: {
                return NExec.ofSystem("kill", "-9", processId)
                        .at(connectionString)
                        .setFailFast(isFailFast())
                        .getResultCode() == 0;
            }
            case WINDOWS: {
                if(NBlankable.isBlank(connectionString)) {
                    String taskkill = NWorkspace.of().findSysCommand("taskkill").orNull();
                    if (taskkill != null) {
                        return NExec.ofSystem(taskkill, "/PID", processId, "/F")
                                .at(connectionString)
                                .setFailFast(isFailFast())
                                .getResultCode() == 0;
                    }
                    throw new NUnsupportedOperationException(NMsg.ofC("unsupported kill process in : %s", NEnv.of().getOsFamily().id()));
                }else{
                    return NExec.ofSystem("taskkill", "/PID", processId, "/F")
                            .at(connectionString)
                            .setFailFast(isFailFast())
                            .getResultCode() == 0;
                }
            }
        }
        if (isFailFast()) {
            throw new NUnsupportedOperationException(NMsg.ofC("unsupported kill process in : %s", NEnv.of().getOsFamily().id()));
        } else {
            return false;
        }
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
    public NExecutionEngineFamily getPlatformFamily() {
        return platformFamily;
    }

    public NPs setPlatformFamily(NExecutionEngineFamily platformFamily) {
        this.platformFamily = platformFamily;
        return this;
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
        NExecutionEngineLocation[] availableJava = NExecutionEngines.of().findExecutionEngines(NExecutionEngineFamily.JAVA,
                java -> NExecutionEngineLocation.JAVA_PRODUCT_JDK.equals(java.getProduct()) && (nvf == null || nvf.acceptVersion(NVersion.get(java.getVersion()).get()))
        ).toArray(NExecutionEngineLocation[]::new);
        for (NExecutionEngineLocation java : availableJava) {
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
        NEnv target = NEnv.of(connectionString);
        NOsFamily cmdOsFamily = target.getOsFamily();
        NExecutionEngineFamily processType = NUtils.firstNonNull(platformFamily, NExecutionEngineFamily.OS);
        switch (processType) {
            case JAVA:
                return getResultListJava(target, cmdOsFamily);
            case OS:
                return getResultListOS(target, cmdOsFamily);
        }
        if (isFailFast()) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported list processes of type : %s", processType));
        }
        return new NStreamEmpty<>("process-" + processType.id());
    }

    private NStream<NPsInfo> getResultListOS(NEnv target, NOsFamily cmdOsFamily) {
        switch (cmdOsFamily) {
            case LINUX: {
                NExec u = NExec.of()
                        .setIn(NExecInput.ofNull())
                        .at(connectionString)
                        .addCommand("ps", "-eo", "user,pid,%cpu,%mem,vsz,rss,tty,stat,lstart,time,command")
                        .grabErr()
                        .setFailFast(isFailFast())
                        .grabOut();
                String grabbedOutString = u.getGrabbedOutString();
                return new LinuxPsParser().parse(new StringReader(grabbedOutString));
            }
            case UNIX:
            case MACOS: {
                NExec u = NExec.of()
                        .setIn(NExecInput.ofNull())
                        .at(connectionString)
                        .addCommand("ps", "aux")
                        .grabErr()
                        .setFailFast(isFailFast())
                        .grabOut();
                return new UnixPsParser().parse(new StringReader(u.getGrabbedOutString()));
            }
            case WINDOWS: {
                final int IMPL_WmiObject_Win32_Process_Csv = 1;
                final int IMPL_WmiObject_Win32_Process_NoFile = 2;
                int mode = IMPL_WmiObject_Win32_Process_NoFile;
                switch (mode) {
                    case IMPL_WmiObject_Win32_Process_Csv: {
                        return new WindowsPsCsvCaller(connectionString).call(isFailFast());
                    }
                    case IMPL_WmiObject_Win32_Process_NoFile: {
                        return new WindowsPs1Caller(connectionString).call(isFailFast());
                    }
                }
            }
        }
        if (isFailFast()) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported list processes of type : OS"));
        }
        return new NStreamEmpty<>("process");
    }


    private NStream<NPsInfo> getResultListJava(NEnv target, NOsFamily cmdOsFamily) {
        boolean remote = !NBlankable.isBlank(connectionString);
        String separator;
        if (remote) {
            switch (cmdOsFamily) {
                case WINDOWS: {
                    separator = "\\";
                    break;
                }
                default: {
                    separator = "/";
                }
            }
        } else {
            separator = File.separator;
        }
        NIterator<NPsInfo> it = NIteratorBuilder.ofSupplier(() -> {
            String cmd = "jps";
            NExec b = null;
            boolean mainArgs = true;
            boolean vmArgs = true;
            String jdkHome = null;
            if (!remote) {
                jdkHome = getJpsJavaHome2("");
            }
            if (jdkHome != null) {
                cmd = jdkHome + separator + "bin" + separator + cmd;
            }
            b = NExec.of()
                    .system()
                    .at(connectionString)
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
        }, () -> NElement.ofString("jps")).map(
                NFunction.of(
                        (String line) -> {
                            DefaultNPsInfoBuilder p = new DefaultNPsInfoBuilder();
                            int s1 = line.indexOf(' ');
                            int s2 = line.indexOf(' ', s1 + 1);
                            String pid = line.substring(0, s1).trim();
                            String cls = line.substring(s1 + 1, s2 < 0 ? line.length() : s2).trim();
                            String cmdLineString = s2 >= 0 ? line.substring(s2 + 1).trim() : "";
                            String[] parsedCmdLine = null;
                            parsedCmdLine = betterArgs(pid, target);
                            if (parsedCmdLine == null) {
                                parsedCmdLine = NCmdLine.of(cmdLineString, null).toStringArray();
                            }
                            p.setId(pid)
                                    .setName(cls)
                                    .setCmdLine(cmdLineString)
                                    .setCmdLineArgs(parsedCmdLine);
                            return p.build();
                        }).redescribe(NElementDescribables.ofDesc("processInfo"))).build();
        return new NStreamFromNIterator<>("process-" + getPlatformFamily(), it);
    }

    private String[] betterArgs(String pid, NEnv target) {
        switch (target.getOsFamily()) {
            case LINUX:
            case UNIX:
            case MACOS: {
                try {
                    NPath procFile =
                            NBlankable.isBlank(connectionString) ?
                                    NPath.of("/proc/" + pid + "/cmdline") :
                                    NPath.of(connectionString.withPath("/proc/" + pid + "/cmdline"));
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
