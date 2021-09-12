package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.unix.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.win.WindowsNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.util.NdiUtils;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutsSettingsNdiSubCommand extends AbstractNutsSettingsSubCommand {

    public static SystemNdi createNdi(NutsSession session) {
        SystemNdi ndi = null;
        NutsWorkspaceEnvManager workspaceEnvManager = session.getWorkspace().env();
        switch (workspaceEnvManager.getOsFamily()) {
            case LINUX:
            case UNIX: {
                switch (workspaceEnvManager.getShellFamily()) {
                    case BASH: {
                        ndi = new BashNdi(session);
                        break;
                    }
                    case CSH: {
                        ndi = new CshNdi(session);
                        break;
                    }
                    case KSH: {
                        ndi = new KshNdi(session);
                        break;
                    }
                    case ZSH: {
                        ndi = new ZshNdi(session);
                        break;
                    }
                    case FISH: {
                        ndi = new FishNdi(session);
                        break;
                    }
                }
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
        NdiScriptOptions options = new NdiScriptOptions();
        options.setSession(session.copy());
        List<String> idsToInstall = new ArrayList<>();

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
                options.setFetch(a.getBooleanValue());
            } else if ((a = commandLine.nextString("-d", "--workdir")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setWorkingDirectory(a.getStringValue());
                }
            } else if ((a = commandLine.nextString("--icon")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setIcon(a.getStringValue());
                }
            } else if ((a = commandLine.next("--menu")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setCreateMenuShortcut(parseCond(a));
                }
            } else if ((a = commandLine.nextString("--menu-category")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setMenuCategory(a.getStringValue());
                    if (options.getLauncher().getMenuCategory() != null && !options.getLauncher().getMenuCategory().isEmpty()) {
                        if (options.getLauncher().getCreateMenuShortcut() == NutsSupportCondition.NEVER) {
                            options.getLauncher().setCreateMenuShortcut(NutsSupportCondition.PREFERRED);
                        }
                    }
                }
            } else if ((a = commandLine.nextBoolean("--desktop")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setCreateDesktopShortcut(parseCond(a));
                }
            } else if ((a = commandLine.nextString("--desktop-name")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setShortcutName(a.getStringValue());
                    if (options.getLauncher().getCreateDesktopShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateDesktopShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--menu-name")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setShortcutName(a.getStringValue());
                    if (options.getLauncher().getCreateDesktopShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateMenuShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--shortcut-name")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setShortcutName(a.getStringValue());
                    if (options.getLauncher().getCreateCustomShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateCustomShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--shortcut-path")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setCustomShortcutPath(a.getStringValue());
                    if (options.getLauncher().getCreateCustomShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateCustomShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextBoolean("-x", "--external", "--spawn")) != null) {
                if (a.getBooleanValue()) {
                    options.getLauncher().getNutsOptions().add("--spawn");
                }
            } else if ((a = commandLine.nextBoolean("-b", "--embedded")) != null) {
                if (a.getBooleanValue()) {
                    options.getLauncher().getNutsOptions().add("--embedded");
                }
            } else if ((a = commandLine.nextBoolean("--terminal")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().setOpenTerminal(a.getBooleanValue());
                }
            } else if ((a = commandLine.nextBoolean("-e", "--env")) != null) {
                options.setIncludeEnv(a.getBooleanValue());
            } else if ((a = commandLine.nextBoolean("--system")) != null) {
                if (a.getBooleanValue()) {
                    options.getLauncher().getNutsOptions().add("--system");
                }
            } else if ((a = commandLine.nextBoolean("--current-user")) != null) {
                if (a.getBooleanValue()) {
                    options.getLauncher().getNutsOptions().add("--current-user");
                }
            } else if ((a = commandLine.nextBoolean("--as-root")) != null) {
                if (a.isEnabled() && a.getBooleanValue()) {
                    options.getLauncher().getNutsOptions().add("--as-root");
                }
            } else if ((a = commandLine.nextBoolean("--sudo")) != null) {
                if (a.isEnabled() && a.getBooleanValue()) {
                    options.getLauncher().getNutsOptions().add("--sudo");
                }
            } else if ((a = commandLine.nextString("--run-as")) != null) {
                if (a.isEnabled()) {
                    options.getLauncher().getNutsOptions().add("--run-as=" + a.getStringValue());
                }
            } else if ((a = commandLine.nextString("-X", "--exec-options")) != null) {
                options.getLauncher().getNutsOptions().add("--exec-options=" + a.getStringValue());
            } else if ((a = commandLine.nextString("-i", "--installed")) != null) {
                session.setConfirm(NutsConfirmationMode.YES);
                for (NutsId resultId : ws.search().setInstallStatus(
                        ws.filters().installStatus().byInstalled(true)
                ).getResultIds()) {
                    idsToInstall.add(resultId.getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = commandLine.nextString("-c", "--companions")) != null) {
                session.setConfirm(NutsConfirmationMode.YES);
                for (NutsId companion : ws.getCompanionIds(session)) {
                    idsToInstall.add(ws.search().addId(companion).setLatest(true).getResultIds().required().getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = commandLine.nextString("--switch")) != null) {
                Boolean booleanValue = a.getBooleanValue(null);
                if (booleanValue != null) {
                    options.getLauncher().setSystemWideConfig(booleanValue);
                }
            } else if ((a = commandLine.nextString("--ignore-unsupported-os")) != null) {
                if (a.isEnabled()) {
                    ignoreUnsupportedOs = a.getBooleanValue();
                }
            } else if (commandLine.peek().getStringKey().equals("-w") || commandLine.peek().getStringKey().equals("--workspace")) {
                a = commandLine.nextString();
                if (a.isEnabled()) {
                    options.getLauncher().setSwitchWorkspaceLocation(a.getStringValue());
                }
            } else if (commandLine.peek().getStringKey().equals("-n") || commandLine.peek().getStringKey().equals("--name")) {
                a = commandLine.nextString();
                if (a.isEnabled()) {
                    options.getLauncher().setCustomScriptPath(a.getStringValue());
                }
            } else if (commandLine.peek().isOption()) {
                session.configureLast(commandLine);
            } else {
                idsToInstall.add(commandLine.next().getString());
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
            if (!idsToInstall.isEmpty()) {
                printResults(session, ndi.addScript(options, idsToInstall.toArray(new String[0])));
            }
        }
    }

    private NutsSupportCondition parseCond(NutsArgument a) {
        String s = a.getStringValue("");
        switch (s) {
            case "supported": {
                return NutsSupportCondition.SUPPORTED;
            }
            case "never": {
                return NutsSupportCondition.NEVER;
            }
            case "always": {
                return NutsSupportCondition.ALWAYS;
            }
            case "preferred":
            case "": {
                return NutsSupportCondition.PREFERRED;
            }
            default: {
                if (a.getBooleanValue()) {
                    return NutsSupportCondition.PREFERRED;
                } else {
                    return NutsSupportCondition.NEVER;
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
                for (String id : idsToUninstall) {
                    try {
                        ndi.removeNutsScript(
                                id,
                                null,
                                session.copy().setTrace(subTrace)
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
        NutsSupportCondition createDesktop = NutsSupportCondition.NEVER;
        NutsSupportCondition createMenu = NutsSupportCondition.NEVER;
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
                if (a.isEnabled()) {
                    createMenu = parseCond(a);
                }
            } else if ((a = commandLine.nextString("--menu-category")) != null) {
                if (a.isEnabled()) {
                    menuCategory = a.getStringValue();
                    if (menuCategory != null && !menuCategory.isEmpty()) {
                        if (createMenu == NutsSupportCondition.NEVER) {
                            createMenu = NutsSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.nextString("--menu-name")) != null) {
                if (a.isEnabled()) {
                    shortcutName = a.getStringValue();
                    if (shortcutName != null && !shortcutName.isEmpty()) {
                        if (createMenu == NutsSupportCondition.NEVER) {
                            createMenu = NutsSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.nextString("--desktop-name")) != null) {
                if (a.isEnabled()) {
                    shortcutName = a.getStringValue();
                    if (shortcutName != null && !shortcutName.isEmpty()) {
                        if (createDesktop == NutsSupportCondition.NEVER) {
                            createDesktop = NutsSupportCondition.PREFERRED;
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
                NdiScriptOptions oo = new NdiScriptOptions()
                        .setSession(session);
                oo.getLauncher().setSwitchWorkspaceLocation(switchWorkspaceLocation);
                oo.getLauncher().setCreateDesktopShortcut(createDesktop);
                oo.getLauncher().setMenuCategory(menuCategory);
                oo.getLauncher().setCreateMenuShortcut(createMenu);
                oo.getLauncher().setShortcutName(shortcutName);
                ndi.switchWorkspace(oo);
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
