package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.io.IOException;
import java.util.HashMap;

public class NdiMain extends NutsApplication {
    public static void main(String[] args) {
        new NdiMain().launch(args);
    }

    public LinuxNdi createNdi(NutsApplicationContext appContext) {
        LinuxNdi ndi = null;
        if (appContext.getWorkspace().getConfigManager().getPlatformOs().getName().equals("linux")) {
            ndi = new LinuxNdi(appContext);
        }
        return ndi;
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        CommandLine cmd = new CommandLine(appContext);
        cmd.requireNonEmpty();
        Argument a;
        if (cmd.hasNext()) {
            if ((a = cmd.readNonOption("in", "install")) != null) {
                LinuxNdi ndi = createNdi(appContext);
                if (ndi == null) {
                    throw new NutsExecutionException("Platform not supported : " + appContext.getWorkspace().getConfigManager().getPlatformOs(),2);
                }
                boolean force = false;
                boolean silent = false;
                boolean forceAll = false;
                boolean fetch = false;
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
                        try {
                            ndi.createNutsScript(cmd.read().getString(), force, forceAll, silent,fetch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return 0;
        } else {
            appContext.err().print("Missing arguments\n");
            return 1;
        }
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
            }
        }
        LinuxNdi ndi = createNdi(applicationContext);
        if (ndi != null) {
            for (String s : new String[]{"nuts", "ndi", "nsh","nadmin","nfind"}) {
                try {
                    ndi.createNutsScript(s, force, false, silent,true);
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
