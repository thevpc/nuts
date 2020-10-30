package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.common.javashell.AutoCompleteCandidate;
import net.vpc.common.javashell.JShellException;
import net.vpc.common.javashell.JShellBuiltin;
import net.vpc.common.strings.StringUtils;
import org.jline.reader.Candidate;

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
    public void run(NutsApplicationContext applicationContext) {
        String[] args = applicationContext.getArguments();
        applicationContext.getWorkspace().io().term().enableRichTerm();
        applicationContext.getWorkspace().io().term().getSystemTerminal()
                .setAutoCompleteResolver(new NutsCommandAutoCompleteProcessor() {
                    @Override
                    public List<NutsArgumentCandidate> resolveCandidates(NutsCommandLine commandline, int wordIndex,NutsWorkspace workspace) {
                        List<NutsArgumentCandidate> candidates=new ArrayList<>();
                        NutsShellContext nutsConsoleContext = (NutsShellContext) workspace.userProperties().get(NutsShellContext.class.getName());
                        if (wordIndex == 0) {
                            for (JShellBuiltin command : nutsConsoleContext.builtins().getAll()) {
                                candidates.add(workspace.commandLine().createCandidate(command.getName()).build());
                            }
                        } else {
                            List<String> autoCompleteWords = new ArrayList<>(Arrays.asList(commandline.toArray()));
                            int x = commandline.getCommandName().length();

                            List<AutoCompleteCandidate> autoCompleteCandidates
                                    = nutsConsoleContext.resolveAutoCompleteCandidates(commandline.getCommandName(), autoCompleteWords, wordIndex, commandline.toString());
                            for (Object cmdCandidate0 : autoCompleteCandidates) {
                                AutoCompleteCandidate cmdCandidate = (AutoCompleteCandidate) cmdCandidate0;
                                if (cmdCandidate != null) {
                                    String value = cmdCandidate.getValue();
                                    if (!StringUtils.isBlank(value)) {
                                        String display = cmdCandidate.getDisplay();
                                        if (StringUtils.isBlank(display)) {
                                            display = value;
                                        }
                                        candidates.add(workspace.commandLine().createCandidate(value).setDisplay(display).build());
                                    }
                                }
                            }
                        }
                        return candidates;
                    }
                });
        NutsJavaShell c = new NutsJavaShell(applicationContext);
        try {
            c.executeShell(args);
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
        LOG.log(Level.FINER, "[Nsh] Installation...");
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
            LOG.log(Level.FINER, "[Nsh] Activating options trace={0} force={1}", new Object[]{trace, force});
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
        sessionCopy.setSilent();
        for (JShellBuiltin command : commands) {
            if (!CONTEXTUAL_BUILTINS.contains(command.getName())) {
                //avoid recursive definition!
                if (ws.aliases().add(new NutsCommandAliasConfig()
                        .setFactoryId("nsh")
                        .setName(command.getName())
                        .setCommand(nshIdStr, "-c", command.getName())
                        .setOwner(applicationContext.getAppId())
                        .setHelpCommand(nshIdStr, "-c", "help", "--code", command.getName()),
                        new net.vpc.app.nuts.NutsAddOptions()
                                .setSession(sessionCopy.yes(true/*force*/).setSilent())
                )) {
                    reinstalled.add(command.getName());
                } else {
                    firstInstalled.add(command.getName());
                }
            }
        }

        if (firstInstalled.size() > 0) {
            LOG.log(Level.FINER, "[Nsh] registered {0} nsh commands : {1}", new Object[]{firstInstalled.size(), firstInstalled.toString()});
        }
        if (reinstalled.size() > 0) {
            LOG.log(Level.FINER, "[Nsh] re-registered {0} nsh commands : {1}", new Object[]{reinstalled.size(), reinstalled.toString()});
        }
        if (trace && applicationContext.getSession().isPlainOut()) {
            if (firstInstalled.size() > 0) {
                applicationContext.getSession().out().printf("registered ==%s== nsh commands : ==%s== \n", firstInstalled.size(), firstInstalled.toString());
            }
            if (reinstalled.size() > 0) {
                applicationContext.getSession().out().printf("re-registered ==%s== nsh commands : ==%s== \n", reinstalled.size(), reinstalled.toString());
            }
        }
        cfg.save(false, applicationContext.getSession());
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[Nsh] Update...");
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplication(applicationContext);
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
        LOG.log(Level.FINER, "[Nsh] Uninstallation...");
        try {
            NutsWorkspace ws = applicationContext.getWorkspace();
            try {
                ws.aliases().removeFactory("nsh", null);
            } catch (Exception notFound) {
                //ignore!
            }
            for (NutsWorkspaceCommandAlias command : ws.aliases().findByOwner(applicationContext.getAppId(), applicationContext.getSession())) {
                try {
                    ws.aliases().remove(command.getName(), new net.vpc.app.nuts.NutsRemoveOptions());
                } catch (Exception ex) {
                    if (applicationContext.getSession().isPlainTrace()) {
                        applicationContext.getSession().err().printf("Unable to uninstall ==%s== .\n", command.getName());
                    }
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }
}
