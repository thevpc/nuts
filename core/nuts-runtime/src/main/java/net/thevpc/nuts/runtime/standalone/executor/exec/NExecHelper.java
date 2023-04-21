package net.thevpc.nuts.runtime.standalone.executor.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class NExecHelper extends AbstractSyncIProcessExecHelper {

    NExecCommand pb;
    NPrintStream out;

    public NExecHelper(NExecCommand pb, NSession session, NPrintStream out) {
        super(session);
        this.pb = pb;
        this.out = out;
    }

    public static NExecHelper ofArgs(String[] args, Map<String, String> env, Path directory,
                                     boolean showCommand, boolean failFast, long sleep,
                                     NExecInput in,
                                     NExecOutput out,
                                     NExecOutput err,
                                     NRunAs runAs,
                                     NSession session) {
        NExecCommand pb = NExecCommand.of(session);
        NCmdLineUtils.OptionsAndArgs optionsAndArgs = NCmdLineUtils.parseOptionsFirst(args);
        pb.setCommand(optionsAndArgs.getArgs())
                .addExecutorOptions(optionsAndArgs.getOptions())
                .setRunAs(runAs)
                .setEnv(env)
                .setDirectory(directory == null ? null : NPath.of(directory, session))
                .setSleepMillis((int) sleep)
                .setFailFast(failFast);
        pb.setIn(CoreIOUtils.validateIn(in, session));
        pb.setOut(CoreIOUtils.validateOut(out, session));
        pb.setErr(CoreIOUtils.validateErr(err, session));

        NLog _LL = NLog.of(NWorkspaceUtils.class, session);
        NCmdLine commandOut = NCmdLine.of(pb.getCommand());
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NLogVerb.START).log(
                    NMsg.ofJ("[exec] {0}",
                            commandOut
                    ));
        }
        if (showCommand || NBootManager.of(session).getCustomBootOption("---show-command")
                .flatMap(NLiteral::asBoolean)
                .orElse(false)) {
            if (session.out().getTerminalMode() == NTerminalMode.FORMATTED) {
                session.out().print(NMsg.ofC("%s ", NTexts.of(session).ofStyled("[exec]", NTextStyle.primary4())));
                session.out().println(NTexts.of(session).ofText(commandOut));
            } else {
                session.out().print("exec ");
                session.out().println(commandOut);
            }
        }
        return new NExecHelper(pb, session, session.out());
    }

    public static NExecHelper ofDefinition(NDefinition nutMainFile,
                                           String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast, long sleep,
                                           NExecInput in, NExecOutput out, NExecOutput err,
                                           NRunAs runAs,
                                           NSession session
    ) throws NExecutionException {
        Path wsLocation = NLocations.of(session).getWorkspaceLocation().toFile();
        Path pdirectory = null;
        if (NBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        return ofArgs(args, env, pdirectory, showCommand, failFast,
                sleep,
                in, out, err, runAs,
                session);
    }


    public int exec() {
        if (getSession().isDry()) {
            if (out.getTerminalMode() == NTerminalMode.FORMATTED) {
                out.print("[dry] ==[exec]== ");
                out.println(pb.format());
            } else {
                out.print("[dry] exec ");
                out.println(pb.format());
            }
            return 0;
        }
        if (out != null) {
            out.resetLine();
        }
        return pb.getResult();
    }

    public Future<Integer> execAsync() {
        if (out != null) {
            out.run(NTerminalCommand.MOVE_LINE_START, getSession());
        }
        return NScheduler.of(getSession()).executorService().submit(() -> pb.getResult());
    }
}
