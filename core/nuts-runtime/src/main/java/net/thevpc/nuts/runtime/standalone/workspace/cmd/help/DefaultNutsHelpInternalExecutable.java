/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.help;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNutsExecutableCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


/**
 * @author thevpc
 */
public class DefaultNutsHelpInternalExecutable extends DefaultInternalNutsExecutableCommand {
    private final NutsLogger LOG;

    public DefaultNutsHelpInternalExecutable(String[] args, NutsSession session) {
        super("help", args, session);
        LOG = NutsLogger.of(DefaultNutsHelpInternalExecutable.class, session);
    }

    @Override
    public void execute() {
        List<String> helpFor = new ArrayList<>();
        NutsSession session = getSession();
        NutsCommandLine cmdLine = NutsCommandLine.of(args, session);
        boolean helpColors = false;
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            if (a.isOption()) {
                switch (a.getKey().getString()) {
                    case "--colors":
                    case "--ntf": {
                        NutsArgument c = cmdLine.nextBoolean();
                        if (c.isEnabled()) {
                            helpColors = c.getValue().getBoolean();
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
                helpFor.add(a.getString());
                helpFor.addAll(Arrays.asList(cmdLine.toStringArray()));
                cmdLine.skipAll();
            }
        }

        if (helpColors) {
            NutsTexts txt = NutsTexts.of(session);
            NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/ntf-help.ntf",
                    txt.parser().createLoader(getClass().getClassLoader())
            );
            session.getTerminal().out().print(
                    n == null ? ("no help found for " + name) : n.toString()
            );
        }
        NutsContentType outputFormat = session.getOutputFormat();
        NutsPrintStream fout = NutsPrintStream.ofInMemory(session);
        if (!helpColors && helpFor.isEmpty()) {
            fout.println(NutsWorkspaceExt.of(session.getWorkspace()).getHelpText(session));
            fout.flush();
        }
        for (String arg : helpFor) {
            NutsExecutableInformation w = null;
            if (arg.equals("help")) {
                fout.println(arg + " :");
                showDefaultHelp();
                fout.flush();
            } else {
                try {
                    w = session.exec().addCommand(arg).which();
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.FINE).error(ex).log(NutsMessage.jstyle("failed to execute : {0}", arg));
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
        session.out().printlnf(NutsString.of(fout.toString(), session));
    }

}
