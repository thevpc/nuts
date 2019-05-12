package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.IOException;
import java.util.ArrayList;

public class NdiMain extends NutsApplication {

    public static void main(String[] args) {
        new NdiMain().run(args);
    }

    public SystemNdi createNdi(NutsApplicationContext appContext) {
        SystemNdi ndi = null;
        NutsOsFamily fam = appContext.getWorkspace().config().getPlatformOsFamily();
        if (fam == NutsOsFamily.LINUX || fam == NutsOsFamily.UNIX) {
            ndi = new LinuxNdi(appContext);
        }
        return ndi;
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NutsCommandLine cmd = appContext.newCommandLine();
        NutsArgument a;
        while (cmd.hasNext()) {
            if (appContext.configure(cmd)) {

            } else if ((a = cmd.readNonOption("in", "install")) != null) {
                SystemNdi ndi = createNdi(appContext);
                if (ndi == null) {
                    throw new NutsExecutionException("Platform not supported : " + appContext.getWorkspace().config().getPlatformOs(), 2);
                }
                boolean force = false;
                boolean trace = true;
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
                    } else if ((a = cmd.readBooleanOption("--trace")) != null) {
                        trace = a.getBooleanValue();
                    } else if ((a = cmd.readBooleanOption("-t", "--fetch")) != null) {
                        fetch = a.getBooleanValue();
                    } else if ((a = cmd.readBooleanOption("-x", "--external", "--spawn")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.SPAWN;
                        }
                    } else if ((a = cmd.readBooleanOption("-m", "--embedded")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.EMBEDDED;
                        }
                    } else if ((a = cmd.readBooleanOption("-n", "--native", "--syscall")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.SYSCALL;
                        }
                    } else if ((a = cmd.readStringOption("-X", "--exec-options")) != null) {
                        executorOptions.add(a.getValue().getString());
                    } else if (cmd.get().isOption()) {
                        cmd.unexpectedArgument("ndi");
                    } else {
                        run = true;
                        NutsArgument aa = null;
                        aa = cmd.read();
                        if (cmd.isExecMode()) {
                            try {
                                ndi.createNutsScript(
                                        new NdiScriptOptions().setId(aa.getString())
                                                .setForce(force)
                                                .setForceBoot(forceAll)
                                                .setFetch(fetch)
                                                .setTrace(trace)
                                                .setExecType(execType)
                                                .setExecutorOptions(executorOptions)
                                );
                            } catch (IOException e) {
                                throw new NutsExecutionException("Unable to run script " + aa + " : " + e.toString(), e);
                            }
                        }
                    }
                }
                if (!run) {
                    appContext.err().print("Missing arguments\n");
                    throw new NutsExecutionException("Missing arguments", 1);
                }
                try {
                    ndi.configurePath(force, trace);
                } catch (IOException e) {
                    throw new NutsExecutionException("Unable to configure path : " + e.toString(), e);
                }
            } else {
                cmd.unexpectedArgument("ndi");
            }
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
        NutsCommandLine cmd = applicationContext.newCommandLine();
        NutsArgument a;
        boolean force = false;
        boolean trace = true;
        while (cmd.hasNext()) {
            if ((a = cmd.readBooleanOption("-f", "--force")) != null) {
                force = a.getBooleanValue();
            } else if ((a = cmd.readBooleanOption("--trace")) != null) {
                trace = a.getBooleanValue();
            } else {
                cmd.unexpectedArgument("ndi --nuts-exec-mode=on-install");
            }
        }
        SystemNdi ndi = createNdi(applicationContext);
        if (ndi != null) {
            try {
                ndi.configurePath(force, trace);
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
                                    .setTrace(trace)
                                    .setExecType(NutsExecutionType.SPAWN)
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
        onInstallApplication(applicationContext);
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
    }
}
