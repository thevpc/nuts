package net.thevpc.nuts.runtime.standalone.executor.exec;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.concurrent.NConcurrent;


import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.text.NExecWriter;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class NExecHelper extends AbstractSyncIProcessExecHelper {

    NExec pb;
    NPrintStream out;

    public NExecHelper(NExec pb, NPrintStream out) {
        super();
        this.pb = pb;
        this.out = out;
    }

    public static NExecHelper ofArgs(String[] args, Map<String, String> env, Path directory,
                                     boolean showCommand, boolean failFast, NDuration sleep,
                                     NExecInput in,
                                     NExecOutput out,
                                     NExecOutput err,
                                     NRunAs runAs) {
        NExec pb = NExec.of();
        NCmdLineUtils.OptionsAndArgs optionsAndArgs = NCmdLineUtils.parseOptionsFirst(args);
        pb.command(optionsAndArgs.getArgs())
                .addExecutorOptions(optionsAndArgs.getOptions())
                .runAs(runAs)
                .env(env)
                .directory(directory == null ? null : NPath.of(directory))
                .sleepDuration(sleep)
                .failFast(failFast);
        pb.in(CoreIOUtils.validateIn(in));
        pb.out(CoreIOUtils.validateOut(out));
        pb.err(CoreIOUtils.validateErr(err));

        NLog _LL = NLog.of(NWorkspaceUtils.class);
        NCmdLine commandOut = NCmdLine.of(pb.command());
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.log(
                    NMsg.ofC("[exec] %s",
                            commandOut
                    ).asFinest().withIntent(NMsgIntent.START));
        }
        NSession session = NSession.of();
        if (showCommand || NWorkspace.of().getCustomBootOption("---show-command")
                .flatMap(NLiteral::asBoolean)
                .orElse(false)) {

            if (NOut.terminalMode() == NTerminalMode.FORMATTED) {
                NOut.print(NMsg.ofC("%s ", NText.ofStyled("[exec]", NTextStyle.primary4())));
                NOut.println(NText.of(commandOut));
            } else {
                NOut.print("exec ");
                NOut.println(commandOut);
            }
        }
        return new NExecHelper(pb, session.out());
    }

    public static NExecHelper ofDefinition(NDefinition nutMainFile,
                                           String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast, NDuration sleep,
                                           NExecInput in, NExecOutput out, NExecOutput err,
                                           NRunAs runAs
    ) throws NExecutionException {
        Path wsLocation = NWorkspace.of().workspaceLocation().toPath().get();
        Path pdirectory = null;
        if (NBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        return ofArgs(args, env, pdirectory, showCommand, failFast,
                sleep,
                in, out, err, runAs
        );
    }


    public int exec() {
        NSession session = NSession.of();
        if (session.isDry()) {
            if (out.terminalMode() == NTerminalMode.FORMATTED) {
                out.print("[dry] ==[exec]== ");
                out.println(NExecWriter.of().format(pb));
            } else {
                out.print("[dry] exec ");
                out.println(NExecWriter.of().format(pb));
            }
            return NExecutionException.SUCCESS;
        }
        if (out != null) {
            out.flush();
        }
        return pb.exitCode();
    }

    public Future<Integer> execAsync() {
        if (out != null) {
            out.run(NTerminalCmd.MOVE_LINE_START);
        }
        return NConcurrent.of().executorService().submit(() -> pb.exitCode());
    }
}
