package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.IProcessExecHelper;
import net.thevpc.nuts.runtime.bundles.io.ProcessBuilder2;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.standalone.util.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.logging.Level;

public class ProcessExecHelper implements IProcessExecHelper {

    ProcessBuilder2 pb;
    NutsSession session;
    NutsPrintStream out;

    public ProcessExecHelper(ProcessBuilder2 pb, NutsSession session, NutsPrintStream out) {
        this.pb = pb;
        this.session = session;
        this.out = out;
    }

    public static ProcessExecHelper ofArgs(String[] args, Map<String, String> env, Path directory, NutsSessionTerminal prepareTerminal,
                                           NutsSessionTerminal execTerminal, boolean showCommand, boolean failFast, long sleep,
                                           boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                           NutsRunAs runAs,
                                           NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        List<String> newCommands = buildEffectiveCommand(args, runAs, session);
        NutsPrintStream out = null;
        NutsPrintStream err = null;
        InputStream in = null;
        ProcessBuilder2 pb = new ProcessBuilder2(session);
        pb.setCommand(newCommands)
                .setEnv(env)
                .setDirectory(directory == null ? null : directory.toFile())
                .setSleepMillis(sleep)
                .setFailFast(failFast);
        if (!inheritSystemIO) {
            if (inputFile == null) {
                in = execTerminal.in();
                if (ws.io().setSession(session).isStandardInputStream(in)) {
                    in = null;
                }
            }
            if (outputFile == null) {
                out = execTerminal.out();
                if (ws.io().setSession(session).isStandardOutputStream(out)) {
                    out = null;
                }
            }
            err = execTerminal.err();
            if (ws.io().setSession(session).isStandardErrorStream(err)) {
                err = null;
            }
            if (out != null) {
                out.run(NutsTerminalCommand.MOVE_LINE_START);
            }
        }
        if (out == null && err == null && in == null && inputFile == null && outputFile == null) {
            pb.inheritIO();
            if (redirectErr) {
                pb.setRedirectErrorStream();
            }
        } else {
            if (inputFile == null) {
                pb.setIn(in);
            } else {
                pb.setRedirectFileInput(inputFile);
            }
            if (outputFile == null) {
                pb.setOutput(out == null ? null : out.asPrintStream());
            } else {
                pb.setRedirectFileOutput(outputFile);
            }
            if (redirectErr) {
                pb.setRedirectErrorStream();
            } else {
                pb.setErr(err == null ? null : err.asPrintStream());
            }
        }

        NutsLogger _LL = session.getWorkspace().log().setSession(session).of(NutsWorkspaceUtils.class);
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NutsLogVerb.START).formatted().log("[exec] {0}",
                    ws.text().forCode("sh",
                            pb.getCommandString()
                    ));
        }
        if (showCommand || CoreBooleanUtils.getSysBoolNutsProperty("show-command", false)) {
            if (prepareTerminal.out().mode() == NutsTerminalMode.FORMATTED) {
                prepareTerminal.out().printf("%s ", ws.text().forStyled("[exec]", NutsTextStyle.primary4()));
                prepareTerminal.out().println(ws.text().forCode("sh", pb.getCommandString()));
            } else {
                prepareTerminal.out().print("exec ");
                prepareTerminal.out().printf("%s%n", pb.getCommandString());
            }
        }
        return new ProcessExecHelper(pb, session, out == null ? execTerminal.out() : out);
    }

    public static ProcessExecHelper ofDefinition(NutsDefinition nutMainFile,
                                                 String[] args, Map<String, String> env, String directory, Map<String, String> execProperties, boolean showCommand, boolean failFast, long sleep, boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                                 NutsRunAs runAs,
                                                 NutsSession session,
                                                 NutsSession execSession
    ) throws NutsExecutionException {
        NutsWorkspace workspace = execSession.getWorkspace();
        NutsId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getPath();
        String storeFolder = nutMainFile.getInstallInformation().getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
//        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
//            map.put((String) entry.getKey(), (String) entry.getValue());
//        }
        for (Map.Entry<String, String> entry : execProperties.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        Path nutsJarFile = workspace.fetch().setNutsApi().setSession(CoreNutsUtils.silent(session)).getResultPath();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.toAbsolutePath().normalize().toString());
        }
        map.put("nuts.id", id.getLongName());
        map.put("nuts.id.version", id.getVersion().getValue());
        map.put("nuts.id.name", id.getArtifactId());
        map.put("nuts.id.simpleName", id.getShortName());
        map.put("nuts.id.group", id.getGroupId());
        map.put("nuts.file", nutMainFile.getPath().toString());
        String defaultJavaCommand = NutsJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion("", false, session);

        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", workspace.locations().getWorkspaceLocation());
        map.put("nuts.version", id.getVersion().getValue());
        map.put("nuts.name", id.getArtifactId());
        map.put("nuts.group", id.getGroupId());
        map.put("nuts.face", id.getFace());
        map.put("nuts.repo", id.getRepository());
        map.put("nuts.id", id.toString());
        if (installerFile != null) {
            map.put("nuts.installer", installerFile.toString());
        }
        if (storeFolder == null && installerFile != null) {
            map.put("nuts.store", installerFile.getParent().toString());
        } else if (storeFolder != null) {
            map.put("nuts.store", storeFolder);
        }
        if (env != null) {
            map.putAll(env);
        }
        Function<String, String> mapper = new Function<String, String>() {
            @Override
            public String apply(String skey) {
                if (skey.equals("java") || skey.startsWith("java#")) {
                    String javaVer = skey.substring(4);
                    if (NutsUtilStrings.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return NutsJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion(javaVer, false, session);
                } else if (skey.equals("javaw") || skey.startsWith("javaw#")) {
                    String javaVer = skey.substring(4);
                    if (NutsUtilStrings.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return NutsJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion(javaVer, true, session);
                } else if (skey.equals("nuts")) {
                    NutsDefinition nutsDefinition;
                    nutsDefinition = workspace.fetch().setId(NutsConstants.Ids.NUTS_API)
                            .setSession(session).getResultDefinition();
                    if (nutsDefinition.getPath() != null) {
                        return ("<::expand::> " + apply("java") + " -jar " + nutsDefinition.getPath());
                    }
                    return null;
                }
                return map.get(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            if (!NutsUtilStrings.isBlank(k)) {
                k = k.replace('.', '_');
                if (!NutsUtilStrings.isBlank(e.getValue())) {
                    envmap.put(k, e.getValue());
                }
            }
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = NutsUtilStrings.trim(StringPlaceHolderParser.replaceDollarPlaceHolders(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, workspace.commandLine().parse(s).toStringArray());
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        Path path = Paths.get(workspace.locations().getWorkspaceLocation()).resolve(args[0]).normalize();
        if (Files.exists(path)) {
            CoreIOUtils.setExecutable(path);
        }
        Path pdirectory = null;
        if (NutsUtilStrings.isBlank(directory)) {
            pdirectory = Paths.get(workspace.locations().getWorkspaceLocation());
        } else {
            pdirectory = Paths.get(workspace.locations().getWorkspaceLocation()).resolve(directory);
        }
        return ofArgs(args, envmap, pdirectory, session.getTerminal(), execSession.getTerminal(), showCommand, failFast,
                sleep,
                inheritSystemIO, redirectErr, inputFile, outputFile,runAs,
                session);
    }

    private static String resolveRootUserName(NutsSession session) {
        NutsOsFamily sysFamily = session.getWorkspace().env().getOsFamily();
        switch (sysFamily) {
            case WINDOWS: {
                String s = (String) session.getProperty("WINDOWS_ROOT_USER");
                if (s == null) {
                    s = session.getWorkspace().env().getEnv("WINDOWS_ROOT_USER", null);
                }
                if (NutsUtilStrings.isBlank(s)) {
                    s = "Administrator";
                }
                return s;
            }
            default: {
                return "root";
            }
        }
    }

    private static List<String> buildEffectiveCommand(String[] cmd, NutsRunAs runAsMode, NutsSession session) {
        //String runAsEffective = null;
        NutsOsFamily sysFamily = session.getWorkspace().env().getOsFamily();
        List<String> command = new ArrayList<>(Arrays.asList(cmd));
        if (runAsMode == null) {
            runAsMode = NutsRunAs.CURRENT_USER;
        }
        boolean runWithGui = session.isGui() && session.getWorkspace().env().isGraphicalDesktopEnvironment();
        String rootUserName = resolveRootUserName(session);
        String currentUserName = System.getProperty("user.name");

        //optimize mode
        switch (runAsMode.getMode()) {
            case ROOT: {
                if (rootUserName.equals(currentUserName)) {
                    runAsMode = NutsRunAs.currentUser();
                }
                break;
            }
            case USER: {
                String s = runAsMode.getUser();
                s = s.trim();
                if (currentUserName.equals(s)) {
                    runAsMode = NutsRunAs.currentUser();
                }
                if (!s.equals(runAsMode.getUser())) {
                    runAsMode = NutsRunAs.user(s);
                }
                break;
            }
        }

//        switch (runAsMode.getMode()) {
//            case CURRENT_USER: {
//                break;
//            }
//            case USER: {
//                if (NutsUtilStrings.isBlank(runAsUser)
//                        || System.getProperty("user.name").equals(runAsUser.trim())
//                ) {
//                    runAsMode = NutsExecutionType.SYSTEM;
//                    runAsEffective = null;
//                } else {
//                    runAsEffective = runAsUser.trim();
//                }
//                break;
//            }
//            case SYSTEM_ROOT: {
//                switch (sysFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case WINDOWS: {
//                        runAsEffective = rootUserName;
//                        break;
//                    }
//                    default: {
//                        throw new NutsIllegalArgumentException(session, NutsMessage.plain("cannot run as admin/root on unknown system OS family"));
//                    }
//                }
//                if (System.getProperty("user.name").equals(runAsEffective.trim())) {
//                    runAsMode = NutsExecutionType.SYSTEM_USER.SYSTEM;
//                    runAsEffective = null;
//                }
//                break;
//            }
//            case SYSTEM_SUDO: {
//                runAsEffective = null;
//                break;
//            }
//        }
        switch (runAsMode.getMode()) {
            case CURRENT_USER: {
                List<String> cc = new ArrayList<>();
                cc.addAll(command);
                return cc;
            }
            case ROOT:
            case USER: {
                String runAsEffective = runAsMode.getMode() == NutsRunAs.Mode.USER ? runAsMode.getUser() : rootUserName;
                List<String> cc = new ArrayList<>();
                switch (sysFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX: {
                        if (runWithGui) {
                            NutsDesktopEnvironmentFamily[] de = session.getWorkspace().env().getDesktopEnvironmentFamilies();
                            Path kdesu = CoreIOUtils.sysWhich("kdesu");
                            Path gksu = CoreIOUtils.sysWhich("gksu");
                            String currSu = null;
                            if (Arrays.stream(de).anyMatch(x -> x == NutsDesktopEnvironmentFamily.KDE)) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            } else if (Arrays.stream(de).anyMatch(x -> x == NutsDesktopEnvironmentFamily.KDE)) {
                                if (gksu != null) {
                                    currSu = gksu.toString();
                                }
                            }
                            if (currSu == null) {
                                if (gksu != null) {
                                    currSu = gksu.toString();
                                }
                            }
                            if (currSu == null) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            }
                            if (currSu == null) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.plain("unable to resolve gui su application (kdesu,gksu,...)"));
                            }
                            cc.add(currSu);
                            cc.add(runAsEffective);
                        } else {
                            Path su = CoreIOUtils.sysWhich("su");
                            if (su == null) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.plain("unable to resolve su application"));
                            }
                            cc.add(su.toString());
                            cc.add("-c");
                            cc.add(runAsEffective);
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(Arrays.asList("runas", "/noprofile", "/user:" + runAsEffective));
                        break;
                    }
                    default: {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot run as %s on unknown system OS family",runAsEffective));
                    }
                }
                cc.addAll(command);
                return cc;
            }
            case SUDO: {
                List<String> cc = new ArrayList<>();
                switch (sysFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX: {
                        if (runWithGui) {
                            NutsDesktopEnvironmentFamily[] de = session.getWorkspace().env().getDesktopEnvironmentFamilies();
                            Path kdesu = CoreIOUtils.sysWhich("kdesudo");
                            Path gksu = CoreIOUtils.sysWhich("gksudo");
                            String currSu = null;
                            if (Arrays.stream(de).anyMatch(x -> x == NutsDesktopEnvironmentFamily.KDE)) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            } else if (Arrays.stream(de).anyMatch(x -> x == NutsDesktopEnvironmentFamily.KDE)) {
                                if (gksu != null) {
                                    currSu = gksu.toString();
                                }
                            }
                            if (currSu == null) {
                                if (gksu != null) {
                                    currSu = gksu.toString();
                                }
                            }
                            if (currSu == null) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            }
                            if (currSu == null) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.plain("unable to resolve gui su application (kdesu,gksu,...)"));
                            }
                            cc.add(currSu);
                        } else {
                            Path su = CoreIOUtils.sysWhich("sudo");
                            if (su == null) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.plain("unable to resolve su application"));
                            }
                            cc.add(su.toString());
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(Arrays.asList("runas", "/noprofile", "/user:" + rootUserName));
                        break;
                    }
                    default: {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot run sudo %s on unknown system OS family",currentUserName));
                    }
                }
                cc.addAll(command);
                return cc;
            }
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.plain("cannot run as admin/root on unknown system OS family"));
    }

    public void dryExec() {
        if (out.mode() == NutsTerminalMode.FORMATTED) {
            out.print("[dry] ==[exec]== ");
            out.println(pb.getFormattedCommandString(session));
        } else {
            out.print("[dry] exec ");
            out.printf("%s%n", pb.getCommandString());
        }
    }

    public int exec() {
        try {
            if (out != null) {
                out.resetLine();//.run(NutsTerminalCommand.MOVE_LINE_START);
            }
            ProcessBuilder2 p = pb.start();
            return p.waitFor().getResult();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Future<Integer> execAsync() {
        try {
            if (out != null) {
                out.run(NutsTerminalCommand.MOVE_LINE_START);
            }
            ProcessBuilder2 p = pb.start();
            return new FutureTask<Integer>(() -> p.waitFor().getResult());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}