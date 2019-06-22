package net.vpc.app.nuts.toolbox.nadmin;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.PrintStream;
import java.util.*;

public class NAdminMain extends NutsApplication {

    private List<NAdminSubCommand> subCommands;

    public static void main(String[] args) {
        new NAdminMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        context.getWorkspace().extensions().discoverTypes(Thread.currentThread().getContextClassLoader());
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    context.getWorkspace().extensions().createAllSupported(NAdminSubCommand.class, this)
            );
        }
        Boolean autoSave = true;
        NutsCommandLine cmdLine = context.commandLine();
        boolean empty = true;
        NutsArgument a;
        do {
            if (context.configureFirst(cmdLine)) {
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
                    PrintStream out = context.session().err();
                    out.printf("Unexpected %s%n", cmdLine.peek());
                    out.printf("type for more help : config -h%n");
                    throw new NutsExecutionException(context.getWorkspace(), "Unexpected " + cmdLine.peek(), 1);
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = context.session().err();
            out.printf("Missing config command%n");
            out.printf("type for more help : config -h%n");
            throw new NutsExecutionException(context.getWorkspace(), "Missing config command", 1);
        }
    }

    public void showRepo(NutsApplicationContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.config().isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        PrintStream out = context.session().out();
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
