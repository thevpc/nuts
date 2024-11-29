package net.thevpc.nuts.runtime.standalone.executor.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.env.NBootManager;
import net.thevpc.nuts.env.NLocations;
import net.thevpc.nuts.format.NExecCmdFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class NExecHelper extends AbstractSyncIProcessExecHelper {

    NExecCmd pb;
    NPrintStream out;

    public NExecHelper(NExecCmd pb, NWorkspace workspace, NPrintStream out) {
        super(workspace);
        this.pb = pb;
        this.out = out;
    }

    public static NExecHelper ofArgs(String[] args, Map<String, String> env, Path directory,
                                     boolean showCommand, boolean failFast, long sleep,
                                     NExecInput in,
                                     NExecOutput out,
                                     NExecOutput err,
                                     NRunAs runAs,
                                     NWorkspace workspace) {
        NExecCmd pb = NExecCmd.of();
        NCmdLineUtils.OptionsAndArgs optionsAndArgs = NCmdLineUtils.parseOptionsFirst(args);
        pb.setCommand(optionsAndArgs.getArgs())
                .addExecutorOptions(optionsAndArgs.getOptions())
                .setRunAs(runAs)
                .setEnv(env)
                .setDirectory(directory == null ? null : NPath.of(directory))
                .setSleepMillis((int) sleep)
                .setFailFast(failFast);
        pb.setIn(CoreIOUtils.validateIn(in));
        pb.setOut(CoreIOUtils.validateOut(out));
        pb.setErr(CoreIOUtils.validateErr(err));

        NLog _LL = NLog.of(NWorkspaceUtils.class);
        NCmdLine commandOut = NCmdLine.of(pb.getCommand());
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NLogVerb.START).log(
                    NMsg.ofC("[exec] %s",
                            commandOut
                    ));
        }
        NSession session = workspace.currentSession();
        if (showCommand || NBootManager.of().getCustomBootOption("---show-command")
                .flatMap(NLiteral::asBoolean)
                .orElse(false)) {

            if (session.out().getTerminalMode() == NTerminalMode.FORMATTED) {
                session.out().print(NMsg.ofC("%s ", NText.ofStyled("[exec]", NTextStyle.primary4())));
                session.out().println(NText.of(commandOut));
            } else {
                session.out().print("exec ");
                session.out().println(commandOut);
            }
        }
        return new NExecHelper(pb, workspace, session.out());
    }

    public static NExecHelper ofDefinition(NDefinition nutMainFile,
                                           String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast, long sleep,
                                           NExecInput in, NExecOutput out, NExecOutput err,
                                           NRunAs runAs,
                                           NWorkspace workspace
    ) throws NExecutionException {
        Path wsLocation = NLocations.of().getWorkspaceLocation().toPath().get();
        Path pdirectory = null;
        if (NBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        return ofArgs(args, env, pdirectory, showCommand, failFast,
                sleep,
                in, out, err, runAs,
                workspace);
    }


    public int exec() {
        NSession session = workspace.currentSession();
        if (session.isDry()) {
            if (out.getTerminalMode() == NTerminalMode.FORMATTED) {
                out.print("[dry] ==[exec]== ");
                out.println(NExecCmdFormat.of(pb).format());
            } else {
                out.print("[dry] exec ");
                out.println(NExecCmdFormat.of(pb).format());
            }
            return NExecutionException.SUCCESS;
        }
        if (out != null) {
            out.resetLine();
        }
        return pb.getResultCode();
    }

    public Future<Integer> execAsync() {
        if (out != null) {
            out.run(NTerminalCmd.MOVE_LINE_START);
        }
        return NScheduler.of().executorService().submit(() -> pb.getResultCode());
    }
}
