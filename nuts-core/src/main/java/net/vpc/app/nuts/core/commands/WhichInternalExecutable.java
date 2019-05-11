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
import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsExecutableInfo;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class WhichInternalExecutable extends InternalExecutable {

    private final NutsExecCommand execCommand;

    public WhichInternalExecutable(String[] args, NutsWorkspace ws, NutsSession session, NutsExecCommand execCommand) {
        super("which", args, ws, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> commands = new ArrayList<String>();
        NutsCommandLine commandLine = new NutsCommandLine(args);
        while (commandLine.hasNext()) {
            NutsCommandArg a = commandLine.next();
            if (commands.isEmpty()) {
                if (a.isOption()) {
                    switch (a.strKey()) {
                        case "--help": {
                            showDefaultHelp();
                            return;
                        }
                        default: {
                            throw new NutsIllegalArgumentException("which: Unsupported option " + a.toString());
                        }
                    }
                } else {
                    commands.add(a.toString());
                }
            } else {
                commands.add(a.toString());
            }
        }
        if (commands.isEmpty()) {
            throw new NutsIllegalArgumentException("which: missing commands");
        }
        for (String arg : this.args) {
            PrintStream out = getSession(true).getTerminal().fout();
            try {
                NutsExecutableInfo p = execCommand.copy().session(getSession(true)).clearCommand().parseOptions(arg).which();
                boolean showDesc = false;
                switch (p.getType()) {
                    case SYSTEM: {
                        out.printf("[[%s]] : ==system command== %s%n", arg, p.getDescription());
                        break;
                    }
                    case ALIAS: {
                        out.printf("[[%s]] : ==nuts alias== (owner %N ) : %N%n", arg, p.getId() == null ? null : ws.formatter().createIdFormat().toString(p.getId()), NutsCommandLine.escapeArguments(ws.config().findCommandAlias(p.getName()).getCommand()));
                        break;
                    }
                    case COMPONENT: {
                        if (p.getId() == null) {
                            throw new NutsNotFoundException(arg);
                        }
                        out.printf("[[%s]] : ==nuts component== %N%n", arg, ws.formatter().createIdFormat().toString(p.getId()), p.getDescription());
                        break;
                    }
                    case INTERNAL: {
                        out.printf("[[%s]] : ==internal command== %n", arg);
                        break;
                    }
                }
                if (showDesc) {
                    out.printf("\t %N%n", arg, p.getDescription());
                }
            } catch (NutsNotFoundException ex) {
                out.printf("[[%s]] : @@not found@@%n", arg);
            }
        }
    }

}
