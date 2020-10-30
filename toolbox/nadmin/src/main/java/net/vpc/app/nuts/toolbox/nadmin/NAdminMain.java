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
        NutsCommandLine cmdLine = context.getCommandLine();
        boolean empty = true;
        NutsArgument a;
        do {
            if (context.configureFirst(cmdLine)) {
                //
            } else {
                NAdminSubCommand selectedSubCommand = null;
                for (NAdminSubCommand subCommand : subCommands) {
                    if (subCommand.exec(cmdLine, autoSave, context)) {
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
                    PrintStream out = context.getSession().err();
                    out.printf("Unexpected %s%n", cmdLine.peek());
                    out.printf("type for more help : nadmin -h%n");
                    throw new NutsExecutionException(context.getWorkspace(), "Unexpected " + cmdLine.peek(), 1);
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = context.getSession().err();
            out.printf("Missing nadmin command%n");
            out.printf("type for more help : nadmin -h%n");
            throw new NutsExecutionException(context.getWorkspace(), "Missing nadmin command", 1);
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        NutsWorkspace ws = applicationContext.getWorkspace();
        if(applicationContext.getSession().isPlainTrace()){
            applicationContext.getSession().out().println("looking for java installations in default locations...");
            applicationContext.getSession().out().println("you can always manually add another installation manually using 'nadmin add java' command.");
        }
        for (NutsSdkLocation java : ws.sdks().searchSystem("java", applicationContext.getSession().copy().setTrace(false))) {
            ws.sdks().add(java,new NutsAddOptions().setSession(applicationContext.getSession()));
        }
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsWorkspace ws = applicationContext.getWorkspace();
        for (NutsSdkLocation java : ws.sdks().searchSystem("java", applicationContext.getSession())) {
            ws.sdks().add(java,new NutsAddOptions().setSession(applicationContext.getSession()));
        }
    }
}
