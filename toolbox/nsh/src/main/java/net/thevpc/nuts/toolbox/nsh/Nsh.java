package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.DefaultJShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellOptions;
import net.thevpc.nuts.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Nsh implements NutsApplication {

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
    public void onInstallApplication(NutsApplicationContext applicationContext) {
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
//        parameters.put("forList", nshIdStr + " --no-color -c find-forCommand");
//        parameters.put("find", nshIdStr + " --no-color -c find-forCommand %n");
//        parameters.put("exec", nshIdStr + " -c %n");
//        cfg.installCommandFactory(
//                new NutsWorkspaceCommandFactoryConfig()
//                        .setFactoryId("nsh")
//                        .setFactoryType("forCommand")
//                        .setPriority(1)
//                        .setParameters(parameters)
//        );
//        applicationContext.getWorkspace().io().term().enableRichTerm(session);

        JShell c = new JShell(applicationContext,null);
        JShellBuiltin[] commands = c.getRootContext().builtins().getAll();
        Set<String> reinstalled = new TreeSet<>();
        Set<String> firstInstalled = new TreeSet<>();
        NutsSession sessionCopy = session.copy();
        sessionCopy.setTrace(false);
        for (JShellBuiltin command : commands) {
            if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                //avoid recursive definition!
                if (ws.commands()
                        .setSession(sessionCopy.setConfirm(NutsConfirmationMode.YES).setTrace(false))
                        .addCommand(new NutsCommandConfig()
                                .setFactoryId("nsh")
                                .setName(command.getName())
                                .setCommand(nshIdStr, "-c", command.getName())
                                .setOwner(applicationContext.getAppId())
                                .setHelpCommand(nshIdStr, "-c", "help", "--ntf", command.getName())
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
            NutsTextManager factory = session.getWorkspace().text();
            if (firstInstalled.size() > 0) {
                session.out().printf("registered %s nsh commands : %s \n",
                        factory.forStyled(""+firstInstalled.size(),NutsTextStyle.primary3())
                        ,factory.forStyled(String.join(", ", firstInstalled),NutsTextStyle.primary3())
                );
            }
            if (reinstalled.size() > 0) {
                session.out().printf("re-registered %s nsh commands : %s \n",
                        factory.forStyled(""+reinstalled.size(),NutsTextStyle.primary3())
                        ,factory.forStyled(String.join(", ", reinstalled),NutsTextStyle.primary3())
                );
            }
        }
        cfg.save(false);
    }

    @Override
    public void onUpdateApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[nsh] update...");
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplication(applicationContext);
    }

    @Override
    public void onUninstallApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[nsh] uninstallation...");
        try {
            NutsWorkspace ws = applicationContext.getWorkspace();
            try {
                ws.commands().removeCommandFactory("nsh");
            } catch (Exception notFound) {
                //ignore!
            }
            for (NutsWorkspaceCustomCommand command : ws.commands().findCommandsByOwner(applicationContext.getAppId())) {
                try {
                    ws.commands().removeCommand(command.getName());
                } catch (Exception ex) {
                    if (applicationContext.getSession().isPlainTrace()) {
                        NutsTextManager factory = ws.text();
                        applicationContext.getSession().err().printf("unable to uninstall %s.\n",
                                factory.forStyled(command.getName(),NutsTextStyle.primary3())
                        );
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
        DefaultJShellOptionsParser options = new DefaultJShellOptionsParser(applicationContext);
        JShellOptions o = options.parse(applicationContext.getCommandLine().toStringArray());

//        if (o.isEffectiveInteractive()) {
//            applicationContext.getWorkspace().io().term().enableRichTerm(applicationContext.getSession());
//        }
        new JShell(applicationContext,
                null/*inherit args from applicationContext*/
        ).run();
    }

}
