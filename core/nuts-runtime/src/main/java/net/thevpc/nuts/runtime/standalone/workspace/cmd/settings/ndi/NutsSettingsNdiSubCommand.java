package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix.LinuxNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix.MacosNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win.WindowsNdi;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutsSettingsNdiSubCommand extends AbstractNutsSettingsSubCommand {

    public static SystemNdi createNdi(NutsSession session) {
        SystemNdi ndi = null;
        NutsWorkspaceEnvManager workspaceEnvManager = session.env();
        switch (workspaceEnvManager.getOsFamily()) {
            case LINUX:
            case UNIX: {

                ndi = new LinuxNdi(session);
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
            if ((a = commandLine.nextBoolean("-t", "--fetch").orNull()) != null) {
                options.setFetch(a.getBooleanValue().get(session));
            } else if ((a = commandLine.nextString("-d", "--workdir").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setWorkingDirectory(a.getStringValue().get(session));
                }
            } else if ((a = commandLine.nextString("--icon").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setIcon(a.getStringValue().get(session));
                }
            } else if ((a = commandLine.next("--menu").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setCreateMenuShortcut(parseCond(a,session));
                }
            } else if ((a = commandLine.nextString("--menu-category").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setMenuCategory(a.getStringValue().get(session));
                    if (options.getLauncher().getMenuCategory() != null && !options.getLauncher().getMenuCategory().isEmpty()) {
                        if (options.getLauncher().getCreateMenuShortcut() == NutsSupportCondition.NEVER) {
                            options.getLauncher().setCreateMenuShortcut(NutsSupportCondition.PREFERRED);
                        }
                    }
                }
            } else if ((a = commandLine.nextBoolean("--desktop").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setCreateDesktopShortcut(parseCond(a,session));
                }
            } else if ((a = commandLine.nextString("--desktop-name").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setShortcutName(a.getStringValue().get(session));
                    if (options.getLauncher().getCreateDesktopShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateDesktopShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--menu-name").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setShortcutName(a.getStringValue().get(session));
                    if (options.getLauncher().getCreateDesktopShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateMenuShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--shortcut-name").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setShortcutName(a.getStringValue().get(session));
                    if (options.getLauncher().getCreateCustomShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateCustomShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextString("--shortcut-path").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setCustomShortcutPath(a.getStringValue().get(session));
                    if (options.getLauncher().getCreateCustomShortcut() == NutsSupportCondition.NEVER) {
                        options.getLauncher().setCreateCustomShortcut(NutsSupportCondition.PREFERRED);
                    }
                }
            } else if ((a = commandLine.nextBoolean("-x", "--external", "--spawn").orNull()) != null) {
                if (a.getBooleanValue().get(session)) {
                    options.getLauncher().getNutsOptions().add("--spawn");
                }
            } else if ((a = commandLine.nextBoolean("-b", "--embedded").orNull()) != null) {
                if (a.getBooleanValue().get(session)) {
                    options.getLauncher().getNutsOptions().add("--embedded");
                }
            } else if ((a = commandLine.nextBoolean("--terminal").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().setOpenTerminal(a.getBooleanValue().get(session));
                }
            } else if ((a = commandLine.nextBoolean("-e", "--env").orNull()) != null) {
                options.setIncludeEnv(a.getBooleanValue().get(session));
            } else if ((a = commandLine.nextBoolean("--system").orNull()) != null) {
                if (a.getBooleanValue().get(session)) {
                    options.getLauncher().getNutsOptions().add("--system");
                }
            } else if ((a = commandLine.nextBoolean("--current-user").orNull()) != null) {
                if (a.getBooleanValue().get(session)) {
                    options.getLauncher().getNutsOptions().add("--current-user");
                }
            } else if ((a = commandLine.nextBoolean("--as-root").orNull()) != null) {
                if (a.isActive() && a.getBooleanValue().get(session)) {
                    options.getLauncher().getNutsOptions().add("--as-root");
                }
            } else if ((a = commandLine.nextBoolean("--sudo").orNull()) != null) {
                if (a.isActive() && a.getBooleanValue().get(session)) {
                    options.getLauncher().getNutsOptions().add("--sudo");
                }
            } else if ((a = commandLine.nextString("--run-as").orNull()) != null) {
                if (a.isActive()) {
                    options.getLauncher().getNutsOptions().add("--run-as=" + a.getStringValue());
                }
            } else if ((a = commandLine.nextString("-X", "--exec-options").orNull()) != null) {
                options.getLauncher().getNutsOptions().add("--exec-options=" + a.getStringValue());
            } else if ((a = commandLine.nextString("-i", "--installed").orNull()) != null) {
                session.setConfirm(NutsConfirmationMode.YES);
                for (NutsId resultId : session.search().setInstallStatus(
                        NutsInstallStatusFilters.of(session).byInstalled(true)
                ).getResultIds()) {
                    idsToInstall.add(resultId.getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = commandLine.nextString("-c", "--companions").orNull()) != null) {
                session.setConfirm(NutsConfirmationMode.YES);
                for (NutsId companion : session.extensions().getCompanionIds()) {
                    idsToInstall.add(session.search().addId(companion).setLatest(true).getResultIds().required().getLongName());
                    missingAnyArgument = false;
                }
            } else if ((a = commandLine.nextString("--switch").orNull()) != null) {
                if(a.isActive()) {
                    options.getLauncher().setSystemWideConfig(a.getBooleanValue().get(session));
                }
            } else if ((a = commandLine.nextString("--ignore-unsupported-os").orNull()) != null) {
                if (a.isActive()) {
                    ignoreUnsupportedOs = a.getBooleanValue().get(session);
                }
            } else if (commandLine.peek().get(session).getKey().asString().get(session).equals("-w") || commandLine.peek().get(session).getKey().asString().get(session).equals("--workspace")) {
                a = commandLine.nextString().get(session);
                if (a.isActive()) {
                    options.getLauncher().setSwitchWorkspaceLocation(a.getStringValue().get(session));
                }
            } else if (commandLine.peek().get(session).getKey().asString().get(session).equals("-n") || commandLine.peek().get(session).getKey().asString().get(session).equals("--name")) {
                a = commandLine.nextString().get(session);
                if (a.isActive()) {
                    options.getLauncher().setCustomScriptPath(a.getStringValue().get(session));
                }
            } else if (commandLine.isNextOption()) {
                session.configureLast(commandLine);
            } else {
                idsToInstall.add(commandLine.next().flatMap(NutsValue::asString).get(session));
                missingAnyArgument = false;
            }
        }

        if (missingAnyArgument) {
            commandLine.peek().get(session);
        }
        if (commandLine.isExecMode()) {
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.cstyle("platform not supported : %s", session.env().getOs()), 2);
            }
            if (!idsToInstall.isEmpty()) {
                printResults(session, ndi.addScript(options, idsToInstall.toArray(new String[0])));
            }
        }
    }

    private NutsSupportCondition parseCond(NutsArgument a,NutsSession session) {
        String s = a.getValue().asString().orElse("");
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
                if (a.getBooleanValue().get(session)) {
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
        boolean missingAnyArgument = true;
        NutsArgument a;
        boolean ignoreUnsupportedOs = false;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--ignore-unsupported-os").orNull()) != null) {
                if (a.isActive()) {
                    ignoreUnsupportedOs = a.getBooleanValue().get(session);
                }
            } else if (commandLine.isNextOption()) {
                session.configureLast(commandLine);
            } else {
                idsToUninstall.add(commandLine.next().flatMap(NutsValue::asString).get(session));
                missingAnyArgument = false;
            }
        }
        if (missingAnyArgument) {
            commandLine.peek().get(session);
        }
        if (commandLine.isExecMode()) {
            if (forceAll) {
                session.setConfirm(NutsConfirmationMode.YES);
            }
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.cstyle("platform not supported : %s", session.env().getOs()), 2);
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
                    } catch (UncheckedIOException | NutsIOException e) {
                        throw new NutsExecutionException(session, NutsMessage.cstyle("unable to run script %s : %s", id, e), e);
                    }
                }
            }
        }
    }

    public void runSwitch(NutsCommandLine commandLine, NutsSession session) {
        String switchWorkspaceLocation = null;
        String switchWorkspaceApi = null;
        NutsArgument a;
        boolean ignoreUnsupportedOs = false;
        NutsSupportCondition createDesktop = NutsSupportCondition.NEVER;
        NutsSupportCondition createMenu = NutsSupportCondition.NEVER;
        String menuCategory = null;
        String shortcutName = null;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--ignore-unsupported-os").orNull()) != null) {
                if (a.isActive()) {
                    ignoreUnsupportedOs = a.getBooleanValue().get(session);
                }
            } else if (commandLine.peek().get(session).getKey().asString().get(session).equals("-w")
                    || commandLine.peek().get(session).getKey().asString().get(session).equals("--workspace")) {
                switchWorkspaceLocation = commandLine.nextStringValueLiteral().get(session);
            } else if (commandLine.peek().get(session).getKey().asString().get(session).equals("-a")
                    || commandLine.peek().get(session).getKey().asString().get(session).equals("--api")) {
                switchWorkspaceApi = commandLine.nextStringValueLiteral().get(session);
            } else if ((a = commandLine.next("--menu").orNull()) != null) {
                if (a.isActive()) {
                    createMenu = parseCond(a,session);
                }
            } else if ((a = commandLine.nextString("--menu-category").orNull()) != null) {
                if (a.isActive()) {
                    menuCategory = a.getStringValue().get(session);
                    if (menuCategory != null && !menuCategory.isEmpty()) {
                        if (createMenu == NutsSupportCondition.NEVER) {
                            createMenu = NutsSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.nextString("--menu-name").orNull()) != null) {
                if (a.isActive()) {
                    shortcutName = a.getStringValue().get(session);
                    if (shortcutName != null && !shortcutName.isEmpty()) {
                        if (createMenu == NutsSupportCondition.NEVER) {
                            createMenu = NutsSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.nextString("--desktop-name").orNull()) != null) {
                if (a.isActive()) {
                    shortcutName = a.getStringValue().get(session);
                    if (shortcutName != null && !shortcutName.isEmpty()) {
                        if (createDesktop == NutsSupportCondition.NEVER) {
                            createDesktop = NutsSupportCondition.PREFERRED;
                        }
                    }
                }
            } else if ((a = commandLine.next("--desktop").orNull()) != null) {
                if (a.isActive()) {
                    createDesktop = parseCond(a,session);
                }
            } else if (commandLine.isNextOption()) {
                commandLine.throwUnexpectedArgument(session);
            } else if (switchWorkspaceLocation == null) {
                switchWorkspaceLocation = commandLine.next().flatMap(NutsValue::asString).get(session);
            } else if (switchWorkspaceApi == null) {
                switchWorkspaceApi = commandLine.next().flatMap(NutsValue::asString).get(session);
            } else if (commandLine.isNextOption()) {
                session.configureLast(commandLine);
            } else {
                commandLine.throwUnexpectedArgument(session);
            }
        }
        if (commandLine.isExecMode()) {
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.cstyle("platform not supported : %s ", session.env().getOs()), 2);
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
        if (session.isTrace()) {
            result = Arrays.stream(result).filter(x -> x.getStatus() != PathInfo.Status.DISCARDED).toArray(PathInfo[]::new);
            if (session.isPlainTrace()) {
                int namesSize = Arrays.stream(result).mapToInt(x -> x.getPath().getName().length()).max().orElse(1);
                for (PathInfo ndiScriptInfo : result) {
                    session.out().resetLine().printf("%s script %-" + namesSize + "s for %s"
                                    + " at %s%n",
                            (ndiScriptInfo.getStatus() == PathInfo.Status.OVERRIDDEN)
                                    ? NutsTexts.of(session).ofStyled("re-install", NutsTextStyles.of(NutsTextStyle.success(), NutsTextStyle.underlined()))
                                    : NutsTexts.of(session).ofStyled("install", NutsTextStyle.success())
                            ,
                            NutsTexts.of(session).ofStyled(ndiScriptInfo.getPath().getName(), NutsTextStyle.path()),
                            ndiScriptInfo.getId(),
                            NutsTexts.of(session).ofStyled(CoreIOUtils.betterPath(ndiScriptInfo.getPath().toString()), NutsTextStyle.path())
                    );
                }

            } else {
                session.out().printlnf(result);
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
