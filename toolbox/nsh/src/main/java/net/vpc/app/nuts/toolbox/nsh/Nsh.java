package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Nsh extends NutsApplication {
    public static void main(String[] args) {
        new Nsh().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext applicationContext) {
        String[] args = applicationContext.getArgs();
        NutsJavaShell c = new NutsJavaShell(applicationContext);
        return c.run(args);
    }

    @Override
    protected int onInstallApplication(NutsApplicationContext applicationContext) {
        HashMap<String, String> parameters = new HashMap<>();
        String nshIdStr = applicationContext.getAppId().toString();
        parameters.put("list", nshIdStr + " --no-colors -c find-command");
        parameters.put("find", nshIdStr + " --no-colors -c find-command %n");
        parameters.put("exec", nshIdStr + " -c %n");
        NutsWorkspaceConfigManager cfg = applicationContext.getWorkspace().getConfigManager();
        cfg.installCommandFactory(
                new NutsWorkspaceCommandFactoryConfig()
                        .setFactoryId("nsh")
                        .setFactoryType("command")
                        .setPriority(1)
                        .setParameters(parameters)
        );
        NutsJavaShell c = new NutsJavaShell(applicationContext);
        for (NutsCommand command : c.getCommands()) {
            cfg.installCommand(
                    new NutsWorkspaceCommandConfig()
                            .setFactoryId("nsh")
                            .setName(command.getName())
                            .setCommand(nshIdStr, "-c", command.getName())
                            .setId(applicationContext.getAppId())
            );
        }
        cfg.save();
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
            cfg.uninstallCommandFactory("nsh");
            for (NutsWorkspaceCommand command : cfg.findCommands(applicationContext.getAppId())) {
                cfg.uninstallCommand(command.getName());
            }
        } catch (Exception ex) {
            //ignore
        }
        return 0;
    }
}
