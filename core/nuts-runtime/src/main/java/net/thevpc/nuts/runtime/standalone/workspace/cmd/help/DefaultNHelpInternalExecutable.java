/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.help;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


/**
 * @author thevpc
 */
public class DefaultNHelpInternalExecutable extends DefaultInternalNExecutableCommand {
    private final NLogger LOG;

    public DefaultNHelpInternalExecutable(String[] args, NSession session) {
        super("help", args, session);
        LOG = NLogger.of(DefaultNHelpInternalExecutable.class, session);
    }

    @Override
    public void execute() {
        if(getSession().isDry()){
            dryExecute();
            return;
        }
        List<String> helpFor = new ArrayList<>();
        NSession session = getSession();
        NCommandLine cmdLine = NCommandLine.of(args);
        boolean helpColors = false;
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get(session);
            if (a.isOption()) {
                switch (a.key()) {
                    case "--colors":
                    case "--ntf": {
                        NArg c = cmdLine.nextBoolean().get(session);
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
            NText n = txt.parser().parse(NPath.of("classpath:/net/thevpc/nuts/runtime/ntf-help.ntf", session));
            session.getTerminal().out().print(
                    n == null ? NTexts.of(session).ofStyled(("no help found for " + name), NTextStyle.error()) : n
            );
        }
        NContentType outputFormat = session.getOutputFormat();
        NPrintStream fout = NPrintStream.ofInMemory(session);
        if (!helpColors && helpFor.isEmpty()) {
            fout.println(NWorkspaceExt.of(session.getWorkspace()).getHelpText(session));
            fout.flush();
        }
        for (String arg : helpFor) {
            NExecutableInformation w = null;
            if (arg.equals("help")) {
                fout.println(arg + " :");
                showDefaultHelp();
                fout.flush();
            } else {
                try {
                    w = NExecCommand.of(session).addCommand(arg).which();
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.FINE).error(ex).log(NMsg.ofJ("failed to execute : {0}", arg));
                    //ignore
                }
                if (w != null) {
                    fout.println(arg + " :");
                    fout.println(w.getHelpText());
                    fout.flush();
                } else {
                    session.getTerminal().err().println(arg + " : Not found");
                }
            }
        }
        session.out().println(NString.of(fout.toString(), session));
    }

}
