package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix.LinuxNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix.MacosNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win.WindowsNdi;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTexts;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NSettingsNdiSubCommand extends AbstractNSettingsSubCommand {

    public static SystemNdi createNdi(NSession session) {
        SystemNdi ndi = null;
        NEnvs workspaceEnvManager = NEnvs.of(session);
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

    public void runAddLauncher(NCommandLine commandLine, NSession session) {
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
                    commandLine.withNextFlag((v, a, s) -> d.options.setFetch(v));
                    break;
                }
                case "-d":
                case "--workdir": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().setWorkingDirectory(v));
                    break;
                }
                case "--icon": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().setIcon(v));
                    break;
                }
                case "--menu": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().setCreateMenuLauncher(parseCond(v, session)));
                    break;
                }
                case "--menu-category": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.options.getLauncher().setMenuCategory(v);
                        if (d.options.getLauncher().getMenuCategory() != null && !d.options.getLauncher().getMenuCategory().isEmpty()) {
                            if (d.options.getLauncher().getCreateMenuLauncher() == NSupportMode.NEVER) {
                                d.options.getLauncher().setCreateMenuLauncher(NSupportMode.PREFERRED);
                            }
                        }
                    });
                    break;
                }
                case "--desktop": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().setCreateDesktopLauncher(parseCond(v, session)));
                    break;
                }
                case "--desktop-name": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateDesktopLauncher() == NSupportMode.NEVER) {
                            d.options.getLauncher().setCreateDesktopLauncher(NSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--menu-name": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateDesktopLauncher() == NSupportMode.NEVER) {
                            d.options.getLauncher().setCreateMenuLauncher(NSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--shortcut-name": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateUserLauncher() == NSupportMode.NEVER) {
                            d.options.getLauncher().setCreateUserLauncher(NSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--shortcut-path": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.options.getLauncher().setCustomShortcutPath(v);
                        if (d.options.getLauncher().getCreateUserLauncher() == NSupportMode.NEVER) {
                            d.options.getLauncher().setCreateUserLauncher(NSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "-x":
                case "--external":
                case "--spawn": {
                    commandLine.withNextTrueFlag((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--spawn"));
                    break;
                }
                case "-b":
                case "--embedded": {
                    commandLine.withNextTrueFlag((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--embedded"));
                    break;
                }
                case "--terminal": {
                    commandLine.withNextFlag((v, a, s) -> d.options.getLauncher().setOpenTerminal(v));
                    break;
                }
                case "-e":
                case "--env": {
                    commandLine.withNextFlag((v, a, s) -> d.options.setIncludeEnv(v));
                    break;
                }
                case "--system": {
                    commandLine.withNextTrueFlag((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--system"));
                    break;
                }
                case "--current-user": {
                    commandLine.withNextTrueFlag((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--current-user"));
                    break;
                }
                case "--as-root": {
                    commandLine.withNextTrueFlag((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--as-root"));
                    break;
                }
                case "--run-as": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--run-as=" + v));
                    break;
                }
                case "-X":
                case "--exec-options": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().getNutsOptions().add("--exec-options=" + v));
                    break;
                }
                case "-i":
                case "--installed": {
                    commandLine.withNextTrueFlag((v, a, s) -> {
                        session.setConfirm(NConfirmationMode.YES);
                        for (NId resultId : NSearchCommand.of(session).setInstallStatus(
                                NInstallStatusFilters.of(session).byInstalled(true)
                        ).getResultIds()) {
                            d.idsToInstall.add(resultId.getLongName());
                            d.missingAnyArgument = false;
                        }
                    });
                    break;
                }
                case "-c":
                case "--companions": {
                    commandLine.withNextTrueFlag((v, a, s) -> {
                        session.setConfirm(NConfirmationMode.YES);
                        for (NId companion : session.extensions().getCompanionIds()) {
                            d.idsToInstall.add(NSearchCommand.of(session).addId(companion).setLatest(true).getResultIds().required().getLongName());
                            d.missingAnyArgument = false;
                        }
                    });
                    break;
                }
                case "--switch": {
                    commandLine.withNextFlag((v, a, s) -> d.options.getLauncher().setSwitchWorkspace(v));
                    break;
                }
                case "--ignore-unsupported-os": {
                    commandLine.withNextFlag((v, a, s) -> d.ignoreUnsupportedOs = v);
                    break;
                }
                case "-w":
                case "--workspace": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().setSwitchWorkspaceLocation(v));
                    break;
                }
                case "-n":
                case "--name": {
                    commandLine.withNextEntry((v, a, s) -> d.options.getLauncher().setCustomScriptPath(v));
                    break;
                }
                default: {
                    if (commandLine.isNextOption()) {
                        session.configureLast(commandLine);
                    } else {
                        d.idsToInstall.add(commandLine.next().flatMap(NLiteral::asString).get(session));
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
                throw new NExecutionException(session, NMsg.ofC("platform not supported : %s", NEnvs.of(session).getOs()), 2);
            }
            if (!d.idsToInstall.isEmpty()) {
                printResults(session, ndi.addScript(d.options, d.idsToInstall.toArray(new String[0])));
            }
        }
    }


    private NSupportMode parseCond(String s, NSession session) {
        switch (s) {
            case "supported": {
                return NSupportMode.SUPPORTED;
            }
            case "never": {
                return NSupportMode.NEVER;
            }
            case "always": {
                return NSupportMode.ALWAYS;
            }
            case "preferred":
            case "": {
                return NSupportMode.PREFERRED;
            }
            default: {
                if (NLiteral.of(s).asBoolean().get(session)) {
                    return NSupportMode.PREFERRED;
                } else {
                    return NSupportMode.NEVER;
                }
            }
        }
    }

    public void runRemoveLauncher(NCommandLine commandLine, NSession session) {
        ArrayList<String> idsToUninstall = new ArrayList<>();
        boolean forceAll = false;
        boolean missingAnyArgument = true;
        NArg a;
        boolean ignoreUnsupportedOs = false;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextEntry("--ignore-unsupported-os").orNull()) != null) {
                if (a.isActive()) {
                    ignoreUnsupportedOs = a.getBooleanValue().get(session);
                }
            } else if (commandLine.isNextOption()) {
                session.configureLast(commandLine);
            } else {
                idsToUninstall.add(commandLine.next().flatMap(NLiteral::asString).get(session));
                missingAnyArgument = false;
            }
        }
        if (missingAnyArgument) {
            commandLine.peek().get(session);
        }
        if (commandLine.isExecMode()) {
            if (forceAll) {
                session.setConfirm(NConfirmationMode.YES);
            }
            SystemNdi ndi = createNdi(session);
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NExecutionException(session, NMsg.ofC("platform not supported : %s", NEnvs.of(session).getOs()), 2);
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
                    } catch (UncheckedIOException | NIOException e) {
                        throw new NExecutionException(session, NMsg.ofC("unable to run script %s : %s", id, e), e);
                    }
                }
            }
        }
    }

    public void runSwitch(NCommandLine commandLine, NSession session) {
        class Data {
            String switchWorkspaceLocation = null;
            String switchWorkspaceApi = null;
            boolean ignoreUnsupportedOs = false;
            NSupportMode createDesktop = NSupportMode.NEVER;
            NSupportMode createMenu = NSupportMode.NEVER;
            String menuCategory = null;
            String shortcutName = null;
        }
        Data d = new Data();
        while (commandLine.hasNext()) {
            switch (commandLine.peek().get().key()) {
                case "--ignore-unsupported-os": {
                    commandLine.withNextFlag((v, a, s) -> d.ignoreUnsupportedOs = v);
                    break;
                }
                case "-w":
                case "--workspace": {
                    commandLine.withNextEntry((v, a, s) -> d.switchWorkspaceLocation = v);
                    break;
                }
                case "-a":
                case "--api": {
                    commandLine.withNextEntry((v, a, s) -> d.switchWorkspaceApi = v);
                    break;
                }
                case "--menu": {
                    commandLine.withNextEntry((v, a, s) -> d.createMenu = parseCond(v, session));
                    break;
                }
                case "--menu-category": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.menuCategory = v;
                        if (d.menuCategory != null && !d.menuCategory.isEmpty()) {
                            if (d.createMenu == NSupportMode.NEVER) {
                                d.createMenu = NSupportMode.PREFERRED;
                            }
                        }
                    });
                    break;
                }
                case "--menu-name": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.shortcutName = v;
                        if (d.shortcutName != null && !d.shortcutName.isEmpty()) {
                            if (d.createMenu == NSupportMode.NEVER) {
                                d.createMenu = NSupportMode.PREFERRED;
                            }
                        }
                    });
                    break;
                }
                case "--desktop-name": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.shortcutName = v;
                        if (d.shortcutName != null && !d.shortcutName.isEmpty()) {
                            if (d.createDesktop == NSupportMode.NEVER) {
                                d.createDesktop = NSupportMode.PREFERRED;
                            }
                        }
                    });
                    break;
                }
                case "--desktop": {
                    commandLine.withNextEntry((v, a, s) -> {
                        d.createDesktop = parseCond(v, session);
                    });
                    break;
                }
                default: {
                    if (commandLine.isNextOption()) {
                        commandLine.throwUnexpectedArgument();
                    } else if (d.switchWorkspaceLocation == null) {
                        d.switchWorkspaceLocation = commandLine.next().flatMap(NLiteral::asString).get(session);
                    } else if (d.switchWorkspaceApi == null) {
                        d.switchWorkspaceApi = commandLine.next().flatMap(NLiteral::asString).get(session);
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
                throw new NExecutionException(session, NMsg.ofC("platform not supported : %s ", NEnvs.of(session).getOs()), 2);
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

    private void printResults(NSession session, PathInfo[] result) {
        if (session.isTrace()) {
            result = Arrays.stream(result).filter(x -> x.getStatus() != PathInfo.Status.DISCARDED).toArray(PathInfo[]::new);
            if (session.isPlainTrace()) {
                int namesSize = Arrays.stream(result).mapToInt(x -> x.getPath().getName().length()).max().orElse(1);
                for (PathInfo ndiScriptInfo : result) {
                    NTexts txt = NTexts.of(session);
                    session.out().resetLine().println(NMsg.ofC(
                            "%s script %-" + namesSize + "s for %s"
                                    + " at %s",
                            (ndiScriptInfo.getStatus() == PathInfo.Status.OVERRIDDEN)
                                    ? txt.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined()))
                                    : txt.ofStyled("install", NTextStyle.success())
                            ,
                            txt.ofStyled(ndiScriptInfo.getPath().getName(), NTextStyle.path()),
                            ndiScriptInfo.getId(),
                            txt.ofStyled(CoreIOUtils.betterPath(ndiScriptInfo.getPath().toString()), NTextStyle.path())
                    ));
                }

            } else {
                session.out().println(result);
            }
        }
    }

    @Override
    public boolean exec(NCommandLine cmdLine, Boolean autoSave, NSession session) {
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
