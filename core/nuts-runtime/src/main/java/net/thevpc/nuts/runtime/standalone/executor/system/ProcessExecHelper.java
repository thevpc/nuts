package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.NRecommendationPhase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.logging.Level;

public class ProcessExecHelper extends AbstractSyncIProcessExecHelper {

    private NDefinition definition;
    private ProcessBuilder2 pb;
    private NPrintStream trace;
    private NExecInput in;
    private NExecOutput out;
    private NExecOutput err;

    public ProcessExecHelper(NDefinition definition, ProcessBuilder2 pb, NSession session, NPrintStream trace, NExecInput in, NExecOutput out, NExecOutput err) {
        super(session);
        this.pb = pb;
        this.trace = trace;
        this.definition = definition;
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public static ProcessExecHelper ofArgs(NDefinition definition, String[] args, Map<String, String> env, Path directory,
                                           boolean showCommand, boolean failFast, long sleep,
                                           NExecInput in, NExecOutput out, NExecOutput err,
                                           NRunAs runAs,
                                           NSession session) {
        List<String> newCommands = buildEffectiveCommand(args, runAs, session);
        ProcessBuilder2 pb = new ProcessBuilder2(session);
        pb.setCommand(newCommands)
                .setEnv(env)
                .setDirectory(directory == null ? null : directory.toFile())
                .setSleepMillis(sleep)
                .setFailFast(failFast);
        in= CoreIOUtils.validateIn(in,session);
        out= CoreIOUtils.validateOut(out,session);
        err= CoreIOUtils.validateErr(err,session);

        NLog _LL = NLog.of(NWorkspaceUtils.class, session);
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NLogVerb.START).log(
                    NMsg.ofJ("[exec] {0}",
                            NTexts.of(session).ofCode("system",
                                    pb.getCommandString()
                            )));
        }
        if (showCommand || CoreNUtils.isShowCommand(session)) {
            if (session.out().getTerminalMode() == NTerminalMode.FORMATTED) {
                session.out().print(NMsg.ofC("%s ", NTexts.of(session).ofStyled("[exec]", NTextStyle.primary4())));
                session.out().println(NTexts.of(session).ofCode("system", pb.getCommandString()));
            } else {
                session.out().print("exec ");
                session.out().println(NMsg.ofPlain(pb.getCommandString()));
            }
        }
        return new ProcessExecHelper(definition, pb, session, session.out(), in, out, err);
    }

    public static ProcessExecHelper ofDefinition(NDefinition nutMainFile,
                                                 String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast, long sleep,
                                                 NExecInput in, NExecOutput out, NExecOutput err,
                                                 NRunAs runAs,
                                                 NSession session,
                                                 NSession execSession
    ) throws NExecutionException {
        NId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getContent().map(NPath::toFile).orNull();
        NPath storeFolder = nutMainFile.getInstallInformation().get(session).getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
        Path nutsJarFile = NFetchCommand.ofNutsApi(session).getResultPath();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.toAbsolutePath().normalize().toString());
        }
        map.put("nuts.artifact", id.toString());
        map.put("nuts.file", nutMainFile.getContent().map(NPath::toFile).map(Object::toString).orNull());
        String defaultJavaCommand = NJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion("", false, session);
        if (defaultJavaCommand == null) {
            throw new NExecutionException(session, NMsg.ofPlain("no java version was found"), 1);
        }
        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", NLocations.of(session).getWorkspaceLocation().toString());
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
                    if (NBlankable.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    String s = NJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion(javaVer, false, session);
                    if (s == null) {
                        throw new NExecutionException(session, NMsg.ofC("no java version %s was found", javaVer), 1);
                    }
                    return s;
                } else if (skey.equals("javaw") || skey.startsWith("javaw#")) {
                    String javaVer = skey.substring(6);
                    if (NBlankable.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    String s = NJavaSdkUtils.of(execSession.getWorkspace()).resolveJavaCommandByVersion(javaVer, true, session);
                    if (s == null) {
                        throw new NExecutionException(session, NMsg.ofC("no java version %s was found", javaVer), 1);
                    }
                    return s;
                } else if (skey.equals("nuts")) {
                    NDefinition nDefinition;
                    nDefinition = NFetchCommand.ofNutsApi(session)
                            .getResultDefinition();
                    if (nDefinition.getContent().isPresent()) {
                        return ("<::expand::> " + apply("java") + " -jar " + nDefinition.getContent());
                    }
                    return null;
                }
                return map.get(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            if (!NBlankable.isBlank(k)) {
                k = k.replace('.', '_').toUpperCase();
                if (!NBlankable.isBlank(e.getValue())) {
                    envmap.put(k, e.getValue());
                }
            }
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = NStringUtils.trim(StringPlaceHolderParser.replaceDollarPlaceHolders(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, NCmdLine.of(s, NShellFamily.BASH, session).setExpandSimpleOptions(false).toStringArray());
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        Path wsLocation = NLocations.of(session).getWorkspaceLocation().toFile();
        Path path = wsLocation.resolve(args[0]).normalize();
        if (Files.exists(path)) {
            NPath.of(path, session).addPermissions(NPathPermission.CAN_EXECUTE);
        }
        Path pdirectory = null;
        if (NBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        return ofArgs(nutMainFile, args, envmap, pdirectory, showCommand, failFast,
                sleep,
                in, out, err,
                runAs,
                session);
    }

    private static String resolveRootUserName(NSession session) {
        NOsFamily sysFamily = NEnvs.of(session).getOsFamily();
        switch (sysFamily) {
            case WINDOWS: {
                String s = (String) session.getProperty("nuts.windows.root-user");
                if (s == null) {
                    s = NConfigs.of(session).getConfigProperty("nuts.windows.root-user").flatMap(NLiteral::asString).orNull();
                }
                if (NBlankable.isBlank(s)) {
                    s = "Administrator";
                }
                return s;
            }
            default: {
                return "root";
            }
        }
    }

    private static List<String> buildEffectiveCommand(String[] cmd, NRunAs runAsMode, NSession session) {
        //String runAsEffective = null;
        NOsFamily sysFamily = NEnvs.of(session).getOsFamily();
        List<String> command = new ArrayList<>(Arrays.asList(cmd));
        if (runAsMode == null) {
            runAsMode = NRunAs.CURRENT_USER;
        }
        boolean runWithGui = session.isGui() && NEnvs.of(session).isGraphicalDesktopEnvironment();
        String rootUserName = resolveRootUserName(session);
        String currentUserName = System.getProperty("user.name");

        //optimize mode
        switch (runAsMode.getMode()) {
            case ROOT: {
                if (rootUserName.equals(currentUserName)) {
                    runAsMode = NRunAs.currentUser();
                }
                break;
            }
            case USER: {
                String s = runAsMode.getUser();
                s = s.trim();
                if (currentUserName.equals(s)) {
                    runAsMode = NRunAs.currentUser();
                }
                if (!s.equals(runAsMode.getUser())) {
                    runAsMode = NRunAs.user(s);
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
//                        throw new NutsIllegalArgumentException(session, NMsg.plain("cannot run as admin/root on unknown system OS family"));
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
                String runAsEffective = runAsMode.getMode() == NRunAs.Mode.USER ? runAsMode.getUser() : rootUserName;
                List<String> cc = new ArrayList<>();
                switch (sysFamily) {
                    case LINUX:
                    case MACOS:
                    case UNIX: {
                        if (runWithGui) {
                            Set<NDesktopEnvironmentFamily> de = NEnvs.of(session).getDesktopEnvironmentFamilies();
                            Path kdesu = NSysExecUtils.sysWhich("kdesu");
                            Path gksu = NSysExecUtils.sysWhich("gksu");
                            String currSu = null;
                            if (de.contains(NDesktopEnvironmentFamily.KDE)) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            } else if (de.contains(NDesktopEnvironmentFamily.GNOME)) {
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
                                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to resolve gui su application (kdesu,gksu,...)"));
                            }
                            cc.add(currSu);
                            cc.add(runAsEffective);
                        } else {
                            Path su = NSysExecUtils.sysWhich("su");
                            if (su == null) {
                                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to resolve su application"));
                            }
                            cc.add(su.toString());
                            cc.add(runAsEffective);
                            cc.add("-c");
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(Arrays.asList("runas", "/noprofile", "/user:" + runAsEffective));
                        break;
                    }
                    default: {
                        throw new NIllegalArgumentException(session, NMsg.ofC("cannot run as %s on unknown system OS family", runAsEffective));
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
                            Set<NDesktopEnvironmentFamily> de = NEnvs.of(session).getDesktopEnvironmentFamilies();
                            Path kdesu = NSysExecUtils.sysWhich("kdesudo");
                            Path gksu = NSysExecUtils.sysWhich("gksudo");
                            String currSu = null;
                            if (de.contains(NDesktopEnvironmentFamily.KDE)) {
                                if (kdesu != null) {
                                    currSu = kdesu.toString();
                                }
                            } else if (de.contains(NDesktopEnvironmentFamily.GNOME)) {
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
                                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to resolve gui su application (kdesu,gksu,...)"));
                            }
                            cc.add(currSu);
                        } else {
                            Path su = NSysExecUtils.sysWhich("sudo");
                            if (su == null) {
                                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to resolve su application"));
                            }
                            cc.add(su.toString());
                            cc.add("-S");
                        }
                        break;
                    }
                    case WINDOWS: {
                        cc.addAll(Arrays.asList("runas", "/noprofile", "/user:" + rootUserName));
                        break;
                    }
                    default: {
                        throw new NIllegalArgumentException(session, NMsg.ofC("cannot run sudo %s on unknown system OS family", currentUserName));
                    }
                }
                cc.addAll(command);
                return cc;
            }
        }
        throw new NIllegalArgumentException(session, NMsg.ofPlain("cannot run as admin/root on unknown system OS family"));
    }

    public int exec() {
        if (getSession().isDry()) {
            if (trace.getTerminalMode() == NTerminalMode.FORMATTED) {
                trace.print("[dry] ==[exec]== ");
                trace.println(pb.getFormattedCommandString(getSession()));
            } else {
                trace.print("[dry] exec ");
                trace.println(NMsg.ofPlain(pb.getCommandString()));
            }
            return 0;
        } else {
            try {
                if (trace != null) {
                    trace.resetLine();//.run(NutsTerminalCommand.MOVE_LINE_START);
                }
                ProcessBuilder2 p = pb.start();
                return waitResult(p);
            } catch (IOException ex) {
                throw new NIOException(getSession(), ex);
            }
        }
    }

    public Future<Integer> execAsync() {
        try {
            if (trace != null) {
                trace.run(NTerminalCommand.MOVE_LINE_START, getSession());
            }
            ProcessBuilder2 p = pb.start();
            return new FutureTask<Integer>(() -> waitResult(p));
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
    }

    private int waitResult(ProcessBuilder2 p) {
        Exception err = null;
        try {
            int a = p.waitFor().getResult();
            if (a != 0) {
                err = new NExecutionException(getSession(), NMsg.ofC("process returned error code %s", a), err);
            }
            return a;
        } catch (Exception ex) {
            err = ex;
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) err;
            }
            throw new NExecutionException(getSession(), NMsg.ofPlain("error executing process"), err);
        } finally {
            if (err != null) {
                if (definition != null) {
                    NWorkspaceExt.of(getSession()).getModel().recomm.getRecommendations(new RequestQueryInfo(definition.getId().toString(), err), NRecommendationPhase.EXEC, false, getSession());
                }
            }
        }
    }
}
