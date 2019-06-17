package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.toolbox.nsh.term.NutsJLineTerminal;

import java.util.*;
import net.vpc.common.javashell.JShellException;
import net.vpc.common.javashell.JShellBuiltin;

public class Nsh extends NutsApplication {

    private static final HashSet<String> CONTEXTUAL_BUILTINS = new HashSet<>(Arrays.asList(
            "showerr", "cd", "set", "unset", "enable",
            "login", "logout", "help", "version", "alias",
            "unalias", "exit"
    ));

    public static void main(String[] args) {
        new Nsh().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {
        String[] args = applicationContext.getArguments();
        NutsSystemTerminal st = applicationContext.getWorkspace().io().getSystemTerminal();
        if (st instanceof NutsJLineTerminal || st.getParent() instanceof NutsJLineTerminal) {
            //that's ok
        } else {
            applicationContext.getWorkspace().io().setSystemTerminal(new NutsJLineTerminal());
        }
        NutsJavaShell c = new NutsJavaShell(applicationContext);
        try {
            c.run(args);
        } catch (NutsExecutionException ex) {
            throw ex;
        } catch (JShellException ex) {
            throw new NutsExecutionException(applicationContext.getWorkspace(), ex.getMessage(), ex, ex.getResult());
        } catch (Exception ex) {
            throw new NutsExecutionException(applicationContext.getWorkspace(), ex.getMessage(), ex, 100);
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        NutsCommandLine cmd = applicationContext.commandLine()
                .setCommandName("nsh --nuts-exec-mode=install");
        NutsArgument a;
        boolean force = false;
        boolean trace = applicationContext.getSession().isTrace();
        while (cmd.hasNext()) {
            if ((a = cmd.nextBoolean("-f", "--force")) != null) {
                force = a.getBooleanValue();
            } else if ((a = cmd.nextBoolean("-t", "--trace")) != null) {
                trace = a.getBooleanValue();
            } else {
                cmd.unexpectedArgument();
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
        JShellBuiltin[] commands = c.getGlobalContext().builtins().getAll();
        Set<String> reinstalled = new TreeSet<>();
        Set<String> firstInstalled = new TreeSet<>();
        NutsSession sessionCopy = applicationContext.getSession().copy();
        sessionCopy.setTrace(false);
        for (JShellBuiltin command : commands) {
            if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                //avoid recursive definition!
                if (cfg.addCommandAlias(new NutsCommandAliasConfig()
                        .setFactoryId("nsh")
                        .setName(command.getName())
                        .setCommand(nshIdStr, "-c", command.getName())
                        .setOwner(applicationContext.getAppId())
                        .setHelpCommand(nshIdStr, "-c", "help", "--code", command.getName()),
                        new net.vpc.app.nuts.NutsAddOptions()
                                .session(sessionCopy.force(force).trace(false))
                )) {
                    reinstalled.add(command.getName());
                } else {
                    firstInstalled.add(command.getName());
                }
            }
        }
        if (trace) {
            if (applicationContext.getSession().isPlainOut()) {
                if (firstInstalled.size() > 0) {
                    applicationContext.session().out().printf("Installed ==%s== nsh commands : ==%s== \n", firstInstalled.size(), firstInstalled.toString());
                }
                if (reinstalled.size() > 0) {
                    applicationContext.session().out().printf("Reinstalled ==%s== nsh commands : ==%s== \n", reinstalled.size(), reinstalled.toString());
                }
            }
        }
        cfg.save(false);
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplication(applicationContext);
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
                    if (applicationContext.getSession().isPlainTrace()) {
                        applicationContext.session().err().printf("Unable to uninstall ==%s== .\n", command.getName());
                    }
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }
}
