package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.io.IOException;

public class NdiMain extends NutsApplication {
    public static void main(String[] args) {
        new NdiMain().launch(args);
    }

    public SystemNdi createNdi(NutsApplicationContext appContext) {
        SystemNdi ndi = null;
        if (appContext.getWorkspace().getConfigManager().getPlatformOs().getName().equals("linux")) {
            ndi = new LinuxNdi(appContext);
        }
        return ndi;
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        CommandLine cmd = new CommandLine(appContext);
        Argument a;
        while (cmd.hasNext()) {
            if (appContext.configure(cmd)) {

            }else if ((a = cmd.readNonOption("in", "install")) != null) {
                SystemNdi ndi = createNdi(appContext);
                if (ndi == null) {
                    throw new NutsExecutionException("Platform not supported : " + appContext.getWorkspace().getConfigManager().getPlatformOs(), 2);
                }
                boolean force = false;
                boolean silent = false;
                boolean forceAll = false;
                boolean fetch = false;
                boolean run = false;
                while (cmd.hasNext()) {
                    if ((a = cmd.readBooleanOption("-f", "--force")) != null) {
                        force = a.getBooleanValue();
                    } else if ((a = cmd.readBooleanOption("-F", "--force-all")) != null) {
                        forceAll = a.getBooleanValue();
                        if (forceAll && !force) {
                            force = true;
                        }
                    } else if ((a = cmd.readBooleanOption("-s", "--silent")) != null) {
                        silent = a.getBooleanValue();
                    } else if ((a = cmd.readBooleanOption("-t", "--fetch")) != null) {
                        fetch = a.getBooleanValue();
                    } else if (cmd.isOption()) {
                        cmd.unexpectedArgument("ndi");
                    } else {
                        run=true;
                        try {
                            ndi.createNutsScript(cmd.read().getStringExpression(), force, forceAll, silent, fetch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(!run){
                    appContext.err().print("Missing arguments\n");
                    return 1;
                }
                try {
                    ndi.configurePath(force, silent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                cmd.unexpectedArgument("ndi");
            }
        }
        return 0;
    }

    @Override
    protected int onInstallApplication(NutsApplicationContext applicationContext) {
        CommandLine cmd = new CommandLine(applicationContext);
        Argument a;
        boolean force = false;
        boolean silent = false;
        while (cmd.hasNext()) {
            if ((a = cmd.readBooleanOption("-f", "--force")) != null) {
                force = a.getBooleanValue();
            } else if ((a = cmd.readBooleanOption("-s", "--silent")) != null) {
                silent = a.getBooleanValue();
            }
        }
        SystemNdi ndi = createNdi(applicationContext);
        if (ndi != null) {
            try {
                ndi.configurePath(force, silent);
            } catch (IOException e) {
                applicationContext.out().println("ndi: install failed : " + e.toString());
                return 1;
            }
            for (String s : new String[]{"nuts", "ndi", "nsh", "nadmin", "nfind"}) {
                try {
                    ndi.createNutsScript(s, force, false, silent, true);
                } catch (IOException e) {
                    applicationContext.out().println("ndi: " + s + "install failed : " + e.toString());
                    return 1;
                }
            }
        }
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

        return 0;
    }
}
