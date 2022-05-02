package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.NutsRecommendationPhase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.logging.Level;

public class ProcessExecHelper extends AbstractSyncIProcessExecHelper {

    NutsDefinition definition;
    ProcessBuilder2 pb;
    NutsPrintStream out;

    public ProcessExecHelper(NutsDefinition definition, ProcessBuilder2 pb, NutsSession session, NutsPrintStream out) {
        super(session);
        this.pb = pb;
        this.out = out;
        this.definition = definition;
    }

    public static ProcessExecHelper ofArgs(NutsDefinition definition, String[] args, Map<String, String> env, Path directory, NutsSessionTerminal prepareTerminal,
                                           NutsSessionTerminal execTerminal, boolean showCommand, boolean failFast, long sleep,
                                           boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                           NutsRunAs runAs,
                                           NutsSession session) {
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
                if (NutsInputStreams.of(session).isStdin(in)) {
                    in = null;
                }
            }
            if (outputFile == null) {
                out = execTerminal.out();
                if (NutsPrintStreams.of(session).isStdout(out)) {
                    out = null;
                }
            }
            err = execTerminal.err();
            if (NutsPrintStreams.of(session).isStderr(err)) {
                err = null;
            }
            if (out != null) {
                out.run(NutsTerminalCommand.MOVE_LINE_START, session);
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

        NutsLogger _LL = NutsLogger.of(NutsWorkspaceUtils.class, session);
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NutsLoggerVerb.START).log(
                    NutsMessage.jstyle("[exec] {0}",
                            NutsTexts.of(session).ofCode("system",
                                    pb.getCommandString()
                            )));
        }
        if (showCommand || CoreNutsUtils.isShowCommand(session)) {
            if (prepareTerminal.out().mode() == NutsTerminalMode.FORMATTED) {
                prepareTerminal.out().printf("%s ", NutsTexts.of(session).ofStyled("[exec]", NutsTextStyle.primary4()));
                prepareTerminal.out().println(NutsTexts.of(session).ofCode("system", pb.getCommandString()));
            } else {
                prepareTerminal.out().print("exec ");
                prepareTerminal.out().printf("%s%n", pb.getCommandString());
            }
        }
        return new ProcessExecHelper(definition,pb, session, out == null ? execTerminal.out() : out);
    }

    public static ProcessExecHelper ofDefinition(NutsDefinition nutMainFile,
                                                 String[] args, Map<String, String> env, String directory, Map<String, String> execProperties, boolean showCommand, boolean failFast, long sleep, boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                                 NutsRunAs runAs,
                                                 NutsSession session,
                                                 NutsSession execSession
    ) throws NutsExecutionException {
        NutsId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getFile();
        NutsPath storeFolder = nutMainFile.getInstallInformation().getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
//        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
//            map.put((String) entry.getKey(), (String) entry.getValue());
//        }
        for (Map.Entry<String, String> entry : execProperties.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        Path nutsJarFile = session.fetch().setNutsApi().setSession(session).getResultPath();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.toAbsolutePath().normalize().toString());
        }
        map.put("nuts.artifact", id.toString());
        map.put("nuts.file", nutMainFile.getFile().toString());
        String defaultJavaCommand = NutsJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion("", false, session);
        if (defaultJavaCommand == null) {
            throw new NutsExecutionException(session, NutsMessage.plain("no java version was found"), 1);
        }
        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", session.locations().getWorkspaceLocation().toString());
        if (installerFile != null) {
            map.put("nuts.installer", installerFile.toString());
        }
        if (storeFolder == null && installerFile != null) {
            map.put("nuts.store", installerFile.getParent().toString());
        } else if (storeFolder != null) {
            map.put("nuts.store", storeFolder.toString());
        }
        if (env != null) {
            map.putAll(env);
        }
        Function<String, String> mapper = new Function<String, String>() {
            @Override
            public String apply(String skey) {
                if (skey.equals("java") || skey.startsWith("java#")) {
                    String javaVer = skey.substring(5);
                    if (NutsBlankable.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    String s = NutsJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion(javaVer, false, session);
                    if (s == null) {
                        throw new NutsExecutionException(session, NutsMessage.cstyle("no java version %s was found", javaVer), 1);
                    }
                    return s;
                } else if (skey.equals("javaw") || skey.startsWith("javaw#")) {
                    String javaVer = skey.substring(6);
                    if (NutsBlankable.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    String s = NutsJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion(javaVer, true, session);
                    if (s == null) {
                        throw new NutsExecutionException(session, NutsMessage.cstyle("no java version %s was found", javaVer), 1);
                    }
                    return s;
                } else if (skey.equals("nuts")) {
                    NutsDefinition nutsDefinition;
                    nutsDefinition = session.fetch().setId(NutsConstants.Ids.NUTS_API)
                            .setSession(session).getResultDefinition();
                    if (nutsDefinition.getFile() != null) {
                        return ("<::expand::> " + apply("java") + " -jar " + nutsDefinition.getFile());
                    }
                    return null;
                }
                return map.get(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            if (!NutsBlankable.isBlank(k)) {
                k = k.replace('.', '_').toUpperCase();
                if (!NutsBlankable.isBlank(e.getValue())) {
                    envmap.put(k, e.getValue());
                }
            }
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = NutsStringUtils.trim(StringPlaceHolderParser.replaceDollarPlaceHolders(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, NutsCommandLine.of(s, NutsShellFamily.BASH, session).setExpandSimpleOptions(false).toStringArray());
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        Path wsLocation = session.locations().getWorkspaceLocation().toFile();
        Path path = wsLocation.resolve(args[0]).normalize();
        if (Files.exists(path)) {
            NutsPath.of(path, session).addPermissions(NutsPathPermission.CAN_EXECUTE);
        }
        Path pdirectory = null;
        if (NutsBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        return ofArgs(nutMainFile,args, envmap, pdirectory, session.getTerminal(), execSession.getTerminal(), showCommand, failFast,
                sleep,
                inheritSystemIO, redirectErr, inputFile, outputFile, runAs,
                session);
    }

    private static String resolveRootUserName(NutsSession session) {
        NutsOsFamily sysFamily = session.env().getOsFamily();
        switch (sysFamily) {
            case WINDOWS: {
                String s = (String) session.getProperty("nuts.windows.root-user");
                if (s == null) {
                    s = session.config().getConfigProperty("nuts.windows.root-user").asString().get(session);
                }
                if (NutsBlankable.isBlank(s)) {
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
        NutsOsFamily sysFamily = session.env().getOsFamily();
        List<String> command = new ArrayList<>(Arrays.asList(cmd));
        if (runAsMode == null) {
            runAsMode = NutsRunAs.CURRENT_USER;
        }
        boolean runWithGui = session.isGui() && session.env().isGraphicalDesktopEnvironment();
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
//                if (NutsBlankable.isBlank(runAsUser)
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
                            Set<NutsDesktopEnvironmentFamily> de = session.env().getDesktopEnvironmentFamilies();
                            Path kdesu = NutsSysExecUtils.sysWhich("kdesu");
                            Path gksu = NutsSysExecUtils.sysWhich("gksu");
                            String currSu = null;
                            if (de.contains(NutsDesktopEnvironmentFamily.KDE)) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            } else if (de.contains(NutsDesktopEnvironmentFamily.GNOME)) {
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
                            Path su = NutsSysExecUtils.sysWhich("su");
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot run as %s on unknown system OS family", runAsEffective));
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
                            Set<NutsDesktopEnvironmentFamily> de = session.env().getDesktopEnvironmentFamilies();
                            Path kdesu = NutsSysExecUtils.sysWhich("kdesudo");
                            Path gksu = NutsSysExecUtils.sysWhich("gksudo");
                            String currSu = null;
                            if (de.contains(NutsDesktopEnvironmentFamily.KDE)) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            } else if (de.contains(NutsDesktopEnvironmentFamily.GNOME)) {
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
                            Path su = NutsSysExecUtils.sysWhich("sudo");
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot run sudo %s on unknown system OS family", currentUserName));
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
            out.println(pb.getFormattedCommandString(getSession()));
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
            return waitResult(p);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    public Future<Integer> execAsync() {
        try {
            if (out != null) {
                out.run(NutsTerminalCommand.MOVE_LINE_START, getSession());
            }
            ProcessBuilder2 p = pb.start();
            return new FutureTask<Integer>(() -> waitResult(p));
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    private int waitResult(ProcessBuilder2 p) {
        Exception err = null;
        try {
            int a = p.waitFor().getResult();
            if (a != 0) {
                err = new NutsExecutionException(getSession(), NutsMessage.cstyle("process returned error code %s", a), err);
            }
            return a;
        } catch (Exception ex) {
            err = ex;
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) err;
            }
            throw new NutsExecutionException(getSession(), NutsMessage.cstyle("error executing process"), err);
        } finally {
            if (err != null) {
                if (definition != null) {
                    NutsWorkspaceExt.of(getSession()).getModel().recomm.getRecommendations(new RequestQueryInfo(definition.getId().toString(), err), NutsRecommendationPhase.EXEC, false, getSession());
                }
            }
        }
    }
}
