package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NdiMain extends NutsApplication {

    public static void main(String[] args) {
        new NdiMain().runAndExit(args);
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
    public void run(NutsApplicationContext context) {
        NutsCommand cmd = context.commandLine()
                .setCommandName("ndi")
                .required();
        List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
        ArrayList<String> ids = new ArrayList<>();
        boolean force = false;
        boolean trace = true;
        boolean forceAll = false;
        boolean fetch = false;
        ArrayList<String> executorOptions = new ArrayList<>();
        NutsExecutionType execType = null;
        NutsArgument a;
        while (cmd.hasNext()) {
            if (context.configureFirst(cmd)) {
                // ignore
            } else if ((a = cmd.next("in", "install")) != null) {
                while (cmd.hasNext()) {
                    if ((a = cmd.nextBoolean("-f", "--force")) != null) {
                        force = a.getValue().getBoolean();
                    } else if ((a = cmd.nextBoolean("-F", "--force-all")) != null) {
                        forceAll = a.getValue().getBoolean();
                        if (forceAll && !force) {
                            force = true;
                        }
                    } else if ((a = cmd.nextBoolean("--trace")) != null) {
                        trace = a.getValue().getBoolean();
                    } else if ((a = cmd.nextBoolean("-t", "--fetch")) != null) {
                        fetch = a.getValue().getBoolean();
                    } else if ((a = cmd.nextBoolean("-x", "--external", "--spawn")) != null) {
                        if (a.getValue().getBoolean()) {
                            execType = NutsExecutionType.SPAWN;
                        }
                    } else if ((a = cmd.nextBoolean("-m", "--embedded")) != null) {
                        if (a.getValue().getBoolean()) {
                            execType = NutsExecutionType.EMBEDDED;
                        }
                    } else if ((a = cmd.nextBoolean("-n", "--native", "--syscall")) != null) {
                        if (a.getValue().getBoolean()) {
                            execType = NutsExecutionType.SYSCALL;
                        }
                    } else if ((a = cmd.nextString("-X", "--exec-options")) != null) {
                        executorOptions.add(a.getValue().getString());
                    } else if (cmd.peek().isOption()) {
                        cmd.unexpectedArgument();
                    } else {
                        ids.add(cmd.next().getString());
                    }
                }
            } else {
                cmd.unexpectedArgument();
            }
        }

        if (ids.isEmpty()) {
            cmd.required();
        }
        if (cmd.isExecMode()) {
            SystemNdi ndi = createNdi(context);
            if (ndi == null) {
                throw new NutsExecutionException(context.getWorkspace(), "Platform not supported : " + context.getWorkspace().config().getPlatformOs(), 2);
            }
            boolean subTrace = trace;
            if (!context.getSession().isPlainTrace()) {
                subTrace = false;
            }
            for (String id : ids) {
                try {
                    result.addAll(
                            Arrays.asList(
                                    ndi.createNutsScript(
                                            new NdiScriptOptions().setId(id)
                                                    .setForce(force)
                                                    .setForceBoot(forceAll)
                                                    .setFetch(fetch)
                                                    .setTrace(subTrace)
                                                    .setExecType(execType)
                                                    .setExecutorOptions(executorOptions)
                                    )
                            ));
                } catch (IOException e) {
                    throw new NutsExecutionException(context.getWorkspace(), "Unable to run script " + id + " : " + e.toString(), e);
                }
            }
            try {
                ndi.configurePath(force, trace);
            } catch (IOException e) {
                throw new NutsExecutionException(context.getWorkspace(), "Unable to configure path : " + e.toString(), e);
            }
            if(trace){
                context.printOutObject(context.out());
            }
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext context) {
        NutsCommand cmd = context.commandLine()
                .setCommandName("ndi --nuts-exec-mode=on-install");
        NutsArgument a;
        boolean force = false;
        boolean trace = true;
        while (cmd.hasNext()) {
            if ((a = cmd.nextBoolean("-f", "--force")) != null) {
                force = a.getValue().getBoolean();
            } else if ((a = cmd.nextBoolean("--trace")) != null) {
                trace = a.getValue().getBoolean();
            } else {
                cmd.unexpectedArgument();
            }
        }
        SystemNdi ndi = createNdi(context);
        if (ndi != null) {
            try {
                ndi.configurePath(force, trace);
            } catch (IOException e) {
                throw new NutsExecutionException(context.getWorkspace(), "ndi: install failed : " + e.toString(), 1);
            }
            List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
            boolean subTrace = trace;
            if (context.getSession().isPlainOut()) {
                subTrace = false;
            }
            for (String s : new String[]{"nuts", "ndi", "nsh", "nadmin"}) {
                try {
                    result.addAll(Arrays.asList(
                            ndi.createNutsScript(
                                    new NdiScriptOptions().setId(s)
                                            .setForce(force)
                                            .setForceBoot(false)
                                            .setFetch(false)
                                            .setTrace(subTrace)
                                            .setExecType(NutsExecutionType.EMBEDDED)
                                            .setExecutorOptions(new ArrayList<>()))
                    ));
                } catch (IOException e) {
                    throw new NutsExecutionException(context.getWorkspace(), "ndi: " + s + "install failed : " + e.toString(), 1);
                }
            }
            if (trace && !context.getSession().isPlainOut()) {
                context.printOutObject(result);
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
