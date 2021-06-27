package net.thevpc.nuts.toolbox.nadmin;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class NAdminMain implements NutsApplication {

    private List<NAdminSubCommand> subCommands;
    private NutsApplicationContext applicationContext;

    public static void main(String[] args) {
        new NAdminMain().runAndExit(args);
    }

    public List<NAdminSubCommand> getSubCommands() {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    applicationContext.getWorkspace().extensions().createAllSupported(NAdminSubCommand.class, this)
            );
        }
        return subCommands;
    }

    @Override
    public void onInstallApplication(NutsApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        NutsCommandLine cmd = applicationContext.getCommandLine();
        NutsArgument a;
        while (cmd.hasNext()) {
            if (applicationContext.configureFirst(cmd)) {
                // consumed
            } else if ((a = cmd.nextBoolean("--skip-init")) != null) {
                if (a.getBooleanValue()) {
                    return;
                }
            } else {
                cmd.unexpectedArgument();
            }
        }
        NutsWorkspace ws = applicationContext.getWorkspace();
        if (applicationContext.getSession().isPlainTrace()) {
            applicationContext.getSession().out().println("looking for java installations in default locations...");
        }
        NutsSdkLocation[] found = ws.sdks()
                .setSession(applicationContext.getSession().copy().setTrace(false))
                .searchSystem("java");
        int someAdded = 0;
        for (NutsSdkLocation java : found) {
            if (ws.sdks().add(java)) {
                someAdded++;
            }
        }
        NutsTextManager factory = applicationContext.getWorkspace().text();
        if (applicationContext.getSession().isPlainTrace()) {
            if (someAdded == 0) {
                applicationContext.getSession().out().print("```error no new``` java installation locations found...\n");
            } else if (someAdded == 1) {
                applicationContext.getSession().out().printf("%s new java installation location added...\n",factory.forStyled("1",NutsTextStyle.primary2()));
            } else {
                applicationContext.getSession().out().printf("%s new java installation locations added...\n", factory.forStyled(""+someAdded,NutsTextStyle.primary2()));
            }
            applicationContext.getSession().out().println("you can always add another installation manually using 'nadmin add java' command.");
        }
        if (!ws.config().isReadOnly()) {
            ws.config().save();
        }

        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("add", "script", "--ignore-unsupported-os", "--embedded"));
        LinkedHashSet<String> companions = new LinkedHashSet<>();
        companions.add("net.thevpc.nuts:nuts");
        companions.add("net.thevpc.nuts.toolbox:nadmin");
        companions.addAll(applicationContext.getWorkspace().getCompanionIds(applicationContext.getSession()).stream().map(NutsId::getShortName).collect(Collectors.toList()));
        args.addAll(companions);
        applicationContext.getSession().setConfirm(NutsConfirmationMode.YES);

        run(this.applicationContext.getSession(), args.toArray(new String[0]));
    }

    @Override
    public void onUpdateApplication(NutsApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        NutsCommandLine cmd = applicationContext.getCommandLine();
        NutsArgument a;
        while (cmd.hasNext()) {
            if (applicationContext.configureFirst(cmd)) {
                // consumed
            } else if ((a = cmd.nextBoolean("--skip-init")) != null) {
                if (a.getBooleanValue()) {
                    return;
                }
            } else {
                cmd.unexpectedArgument();
            }
        }
        NutsWorkspace ws = applicationContext.getWorkspace();
        for (NutsSdkLocation java : ws.sdks().searchSystem("java")) {
            ws.sdks().add(java);
        }
        ws.config().save();

        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("add", "script", "--ignore-unsupported-os", "--embedded"));
        LinkedHashSet<String> companions = new LinkedHashSet<>();
        companions.add("net.thevpc.nuts:nuts");
        companions.add("net.thevpc.nuts.toolbox:nadmin");
        companions.addAll(applicationContext.getWorkspace().getCompanionIds(applicationContext.getSession()).stream().map(NutsId::getShortName).collect(Collectors.toList()));
        args.addAll(companions);
        applicationContext.getSession().setConfirm(NutsConfirmationMode.YES);
        run(applicationContext.getSession(), args.toArray(new String[0]));
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        applicationContext.getWorkspace().extensions().discoverTypes(
                applicationContext.getAppId(),
                Thread.currentThread().getContextClassLoader());

        Boolean autoSave = true;
        NutsCommandLine cmdLine = applicationContext.getCommandLine();
        boolean empty = true;
        NutsArgument a;
        do {
            if (applicationContext.configureFirst(cmdLine)) {
                //
            } else {
                NAdminSubCommand selectedSubCommand = null;
                for (NAdminSubCommand subCommand : getSubCommands()) {
                    if (subCommand.exec(cmdLine, autoSave, applicationContext)) {
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
                    NutsPrintStream out = applicationContext.getSession().err();
                    out.printf("Unexpected %s%n", cmdLine.peek());
                    out.printf("type for more help : nadmin -h%n");
                    throw new NutsExecutionException(applicationContext.getSession(), "Unexpected " + cmdLine.peek(), 1);
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            NutsPrintStream out = applicationContext.getSession().err();
            out.printf("missing nadmin command%n");
            out.printf("type for more help : nadmin -h%n");
            throw new NutsExecutionException(applicationContext.getSession(), "missing nadmin command", 1);
        }
    }


}
