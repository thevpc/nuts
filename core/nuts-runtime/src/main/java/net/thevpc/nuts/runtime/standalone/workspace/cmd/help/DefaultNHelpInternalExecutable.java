/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.help;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


/**
 * @author thevpc
 */
public class DefaultNHelpInternalExecutable extends DefaultInternalNExecutableCommand {
    private final NLog LOG;

    public DefaultNHelpInternalExecutable(String[] args, NExecCommand execCommand) {
        super("help", args, execCommand);
        LOG = NLog.of(DefaultNHelpInternalExecutable.class, execCommand.getSession());
    }

    @Override
    public int execute() {
        if (getSession().isDry()) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        List<String> helpFor = new ArrayList<>();
        NSession session = getSession();
        NCmdLine cmdLine = NCmdLine.of(args);
        boolean helpColors = false;
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get(session);
            if (a.isOption()) {
                switch (a.key()) {
                    case "--colors":
                    case "--ntf": {
                        NArg c = cmdLine.nextFlag().get(session);
                        if (c.isActive()) {
                            helpColors = c.getBooleanValue().get(session);
                        }
                        break;
                    }
                    case "-h":
                    case "--help": {
                        cmdLine.skip();
                        // just ignore, this is help anyways!
                        break;
                    }
                    default: {
                        session.configureLast(cmdLine);
                    }
                }
            } else {
                cmdLine.skip();
                helpFor.add(a.asString().get(session));
                helpFor.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
            }
        }

        if (helpColors) {
            NTexts txt = NTexts.of(session);
            NText n = txt.parser().parse(NPath.of("classpath:/net/thevpc/nuts/runtime/ntf-help.ntf",
                    this.getClass().getClassLoader(),
                    session));
            session.getTerminal().out().print(
                    n == null ? NTexts.of(session).ofStyled(("no help found for " + name), NTextStyle.error()) : n
            );
        }
        //NPrintStream fout = NPrintStream.ofInMemory(session).setTerminalMode(NTerminalMode.FORMATTED);
        NPrintStream out = session.out();
        if (!helpColors && helpFor.isEmpty()) {
            out.println(NWorkspaceExt.of(session.getWorkspace()).getHelpText(session));
            out.flush();
        }
        for (String arg : helpFor) {
            NExecutableInformation w = null;
            if (arg.equals("help")) {
                out.println(NMsg.ofC("%s :", arg));
                showDefaultHelp();
                out.flush();
            } else {
                try {
                    try {
                        w = NExecCommand.of(session).addCommand(arg).which();
                    } catch (Exception ex) {
                        LOG.with().session(session).level(Level.FINE).error(ex).log(NMsg.ofC("failed to execute : %s", arg));
                        //ignore
                    }
                    if (w != null) {
                        out.println(NMsg.ofC("%s :", arg));
                        out.println(w.getHelpText());
                        out.flush();
                    } else {
                        session.getTerminal().err().println(NMsg.ofC("%s : not found", arg));
                    }
                } finally {
                    if (w != null) {
                        w.close();
                    }
                }
            }
        }
        return NExecutionException.SUCCESS;
    }

    @Override
    public void close() {

    }
}
