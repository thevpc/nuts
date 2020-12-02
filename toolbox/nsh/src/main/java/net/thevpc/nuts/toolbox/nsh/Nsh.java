package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.common.strings.StringUtils;
import net.thevpc.jshell.AutoCompleteCandidate;
import net.thevpc.jshell.JShellBuiltin;
import net.thevpc.jshell.JShellException;
import net.thevpc.nuts.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Nsh extends NutsApplication {

    public static final Logger LOG = Logger.getLogger(Nsh.class.getName());
    private static final HashSet<String> CONTEXTUAL_BUILTINS = new HashSet<>(Arrays.asList(
            "showerr", "cd", "set", "unset", "enable",
            "login", "logout", "help", "version", "alias",
            "unalias", "exit"
    ));

    public static void main(String[] args) {
        new Nsh().runAndExit(args);
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[nsh] Installation...");
        NutsCommandLine cmd = applicationContext.getCommandLine()
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
        if (trace || force) {
            LOG.log(Level.FINER, "[nsh] Activating options trace={0} force={1}", new Object[]{trace, force});
        }
        //id will not include version or
        String nshIdStr = applicationContext.getAppId().getShortName();
        NutsWorkspace ws = applicationContext.getWorkspace();
        NutsWorkspaceConfigManager cfg = ws.config();
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
        JShellBuiltin[] commands = c.getRootContext().builtins().getAll();
        Set<String> reinstalled = new TreeSet<>();
        Set<String> firstInstalled = new TreeSet<>();
        NutsSession sessionCopy = applicationContext.getSession().copy();
        sessionCopy.setTrace(false);
        for (JShellBuiltin command : commands) {
            if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                //avoid recursive definition!
                if (ws.aliases().add(new NutsCommandAliasConfig()
                                .setFactoryId("nsh")
                                .setName(command.getName())
                                .setCommand(nshIdStr, "-c", command.getName())
                                .setOwner(applicationContext.getAppId())
                                .setHelpCommand(nshIdStr, "-c", "help", "--code", command.getName()),
                        new NutsAddOptions()
                                .setSession(sessionCopy.setConfirm(NutsConfirmationMode.YES).setTrace(false))
                )) {
                    reinstalled.add(command.getName());
                } else {
                    firstInstalled.add(command.getName());
                }
            }
        }

        if (firstInstalled.size() > 0) {
            LOG.log(Level.FINER, "[nsh] registered {0} nsh commands : {1}", new Object[]{firstInstalled.size(),
                    String.join(", ", firstInstalled)
            });
        }
        if (reinstalled.size() > 0) {
            LOG.log(Level.FINER, "[nsh] re-registered {0} nsh commands : {1}", new Object[]{reinstalled.size(),
                    String.join(", ", reinstalled)
            });
        }
        if (trace && applicationContext.getSession().isPlainOut()) {
            if (firstInstalled.size() > 0) {
                applicationContext.getSession().out().printf("registered ####%s#### nsh commands : ####%s#### \n", firstInstalled.size(),
                        String.join(", ", firstInstalled));
            }
            if (reinstalled.size() > 0) {
                applicationContext.getSession().out().printf("re-registered ####%s#### nsh commands : ####%s#### \n", reinstalled.size(),
                        String.join(", ", reinstalled));
            }
        }
        cfg.save(false, applicationContext.getSession());
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[nsh] Update...");
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplication(applicationContext);
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[nsh] Uninstallation...");
        try {
            NutsWorkspace ws = applicationContext.getWorkspace();
            try {
                ws.aliases().removeFactory("nsh", null);
            } catch (Exception notFound) {
                //ignore!
            }
            for (NutsWorkspaceCommandAlias command : ws.aliases().findByOwner(applicationContext.getAppId(), applicationContext.getSession())) {
                try {
                    ws.aliases().remove(command.getName(), new NutsRemoveOptions());
                } catch (Exception ex) {
                    if (applicationContext.getSession().isPlainTrace()) {
                        applicationContext.getSession().err().printf("Unable to uninstall ####%s#### .\n", command.getName());
                    }
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {
        new NutsJavaShell(applicationContext)
                .executeShell(applicationContext.getArguments());
    }

}
