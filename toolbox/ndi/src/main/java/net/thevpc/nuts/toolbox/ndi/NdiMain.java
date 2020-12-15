package net.thevpc.nuts.toolbox.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndi.sys.LinuxNdi;
import net.thevpc.nuts.toolbox.ndi.sys.MacosNdi;
import net.thevpc.nuts.toolbox.ndi.sys.UnixNdi;
import net.thevpc.nuts.toolbox.ndi.sys.WindowsNdi;
import net.thevpc.nuts.toolbox.ndi.util.NdiUtils;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class NdiMain extends NutsApplication {

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

    public void runSwitch(NutsCommandLine cmdLine, NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        String switchWorkspaceLocation = null;
        String switchWorkspaceApi = null;
        List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
        ArrayList<String> executorOptions = new ArrayList<>();
        NutsExecutionType execType = null;
        NutsArgument a;
        Runnable action = null;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //consumed
            } else if (cmdLine.peek().getStringKey().equals("-w") || cmdLine.peek().getStringKey().equals("--workspace")) {
                switchWorkspaceLocation = cmdLine.nextString().getStringValue();
            } else if (cmdLine.peek().getStringKey().equals("-a") || cmdLine.peek().getStringKey().equals("--api")) {
                switchWorkspaceApi = cmdLine.nextString().getStringValue();
            } else if (cmdLine.peek().isOption()) {
                cmdLine.unexpectedArgument();
            } else if (switchWorkspaceLocation == null) {
                switchWorkspaceLocation = cmdLine.next().getString();
            } else if (switchWorkspaceApi == null) {
                switchWorkspaceApi = cmdLine.next().getString();
            } else {
                cmdLine.unexpectedArgument();
            }
        }
        if (cmdLine.isExecMode()) {
            SystemNdi ndi = createNdi(context);
            if (ndi == null) {
                throw new NutsExecutionException(ws, "Platform not supported : " + ws.env().getOs(), 2);
            }
            boolean subTrace = context.getSession().isTrace();
            if (!context.getSession().isPlainTrace()) {
                subTrace = false;
            }
            if (switchWorkspaceLocation != null || switchWorkspaceApi != null) {
                ndi.switchWorkspace(switchWorkspaceLocation, switchWorkspaceApi);
            }
        }

    }

    public void runLink(NutsCommandLine cmdLine, NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        String switchWorkspaceLocation = null;
        String switchWorkspaceApi = null;
        String linkName = null;
        boolean fetch = false;
        ArrayList<String> executorOptions = new ArrayList<>();
        NutsExecutionType execType = null;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //consumed
            } else if (cmdLine.peek().getStringKey().equals("-w") || cmdLine.peek().getStringKey().equals("--workspace")) {
                switchWorkspaceLocation = cmdLine.nextString().getStringValue();
            } else if (cmdLine.peek().getStringKey().equals("-a") || cmdLine.peek().getStringKey().equals("--api")) {
                switchWorkspaceApi = cmdLine.nextString().getStringValue();
            } else if (cmdLine.peek().getStringKey().equals("-n") || cmdLine.peek().getStringKey().equals("--name")) {
                linkName = cmdLine.nextString().getStringValue();
            } else if (cmdLine.peek().isOption()) {
                cmdLine.unexpectedArgument();
            } else if (linkName == null) {
                linkName = cmdLine.next().getString();
            } else if (switchWorkspaceLocation == null) {
                switchWorkspaceLocation = cmdLine.next().getString();
            } else if (switchWorkspaceApi == null) {
                switchWorkspaceApi = cmdLine.next().getString();
            } else {
                cmdLine.unexpectedArgument();
            }
        }
        if (linkName == null) {
            linkName="nuts-%v";
        }else if(Files.isDirectory(Paths.get(linkName))){
            linkName=Paths.get(linkName).resolve("nuts-%v").toString();
        }else if(linkName.endsWith("/") || linkName.endsWith("\\")){
            linkName=Paths.get(linkName).resolve("nuts-%v").toString();
        }
        if (cmdLine.isExecMode()) {
            SystemNdi ndi = createNdi(context);
            NutsWorkspaceConfigManager config = ws.config();
            if (ndi == null) {
                throw new NutsExecutionException(ws, "Platform not supported : " + ws.env().getOs(), 2);
            }
            boolean subTrace = context.getSession().isTrace();
            if (!context.getSession().isPlainTrace()) {
                subTrace = false;
            }
            if (linkName != null) {
                NdiScriptnfo[] r = ndi.createNutsScript(
                        new NdiScriptOptions().setId(
                                switchWorkspaceApi == null ? ws.getApiId().toString() :
                                        ws.getApiId().builder().setVersion(switchWorkspaceApi).build().toString()
                        )
                                .setPreferredScriptName(linkName)
                                .setSession(context.getSession().copy().setTrace(subTrace))
                                .setForceBoot(context.getSession().isYes())
                                .setFetch(fetch)
                                .setExecType(execType)
                                .setExecutorOptions(executorOptions)
                                .setIncludeEnv(true)
                );
                printResults(context, Arrays.asList(r), ws);
            }
        }
    }

    public void runInstall(NutsCommandLine cmdLine, NutsApplicationContext context) {
        ArrayList<String> idsToInstall = new ArrayList<>();
        NutsWorkspace ws = context.getWorkspace();
        List<NdiScriptnfo> result = new ArrayList<NdiScriptnfo>();
        ArrayList<String> executorOptions = new ArrayList<>();
        NutsExecutionType execType = null;
        boolean fetch = false;
        boolean missingAnyArgument = true;
        NutsArgument a;
//        boolean forceAll = false;
        Boolean persistentConfig = null;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {

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
                context.getSession().setConfirm(NutsConfirmationMode.YES);
                for (NutsId resultId : ws.search().addInstallStatus(NutsInstallStatus.INSTALLED).getResultIds()) {
                    idsToInstall.add(resultId.getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = cmdLine.nextString("-c", "--companions")) != null) {
                context.getSession().setConfirm(NutsConfirmationMode.YES);
                for (NutsId companion : ws.companionIds()) {
                    idsToInstall.add(ws.search().addId(companion).setLatest(true).getResultIds().required().getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = cmdLine.nextString("--switch")) != null) {
                Boolean booleanValue = a.getBooleanValue(null);
                if (booleanValue != null) {
                    persistentConfig = booleanValue;
                }
            } else if (cmdLine.peek().isOption()) {
                cmdLine.unexpectedArgument();
            } else {
                idsToInstall.add(cmdLine.next().getString());
                missingAnyArgument = false;
            }
        }

        if (missingAnyArgument) {
            cmdLine.required();
        }
        Path workspaceLocation = ws.locations().getWorkspaceLocation();
        if (cmdLine.isExecMode()) {
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
                if (workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/default-workspace"))) {
                    persistentConfig = true;
                } else {
                    persistentConfig = false;
                }
                for (String id : idsToInstall) {
                    try {
                        result.addAll(
                                Arrays.asList(
                                        ndi.createNutsScript(
                                                new NdiScriptOptions().setId(id)
                                                        .setSession(context.getSession().copy().setTrace(subTrace))
                                                        .setForceBoot(context.getSession().isYes())
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
                printResults(context, result, ws);
            }
        }
    }

    public void runUninstall(NutsCommandLine cmdLine, NutsApplicationContext context) {
        ArrayList<String> idsToUninstall = new ArrayList<>();
        boolean forceAll = false;
        NutsWorkspace ws = context.getWorkspace();
        boolean missingAnyArgument = true;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                //consumed
            } else if (cmdLine.peek().isOption()) {
                cmdLine.unexpectedArgument();
            } else {
                idsToUninstall.add(cmdLine.next().getString());
                missingAnyArgument = false;
            }
        }
        if (missingAnyArgument) {
            cmdLine.required();
        }
        Path workspaceLocation = ws.locations().getWorkspaceLocation();
        if (cmdLine.isExecMode()) {
            if (forceAll) {
                context.getSession().setConfirm(NutsConfirmationMode.YES);
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
        }
    }

    private void printResults(NutsApplicationContext context, List<NdiScriptnfo> result, NutsWorkspace ws) {
        if (context.getSession().isTrace()) {
            if (context.getSession().isPlainTrace()) {
                int namesSize = result.stream().mapToInt(x -> x.getName().length()).max().orElse(1);
                for (NdiScriptnfo ndiScriptnfo : result) {
                    context.getSession().out().printf("%s script ##%-" + namesSize + "s## for " +
                                    ws.id().formatter(ndiScriptnfo.getId().getLongNameId()).format()
                                    + " at ####%s####%n", ndiScriptnfo.isOverride() ?
                                    ws.formats().text().builder().appendStyled( "re-installing",NutsTextNodeStyle.SUCCESS2) :
                                    ws.formats().text().builder().appendStyled("installing",NutsTextNodeStyle.SUCCESS1),
                            ndiScriptnfo.getName(), NdiUtils.betterPath(ndiScriptnfo.getPath().toString()));
                }

            } else {
                context.getSession().formatObject(result).println();
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
            LinkedHashSet<String> companions = new LinkedHashSet<>();
            companions.add("net.thevpc.nuts:nuts");
            companions.add("net.thevpc.nuts.toolbox:ndi");
            companions.addAll(context.getWorkspace().companionIds().stream().map(NutsId::getShortName).collect(Collectors.toList()));
            args.addAll(companions);
            context.getSession().setConfirm(NutsConfirmationMode.YES);
            run(context.getSession(), args.toArray(new String[0]));
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

    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine cmdLine = context.getCommandLine()
                .setCommandName("ndi")
                .required();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
                // ignore
            } else if ((a = cmdLine.next("in", "install")) != null) {
                runInstall(cmdLine, context);
                return;
            } else if ((a = cmdLine.next("un", "uninstall")) != null) {
                runUninstall(cmdLine, context);
                return;
            } else if ((a = cmdLine.next("sw", "switch")) != null) {
                runSwitch(cmdLine, context);
                return;
            } else if ((a = cmdLine.next("ln", "link")) != null) {
                runLink(cmdLine, context);
                return;
            } else {
                cmdLine.unexpectedArgument();
            }
        }
        cmdLine.required();
    }
}
