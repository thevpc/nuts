package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionFilter;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.net.NConnexionString;
import net.thevpc.nuts.platform.NPlatformLocation;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NPlatformFamily;
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
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.*;

public class DefaultNPs implements NPs {

    private NPlatformFamily platformFamily;
    private String connexionString;
    private boolean failFast;

    public DefaultNPs() {
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public String getConnexionString() {
        return connexionString;
    }

    @Override
    public NPs setConnexionString(String host) {
        this.connexionString = host;
        return this;
    }

    @Override
    public NPs at(String host) {
        return setConnexionString(host);
    }

    @Override
    public NPs setConnexionString(NConnexionString host) {
        this.connexionString = host == null ? "" : host.toString();
        return this;
    }

    @Override
    public NPs at(NConnexionString host) {
        return setConnexionString(host);
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
        switch (NWorkspace.of().getOsFamily()) {
            case LINUX:
            case MACOS:
            case UNIX: {
                return NExecCmd.ofSystem("kill", "-9", processId)
                        .setFailFast(isFailFast())
                        .getResultCode() == 0;
            }
            case WINDOWS: {
                String taskkill = NWorkspace.of().findSysCommand("taskkill").orNull();
                if (taskkill != null) {
                    return NExecCmd.ofSystem(taskkill, "/PID", processId, "/F")
                            .setFailFast(isFailFast())
                            .getResultCode() == 0;
                }
                throw new NUnsupportedOperationException(NMsg.ofC("unsupported kill process in : %s", NWorkspace.of().getOsFamily().id()));
            }
        }
        if (isFailFast()) {
            throw new NUnsupportedOperationException(NMsg.ofC("unsupported kill process in : %s", NWorkspace.of().getOsFamily().id()));
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
    public NPlatformFamily getPlatformFamily() {
        return platformFamily;
    }

    public NPs setPlatformFamily(NPlatformFamily platformFamily) {
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
        if (NBlankable.isBlank(connexionString)) {
            NPlatformFamily processType = NUtils.firstNonNull(platformFamily, NPlatformFamily.OS);
            switch (processType) {
                case JAVA:
                    return getResultListJava();
                case OS:
                    return getResultListOS();
            }
            if (isFailFast()) {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported list processes of type : %s", processType));
            }
            return new NStreamEmpty<>("process-" + processType.id());
        } else {
            String str = NExecCmd.of("ps", "--json", "aux")
                    .at(connexionString)
                    .failFast()
                    .getGrabbedOutOnlyString();
            DefaultNPsInfoBuilder[] arr = NElementParser.ofJson().parse(str, DefaultNPsInfoBuilder[].class);
            return NStream.ofArray(arr).map(
                    DefaultNPsInfoBuilder::build
            );

        }
    }

    private NStream<NPsInfo> getResultListOS() {
        switch (NWorkspace.of().getOsFamily()) {
            case LINUX: {
                NExecCmd u = NExecCmd.of()
                        .setIn(NExecInput.ofNull())
                        .addCommand("ps", "-eo", "user,pid,%cpu,%mem,vsz,rss,tty,stat,lstart,time,command")
                        .grabErr()
                        .setFailFast(isFailFast())
                        .grabOut();
                String grabbedOutString = u.getGrabbedOutString();
                return new LinuxPsParser().parse(new StringReader(grabbedOutString));
            }
            case UNIX:
            case MACOS: {
                NExecCmd u = NExecCmd.of()
                        .setIn(NExecInput.ofNull())
                        .addCommand("ps", "aux")
                        .grabErr()
                        .setFailFast(isFailFast())
                        .grabOut();
                return new UnixPsParser().parse(new StringReader(u.getGrabbedOutString()));
            }
            case WINDOWS: {
                final int IMPL_WmiObject_Win32_Process_Csv=1;
                final int IMPL_WmiObject_Win32_Process_NoFile=2;
                int mode=IMPL_WmiObject_Win32_Process_Csv;
                switch (mode){
                    case IMPL_WmiObject_Win32_Process_Csv:{
                        return new WindowsPsCsvCaller().call(isFailFast());
                    }
                    case IMPL_WmiObject_Win32_Process_NoFile:{
                        return new WindowsPs1Caller().call(isFailFast());
                    }
                }
            }
        }
        if (isFailFast()) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported list processes of type : OS"));
        }
        return new NStreamEmpty<>("process");
    }

    private NStream<NPsInfo> getResultListJava() {
        NIterator<NPsInfo> it = NIteratorBuilder.ofSupplier(() -> {
            String cmd = "jps";
            NExecCmd b = null;
            boolean mainArgs = true;
            boolean vmArgs = true;
            String jdkHome = getJpsJavaHome2("");
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
        }, () -> NElement.ofString("jps")).map(
                NFunction.of(
                        (String line) -> {
                            DefaultNPsInfoBuilder p = new DefaultNPsInfoBuilder();
                            int s1 = line.indexOf(' ');
                            int s2 = line.indexOf(' ', s1 + 1);
                            String pid = line.substring(0, s1).trim();
                            String cls = line.substring(s1 + 1, s2 < 0 ? line.length() : s2).trim();
                            String cmdLineString = s2 >= 0 ? line.substring(s2 + 1).trim() : "";
                            String[] parsedCmdLine = betterArgs(pid);
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
