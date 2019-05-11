/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsExecutableInfo;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsUnsupportedOperationException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.fprint.FormattedPrintStream;

/**
 *
 * @author vpc
 */
public class HelpInternalExecutable extends InternalExecutable {

    public HelpInternalExecutable(String[] args, NutsWorkspace ws, NutsSession session) {
        super("help", args, ws, session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> helpFor = new ArrayList<>();
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsOutputFormat outputFormat = NutsOutputFormat.PLAIN;
        while (cmd.hasNext()) {
            NutsCommandArg a = cmd.next();
            if (a.isOption()) {
                switch (a.strKey()) {
                    case "--trace-format": {
                        outputFormat = (NutsOutputFormat.valueOf(cmd.getValueFor(a).getString().toUpperCase()));
                        break;
                    }
                    case "--json": {
                        outputFormat = (NutsOutputFormat.JSON);
                        break;
                    }
                    case "--props": {
                        outputFormat = (NutsOutputFormat.PROPS);
                        break;
                    }
                    case "--table": {
                        outputFormat = (NutsOutputFormat.TABLE);
                        break;
                    }
                    case "--tree": {
                        outputFormat = (NutsOutputFormat.TREE);
                        break;
                    }
                    case "--plain": {
                        outputFormat = (NutsOutputFormat.PLAIN);
                        break;
                    }
                    default: {
                        throw new NutsIllegalArgumentException("Unsupported argument " + a);
                    }
                }

            } else {
                helpFor.add(a.getString());
            }
        }
        switch (outputFormat) {
            case PLAIN: {
                PrintStream fout = getSession(true).getTerminal().fout();
                if (helpFor.isEmpty()) {
                    fout.println(NutsWorkspaceExt.of(ws).getHelpText());
                    fout.flush();
                }
                for (String arg : helpFor) {
                    NutsExecutableInfo w = null;
                    if (arg.equals("help")) {
                        fout.println(arg + " :");
                        showDefaultHelp();
                        fout.flush();
                    } else {
                        try {
                            w = ws.exec().command(arg).which();
                        } catch (Exception ex) {
                        }
                        if (w != null) {
                            fout.println(arg + " :");
                            FormattedPrintStream t=(FormattedPrintStream)fout;
                            fout.println(w.getHelpText());
                            fout.flush();
                        } else {
                            getSession(true).getTerminal().ferr().println(arg + " : Not found");
                        }
                    }
                }
                break;
            }
            default: {
                throw new NutsUnsupportedOperationException("Unsupported format " + outputFormat);
            }
        }

    }

}
