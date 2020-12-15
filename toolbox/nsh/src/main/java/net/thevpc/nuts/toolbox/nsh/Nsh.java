package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.jshell.JShellBuiltin;
import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
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
        NutsSession session = applicationContext.getSession();
        cmd.process(session, NutsCommandLineProcessor.NOP);
        if (session.isTrace() || session.isYes()) {
            LOG.log(Level.FINER, "[nsh] activating options trace={0} yes={1}", new Object[]{session.isTrace(), session.isYes()});
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
        applicationContext.getWorkspace().io().term().enableRichTerm(session);

        NutsJavaShell c = new NutsJavaShell(applicationContext);
        JShellBuiltin[] commands = c.getRootContext().builtins().getAll();
        Set<String> reinstalled = new TreeSet<>();
        Set<String> firstInstalled = new TreeSet<>();
        NutsSession sessionCopy = session.copy();
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
        if (session.isPlainTrace()) {
            if (firstInstalled.size() > 0) {
                session.out().printf("registered ####%s#### nsh commands : ####%s#### \n", firstInstalled.size(),
                        String.join(", ", firstInstalled));
            }
            if (reinstalled.size() > 0) {
                session.out().printf("re-registered ####%s#### nsh commands : ####%s#### \n", reinstalled.size(),
                        String.join(", ", reinstalled));
            }
        }
        cfg.save(false, session);
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[nsh] update...");
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplication(applicationContext);
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[nsh] uninstallation...");
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
                        applicationContext.getSession().err().printf("unable to uninstall ####%s#### .\n", command.getName());
                    }
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {

        //before loading JShell check if we need to activate rich term
        NshOptions options = new NshOptions();
        options.parse(applicationContext.getCommandLine(), applicationContext);

        if (options.isBoot_interactive()) {
            applicationContext.getWorkspace().io().term().enableRichTerm(applicationContext.getSession());
        }
        new NutsJavaShell(applicationContext)
                .executeShell(applicationContext.getArguments());
    }

}
