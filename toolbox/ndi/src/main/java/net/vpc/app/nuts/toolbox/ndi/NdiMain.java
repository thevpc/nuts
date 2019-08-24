package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.IOException;
import java.util.*;

public class NdiMain extends NutsApplication {

    public static final String[] COMPANIONS = {"net.vpc.app.nuts:nuts",
            "net.vpc.app.nuts.toolbox:ndi",
            "net.vpc.app.nuts.toolbox:nsh",
            "net.vpc.app.nuts.toolbox:nadmin"
    };

    public static void main(String[] args) {
        new NdiMain().runAndExit(args);
    }

    public SystemNdi createNdi(NutsApplicationContext appContext) {
        SystemNdi ndi = null;
        switch (appContext.getWorkspace().config().getOsFamily()){
            case LINUX:{
                ndi = new LinuxNdi(appContext);
                break;
            }
            case UNIX:{
                ndi = new UnixNdi(appContext);
                break;
            }
            case MACOS:{
                ndi = new MacosNdi(appContext);
                break;
            }
            case WINDOWS:{
                ndi = new WindowsNdi(appContext);
                break;
            }
        }
        return ndi;
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine cmd = context.commandLine()
                .setCommandName("ndi")
                .required();
        List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
        ArrayList<String> idsToInstall = new ArrayList<>();
        ArrayList<String> idsToUninstall = new ArrayList<>();
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
                    if (context.configureFirst(cmd)) {
                    } else if ((a = cmd.nextBoolean("-F", "--force-all")) != null) {
                        forceAll = a.getBooleanValue();
                    } else if ((a = cmd.nextBoolean("-t", "--fetch")) != null) {
                        fetch = a.getBooleanValue();
                    } else if ((a = cmd.nextBoolean("-x", "--external", "--spawn")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.SPAWN;
                        }
                    } else if ((a = cmd.nextBoolean("-b", "--embedded")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.EMBEDDED;
                        }
                    } else if ((a = cmd.nextBoolean("-s", "--native", "--syscall")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.SYSCALL;
                        }
                    } else if ((a = cmd.nextString("-X", "--exec-options")) != null) {
                        executorOptions.add(a.getStringValue());
                    } else if ((a = cmd.nextString("-i", "--installed")) != null) {
                        forceAll = true;
                        for (NutsId resultId : context.getWorkspace().search().installed().getResultIds()) {
                            idsToInstall.add(resultId.getLongName());
                        }
                    } else if ((a = cmd.nextString("-c", "--companions")) != null) {
                        forceAll = true;
                        for (String companion : COMPANIONS) {
                            idsToInstall.add(context.getWorkspace().search().id(companion).latest().getResultIds().required().getLongName());
                        }
                    } else if (cmd.peek().isOption()) {
                        cmd.unexpectedArgument();
                    } else {
                        idsToInstall.add(cmd.next().getString());
                    }
                }
            } else if ((a = cmd.next("un", "uninstall")) != null) {
                while (cmd.hasNext()) {
                    if (context.configureFirst(cmd)) {
                    } else if (cmd.peek().isOption()) {
                        cmd.unexpectedArgument();
                    } else {
                        idsToUninstall.add(cmd.next().getString());
                    }
                }
            } else {
                cmd.unexpectedArgument();
            }
        }

        if (idsToInstall.isEmpty() && idsToUninstall.isEmpty()) {
            cmd.required();
        }
        if (cmd.isExecMode()) {
            if (forceAll) {
                context.getSession().yes();
            }
            SystemNdi ndi = createNdi(context);
            NutsWorkspaceConfigManager config = context.getWorkspace().config();
            if (ndi == null) {
                throw new NutsExecutionException(context.getWorkspace(), "Platform not supported : " + config.getOs(), 2);
            }
            boolean subTrace = context.getSession().isTrace();
            if (!context.getSession().isPlainTrace()) {
                subTrace = false;
            }
            if (!idsToInstall.isEmpty()) {
                for (String id : idsToInstall) {
                    try {
                        result.addAll(
                                Arrays.asList(
                                        ndi.createNutsScript(
                                                new NdiScriptOptions().setId(id)
                                                        .setSession(context.session().copy().trace(subTrace))
                                                        .setForceBoot(forceAll)
                                                        .setFetch(fetch)
                                                        .setExecType(execType)
                                                        .setExecutorOptions(executorOptions)
                                        )
                                ));
                    } catch (IOException e) {
                        throw new NutsExecutionException(context.getWorkspace(), "Unable to run script " + id + " : " + e.toString(), e);
                    }
                }
                try {
                    ndi.configurePath(context.session());
                } catch (IOException e) {
                    throw new NutsExecutionException(context.getWorkspace(), "Unable to configure path : " + e.toString(), e);
                }
                if (context.getSession().isTrace()) {
                    if (context.getSession().isPlainTrace()) {
                        int namesSize = result.stream().mapToInt(x -> x.getName().length()).max().orElse(1);
                        for (NdiScriptnfo ndiScriptnfo : result) {
                            context.session().out().printf("installing%s script ==%-" + namesSize + "s== for " +
                                            context.getWorkspace().id().set(ndiScriptnfo.getId().getLongNameId()).format()
                                            + " at ==%s==%n", ndiScriptnfo.isOverride() ? " (with override)" : "",
                                    ndiScriptnfo.getName(), NdiUtils.betterPath(ndiScriptnfo.getPath().toString()));
                        }

                    } else {
                        context.workspace().object().session(context.session()).value(result).println();
                    }
                }
            }
            if (!idsToUninstall.isEmpty()) {
                for (String id : idsToUninstall) {
                    try {
                        ndi.removeNutsScript(
                                id,
                                context.session().copy().trace(subTrace)
                        );
                    } catch (IOException e) {
                        throw new NutsExecutionException(context.getWorkspace(), "Unable to run script " + id + " : " + e.toString(), e);
                    }
                }
            }
        }
    }

    protected void onInstallApplicationOrUpdate(NutsApplicationContext context, boolean update) {
        NutsCommandLine cmd = context.commandLine();
        if (update) {
            cmd.setCommandName("ndi --nuts-exec-mode=update");
        } else {
            cmd.setCommandName("ndi --nuts-exec-mode=install");
        }
        NutsArgument a;
        while (cmd.hasNext()) {
            if (context.configureFirst(cmd)) {
                //
            } else if ((a = cmd.nextBoolean("--skip-init")) != null) {
                if (a.getBooleanValue()) {
                    return;
                }
            } else {
                cmd.unexpectedArgument();
            }
        }
        SystemNdi ndi = createNdi(context);
        if (ndi != null) {
            List<String> args = new ArrayList<>();
            args.addAll(Arrays.asList("in", "-b"));
            args.addAll(Arrays.asList(COMPANIONS));
            context.getSession().yes();
            run(args.toArray(new String[0]));

        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext context) {
        onInstallApplicationOrUpdate(context, false);
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallApplicationOrUpdate(applicationContext, true);
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
    }
}
