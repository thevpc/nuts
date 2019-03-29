package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

import java.io.IOException;
import java.util.ArrayList;

public class NdiMain extends NutsApplication {

    public static void main(String[] args) {
        new NdiMain().run(args);
    }

    public SystemNdi createNdi(NutsApplicationContext appContext) {
        SystemNdi ndi = null;
        NutsOsFamily fam = appContext.getWorkspace().getConfigManager().getPlatformOsFamily();
        if (fam==NutsOsFamily.LINUX || fam==NutsOsFamily.UNIX) {
            ndi = new LinuxNdi(appContext);
        }
        return ndi;
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        CommandLine cmd = new CommandLine(appContext);
        Argument a;
        while (cmd.hasNext()) {
            if (appContext.configure(cmd)) {

            } else if ((a = cmd.readNonOption("in", "install")) != null) {
                SystemNdi ndi = createNdi(appContext);
                if (ndi == null) {
                    throw new NutsExecutionException("Platform not supported : " + appContext.getWorkspace().getConfigManager().getPlatformOs(), 2);
                }
                boolean force = false;
                boolean silent = false;
                boolean forceAll = false;
                boolean fetch = false;
                boolean run = false;
                ArrayList<String> executorOptions = new ArrayList<>();
                NutsExecutionType execType = null;
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
                    } else if ((a = cmd.readBooleanOption("-x", "--external")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.EXTERNAL;
                        }
                    } else if ((a = cmd.readBooleanOption("-m", "--embedded")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.EMBEDDED;
                        }
                    } else if ((a = cmd.readBooleanOption("-n", "--native")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.NATIVE;
                        }
                    } else if ((a = cmd.readStringOption("-X", "--exec-options")) != null) {
                        executorOptions.add(a.getStringValue());
                    } else if (cmd.isOption()) {
                        cmd.unexpectedArgument("ndi");
                    } else {
                        run = true;
                        Argument aa = null;
                        try {
                            aa = cmd.read();
                            if (cmd.isExecMode()) {
                                ndi.createNutsScript(
                                        new NdiScriptOptions().setId(aa.getStringExpression())
                                                .setForce(force)
                                                .setForceBoot(forceAll)
                                                .setFetch(fetch)
                                                .setSilent(silent)
                                                .setExecType(execType)
                                                .setExecutorOptions(executorOptions)
                                );
                            }
                        } catch (IOException e) {
                            throw new NutsExecutionException("Unable to run script " + aa + " : " + e.toString(), e, 2);
                        }
                    }
                }
                if (!run) {
                    appContext.err().print("Missing arguments\n");
                    throw new NutsExecutionException("Missing arguments", 1);
                }
                try {
                    ndi.configurePath(force, silent);
                } catch (IOException e) {
                    throw new NutsExecutionException("Unable to configure path : " + e.toString(), e, 2);
                }
            } else {
                cmd.unexpectedArgument("ndi");
            }
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        CommandLine cmd = new CommandLine(applicationContext);
        Argument a;
        boolean force = false;
        boolean silent = false;
        while (cmd.hasNext()) {
            if ((a = cmd.readBooleanOption("-f", "--force")) != null) {
                force = a.getBooleanValue();
            } else if ((a = cmd.readBooleanOption("-s", "--silent")) != null) {
                silent = a.getBooleanValue();
            } else {
                cmd.unexpectedArgument("ndi --nuts-execution-mode=on-install");
            }
        }
        SystemNdi ndi = createNdi(applicationContext);
        if (ndi != null) {
            try {
                ndi.configurePath(force, silent);
            } catch (IOException e) {
                applicationContext.out().println("ndi: install failed : " + e.toString());
                throw new NutsExecutionException("ndi: install failed : " + e.toString(), 1);
            }
            for (String s : new String[]{"nuts", "ndi", "nsh", "nadmin", "nfind"}) {
                try {
                    ndi.createNutsScript(
                            new NdiScriptOptions().setId(s)
                                    .setForce(force)
                                    .setForceBoot(false)
                                    .setFetch(false)
                                    .setSilent(silent)
                                    .setExecType(NutsExecutionType.EXTERNAL)
                                    .setExecutorOptions(new ArrayList<>()));
                } catch (IOException e) {
                    applicationContext.out().println("ndi: " + s + "install failed : " + e.toString());
                    throw new NutsExecutionException("ndi: " + s + "install failed : " + e.toString(), 1);
                }
            }
        }
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
    }
}
