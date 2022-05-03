package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.nsh.jshell.DefaultJShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellOptions;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

public class Nsh implements NutsApplication {

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
        NutsLoggerOp log = NutsLoggerOp.of(Nsh.class, applicationContext.getSession());
        log.level(Level.CONFIG).verb(NutsLoggerVerb.START).log(NutsMessage.ofPlain("[nsh] Installation..."));
        NutsSession session = applicationContext.getSession();
        applicationContext.processCommandLine(new NutsAppCmdProcessor() {
            @Override
            public void onCmdInitParsing(NutsCommandLine commandline, NutsApplicationContext context) {
                commandline.setCommandName("nsh --nuts-exec-mode=install");
            }

            @Override
            public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
                if (session.isTrace() || session.isYes()) {
                    log.level(Level.CONFIG).verb(NutsLoggerVerb.INFO).log(NutsMessage.ofJstyle("[nsh] activating options trace={0} yes={1}", session.isTrace(), session.isYes()));
                }
                //id will not include version or
                String nshIdStr = applicationContext.getAppId().getShortName();
                NutsWorkspaceConfigManager cfg = session.config();
//        HashMap<String, String> parameters = new HashMap<>();
//        parameters.put("forList", nshIdStr + " --!color -c find-forCommand");
//        parameters.put("find", nshIdStr + " --!color -c find-forCommand %n");
//        parameters.put("exec", nshIdStr + " -c %n");
//        cfg.installCommandFactory(
//                new NutsWorkspaceCommandFactoryConfig()
//                        .setFactoryId("nsh")
//                        .setFactoryType("forCommand")
//                        .setPriority(1)
//                        .setParameters(parameters)
//        );
//        applicationContext.getWorkspace().io().term().enableRichTerm(session);

                JShell c = new JShell(applicationContext, null);
                JShellBuiltin[] commands = c.getRootContext().builtins().getAll();
                Set<String> reinstalled = new TreeSet<>();
                Set<String> firstInstalled = new TreeSet<>();
                NutsSession sessionCopy = session.copy();
                for (JShellBuiltin command : commands) {
                    if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                        // avoid recursive definition!
                        // disable trace, summary will be traced later!
                        if (session.copy().setTrace(false)
                                .setConfirm(NutsConfirmationMode.YES)
                                .commands()
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
                    log.level(Level.CONFIG).verb(NutsLoggerVerb.INFO).log(NutsMessage.ofJstyle("[nsh] registered {0} nsh commands : {1}", firstInstalled.size(),
                            String.join(", ", firstInstalled)));
                }
                if (reinstalled.size() > 0) {
                    log.level(Level.CONFIG).verb(NutsLoggerVerb.INFO).log(NutsMessage.ofJstyle("[nsh] re-registered {0} nsh commands : {1}", reinstalled.size(),
                            String.join(", ", reinstalled)));
                }
                if (session.isPlainTrace()) {
                    NutsTexts factory = NutsTexts.of(session);
                    if (firstInstalled.size() > 0) {
                        session.out().printf("registered %s nsh commands : %s \n",
                                factory.ofStyled("" + firstInstalled.size(), NutsTextStyle.primary3())
                                , factory.ofStyled(String.join(", ", firstInstalled), NutsTextStyle.primary3())
                        );
                    }
                    if (reinstalled.size() > 0) {
                        session.out().printf("re-registered %s nsh commands : %s \n",
                                factory.ofStyled("" + reinstalled.size(), NutsTextStyle.primary3())
                                , factory.ofStyled(String.join(", ", reinstalled), NutsTextStyle.primary3())
                        );
                    }
                }
                cfg.save(false);
                if (session.boot().getBootOptions().getInitScripts()
                        .ifEmpty(true)
                        .orElse(false)) {
                    boolean initLaunchers =  session.boot().getBootOptions().getInitLaunchers()
                            .ifEmpty(true)
                            .orElse(false);
                    session.env().addLauncher(
                            new NutsLauncherOptions()
                                    .setId(session.getAppId())
                                    .setCreateScript(true)
                                    .setCreateDesktopLauncher(initLaunchers ? NutsSupportMode.PREFERRED : NutsSupportMode.NEVER)
                                    .setCreateMenuLauncher(initLaunchers ? NutsSupportMode.SUPPORTED : NutsSupportMode.NEVER)
                                    .setOpenTerminal(true)
                    );
                }
            }
        });
    }

    @Override
    public void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsLoggerOp log = NutsLoggerOp.of(Nsh.class, applicationContext.getSession());
        log.level(Level.CONFIG).verb(NutsLoggerVerb.INFO).log(NutsMessage.ofPlain("[nsh] update..."));
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplication(applicationContext);
    }

    @Override
    public void onUninstallApplication(NutsApplicationContext applicationContext) {
        NutsLoggerOp log = NutsLoggerOp.of(Nsh.class, applicationContext.getSession());
        log.level(Level.CONFIG).verb(NutsLoggerVerb.INFO).log(NutsMessage.ofPlain("[nsh] uninstallation..."));
        try {
            NutsSession session = applicationContext.getSession();
            try {
                session.commands().removeCommandFactory("nsh");
            } catch (Exception notFound) {
                //ignore!
            }
            for (NutsWorkspaceCustomCommand command : session.commands().findCommandsByOwner(applicationContext.getAppId())) {
                try {
                    session.commands().removeCommand(command.getName());
                } catch (Exception ex) {
                    if (applicationContext.getSession().isPlainTrace()) {
                        NutsTexts factory = NutsTexts.of(session);
                        applicationContext.getSession().err().printf("unable to uninstall %s.\n",
                                factory.ofStyled(command.getName(), NutsTextStyle.primary3())
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
        new JShell(applicationContext).run();
    }

}
