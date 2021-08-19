package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.CreateNutsScriptCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.DefaultNutsEnvInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.sys.unix.LinuxNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.sys.unix.MacosNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.sys.unix.UnixNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.sys.win.WindowsNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.util.NdiUtils;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class NutsSettingsNdiSubCommand extends AbstractNutsSettingsSubCommand {

    public SystemNdi createNdi(NutsSession session) {
        SystemNdi ndi = null;
        switch (session.getWorkspace().env().getOsFamily()) {
            case LINUX: {
                ndi = new LinuxNdi(session);
                break;
            }
            case UNIX: {
                ndi = new UnixNdi(session);
                break;
            }
            case MACOS: {
                ndi = new MacosNdi(session);
                break;
            }
            case WINDOWS: {
                if (OptionalMsLinkHelper.isSupported()) {
                    ndi = new WindowsNdi(session);
                }
                break;
            }
        }
        return ndi;
    }

    public void runAddLauncher(NutsCommandLine commandLine, NutsSession session) {
        CreateNutsScriptCommand cmd = new CreateNutsScriptCommand();
        cmd.getOptions().setSession(session.copy());
//        ArrayList<String> idsToInstall ;
        NutsWorkspace ws = session.getWorkspace();
//        ArrayList<String> executorOptions = new ArrayList<>();
//        NutsExecutionType execType = null;
//        boolean fetch = false;
        boolean missingAnyArgument = true;
        NutsArgument a;
//        boolean forceAll = false;
//        Boolean persistentConfig = null;
        boolean ignoreUnsupportedOs = false;

//        String linkName = null;
//        boolean env = false;
        commandLine.setCommandName("settings add launcher");
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextBoolean("-t", "--fetch")) != null) {
                cmd.getOptions().setFetch(a.getBooleanValue());
            } else if ((a = commandLine.nextString("-d", "--workdir")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setWorkingDirectory(a.getStringValue());
                }
            } else if ((a = commandLine.nextString("--icon")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setIcon(a.getStringValue());
                }
            } else if ((a = commandLine.next("--menu")) != null) {
                if(a.isEnabled()) {
                    cmd.getOptions().setCreateMenu(parseCond(a));
                }
            } else if ((a = commandLine.nextString("--menu-category")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setMenuCategory(a.getStringValue());
                    if (cmd.getOptions().getMenuCategory() != null && !cmd.getOptions().getMenuCategory().isEmpty()) {
                        if(cmd.getOptions().getCreateMenu()== NutsActionSupportCondition.NEVER) {
                            cmd.getOptions().setCreateMenu(NutsActionSupportCondition.PREFERRED);
                        }
                    }
                }
            } else if ((a = commandLine.nextBoolean("--desktop")) != null) {
                if(a.isEnabled()) {
                    cmd.getOptions().setCreateDesktop(parseCond(a));
                }
            } else if ((a = commandLine.nextString("--desktop-name")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setShortcutName(a.getStringValue());
                    if(cmd.getOptions().getCreateDesktop()== NutsActionSupportCondition.NEVER) {
                        cmd.getOptions().setCreateDesktop(NutsActionSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--menu-name")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setShortcutName(a.getStringValue());
                    if(cmd.getOptions().getCreateDesktop()== NutsActionSupportCondition.NEVER) {
                        cmd.getOptions().setCreateMenu(NutsActionSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--shortcut-name")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setShortcutName(a.getStringValue());
                    if(cmd.getOptions().getCreateShortcut()== NutsActionSupportCondition.NEVER) {
                        cmd.getOptions().getCreateShortcut(NutsActionSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--shortcut-path")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setShortcutPath(a.getStringValue());
                    if(cmd.getOptions().getCreateShortcut()== NutsActionSupportCondition.NEVER) {
                        cmd.getOptions().getCreateShortcut(NutsActionSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextBoolean("-x", "--external", "--spawn")) != null) {
                if (a.getBooleanValue()) {
                    cmd.getOptions().setExecType(NutsExecutionType.SPAWN);
                }
            } else if ((a = commandLine.nextBoolean("-b", "--embedded")) != null) {
                if (a.getBooleanValue()) {
                    cmd.getOptions().setExecType(NutsExecutionType.EMBEDDED);
                }
            } else if ((a = commandLine.nextBoolean("--terminal")) != null) {
                if (a.isEnabled()) {
                    cmd.getOptions().setTerminalMode(a.getBooleanValue());
                }
            } else if ((a = commandLine.nextBoolean("-e", "--env")) != null) {
                cmd.getOptions().setIncludeEnv(a.getBooleanValue());
            } else if ((a = commandLine.nextBoolean("--user-cmd")) != null) {
                if (a.getBooleanValue()) {
                    cmd.getOptions().setExecType(NutsExecutionType.USER_CMD);
                }
            } else if ((a = commandLine.nextBoolean("--root-cmd")) != null) {
                if (a.getBooleanValue()) {
                    cmd.getOptions().setExecType(NutsExecutionType.ROOT_CMD);
                }
            } else if ((a = commandLine.nextString("-X", "--exec-options")) != null) {
                cmd.getOptions().getExecutorOptions().add(a.getStringValue());
            } else if ((a = commandLine.nextString("-i", "--installed")) != null) {
                session.setConfirm(NutsConfirmationMode.YES);
                for (NutsId resultId : ws.search().setInstallStatus(
                        ws.filters().installStatus().byInstalled(true)
                ).getResultIds()) {
                    cmd.getIdsToInstall().add(resultId.getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = commandLine.nextString("-c", "--companions")) != null) {
                session.setConfirm(NutsConfirmationMode.YES);
                for (NutsId companion : ws.getCompanionIds(session)) {
                    cmd.getIdsToInstall().add(ws.search().addId(companion).setLatest(true).getResultIds().required().getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = commandLine.nextString("--switch")) != null) {
                Boolean booleanValue = a.getBooleanValue(null);
                if (booleanValue != null) {
                    cmd.getOptions().setPersistentConfig(booleanValue);
                }
            } else if ((a = commandLine.nextString("--ignore-unsupported-os")) != null) {
                if (a.isEnabled()) {
                    ignoreUnsupportedOs = a.getBooleanValue();
                }
            } else if (commandLine.peek().getStringKey().equals("-w") || commandLine.peek().getStringKey().equals("--workspace")) {
                a = commandLine.nextString();
                if (a.isEnabled()) {
                    cmd.getOptions().setSwitchWorkspaceLocation(a.getStringValue());
                }
            } else if (commandLine.peek().getStringKey().equals("-n") || commandLine.peek().getStringKey().equals("--name")) {
                a = commandLine.nextString();
                if (a.isEnabled()) {
                    cmd.getOptions().setScriptPath(a.getStringValue());
                }
            } else if (commandLine.peek().isOption()) {
                session.configureLast(commandLine);
            } else {
                cmd.getIdsToInstall().add(commandLine.next().getString());
                missingAnyArgument = false;
            }
        }

        if (missingAnyArgument) {
            commandLine.required();
        }
        if (commandLine.isExecMode()) {
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.cstyle("platform not supported : %s", ws.env().getOs()), 2);
            }
            if (!cmd.getIdsToInstall().isEmpty()) {
                printResults(session, ndi.addScript(cmd, session));
            }
        }
    }

    private NutsActionSupportCondition parseCond(NutsArgument a) {
        String s = a.getStringValue("");
        switch (s){
            case "supported":{
                return NutsActionSupportCondition.SUPPORTED;
            }
            case "never":{
                return NutsActionSupportCondition.NEVER;
            }
            case "always":{
                return NutsActionSupportCondition.ALWAYS;
            }
            case "preferred":
            case "": {
                return NutsActionSupportCondition.PREFERRED;
            }
            default:{
                if(a.getBooleanValue()){
                    return NutsActionSupportCondition.PREFERRED;
                }else{
                    return NutsActionSupportCondition.NEVER;
                }
            }
        }
    }

    public void runRemoveLauncher(NutsCommandLine commandLine, NutsSession session) {
        ArrayList<String> idsToUninstall = new ArrayList<>();
        boolean forceAll = false;
        NutsWorkspace ws = session.getWorkspace();
        boolean missingAnyArgument = true;
        NutsArgument a;
        boolean ignoreUnsupportedOs = false;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--ignore-unsupported-os")) != null) {
                if (a.isEnabled()) {
                    ignoreUnsupportedOs = a.getBooleanValue();
                }
            } else if (commandLine.peek().isOption()) {
                session.configureLast(commandLine);
            } else {
                idsToUninstall.add(commandLine.next().getString());
                missingAnyArgument = false;
            }
        }
        if (missingAnyArgument) {
            commandLine.required();
        }
        Path workspaceLocation = Paths.get(ws.locations().getWorkspaceLocation());
        if (commandLine.isExecMode()) {
            if (forceAll) {
                session.setConfirm(NutsConfirmationMode.YES);
            }
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.cstyle("platform not supported : %s", ws.env().getOs()), 2);
            }
            boolean subTrace = session.isTrace();
            if (!session.isPlainTrace()) {
                subTrace = false;
            }
            if (!idsToUninstall.isEmpty()) {
                DefaultNutsEnvInfo env = new DefaultNutsEnvInfo(
                        null, null/*switchWorkspaceLocation*/, session
                );
                for (String id : idsToUninstall) {
                    try {
                        ndi.removeNutsScript(
                                id,
                                session.copy().setTrace(subTrace),
                                env
                        );
                    } catch (UncheckedIOException e) {
                        throw new NutsExecutionException(session, NutsMessage.cstyle("unable to run script %s : %s", id, e), e);
                    }
                }
            }
        }
    }

    public void runSwitch(NutsCommandLine commandLine, NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        String switchWorkspaceLocation = null;
        String switchWorkspaceApi = null;
        NutsArgument a;
        boolean ignoreUnsupportedOs = false;
        NutsActionSupportCondition createDesktop = NutsActionSupportCondition.NEVER;
        NutsActionSupportCondition createMenu = NutsActionSupportCondition.NEVER;
        String menuCategory = null;
        String shortcutName = null;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--ignore-unsupported-os")) != null) {
                if (a.isEnabled()) {
                    ignoreUnsupportedOs = a.getBooleanValue();
                }
            } else if (commandLine.peek().getStringKey().equals("-w") || commandLine.peek().getStringKey().equals("--workspace")) {
                switchWorkspaceLocation = commandLine.nextString().getStringValue();
            } else if (commandLine.peek().getStringKey().equals("-a") || commandLine.peek().getStringKey().equals("--api")) {
                switchWorkspaceApi = commandLine.nextString().getStringValue();
            } else if ((a = commandLine.next("--menu")) != null) {
                if(a.isEnabled()) {
                    createMenu=parseCond(a);
                }
            } else if ((a = commandLine.nextString("--menu-category")) != null) {
                if (a.isEnabled()) {
                    menuCategory = a.getStringValue();
                    if (menuCategory != null && !menuCategory.isEmpty()) {
                        if(createMenu== NutsActionSupportCondition.NEVER) {
                            createMenu = NutsActionSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.nextString("--menu-name")) != null) {
                if (a.isEnabled()) {
                    shortcutName = a.getStringValue();
                    if (shortcutName != null && !shortcutName.isEmpty()) {
                        if(createMenu== NutsActionSupportCondition.NEVER) {
                            createMenu = NutsActionSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.nextString("--desktop-name")) != null) {
                if (a.isEnabled()) {
                    shortcutName = a.getStringValue();
                    if (shortcutName != null && !shortcutName.isEmpty()) {
                        if(createDesktop== NutsActionSupportCondition.NEVER) {
                            createDesktop = NutsActionSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.next("--desktop")) != null) {
                if (a.isEnabled()) {
                    createDesktop = parseCond(a);
                }
            } else if (commandLine.peek().isOption()) {
                commandLine.unexpectedArgument();
            } else if (switchWorkspaceLocation == null) {
                switchWorkspaceLocation = commandLine.next().getString();
            } else if (switchWorkspaceApi == null) {
                switchWorkspaceApi = commandLine.next().getString();
            } else if (commandLine.peek().isOption()) {
                session.configureLast(commandLine);
            } else {
                commandLine.unexpectedArgument();
            }
        }
        if (commandLine.isExecMode()) {
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.cstyle("platform not supported : %s ", ws.env().getOs()), 2);
            }
            if (switchWorkspaceLocation != null || switchWorkspaceApi != null) {
                ndi.switchWorkspace(
                        new NdiScriptOptions()
                                .setEnv(new DefaultNutsEnvInfo(null, switchWorkspaceLocation, session))
                                .setCreateDesktop(createDesktop)
                                .setMenuCategory(menuCategory)
                                .setCreateMenu(createMenu)
                                .setShortcutName(shortcutName)
                );
            }
        }

    }

    private void printResults(NutsSession session, PathInfo[] result) {
        NutsWorkspace ws = session.getWorkspace();
        if (session.isTrace()) {
            result = Arrays.stream(result).filter(x -> x.getStatus() != PathInfo.Status.DISCARDED).toArray(PathInfo[]::new);
            if (session.isPlainTrace()) {
                int namesSize = Arrays.stream(result).mapToInt(x -> x.getPath().getFileName().toString().length()).max().orElse(1);
                for (PathInfo ndiScriptInfo : result) {
                    session.out().resetLine().printf("%s script %-" + namesSize + "s for %s"
                                    + " at %s%n",
                            (ndiScriptInfo.getStatus() == PathInfo.Status.OVERRIDDEN)
                                    ? ws.text().forStyled("re-install", NutsTextStyles.of(NutsTextStyle.success(), NutsTextStyle.underlined()))
                                    : ws.text().forStyled("install", NutsTextStyle.success())
                            ,
                            ws.text().forStyled(ndiScriptInfo.getPath().getFileName().toString(), NutsTextStyle.path()),
                            ndiScriptInfo.getId(),
                            ws.text().forStyled(NdiUtils.betterPath(ndiScriptInfo.getPath().toString()), NutsTextStyle.path())
                    );
                }

            } else {
                session.getWorkspace().formats().object(result).println();
            }
        }
    }

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("add launcher", "lna") != null) {
            runAddLauncher(cmdLine, session);
            return true;
        } else if (cmdLine.next("remove launcher", "lnrm") != null) {
            runRemoveLauncher(cmdLine, session);
            return true;
        } else if (cmdLine.next("switch", "lnsw") != null) {
            runSwitch(cmdLine, session);
            return true;
        }
        return false;
    }
}
