package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nsh.term.NutsJLineTerminal;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.util.*;

public class Nsh extends NutsApplication {

    private static final HashSet<String> INTERNAL_COMMANDS = new HashSet<>(Arrays.asList(
            "showerr", "cd", "set", "unset", "declare-command", "undeclare-command",
            "login", "logout", "help", "alias"
    ));

    public static void main(String[] args) {
        new Nsh().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {
        String[] args = applicationContext.getArgs();
        NutsSystemTerminal st = applicationContext.getWorkspace().getSystemTerminal();
        if (st instanceof NutsJLineTerminal || st.getParent() instanceof NutsJLineTerminal) {
            //that's ok
        } else {
            applicationContext.getWorkspace().setSystemTerminal(new NutsJLineTerminal());
        }
        NutsJavaShell c = new NutsJavaShell(applicationContext);
        int r = c.run(args);
        if (r != 0) {
            throw new NutsExecutionException(r);
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        CommandLine cmd = new CommandLine(applicationContext);
        Argument a;
        boolean force = false;
        boolean trace = true;
        while (cmd.hasNext()) {
            if ((a = cmd.readBooleanOption("-f", "--force")) != null) {
                force = a.getBooleanValue();
            } else if ((a = cmd.readBooleanOption("-t", "--trace")) != null) {
                trace = a.getBooleanValue();
            } else {
                cmd.unexpectedArgument("nsh on-install");
            }
        }
        String nshIdStr = applicationContext.getAppId().toString();
        NutsWorkspaceConfigManager cfg = applicationContext.getWorkspace().config();
//        HashMap<String, String> parameters = new HashMap<>();
//        parameters.put("list", nshIdStr + " --no-color -c find-command");
//        parameters.put("find", nshIdStr + " --no-color -c find-command %n");
//        parameters.put("exec", nshIdStr + " -c %n");
//        cfg.installCommandFactory(
//                new NutsWorkspaceCommandFactoryConfig()
//                        .setFactoryId("nsh")
//                        .setFactoryType("command")
//                        .setPriority(1)
//                        .setParameters(parameters)
//        );
        NutsJavaShell c = new NutsJavaShell(applicationContext);
        NutsCommand[] commands = c.getCommands();
        Set<String> reinstalled = new TreeSet<>();
        Set<String> firstInstalled = new TreeSet<>();
        for (NutsCommand command : commands) {
            if (!INTERNAL_COMMANDS.contains(command.getName())) {
                //avoid recursive definition!
                if (cfg.addCommandAlias(
                        new NutsCommandAliasConfig()
                                .setFactoryId("nsh")
                                .setName(command.getName())
                                .setCommand(nshIdStr, "-c", command.getName())
                                .setOwner(applicationContext.getAppId())
                                .setHelpCommand(nshIdStr, "-c", "help",command.getName())
                        ,
                        new net.vpc.app.nuts.NutsAddOptions().setForce(force).setTrace(false)
                )) {
                    reinstalled.add(command.getName());
                } else {
                    firstInstalled.add(command.getName());
                }
            }
        }
        if (trace) {
            if (firstInstalled.size() > 0) {
                applicationContext.out().printf("Installed ==%s== nsh commands : ==%s== \n", firstInstalled.size(), firstInstalled.toString());
            }
            if (reinstalled.size() > 0) {
                applicationContext.out().printf("Reinstalled ==%s== nsh commands : ==%s== \n", reinstalled.size(), reinstalled.toString());
            }
        }
        cfg.save(false);
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
        try {
            NutsWorkspaceConfigManager cfg = applicationContext.getWorkspace().config();
            try {
                cfg.removeCommandAliasFactory("nsh", null);
            } catch (Exception notFound) {
                //ignore!
            }
            for (NutsWorkspaceCommandAlias command : cfg.findCommandAliases(applicationContext.getAppId())) {
                try {
                    cfg.removeCommandAlias(command.getName(), new net.vpc.app.nuts.NutsRemoveOptions());
                } catch (Exception ex) {
                    applicationContext.out().printf("Unable to uninstall ==%s== .\n", command.getName());
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }
}
