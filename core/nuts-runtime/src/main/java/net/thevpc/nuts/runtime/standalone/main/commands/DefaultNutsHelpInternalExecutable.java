/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.commands;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
//import net.thevpc.nuts.runtime.standalone.util.fprint.FormattedPrintStream;


/**
 *
 * @author thevpc
 */
public class DefaultNutsHelpInternalExecutable extends DefaultInternalNutsExecutableCommand {
    private final NutsLogger LOG;
    public DefaultNutsHelpInternalExecutable(String[] args, NutsSession session) {
        super("help", args, session);
        LOG=session.getWorkspace().log().of(DefaultNutsHelpInternalExecutable.class);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> helpFor = new ArrayList<>();
        NutsCommandLine cmdLine = getSession().getWorkspace().commandLine().create(args);
        NutsContentType outputFormat = NutsContentType.PLAIN;
        boolean helpColors=false;
        while (cmdLine.hasNext()) {
            NutsContentType of = CoreNutsUtils.readOptionOutputFormat(cmdLine);
            if (of != null) {
                outputFormat = of;
            } else {
                NutsArgument a = cmdLine.peek();
                if (a.isOption()) {
                    switch (a.getStringKey()) {
                        case "--colors":
                        case "--ntf":{
                            NutsArgument c = cmdLine.nextBoolean();
                            if(c.isEnabled()) {
                                helpColors = c.getBooleanValue();
                            }
                            break;
                        }
                        default: {
                            cmdLine.unexpectedArgument();
                        }
                    }
                } else {
                    cmdLine.skip();
                    helpFor.add(a.getString());
                    helpFor.addAll(Arrays.asList(cmdLine.toStringArray()));
                    cmdLine.skipAll();
                }
            }
        }

        if(helpColors){
            getSession().getTerminal().out().print(
                getSession().getWorkspace().formats().text().loadFormattedString(
                    "/net/thevpc/nuts/ntf-help.ntf",
                    getClass().getClassLoader(), "no help found for " + name)
            );
        }
        switch (outputFormat) {
            case PLAIN: {
                PrintStream fout = getSession().out();
                if (!helpColors && helpFor.isEmpty()) {
                    fout.println(NutsWorkspaceExt.of(getSession().getWorkspace()).getHelpText(getSession()));
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
                            w = getSession().getWorkspace().exec().addCommand(arg).which();
                        } catch (Exception ex) {
                            LOG.with().session(getSession()).level(Level.FINE).error(ex).log( "failed to execute : {0}", arg);
                            //ignore
                        }
                        if (w != null) {
                            fout.println(arg + " :");
                            fout.println(w.getHelpText());
                            fout.flush();
                        } else {
                            getSession().getTerminal().err().println(arg + " : Not found");
                        }
                    }
                }
                break;
            }
            default: {
                throw new NutsUnsupportedOperationException(getSession().getWorkspace(), "Unsupported format " + outputFormat);
            }
        }

    }

}
