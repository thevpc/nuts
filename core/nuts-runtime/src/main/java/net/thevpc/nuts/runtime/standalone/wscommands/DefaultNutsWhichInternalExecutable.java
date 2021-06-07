/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNutsWhichInternalExecutable extends DefaultInternalNutsExecutableCommand {

    private final NutsExecCommand execCommand;

    public DefaultNutsWhichInternalExecutable(String[] args, NutsSession session, NutsExecCommand execCommand) {
        super("which", args, session);
        this.execCommand = execCommand;
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> commands = new ArrayList<String>();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsCommandLine commandLine = ws.commandLine().create(args);
        while (commandLine.hasNext()) {
            NutsArgument a = commandLine.peek();
            if (a.isOption()) {
                switch (a.getStringKey()) {
                    case "--help": {
                        commandLine.skipAll();
                        showDefaultHelp();
                        return;
                    }
                    default: {
                        commandLine.unexpectedArgument();
                    }
                }
            } else {
                commandLine.skip();
                commands.add(a.toString());
                commands.addAll(Arrays.asList(commandLine.toStringArray()));
                commandLine.skipAll();
            }
        }
        if (commands.isEmpty()) {
            throw new NutsIllegalArgumentException(getSession(), "which: missing commands");
        }
        NutsTextManager factory = ws.text();
        for (String arg : this.args) {
            NutsPrintStream out = getSession().out();
            try {
                NutsExecutableInformation p = execCommand.copy().setSession(getSession()).clearCommand().configure(false, arg).which();
                boolean showDesc = false;
                switch (p.getType()) {
                    case SYSTEM: {
                        out.printf("%s : %s %s%n",
                                factory.forStyled(arg, NutsTextStyle.primary(4)),
                                factory.forStyled("system command", NutsTextStyle.primary(6))
                                , p.getDescription());
                        break;
                    }
                    case ALIAS: {
                        out.printf("%s : %s (owner %s ) : %s%n",
                                factory.forStyled(arg, NutsTextStyle.primary(4)),
                                factory.forStyled("nuts alias", NutsTextStyle.primary(6)),
                                p.getId(),
                                ws.commandLine().create(ws.aliases().find(p.getName()).getCommand())
                        );
                        break;
                    }
                    case ARTIFACT: {
                        if (p.getId() == null) {
                            throw new NutsNotFoundException( getSession(), arg);
                        }
                        out.printf("%s : %s %s%n",
                                factory.forStyled(arg, NutsTextStyle.primary(4)),
                                factory.forStyled("nuts package", NutsTextStyle.primary(6)),
                                p.getId()/*, p.getDescription()*/
                        );
                        break;
                    }
                    case INTERNAL: {
                        out.printf("%s : %s %n",
                                factory.forStyled("internal command", NutsTextStyle.primary(6)),
                                factory.forStyled(arg, NutsTextStyle.primary(4))
                        );
                        break;
                    }
                }
                if (showDesc) {
                    out.printf("\t %s%n", arg/*, p.getDescription()*/);
                }
            } catch (NutsNotFoundException ex) {
                out.printf("%s : %s%n", factory.forStyled(arg,NutsTextStyle.primary(4)),factory.forStyled("not found",NutsTextStyle.error()));
            }
        }
    }

}
