package net.thevpc.nuts.runtime.standalone.executor.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCommandLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NTerminalCommand;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerVerb;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class NExecHelper extends AbstractSyncIProcessExecHelper {

    NExecCommand pb;
    NOutStream out;

    public NExecHelper(NExecCommand pb, NSession session, NOutStream out) {
        super(session);
        this.pb = pb;
        this.out = out;
    }

    public static NExecHelper ofArgs(String[] args, Map<String, String> env, Path directory, NSessionTerminal prepareTerminal,
                                     NSessionTerminal execTerminal, boolean showCommand, boolean failFast, long sleep,
                                     boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                     NRunAs runAs,
                                     NSession session) {
        NOutStream out = null;
        NOutStream err = null;
        InputStream in = null;
        NExecCommand pb = NExecCommand.of(session);
        NCommandLineUtils.OptionsAndArgs optionsAndArgs = NCommandLineUtils.parseOptionsFirst(args);
        pb.setCommand(optionsAndArgs.getArgs())
                .addExecutorOptions(optionsAndArgs.getOptions())
                .setRunAs(runAs)
                .setEnv(env)
                .setDirectory(directory == null ? null : directory.toString())
                .setSleepMillis((int) sleep)
                .setFailFast(failFast);
        if (!inheritSystemIO) {
            if (inputFile == null) {
                in = execTerminal.in();
                if (NIO.of(session).isStdin(in)) {
                    in = null;
                }
            }
            if (outputFile == null) {
                out = execTerminal.out();
                if (NIO.of(session).isStdout(out)) {
                    out = null;
                }
            }
            err = execTerminal.err();
            if (NIO.of(session).isStderr(err)) {
                err = null;
            }
            if (out != null) {
                out.run(NTerminalCommand.MOVE_LINE_START, session);
            }
        }
//        if (out == null && err == null && in == null && inputFile == null && outputFile == null) {
//            pb.inheritIO();
//            if (redirectErr) {
//                pb.setRedirectErrorStream();
//            }
//        } else {
//            if (inputFile == null) {
//                pb.setIn(in);
//            } else {
//                pb.setRedirectFileInput(inputFile);
//            }
//            if (outputFile == null) {
//                pb.setOutput(out == null ? null : out.asPrintStream());
//            } else {
//                pb.setRedirectFileOutput(outputFile);
//            }
//            if (redirectErr) {
//                pb.setRedirectErrorStream();
//            } else {
//                pb.setErr(err == null ? null : err.asPrintStream());
//            }
//        }

        NLogger _LL = NLogger.of(NWorkspaceUtils.class, session);
        NCommandLine commandOut = NCommandLine.of(pb.getCommand());
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NLoggerVerb.START).log(
                    NMsg.ofJstyle("[exec] {0}",
                            commandOut
                    ));
        }
        if (showCommand || NBootManager.of(session).getCustomBootOption("---show-command")
                .flatMap(NValue::asBoolean)
                .orElse(false)) {
            if (prepareTerminal.out().getTerminalMode() == NTerminalMode.FORMATTED) {
                prepareTerminal.out().printf("%s ", NTexts.of(session).ofStyled("[exec]", NTextStyle.primary4()));
                prepareTerminal.out().println(NTexts.of(session).ofText(commandOut));
            } else {
                prepareTerminal.out().print("exec ");
                prepareTerminal.out().printf("%s%n", commandOut);
            }
        }
        return new NExecHelper(pb, session, out == null ? execTerminal.out() : out);
    }

    public static NExecHelper ofDefinition(NDefinition nutMainFile,
                                           String[] args, Map<String, String> env, String directory, Map<String, String> execProperties, boolean showCommand, boolean failFast, long sleep, boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                           NRunAs runAs,
                                           NSession session,
                                           NSession execSession
    ) throws NExecutionException {
        Path wsLocation = NLocations.of(session).getWorkspaceLocation().toFile();
        Path pdirectory = null;
        if (NBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        return ofArgs(args, env, pdirectory, session.getTerminal(), execSession.getTerminal(), showCommand, failFast,
                sleep,
                inheritSystemIO, redirectErr, inputFile, outputFile, runAs,
                session);
    }


    public int exec() {
        if (getSession().isDry()) {
            if (out.getTerminalMode() == NTerminalMode.FORMATTED) {
                out.print("[dry] ==[exec]== ");
                out.println(pb.format());
            } else {
                out.print("[dry] exec ");
                out.printf("%s%n", pb.format());
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
