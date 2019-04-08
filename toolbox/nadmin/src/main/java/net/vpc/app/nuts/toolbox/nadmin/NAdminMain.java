package net.vpc.app.nuts.toolbox.nadmin;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.io.PrintStream;
import java.util.*;

public class NAdminMain extends NutsApplication {

    private List<NAdminSubCommand> subCommands;

    public static void main(String[] args) {
        new NAdminMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    context.getWorkspace().extensions().createAllSupported(NAdminSubCommand.class, this)
            );
        }
        Boolean autoSave = true;
        CommandLine cmdLine = new CommandLine(context);
        boolean empty = true;
        Argument a;
        do {
            if (context.configure(cmdLine)) {
                //
            } else {
                NAdminSubCommand selectedSubCommand = null;
                for (NAdminSubCommand subCommand : subCommands) {
                    if (subCommand.exec(cmdLine, this, autoSave, context)) {
                        selectedSubCommand = subCommand;
                        empty = false;
                        break;
                    }
                }
                if (selectedSubCommand != null) {
                    continue;
                }

                if (!cmdLine.isExecMode()) {
                    return;
                }
                if (cmdLine.hasNext()) {
                    PrintStream out = context.err();
                    out.printf("Unexpected %s\n", cmdLine.get());
                    out.printf("type for more help : config -h\n");
                    throw new NutsExecutionException("Unexpected " + cmdLine.get(),1);
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = context.err();
            out.printf("Missing config command\n");
            out.printf("type for more help : config -h\n");
            throw new NutsExecutionException("Missing config command", 1);
        }
    }

    public void showRepo(NutsApplicationContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.config().isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        PrintStream out = context.out();
        out.print(prefix);
        if (enabled) {
            out.print("==" + repository.config().getName() + disabledString + "==");
        } else {
            out.print("@@" + repository.config().getName() + disabledString + "@@");
        }
        out.print(" : " + repository.getRepositoryType() + " " + repository.config().getLocation(false));
        out.println();

    }

    public void showRepoTree(NutsApplicationContext context, NutsRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        if (repository.config().isSupportedMirroring()) {
            for (NutsRepository c : repository.config().getMirrors()) {
                showRepoTree(context, c, prefix1);
            }
        }
    }

}
