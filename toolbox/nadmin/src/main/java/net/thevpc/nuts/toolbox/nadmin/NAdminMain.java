package net.thevpc.nuts.toolbox.nadmin;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.SystemNdi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class NAdminMain extends NutsApplication {

    private List<NAdminSubCommand> subCommands;
    private NutsApplicationContext applicationContext;

    public static void main(String[] args) {
        new NAdminMain().runAndExit(args);
    }

    public List<NAdminSubCommand> getSubCommands() {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    applicationContext.getWorkspace().extensions().createAllSupported(NAdminSubCommand.class, this, applicationContext.getSession())
            );
        }
        return subCommands;
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
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
        NutsSdkLocation[] found = ws.sdks().searchSystem("java", applicationContext.getSession().copy().setTrace(false));
        int someAdded = 0;
        for (NutsSdkLocation java : found) {
            if (ws.sdks().add(java, new NutsAddOptions().setSession(applicationContext.getSession()))) {
                someAdded++;
            }
        }
        NutsTextNodeFactory factory = applicationContext.getWorkspace().formats().text().factory();
        if (applicationContext.getSession().isPlainTrace()) {
            if (someAdded == 0) {
                applicationContext.getSession().out().print("```error no new``` java installation locations found...\n");
            } else if (someAdded == 1) {
                applicationContext.getSession().out().printf("%s new java installation location added...\n",factory.styled("1",NutsTextNodeStyle.primary(2)));
            } else {
                applicationContext.getSession().out().printf("%s new java installation locations added...\n", factory.styled(""+someAdded,NutsTextNodeStyle.primary(2)));
            }
            applicationContext.getSession().out().println("you can always add another installation manually using 'nadmin add java' command.");
        }
        if (!ws.config().isReadOnly()) {
            ws.config().save(applicationContext.getSession());
        }

        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("add", "script", "--ignore-unsupported-os", "--embedded"));
        LinkedHashSet<String> companions = new LinkedHashSet<>();
        companions.add("net.thevpc.nuts:nuts");
        companions.add("net.thevpc.nuts.toolbox:nadmin");
        companions.addAll(applicationContext.getWorkspace().companionIds().stream().map(NutsId::getShortName).collect(Collectors.toList()));
        args.addAll(companions);
        applicationContext.getSession().setConfirm(NutsConfirmationMode.YES);

        run(this.applicationContext.getSession(), args.toArray(new String[0]));
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
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
        for (NutsSdkLocation java : ws.sdks().searchSystem("java", applicationContext.getSession())) {
            ws.sdks().add(java, new NutsAddOptions().setSession(applicationContext.getSession()));
        }
        ws.config().save(applicationContext.getSession());

        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("add", "script", "--ignore-unsupported-os", "--embedded"));
        LinkedHashSet<String> companions = new LinkedHashSet<>();
        companions.add("net.thevpc.nuts:nuts");
        companions.add("net.thevpc.nuts.toolbox:nadmin");
        companions.addAll(applicationContext.getWorkspace().companionIds().stream().map(NutsId::getShortName).collect(Collectors.toList()));
        args.addAll(companions);
        applicationContext.getSession().setConfirm(NutsConfirmationMode.YES);
        run(applicationContext.getSession(), args.toArray(new String[0]));
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        applicationContext.getWorkspace().extensions().discoverTypes(
                applicationContext.getAppId(),
                Thread.currentThread().getContextClassLoader(),
                applicationContext.getSession());

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
                    PrintStream out = applicationContext.getSession().err();
                    out.printf("Unexpected %s%n", cmdLine.peek());
                    out.printf("type for more help : nadmin -h%n");
                    throw new NutsExecutionException(applicationContext.getWorkspace(), "Unexpected " + cmdLine.peek(), 1);
                }
                break;
            }
        } while (cmdLine.hasNext());
        if (empty) {
            PrintStream out = applicationContext.getSession().err();
            out.printf("missing nadmin command%n");
            out.printf("type for more help : nadmin -h%n");
            throw new NutsExecutionException(applicationContext.getWorkspace(), "missing nadmin command", 1);
        }
    }


}
