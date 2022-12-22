package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix.LinuxNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix.MacosNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win.WindowsNdi;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTextStyles;
import net.thevpc.nuts.text.NutsTexts;

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
        class Data {
            NdiScriptOptions options = new NdiScriptOptions();
            List<String> idsToInstall = new ArrayList<>();

            //        ArrayList<String> idsToInstall ;
            //        ArrayList<String> executorOptions = new ArrayList<>();
//        NutsExecutionType execType = null;
//        boolean fetch = false;
            boolean missingAnyArgument = true;
            //        boolean forceAll = false;
//        Boolean persistentConfig = null;
            boolean ignoreUnsupportedOs = false;
        }
        Data d = new Data();
        d.options.setSession(session.copy());

//        String linkName = null;
//        boolean env = false;
        commandLine.setCommandName("settings add launcher");
        while (commandLine.hasNext()) {
            switch (commandLine.peek().get().key()) {
                case "-t":
                case "--fetch": {
                    commandLine.withNextBoolean((v, a, s) -> d.options.setFetch(v));
                    break;
                }
                case "-d":
                case "--workdir": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().setWorkingDirectory(v));
                    break;
                }
                case "--icon": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().setIcon(v));
                    break;
                }
                case "--menu": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().setCreateMenuLauncher(parseCond(v, session)));
                    break;
                }
                case "--menu-category": {
                    commandLine.withNextString((v, a, s) -> {
                        d.options.getLauncher().setMenuCategory(v);
                        if (d.options.getLauncher().getMenuCategory() != null && !d.options.getLauncher().getMenuCategory().isEmpty()) {
                            if (d.options.getLauncher().getCreateMenuLauncher() == NutsSupportMode.NEVER) {
                                d.options.getLauncher().setCreateMenuLauncher(NutsSupportMode.PREFERRED);
                            }
                        }
                    });
                    break;
                }
                case "--desktop": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().setCreateDesktopLauncher(parseCond(v, session)));
                    break;
                }
                case "--desktop-name": {
                    commandLine.withNextString((v, a, s) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateDesktopLauncher() == NutsSupportMode.NEVER) {
                            d.options.getLauncher().setCreateDesktopLauncher(NutsSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--menu-name": {
                    commandLine.withNextString((v, a, s) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateDesktopLauncher() == NutsSupportMode.NEVER) {
                            d.options.getLauncher().setCreateMenuLauncher(NutsSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--shortcut-name": {
                    commandLine.withNextString((v, a, s) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateUserLauncher() == NutsSupportMode.NEVER) {
                            d.options.getLauncher().setCreateUserLauncher(NutsSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--shortcut-path": {
                    commandLine.withNextString((v, a, s) -> {
                        d.options.getLauncher().setCustomShortcutPath(v);
                        if (d.options.getLauncher().getCreateUserLauncher() == NutsSupportMode.NEVER) {
                            d.options.getLauncher().setCreateUserLauncher(NutsSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "-x":
                case "--external":
                case "--spawn": {
                    commandLine.withNextTrue((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--spawn"));
                    break;
                }
                case "-b":
                case "--embedded": {
                    commandLine.withNextTrue((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--embedded"));
                    break;
                }
                case "--terminal": {
                    commandLine.withNextBoolean((v, a, s) -> d.options.getLauncher().setOpenTerminal(v));
                    break;
                }
                case "-e":
                case "--env": {
                    commandLine.withNextBoolean((v, a, s) -> d.options.setIncludeEnv(v));
                    break;
                }
                case "--system": {
                    commandLine.withNextTrue((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--system"));
                    break;
                }
                case "--current-user": {
                    commandLine.withNextTrue((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--current-user"));
                    break;
                }
                case "--as-root": {
                    commandLine.withNextTrue((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--as-root"));
                    break;
                }
                case "--run-as": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--run-as=" + v));
                    break;
                }
                case "-X":
                case "--exec-options": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--exec-options=" + v));
                    break;
                }
                case "-i":
                case "--installed": {
                    commandLine.withNextTrue((v, a, s) -> {
                        session.setConfirm(NutsConfirmationMode.YES);
                        for (NutsId resultId : session.search().setInstallStatus(
                                NutsInstallStatusFilters.of(session).byInstalled(true)
                        ).getResultIds()) {
                            d.idsToInstall.add(resultId.getLongName());
                            d.missingAnyArgument = false;
                        }
                    });
                    break;
                }
                case "-c":
                case "--companions": {
                    commandLine.withNextTrue((v, a, s) -> {
                        session.setConfirm(NutsConfirmationMode.YES);
                        for (NutsId companion : session.extensions().getCompanionIds()) {
                            d.idsToInstall.add(session.search().addId(companion).setLatest(true).getResultIds().required().getLongName());
                            d.missingAnyArgument = false;
                        }
                    });
                    break;
                }
                case "--switch": {
                    commandLine.withNextBoolean((v, a, s) -> d.options.getLauncher().setSwitchWorkspace(v));
                    break;
                }
                case "--ignore-unsupported-os": {
                    commandLine.withNextBoolean((v, a, s) -> d.ignoreUnsupportedOs = v);
                    break;
                }
                case "-w":
                case "--workspace": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().setSwitchWorkspaceLocation(v));
                    break;
                }
                case "-n":
                case "--name": {
                    commandLine.withNextString((v, a, s) -> d.options.getLauncher().setCustomScriptPath(v));
                    break;
                }
                default: {
                    if (commandLine.isNextOption()) {
                        session.configureLast(commandLine);
                    } else {
                        d.idsToInstall.add(commandLine.next().flatMap(NutsValue::asString).get(session));
                        d.missingAnyArgument = false;
                    }
                }
            }

        }

        if (d.missingAnyArgument) {
            commandLine.peek().get(session);
        }
        if (commandLine.isExecMode()) {
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (d.ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.ofCstyle("platform not supported : %s", session.env().getOs()), 2);
            }
            if (!d.idsToInstall.isEmpty()) {
                printResults(session, ndi.addScript(d.options, d.idsToInstall.toArray(new String[0])));
            }
        }
    }


    private NutsSupportMode parseCond(String s, NutsSession session) {
        switch (s) {
            case "supported": {
                return NutsSupportMode.SUPPORTED;
            }
            case "never": {
                return NutsSupportMode.NEVER;
            }
            case "always": {
                return NutsSupportMode.ALWAYS;
            }
            case "preferred":
            case "": {
                return NutsSupportMode.PREFERRED;
            }
            default: {
                if (NutsValue.of(s).asBoolean().get(session)) {
                    return NutsSupportMode.PREFERRED;
                } else {
                    return NutsSupportMode.NEVER;
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
                throw new NutsExecutionException(session, NutsMessage.ofCstyle("platform not supported : %s", session.env().getOs()), 2);
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
                        throw new NutsExecutionException(session, NutsMessage.ofCstyle("unable to run script %s : %s", id, e), e);
                    }
                }
            }
        }
    }

    public void runSwitch(NutsCommandLine commandLine, NutsSession session) {
        class Data {
            String switchWorkspaceLocation = null;
            String switchWorkspaceApi = null;
            boolean ignoreUnsupportedOs = false;
            NutsSupportMode createDesktop = NutsSupportMode.NEVER;
            NutsSupportMode createMenu = NutsSupportMode.NEVER;
            String menuCategory = null;
            String shortcutName = null;
        }
        Data d = new Data();
        while (commandLine.hasNext()) {
            switch (commandLine.peek().get().key()) {
                case "--ignore-unsupported-os": {
                    commandLine.withNextBoolean((v, a, s) -> d.ignoreUnsupportedOs = v);
                    break;
                }
                case "-w":
                case "--workspace": {
                    commandLine.withNextString((v, a, s) -> d.switchWorkspaceLocation = v);
                    break;
                }
                case "-a":
                case "--api": {
                    commandLine.withNextString((v, a, s) -> d.switchWorkspaceApi = v);
                    break;
                }
                case "--menu": {
                    commandLine.withNextString((v, a, s) -> d.createMenu = parseCond(v, session));
                    break;
                }
                case "--menu-category": {
                    commandLine.withNextString((v, a, s) -> {
                        d.menuCategory = v;
                        if (d.menuCategory != null && !d.menuCategory.isEmpty()) {
                            if (d.createMenu == NutsSupportMode.NEVER) {
                                d.createMenu = NutsSupportMode.PREFERRED;
                            }
                        }
                    });
                    break;
                }
                case "--menu-name": {
                    commandLine.withNextString((v, a, s) -> {
                        d.shortcutName = v;
                        if (d.shortcutName != null && !d.shortcutName.isEmpty()) {
                            if (d.createMenu == NutsSupportMode.NEVER) {
                                d.createMenu = NutsSupportMode.PREFERRED;
                            }
                        }
                    });
                    break;
                }
                case "--desktop-name": {
                    commandLine.withNextString((v, a, s) -> {
                        d.shortcutName = v;
                        if (d.shortcutName != null && !d.shortcutName.isEmpty()) {
                            if (d.createDesktop == NutsSupportMode.NEVER) {
                                d.createDesktop = NutsSupportMode.PREFERRED;
                            }
                        }
                    });
                    break;
                }
                case "--desktop": {
                    commandLine.withNextString((v, a, s) -> {
                        d.createDesktop = parseCond(v, session);
                    });
                    break;
                }
                default: {
                    if (commandLine.isNextOption()) {
                        commandLine.throwUnexpectedArgument();
                    } else if (d.switchWorkspaceLocation == null) {
                        d.switchWorkspaceLocation = commandLine.next().flatMap(NutsValue::asString).get(session);
                    } else if (d.switchWorkspaceApi == null) {
                        d.switchWorkspaceApi = commandLine.next().flatMap(NutsValue::asString).get(session);
                    } else if (commandLine.isNextOption()) {
                        session.configureLast(commandLine);
                    } else {
                        commandLine.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (commandLine.isExecMode()) {
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (d.ignoreUnsupportedOs) {
                    return;
                }
                throw new NutsExecutionException(session, NutsMessage.ofCstyle("platform not supported : %s ", session.env().getOs()), 2);
            }
            if (d.switchWorkspaceLocation != null || d.switchWorkspaceApi != null) {
                NdiScriptOptions oo = new NdiScriptOptions()
                        .setSession(session);
                oo.getLauncher().setSwitchWorkspaceLocation(d.switchWorkspaceLocation);
                oo.getLauncher().setCreateDesktopLauncher(d.createDesktop);
                oo.getLauncher().setMenuCategory(d.menuCategory);
                oo.getLauncher().setCreateMenuLauncher(d.createMenu);
                oo.getLauncher().setShortcutName(d.shortcutName);
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
        if (cmdLine.next("add launcher", "lna").isPresent()) {
            runAddLauncher(cmdLine, session);
            return true;
        } else if (cmdLine.next("remove launcher", "lnrm").isPresent()) {
            runRemoveLauncher(cmdLine, session);
            return true;
        } else if (cmdLine.next("switch", "lnsw").isPresent()) {
            runSwitch(cmdLine, session);
            return true;
        }
        return false;
    }
}
