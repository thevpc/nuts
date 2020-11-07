package net.thevpc.nuts.toolbox.ndi;

import net.thevpc.nuts.toolbox.ndi.sys.LinuxNdi;
import net.thevpc.nuts.toolbox.ndi.sys.MacosNdi;
import net.thevpc.nuts.toolbox.ndi.sys.UnixNdi;
import net.thevpc.nuts.toolbox.ndi.sys.WindowsNdi;
import net.thevpc.nuts.toolbox.ndi.util.NdiUtils;
import net.thevpc.nuts.*;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NdiMain extends NutsApplication {

    public static final String[] COMPANIONS = {
            "net.thevpc.nuts:nuts",
            "net.thevpc.nuts.toolbox:ndi",
            "net.thevpc.nuts.toolbox:nsh",
            "net.thevpc.nuts.toolbox:nadmin"
    };

    public static void main(String[] args) {
        new NdiMain().runAndExit(args);
    }

    public SystemNdi createNdi(NutsApplicationContext appContext) {
        SystemNdi ndi = null;
        switch (appContext.getWorkspace().env().getOsFamily()) {
            case LINUX: {
                ndi = new LinuxNdi(appContext);
                break;
            }
            case UNIX: {
                ndi = new UnixNdi(appContext);
                break;
            }
            case MACOS: {
                ndi = new MacosNdi(appContext);
                break;
            }
            case WINDOWS: {
                ndi = new WindowsNdi(appContext);
                break;
            }
        }
        return ndi;
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine cmdLine = context.getCommandLine()
                .setCommandName("ndi")
                .required();
        List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
        ArrayList<String> idsToInstall = new ArrayList<>();
        ArrayList<String> idsToUninstall = new ArrayList<>();
        String switchWorkspaceLocation=null;
        String switchWorkspaceApi=null;
        boolean forceAll = false;
        boolean fetch = false;
        ArrayList<String> executorOptions = new ArrayList<>();
        NutsExecutionType execType = null;
        NutsArgument a;
        NutsWorkspace ws = context.getWorkspace();
        Boolean persistentConfig=null;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                // ignore
            } else if ((a = cmdLine.next("in", "install")) != null) {
                while (cmdLine.hasNext()) {
                    if (context.configureFirst(cmdLine)) {

                    } else if ((a = cmdLine.nextBoolean("-F", "--force-all")) != null) {
                        forceAll = a.getBooleanValue();
                    } else if ((a = cmdLine.nextBoolean("-t", "--fetch")) != null) {
                        fetch = a.getBooleanValue();
                    } else if ((a = cmdLine.nextBoolean("-x", "--external", "--spawn")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.SPAWN;
                        }
                    } else if ((a = cmdLine.nextBoolean("-b", "--embedded")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.EMBEDDED;
                        }
                    } else if ((a = cmdLine.nextBoolean("--user-cmd")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.USER_CMD;
                        }
                    } else if ((a = cmdLine.nextBoolean("--root-cmd")) != null) {
                        if (a.getBooleanValue()) {
                            execType = NutsExecutionType.ROOT_CMD;
                        }
                    } else if ((a = cmdLine.nextString("-X", "--exec-options")) != null) {
                        executorOptions.add(a.getStringValue());
                    } else if ((a = cmdLine.nextString("-i", "--installed")) != null) {
                        forceAll = true;
                        for (NutsId resultId : ws.search().addInstallStatus(NutsInstallStatus.INSTALLED).getResultIds()) {
                            idsToInstall.add(resultId.getLongName());
                        }
                    } else if ((a = cmdLine.nextString("-c", "--companions")) != null) {
                        forceAll = true;
                        for (String companion : ws.companionIds()) {
                            idsToInstall.add(ws.search().addId(companion).setLatest(true).getResultIds().required().getLongName());
                        }
                    } else if ((a = cmdLine.nextString("--switch")) != null) {
                        Boolean booleanValue = a.getBooleanValue(null);
                        if (booleanValue!=null) {
                            persistentConfig=booleanValue;
                        }
                    } else if (cmdLine.peek().isOption()) {
                        cmdLine.unexpectedArgument();
                    } else {
                        idsToInstall.add(cmdLine.next().getString());
                    }
                }
            } else if ((a = cmdLine.next("un", "uninstall")) != null) {
                while (cmdLine.hasNext()) {
                    if (context.configureFirst(cmdLine)) {
                        //consumed
                    } else if (cmdLine.peek().isOption()) {
                        cmdLine.unexpectedArgument();
                    } else {
                        idsToUninstall.add(cmdLine.next().getString());
                    }
                }
            } else if ((a = cmdLine.next("sw", "switch")) != null) {
                while (cmdLine.hasNext()) {
                    if (context.configureFirst(cmdLine)) {
                        //consumed
                    } else if (cmdLine.peek().getStringKey().equals("-w")||cmdLine.peek().getStringKey().equals("--workspace")) {
                        switchWorkspaceLocation=cmdLine.nextString().getStringValue();
                    } else if (cmdLine.peek().getStringKey().equals("-a")||cmdLine.peek().getStringKey().equals("--api")) {
                        switchWorkspaceApi=cmdLine.nextString().getStringValue();
                    } else if (cmdLine.peek().isOption()) {
                        cmdLine.unexpectedArgument();
                    } else if(switchWorkspaceLocation==null){
                        switchWorkspaceLocation=cmdLine.next().getString();
                    } else if(switchWorkspaceApi==null){
                        switchWorkspaceApi=cmdLine.next().getString();
                    }else{
                        cmdLine.unexpectedArgument();
                    }
                }
            } else {
                cmdLine.unexpectedArgument();
            }
        }

        if (idsToInstall.isEmpty() && idsToUninstall.isEmpty()) {
            cmdLine.required();
        }
        Path workspaceLocation = context.getWorkspace().locations().getWorkspaceLocation();
        if(workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/default-workspace"))){
            persistentConfig=true;
        }else{
            persistentConfig=false;
        }
        if (cmdLine.isExecMode()) {
            if (forceAll) {
                context.getSession().yes();
            }
            SystemNdi ndi = createNdi(context);
            NutsWorkspaceConfigManager config = ws.config();
            if (ndi == null) {
                throw new NutsExecutionException(ws, "Platform not supported : " + ws.env().getOs(), 2);
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
                                                        .setSession(context.getSession().copy().setTrace(subTrace))
                                                        .setForceBoot(forceAll)
                                                        .setFetch(fetch)
                                                        .setExecType(execType)
                                                        .setExecutorOptions(executorOptions)
                                        )
                                ));
                    } catch (UncheckedIOException e) {
                        throw new NutsExecutionException(ws, "Unable to run script " + id + " : " + e.toString(), e);
                    }
                }
                    ndi.configurePath(context.getSession(), persistentConfig);
                if (context.getSession().isTrace()) {
                    if (context.getSession().isPlainTrace()) {
                        int namesSize = result.stream().mapToInt(x -> x.getName().length()).max().orElse(1);
                        for (NdiScriptnfo ndiScriptnfo : result) {
                            context.getSession().out().printf("%s script ==%-" + namesSize + "s== for " +
                                            ws.id().formatter(ndiScriptnfo.getId().getLongNameId()).format()
                                            + " at ==%s==%n", ndiScriptnfo.isOverride() ?
                                            ws.str().append("**","re-installing") :
                                            ws.str().append("##","installing"),
                                    ndiScriptnfo.getName(), NdiUtils.betterPath(ndiScriptnfo.getPath().toString()));
                        }

                    } else {
                        context.getSession().formatObject(result).println();
                    }
                }
            }
            if (!idsToUninstall.isEmpty()) {
                for (String id : idsToUninstall) {
                    try {
                        ndi.removeNutsScript(
                                id,
                                context.getSession().copy().setTrace(subTrace)
                        );
                    } catch (UncheckedIOException e) {
                        throw new NutsExecutionException(ws, "Unable to run script " + id + " : " + e.toString(), e);
                    }
                }
            }
            if(switchWorkspaceLocation!=null || switchWorkspaceApi!=null){
                ndi.switchWorkspace(switchWorkspaceLocation, switchWorkspaceApi);
            }
        }
    }

    protected void onInstallOrUpdateApplication(NutsApplicationContext context, boolean update) {
        NutsCommandLine cmd = context.getCommandLine();
        if (update) {
            cmd.setCommandName("ndi --nuts-exec-mode=update");
        } else {
            cmd.setCommandName("ndi --nuts-exec-mode=install");
        }
        NutsArgument a;
        while (cmd.hasNext()) {
            if (context.configureFirst(cmd)) {
                // consumed
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
            args.addAll(Arrays.asList("install", "--embedded"));
            LinkedHashSet<String> companions=new LinkedHashSet<>();
            companions.add("net.thevpc.nuts:nuts");
            companions.add("net.thevpc.nuts.toolbox:ndi");
            companions.addAll(context.getWorkspace().companionIds());
            args.addAll(companions);
            context.getSession().yes();
            run(context.getSession(),args.toArray(new String[0]));
        }
    }

    @Override
    protected void onInstallApplication(NutsApplicationContext context) {
        onInstallOrUpdateApplication(context, false);
    }

    @Override
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsVersion currentVersion = applicationContext.getAppVersion();
        NutsVersion previousVersion = applicationContext.getAppPreviousVersion();
        onInstallOrUpdateApplication(applicationContext, true);
    }

    @Override
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
    }
}
