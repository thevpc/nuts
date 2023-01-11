package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLineContext;
import net.thevpc.nuts.cmdline.NCommandLineProcessor;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.jshell.DefaultJShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellOptions;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

public class Nsh implements NApplication {

    private static final HashSet<String> CONTEXTUAL_BUILTINS = new HashSet<>(Arrays.asList(
            "showerr", "cd", "set", "unset", "enable",
            "login", "logout", "help", "version", "alias",
            "unalias", "exit"
    ));

    public static void main(String[] args) {
        new Nsh().runAndExit(args);
    }

    @Override
    public void onInstallApplication(NApplicationContext applicationContext) {
        NLoggerOp log = NLoggerOp.of(Nsh.class, applicationContext.getSession());
        log.level(Level.CONFIG).verb(NLoggerVerb.START).log(NMsg.ofPlain("[nsh] Installation..."));
        NSession session = applicationContext.getSession();
        applicationContext.processCommandLine(new NCommandLineProcessor() {
            @Override
            public void onCmdInitParsing(NCommandLine commandLine, NCommandLineContext context) {
                commandLine.setCommandName("nsh --nuts-exec-mode=install");
            }

            @Override
            public void onCmdExec(NCommandLine commandLine, NCommandLineContext context) {
                if (session.isTrace() || session.isYes()) {
                    log.level(Level.CONFIG).verb(NLoggerVerb.INFO).log(NMsg.ofJ("[nsh] activating options trace={0} yes={1}", session.isTrace(), session.isYes()));
                }
                //id will not include version or
                String nshIdStr = applicationContext.getAppId().getShortName();
                NConfigs cfg = NConfigs.of(session);
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
                NSession sessionCopy = session.copy();
                for (JShellBuiltin command : commands) {
                    if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                        // avoid recursive definition!
                        // disable trace, summary will be traced later!
                        if (NCustomCommandManager.of(session.copy().setTrace(false)
                                .setConfirm(NConfirmationMode.YES)
                                )
                                .addCommand(new NCommandConfig()
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
                    log.level(Level.CONFIG).verb(NLoggerVerb.INFO).log(NMsg.ofJ("[nsh] registered {0} nsh commands : {1}", firstInstalled.size(),
                            String.join(", ", firstInstalled)));
                }
                if (reinstalled.size() > 0) {
                    log.level(Level.CONFIG).verb(NLoggerVerb.INFO).log(NMsg.ofJ("[nsh] re-registered {0} nsh commands : {1}", reinstalled.size(),
                            String.join(", ", reinstalled)));
                }
                if (session.isPlainTrace()) {
                    NTexts factory = NTexts.of(session);
                    if (firstInstalled.size() > 0) {
                        session.out().println(NMsg.ofC("registered %s nsh commands : %s",
                                factory.ofStyled("" + firstInstalled.size(), NTextStyle.primary3())
                                , factory.ofStyled(String.join(", ", firstInstalled), NTextStyle.primary3())
                        ));
                    }
                    if (reinstalled.size() > 0) {
                        session.out().println(NMsg.ofC("re-registered %s nsh commands : %s",
                                factory.ofStyled("" + reinstalled.size(), NTextStyle.primary3())
                                , factory.ofStyled(String.join(", ", reinstalled), NTextStyle.primary3())
                        ));
                    }
                }
                cfg.save(false);
                if (NBootManager.of(session).getBootOptions().getInitScripts()
                        .ifEmpty(true)
                        .orElse(false)) {
                    boolean initLaunchers =  NBootManager.of(session).getBootOptions().getInitLaunchers()
                            .ifEmpty(true)
                            .orElse(false);
                    NEnvs.of(session).addLauncher(
                            new NLauncherOptions()
                                    .setId(session.getAppId())
                                    .setCreateScript(true)
                                    .setCreateDesktopLauncher(initLaunchers ? NSupportMode.PREFERRED : NSupportMode.NEVER)
                                    .setCreateMenuLauncher(initLaunchers ? NSupportMode.SUPPORTED : NSupportMode.NEVER)
                                    .setOpenTerminal(true)
                    );
                }
            }
        });
    }

    @Override
    public void onUpdateApplication(NApplicationContext applicationContext) {
        NLoggerOp log = NLoggerOp.of(Nsh.class, applicationContext.getSession());
        log.level(Level.CONFIG).verb(NLoggerVerb.INFO).log(NMsg.ofPlain("[nsh] update..."));
        NVersion currentVersion = applicationContext.getAppVersion();
        NVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplication(applicationContext);
    }

    @Override
    public void onUninstallApplication(NApplicationContext applicationContext) {
        NLoggerOp log = NLoggerOp.of(Nsh.class, applicationContext.getSession());
        log.level(Level.CONFIG).verb(NLoggerVerb.INFO).log(NMsg.ofPlain("[nsh] uninstallation..."));
        try {
            NSession session = applicationContext.getSession();
            try {
                NCustomCommandManager.of(session).removeCommandFactory("nsh");
            } catch (Exception notFound) {
                //ignore!
            }
            for (NWorkspaceCustomCommand command : NCustomCommandManager.of(session).findCommandsByOwner(applicationContext.getAppId())) {
                try {
                    NCustomCommandManager.of(session).removeCommand(command.getName());
                } catch (Exception ex) {
                    if (applicationContext.getSession().isPlainTrace()) {
                        NTexts factory = NTexts.of(session);
                        applicationContext.getSession().err().println(NMsg.ofC("unable to uninstall %s.",
                                factory.ofStyled(command.getName(), NTextStyle.primary3())
                        ));
                    }
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    @Override
    public void run(NApplicationContext applicationContext) {

        //before loading JShell check if we need to activate rich term
        DefaultJShellOptionsParser options = new DefaultJShellOptionsParser(applicationContext);
        JShellOptions o = options.parse(applicationContext.getCommandLine().toStringArray());

//        if (o.isEffectiveInteractive()) {
//            applicationContext.getWorkspace().io().term().enableRichTerm(applicationContext.getSession());
//        }
        new JShell(applicationContext).run();
    }

}
