package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.options.DefaultNShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellOptions;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NLogVerb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;

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
    public void onInstallApplication(NSession session) {
        NLogOp log = NLogOp.of(Nsh.class, session);
        log.level(Level.CONFIG).verb(NLogVerb.START).log(NMsg.ofPlain("[nsh] Installation..."));
        session.processAppCommandLine(new NCmdLineProcessor() {
            @Override
            public void onCmdInitParsing(NCmdLine commandLine, NCmdLineContext context) {
                commandLine.setCommandName("nsh --nuts-exec-mode=install");
            }

            @Override
            public void onCmdExec(NCmdLine commandLine, NCmdLineContext context) {
                if (session.isTrace() || session.isYes()) {
                    log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofJ("[nsh] activating options trace={0} yes={1}", session.isTrace(), session.isYes()));
                }
                //id will not include version or
                String nshIdStr = session.getAppId().getShortName();
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
//        session.getWorkspace().io().term().enableRichTerm(session);

                NShell c = new NShell(new NShellConfiguration().setSession(session)
                        .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                );
                NShellBuiltin[] commands = c.getRootContext().builtins().getAll();
                Set<String> reinstalled = new TreeSet<>();
                Set<String> firstInstalled = new TreeSet<>();
                NSession sessionCopy = session.copy();
                for (NShellBuiltin command : commands) {
                    if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                        // avoid recursive definition!
                        // disable trace, summary will be traced later!
                        if (NCommands.of(session.copy().setTrace(false)
                                        .setConfirm(NConfirmationMode.YES)
                                )
                                .addCommand(new NCommandConfig()
                                        .setFactoryId("nsh")
                                        .setName(command.getName())
                                        .setCommand(nshIdStr, "-c", command.getName())
                                        .setOwner(session.getAppId())
                                        .setHelpCommand(nshIdStr, "-c", "help", "--ntf", command.getName())
                                )) {
                            reinstalled.add(command.getName());
                        } else {
                            firstInstalled.add(command.getName());
                        }
                    }
                }

                if (!firstInstalled.isEmpty()) {
                    log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofJ("[nsh] registered {0} nsh commands : {1}", firstInstalled.size(),
                            String.join(", ", firstInstalled)));
                }
                if (!reinstalled.isEmpty()) {
                    log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofJ("[nsh] re-registered {0} nsh commands : {1}", reinstalled.size(),
                            String.join(", ", reinstalled)));
                }
                if (session.isPlainTrace()) {
                    NTexts factory = NTexts.of(session);
                    if (!firstInstalled.isEmpty()) {
                        session.out().println(NMsg.ofC("registered %s nsh commands : %s",
                                factory.ofStyled("" + firstInstalled.size(), NTextStyle.primary3()),
                                factory.ofStyled(String.join(", ", firstInstalled), NTextStyle.primary3())
                        ));
                    }
                    if (!reinstalled.isEmpty()) {
                        session.out().println(NMsg.ofC("re-registered %s nsh commands : %s",
                                factory.ofStyled("" + reinstalled.size(), NTextStyle.primary3()),
                                factory.ofStyled(String.join(", ", reinstalled), NTextStyle.primary3())
                        ));
                    }
                }
                cfg.save(false);
                if (NBootManager.of(session).getBootOptions().getInitScripts()
                        .ifEmpty(true)
                        .orElse(false)) {
                    boolean initLaunchers = NBootManager.of(session).getBootOptions().getInitLaunchers()
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
    public void onUpdateApplication(NSession session) {
        NLogOp log = NLogOp.of(Nsh.class, session);
        log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofPlain("[nsh] update..."));
        NVersion currentVersion = session.getAppVersion();
        NVersion previousVersion = session.getAppPreviousVersion();
        onInstallApplication(session);
    }

    @Override
    public void onUninstallApplication(NSession session) {
        NLogOp log = NLogOp.of(Nsh.class, session);
        log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofPlain("[nsh] uninstallation..."));
        try {
            try {
                NCommands.of(session).removeCommandFactory("nsh");
            } catch (Exception notFound) {
                //ignore!
            }
            Set<String> uninstalled = new TreeSet<>();
            for (NCustomCommand command : NCommands.of(session).findCommandsByOwner(session.getAppId())) {
                try {
                    NCommands.of(
                            session.copy().setTrace(false)
                                    .setConfirm(NConfirmationMode.YES)
                    ).removeCommand(command.getName());
                    uninstalled.add(command.getName());
                } catch (Exception ex) {
                    if (session.isPlainTrace()) {
                        NTexts factory = NTexts.of(session);
                        session.err().println(NMsg.ofC("unable to unregister %s.",
                                factory.ofStyled(command.getName(), NTextStyle.primary3())
                        ));
                    }
                }
            }
            if (!uninstalled.isEmpty()) {
                log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofJ("[nsh] unregistered {0} nsh commands : {1}", uninstalled.size(),
                        String.join(", ", uninstalled)));
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    @Override
    public void run(NSession session) {

        //before loading NShell check if we need to activate rich term
        DefaultNShellOptionsParser options = new DefaultNShellOptionsParser(session);
        NShellOptions o = options.parse(session.getAppCommandLine().toStringArray());

//        if (o.isEffectiveInteractive()) {
//            session.getWorkspace().io().term().enableRichTerm(session);
//        }
        new NShell(new NShellConfiguration().setSession(session)
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
        ).run();
    }

}
