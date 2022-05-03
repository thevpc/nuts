package net.thevpc.nuts.runtime.standalone.executor.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.concurrent.NutsScheduler;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NutsCommandLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.text.NutsTerminalCommand;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class NutsExecHelper extends AbstractSyncIProcessExecHelper {

    NutsExecCommand pb;
    NutsPrintStream out;

    public NutsExecHelper(NutsExecCommand pb, NutsSession session, NutsPrintStream out) {
        super(session);
        this.pb = pb;
        this.out = out;
    }

    public static NutsExecHelper ofArgs(String[] args, Map<String, String> env, Path directory, NutsSessionTerminal prepareTerminal,
                                        NutsSessionTerminal execTerminal, boolean showCommand, boolean failFast, long sleep,
                                        boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                        NutsRunAs runAs,
                                        NutsSession session) {
        NutsPrintStream out = null;
        NutsPrintStream err = null;
        InputStream in = null;
        NutsExecCommand pb = session.exec();
        NutsCommandLineUtils.OptionsAndArgs optionsAndArgs = NutsCommandLineUtils.parseOptionsFirst(args);
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
                if (NutsIO.of(session).isStdin(in)) {
                    in = null;
                }
            }
            if (outputFile == null) {
                out = execTerminal.out();
                if (NutsIO.of(session).isStdout(out)) {
                    out = null;
                }
            }
            err = execTerminal.err();
            if (NutsIO.of(session).isStderr(err)) {
                err = null;
            }
            if (out != null) {
                out.run(NutsTerminalCommand.MOVE_LINE_START, session);
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

        NutsLogger _LL = NutsLogger.of(NutsWorkspaceUtils.class, session);
        NutsCommandLine commandOut = NutsCommandLine.of(pb.getCommand());
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.with().level(Level.FINE).verb(NutsLoggerVerb.START).log(
                    NutsMessage.ofJstyle("[exec] {0}",
                            commandOut
                    ));
        }
        if (showCommand || session.boot().getCustomBootOption("---show-command")
                .flatMap(NutsValue::asBoolean)
                .orElse(false)) {
            if (prepareTerminal.out().mode() == NutsTerminalMode.FORMATTED) {
                prepareTerminal.out().printf("%s ", NutsTexts.of(session).ofStyled("[exec]", NutsTextStyle.primary4()));
                prepareTerminal.out().println(NutsTexts.of(session).toText(commandOut));
            } else {
                prepareTerminal.out().print("exec ");
                prepareTerminal.out().printf("%s%n", commandOut);
            }
        }
        return new NutsExecHelper(pb, session, out == null ? execTerminal.out() : out);
    }

    public static NutsExecHelper ofDefinition(NutsDefinition nutMainFile,
                                              String[] args, Map<String, String> env, String directory, Map<String, String> execProperties, boolean showCommand, boolean failFast, long sleep, boolean inheritSystemIO, boolean redirectErr, File outputFile, File inputFile,
                                              NutsRunAs runAs,
                                              NutsSession session,
                                              NutsSession execSession
    ) throws NutsExecutionException {
        Path wsLocation = session.locations().getWorkspaceLocation().toFile();
        Path pdirectory = null;
        if (NutsBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        return ofArgs(args, env, pdirectory, session.getTerminal(), execSession.getTerminal(), showCommand, failFast,
                sleep,
                inheritSystemIO, redirectErr, inputFile, outputFile, runAs,
                session);
    }

    public void dryExec() {
        if (out.mode() == NutsTerminalMode.FORMATTED) {
            out.print("[dry] ==[exec]== ");
            out.println(pb.format());
        } else {
            out.print("[dry] exec ");
            out.printf("%s%n", pb.format());
        }
    }

    public int exec() {
        if (out != null) {
            out.resetLine();
        }
        return pb.getResult();
    }

    public Future<Integer> execAsync() {
        if (out != null) {
            out.run(NutsTerminalCommand.MOVE_LINE_START, getSession());
        }
        return NutsScheduler.of(getSession()).executorService().submit(() -> pb.getResult());
    }
}
