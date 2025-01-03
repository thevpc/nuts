package net.thevpc.nuts.runtime.standalone.executor.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;


import net.thevpc.nuts.NShellFamily;
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
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
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
    private boolean dry;

    public ProcessExecHelper(NDefinition definition, ProcessBuilder2 pb, NWorkspace workspace, NPrintStream trace, NExecInput in, NExecOutput out, NExecOutput err, boolean dry) {
        super(workspace);
        this.pb = pb;
        this.trace = trace;
        this.definition = definition;
        this.in = in;
        this.out = out;
        this.err = err;
        this.dry = dry;
    }

    public static ProcessExecHelper ofArgs(NDefinition definition, String[] args, Map<String, String> env, Path directory,
                                           boolean showCommand, boolean failFast, long sleep,
                                           NExecInput in, NExecOutput out, NExecOutput err,
                                           NRunAs runAs, String[] executorOptions,
                                           boolean dry, NWorkspace workspace) {
        List<String> newCommands = NSysExecUtils.buildEffectiveCommandLocal(args, runAs, executorOptions);
        ProcessBuilder2 pb = new ProcessBuilder2(workspace);
        pb.setCommand(newCommands)
                .setEnv(env)
                .setDirectory(directory == null ? null : directory.toFile())
                .setSleepMillis(sleep)
                .setFailFast(failFast);
        pb.setIn(CoreIOUtils.validateIn(in));
        pb.setOut(CoreIOUtils.validateOut(out));
        pb.setErr(CoreIOUtils.validateErr(err));
        NLog _LL = NLog.of(NWorkspaceUtils.class);
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NLogVerb.START).log(
                    NMsg.ofC("[exec] %s",
                            NText.ofCode("system",
                                    pb.getCommandString()
                            )));
        }
        NSession session = workspace.currentSession();
        if (showCommand || CoreNUtils.isShowCommand()) {
            if (session.out().getTerminalMode() == NTerminalMode.FORMATTED) {
                session.out().print(NMsg.ofC("%s ", NText.ofStyled("[exec]", NTextStyle.primary4())));
                session.out().println(NText.ofCode("system", pb.getCommandString()));
            } else {
                session.out().print("exec ");
                session.out().println(NMsg.ofPlain(pb.getCommandString()));
            }
        }
        return new ProcessExecHelper(definition, pb, workspace, session.out(), in, out, err, dry);
    }

    public static ProcessExecHelper ofDefinition(NDefinition nutMainFile,
                                                 String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast, long sleep,
                                                 NExecInput in, NExecOutput out, NExecOutput err,
                                                 NRunAs runAs,
                                                 String[] executorOptions,
                                                 boolean dry, NSession session
    ) throws NExecutionException {
        NWorkspace workspace = session.getWorkspace();
        NId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getContent().flatMap(NPath::toPath).orNull();
        NPath storeFolder = nutMainFile.getInstallInformation().get().getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
        NPath nutsJarFile = NFetchCmd.ofNutsApi().getResultPath();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.normalize().toString());
        }
        map.put("nuts.artifact", id.toString());
        map.put("nuts.file", nutMainFile.getContent().flatMap(NPath::toPath).map(Object::toString).orNull());
        String defaultJavaCommand = NJavaSdkUtils.of(workspace).resolveJavaCommandByVersion("", false);
        if (defaultJavaCommand == null) {
            throw new NExecutionException(NMsg.ofPlain("no java version was found"), NExecutionException.ERROR_1);
        }
        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", NWorkspace.of().getWorkspaceLocation().toString());
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
                    String s = NJavaSdkUtils.of(workspace).resolveJavaCommandByVersion(javaVer, false);
                    if (s == null) {
                        throw new NExecutionException(NMsg.ofC("no java version %s was found", javaVer), NExecutionException.ERROR_1);
                    }
                    return s;
                } else if (skey.equals("javaw") || skey.startsWith("javaw#")) {
                    String javaVer = skey.substring(6);
                    if (NBlankable.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    String s = NJavaSdkUtils.of(workspace).resolveJavaCommandByVersion(javaVer, true);
                    if (s == null) {
                        throw new NExecutionException(NMsg.ofC("no java version %s was found", javaVer), NExecutionException.ERROR_1);
                    }
                    return s;
                } else if (
                        skey.equals(NConstants.Ids.NUTS_API_ARTIFACT_ID)
                        || skey.equals(NConstants.Ids.NUTS_APP_ARTIFACT_ID)
                ) {
                    NDefinition nDefinition;
                    nDefinition = NFetchCmd.ofNutsApp()
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
                Collections.addAll(args2, NCmdLine.of(s, NShellFamily.BASH).setExpandSimpleOptions(false).toStringArray());
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        Path wsLocation = NWorkspace.of().getWorkspaceLocation().toPath().get();
        Path path = wsLocation.resolve(args[0]).normalize();
        if (Files.exists(path)) {
            NPath.of(path).addPermissions(NPathPermission.CAN_EXECUTE);
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
                executorOptions,
                dry, workspace);
    }

    public int exec() {
        NSession session = workspace.currentSession();
        if (session.isDry()) {
            if (trace.getTerminalMode() == NTerminalMode.FORMATTED) {
                trace.print("[dry] ==[exec]== ");
                trace.println(pb.getFormattedCommandString());
            } else {
                trace.print("[dry] exec ");
                trace.println(NMsg.ofPlain(pb.getCommandString()));
            }
            return NExecutionException.SUCCESS;
        } else {
            try {
                if (trace != null) {
                    trace.resetLine();//.run(NutsTerminalCommand.MOVE_LINE_START);
                }
                ProcessBuilder2 p = pb.start();
                return waitResult(p);
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }
    }

    public Future<Integer> execAsync() {
        try {
            if (trace != null) {
                trace.run(NTerminalCmd.MOVE_LINE_START);
            }
            ProcessBuilder2 p = pb.start();
            return new FutureTask<Integer>(() -> waitResult(p));
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    private int waitResult(ProcessBuilder2 p) {
        Exception err = null;
        try {
            int a = p.waitFor().getResult();
            if (a != 0) {
                err = new NExecutionException(NMsg.ofC("process returned error code %s", a), err);
            }
            return a;
        } catch (Exception ex) {
            err = ex;
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) err;
            }
            throw new NExecutionException(NMsg.ofPlain("error executing process"), err);
        } finally {
            if (err != null) {
                if (definition != null) {
                    NWorkspaceExt.of().getModel().recomm.getRecommendations(new RequestQueryInfo(definition.getId().toString(), err), NRecommendationPhase.EXEC, false);
                }
            }
        }
    }
}
