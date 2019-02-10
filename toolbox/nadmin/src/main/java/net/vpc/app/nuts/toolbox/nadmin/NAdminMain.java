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
        new NAdminMain().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext context) {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    context.getWorkspace().getExtensionManager().createAllSupported(NAdminSubCommand.class, this)
            );
        }
        Boolean autoSave = true;
        CommandLine cmdLine = new CommandLine(context);
        boolean empty = true;
        Argument a;
        do {
            if (context.configure(cmdLine)) {
                //
            }else {
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
                    return 0;
                }
                if (cmdLine.hasNext()) {
                    PrintStream out = context.err();
                    out.printf("Unexpected %s\n", cmdLine.get());
                    out.printf("type for more help : config -h\n");
                    return 1;
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = context.err();
            out.printf("Missing config command\n");
            out.printf("type for more help : config -h\n");
            return 1;
        }
        return 0;
    }

    public void showRepo(NutsApplicationContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        PrintStream out = context.out();
        out.print(prefix);
        if (enabled) {
            out.print("==" + repository.getName() + disabledString + "==");
        } else {
            out.print("@@" + repository.getName() + disabledString + "@@");
        }
        out.print(" : " + repository.getRepositoryType() +" "+repository.getConfigManager().getLocation());
        out.println();

    }

    public void showRepoTree(NutsApplicationContext context, NutsRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        for (NutsRepository c : repository.getMirrors()) {
            showRepoTree(context, c, prefix1);
        }
    }


}
