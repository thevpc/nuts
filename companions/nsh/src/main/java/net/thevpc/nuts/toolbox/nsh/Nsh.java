package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NCmdLineContext;

import net.thevpc.nuts.NLauncherOptions;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.options.DefaultNShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellOptions;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NSupportMode;

public class Nsh implements NApplication {

    private static final HashSet<String> CONTEXTUAL_BUILTINS = new HashSet<>(Arrays.asList(
            "showerr", "cd", "set", "unset", "enable",
            "login", "logout", "help", "version", "alias",
            "unalias", "exit"
    ));

    public static void main(String[] args) {
        new Nsh().run(args);
    }

    @Override
    public void onInstallApplication() {
        NLogOp log = NLogOp.of(Nsh.class);
        log.level(Level.CONFIG).verb(NLogVerb.START).log(NMsg.ofPlain("[nsh] Installation..."));
        NSession session = NSession.of().get();
        NApp.of().processCmdLine(new NCmdLineRunner() {
            @Override
            public void init(NCmdLine cmdLine, NCmdLineContext context) {
                cmdLine.setCommandName("nsh --nuts-exec-mode=install");
            }

            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                if (session.isTrace() || session.isYes()) {
                    log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofC("[nsh] activating options trace=%s yes=%s", session.isTrace(), session.isYes()));
                }
                //id will not include version or
                String nshIdStr = NApp.of().getId().get().getShortName();
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
                        if (session.getWorkspace()
                                .addCommand(new NCommandConfig()
                                        .setFactoryId("nsh")
                                        .setName(command.getName())
                                        .setCommand(nshIdStr, "-c", command.getName())
                                        .setOwner(NApp.of().getId().orNull())
                                        .setHelpCommand(nshIdStr, "-c", "help", "--ntf", command.getName())
                                )) {
                            reinstalled.add(command.getName());
                        } else {
                            firstInstalled.add(command.getName());
                        }
                    }
                }

                if (!firstInstalled.isEmpty()) {
                    log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofC("[nsh] registered %s nsh commands : %s", firstInstalled.size(),
                            String.join(", ", firstInstalled)));
                }
                if (!reinstalled.isEmpty()) {
                    log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofC("[nsh] re-registered %s nsh commands : %s", reinstalled.size(),
                            String.join(", ", reinstalled)));
                }
                if (session.isPlainTrace()) {
                    NTexts factory = NTexts.of();
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
                session.getWorkspace().saveConfig(false);
                if (NWorkspace.get().getBootOptions().getInitScripts()
                        .ifEmpty(true)
                        .orElse(false)) {
                    boolean initLaunchers = NWorkspace.get().getBootOptions().getInitLaunchers()
                            .ifEmpty(true)
                            .orElse(false);
                    NWorkspace.get().addLauncher(
                            new NLauncherOptions()
                                    .setId(NApp.of().getId().orNull())
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
    public void onUpdateApplication() {
        NLogOp log = NLogOp.of(Nsh.class);
        log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofPlain("[nsh] update..."));
        NSession session = NSession.of().get();
        NVersion currentVersion = NApp.of().getVersion().orNull();
        NVersion previousVersion = NApp.of().getPreviousVersion().orNull();
        onInstallApplication();
    }

    @Override
    public void onUninstallApplication() {
        NLogOp log = NLogOp.of(Nsh.class);
        log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofPlain("[nsh] uninstallation..."));
        NSession session = NSession.of().get();
        try {
            try {
                NWorkspace.get().removeCommandFactory("nsh");
            } catch (Exception notFound) {
                //ignore!
            }
            Set<String> uninstalled = new TreeSet<>();
            for (NCustomCmd command : NWorkspace.get().findCommandsByOwner(NApp.of().getId().orNull())) {
                try {
                    NWorkspace.get().removeCommand(command.getName());
                    uninstalled.add(command.getName());
                } catch (Exception ex) {
                    if (session.isPlainTrace()) {
                        NTexts factory = NTexts.of();
                        session.err().println(NMsg.ofC("unable to unregister %s.",
                                factory.ofStyled(command.getName(), NTextStyle.primary3())
                        ));
                    }
                }
            }
            if (!uninstalled.isEmpty()) {
                log.level(Level.CONFIG).verb(NLogVerb.INFO).log(NMsg.ofC("[nsh] unregistered %s nsh commands : %s", uninstalled.size(),
                        String.join(", ", uninstalled)));
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    @Override
    public void run() {

        //before loading NShell check if we need to activate rich term
        NSession session = NSession.of().get();
        DefaultNShellOptionsParser options = new DefaultNShellOptionsParser(session);
        NShellOptions o = options.parse(NApp.of().getCmdLine().toStringArray());

//        if (o.isEffectiveInteractive()) {
//            session.getWorkspace().io().term().enableRichTerm(session);
//        }
        new NShell(new NShellConfiguration().setSession(session)
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
        ).run();
    }

}
