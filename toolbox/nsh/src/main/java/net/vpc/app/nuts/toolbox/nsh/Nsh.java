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
        new Nsh().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext applicationContext) {
        String[] args = applicationContext.getArgs();
        NutsSystemTerminal st = applicationContext.getWorkspace().getSystemTerminal();
        if(st instanceof NutsJLineTerminal || st.getParent() instanceof NutsJLineTerminal){
            //that's ok
        }else{
            applicationContext.getWorkspace().setSystemTerminal(new NutsJLineTerminal());
        }
        NutsJavaShell c = new NutsJavaShell(applicationContext);
        return c.run(args);
    }

    @Override
    protected int onInstallApplication(NutsApplicationContext applicationContext) {
        CommandLine cmd=new CommandLine(applicationContext);
        Argument a;
        boolean force=false;
        boolean silent=false;
        while(cmd.hasNext()){
            if((a=cmd.readBooleanOption("-f","--force"))!=null) {
                force = a.getBooleanValue();
            }else if((a=cmd.readBooleanOption("-s","--silent"))!=null){
                silent=a.getBooleanValue();
            }else{
                cmd.unexpectedArgument("nsh on-install");
            }
        }
        String nshIdStr = applicationContext.getAppId().toString();
        NutsWorkspaceConfigManager cfg = applicationContext.getWorkspace().getConfigManager();
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
        int count=0;
        for (NutsCommand command : commands) {
            if(!INTERNAL_COMMANDS.contains(command.getName())) {
                //avoid recursive definition!
                if (cfg.installCommand(
                        new NutsWorkspaceCommandConfig()
                                .setFactoryId("nsh")
                                .setName(command.getName())
                                .setCommand(nshIdStr, "-c", command.getName())
                                .setOwner(applicationContext.getAppId()),
                        force ? NutsConfirmAction.FORCE : NutsConfirmAction.IGNORE
                )) {
                    count++;
                }
            }
        }
        if(!silent){
            applicationContext.out().printf("Installed ==%s== nsh commands.\n", count);
        }
        cfg.save(false);
        return 0;
    }

    @Override
    protected int onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        return 0;
    }

    @Override
    protected int onUninstallApplication(NutsApplicationContext applicationContext) {
        try {
            NutsWorkspaceConfigManager cfg = applicationContext.getWorkspace().getConfigManager();
            cfg.uninstallCommandFactory("nsh",NutsConfirmAction.IGNORE);
            for (NutsWorkspaceCommand command : cfg.findCommands(applicationContext.getAppId())) {
                cfg.uninstallCommand(command.getName(),NutsConfirmAction.IGNORE);
            }
        } catch (Exception ex) {
            //ignore
        }
        return 0;
    }
}
