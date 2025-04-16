package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.ext.NExtensions;
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
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NSupportMode;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NSettingsNdiSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsNdiSubCommand() {
        super();
    }

    public static SystemNdi createNdi() {
        SystemNdi ndi = null;
        switch (NWorkspace.of().getOsFamily()) {
            case LINUX:
            case UNIX: {

                ndi = new LinuxNdi();
                break;
            }
            case MACOS: {
                ndi = new MacosNdi();
                break;
            }
            case WINDOWS: {
                if (OptionalMsLinkHelper.isSupported()) {
                    ndi = new WindowsNdi();
                }
                break;
            }
        }
        return ndi;
    }

    public void runAddLauncher(NCmdLine cmdLine) {
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

//        String linkName = null;
//        boolean env = false;
        cmdLine.setCommandName("settings add launcher");
        NSession session=NSession.of();
        while (cmdLine.hasNext()) {
            switch (cmdLine.peek().get().key()) {
                case "-t":
                case "--fetch": {
                    cmdLine.withNextFlag((v, a) -> d.options.setFetch(v));
                    break;
                }
                case "-d":
                case "--workdir": {
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().setWorkingDirectory(v));
                    break;
                }
                case "--icon": {
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().setIcon(v));
                    break;
                }
                case "--menu": {
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().setCreateMenuLauncher(parseCond(v)));
                    break;
                }
                case "--menu-category": {
                    cmdLine.withNextEntry((v, a) -> {
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
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().setCreateDesktopLauncher(parseCond(v)));
                    break;
                }
                case "--desktop-name": {
                    cmdLine.withNextEntry((v, a) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateDesktopLauncher() == NSupportMode.NEVER) {
                            d.options.getLauncher().setCreateDesktopLauncher(NSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--menu-name": {
                    cmdLine.withNextEntry((v, a) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateDesktopLauncher() == NSupportMode.NEVER) {
                            d.options.getLauncher().setCreateMenuLauncher(NSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--shortcut-name": {
                    cmdLine.withNextEntry((v, a) -> {
                        d.options.getLauncher().setShortcutName(v);
                        if (d.options.getLauncher().getCreateUserLauncher() == NSupportMode.NEVER) {
                            d.options.getLauncher().setCreateUserLauncher(NSupportMode.PREFERRED);
                        }
                    });
                    break;
                }
                case "--shortcut-path": {
                    cmdLine.withNextEntry((v, a) -> {
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
                    cmdLine.withNextTrueFlag((v, a) -> d.options.getLauncher().getNutsOptions().add("--spawn"));
                    break;
                }
                case "-b":
                case "--embedded": {
                    cmdLine.withNextTrueFlag((v, a) -> d.options.getLauncher().getNutsOptions().add("--embedded"));
                    break;
                }
                case "--terminal": {
                    cmdLine.withNextFlag((v, a) -> d.options.getLauncher().setOpenTerminal(v));
                    break;
                }
                case "-e":
                case "--env": {
                    cmdLine.withNextFlag((v, a) -> d.options.setIncludeEnv(v));
                    break;
                }
                case "--system": {
                    cmdLine.withNextTrueFlag((v, a) -> d.options.getLauncher().getNutsOptions().add("--system"));
                    break;
                }
                case "--current-user": {
                    cmdLine.withNextTrueFlag((v, a) -> d.options.getLauncher().getNutsOptions().add("--current-user"));
                    break;
                }
                case "--as-root": {
                    cmdLine.withNextTrueFlag((v, a) -> d.options.getLauncher().getNutsOptions().add("--as-root"));
                    break;
                }
                case "--run-as": {
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().getNutsOptions().add("--run-as=" + v));
                    break;
                }
                case "-X":
                case "--exec-options": {
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().getNutsOptions().add("--exec-options=" + v));
                    break;
                }
                case "-i":
                case "--installed": {
                    cmdLine.withNextTrueFlag((v, a) -> {
                        session.setConfirm(NConfirmationMode.YES);
                        for (NId resultId : NSearchCmd.of()
                                .setDefinitionFilter(NDefinitionFilters.of().byInstalled(true)
                        ).getResultIds()) {
                            d.idsToInstall.add(resultId.getLongName());
                            d.missingAnyArgument = false;
                        }
                    });
                    break;
                }
                case "-c":
                case "--companions": {
                    cmdLine.withNextTrueFlag((v, a) -> {
                        session.setConfirm(NConfirmationMode.YES);
                        for (NId companion : NExtensions.of().getCompanionIds()) {
                            d.idsToInstall.add(NSearchCmd.of().addId(companion).setLatest(true).getResultIds().findFirst().get().getLongName());
                            d.missingAnyArgument = false;
                        }
                    });
                    break;
                }
                case "--switch": {
                    cmdLine.withNextFlag((v, a) -> d.options.getLauncher().setSwitchWorkspace(v));
                    break;
                }
                case "--ignore-unsupported-os": {
                    cmdLine.withNextFlag((v, a) -> d.ignoreUnsupportedOs = v);
                    break;
                }
                case "-w":
                case "--workspace": {
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().setSwitchWorkspaceLocation(v));
                    break;
                }
                case "-n":
                case "--name": {
                    cmdLine.withNextEntry((v, a) -> d.options.getLauncher().setCustomScriptPath(v));
                    break;
                }
                default: {
                    if (cmdLine.isNextOption()) {
                        session.configureLast(cmdLine);
                    } else {
                        d.idsToInstall.add(cmdLine.next().flatMap(NArg::asString).get());
                        d.missingAnyArgument = false;
                    }
                }
            }

        }

        if (d.missingAnyArgument) {
            cmdLine.peek().get();
        }
        if (cmdLine.isExecMode()) {
            SystemNdi ndi = createNdi();
            if (ndi == null) {
                if (d.ignoreUnsupportedOs) {
                    return;
                }
                throw new NExecutionException(NMsg.ofC("platform not supported : %s", NWorkspace.of().getOs()), NExecutionException.ERROR_2);
            }
            if (!d.idsToInstall.isEmpty()) {
                printResults(ndi.addScript(d.options, d.idsToInstall.toArray(new String[0])));
            }
        }
    }


    private NSupportMode parseCond(String s) {
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
                if (NLiteral.of(s).asBoolean().get()) {
                    return NSupportMode.PREFERRED;
                } else {
                    return NSupportMode.NEVER;
                }
            }
        }
    }

    public void runRemoveLauncher(NCmdLine cmdLine) {
        ArrayList<String> idsToUninstall = new ArrayList<>();
        boolean forceAll = false;
        boolean missingAnyArgument = true;
        NArg a;
        boolean ignoreUnsupportedOs = false;
        NSession session=NSession.of();
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.nextEntry("--ignore-unsupported-os").orNull()) != null) {
                if (a.isActive()) {
                    ignoreUnsupportedOs = a.getBooleanValue().get();
                }
            } else if (cmdLine.isNextOption()) {
                session.configureLast(cmdLine);
            } else {
                idsToUninstall.add(cmdLine.next().flatMap(NArg::asString).get());
                missingAnyArgument = false;
            }
        }
        if (missingAnyArgument) {
            cmdLine.peek().get();
        }
        if (cmdLine.isExecMode()) {
            if (forceAll) {
                session.setConfirm(NConfirmationMode.YES);
            }
            SystemNdi ndi = createNdi();
            if (ndi == null) {
                if (ignoreUnsupportedOs) {
                    return;
                }
                throw new NExecutionException(NMsg.ofC("platform not supported : %s", NWorkspace.of().getOs()), NExecutionException.ERROR_2);
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
                                null
                        );
                    } catch (UncheckedIOException | NIOException e) {
                        throw new NExecutionException(NMsg.ofC("unable to run script %s : %s", id, e), e);
                    }
                }
            }
        }
    }

    public void runSwitch(NCmdLine cmdLine) {
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
        NSession session=NSession.of();
        while (cmdLine.hasNext()) {
            switch (cmdLine.peek().get().key()) {
                case "--ignore-unsupported-os": {
                    cmdLine.withNextFlag((v, a) -> d.ignoreUnsupportedOs = v);
                    break;
                }
                case "-w":
                case "--workspace": {
                    cmdLine.withNextEntry((v, a) -> d.switchWorkspaceLocation = v);
                    break;
                }
                case "-a":
                case "--api": {
                    cmdLine.withNextEntry((v, a) -> d.switchWorkspaceApi = v);
                    break;
                }
                case "--menu": {
                    cmdLine.withNextEntry((v, a) -> d.createMenu = parseCond(v));
                    break;
                }
                case "--menu-category": {
                    cmdLine.withNextEntry((v, a) -> {
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
                    cmdLine.withNextEntry((v, a) -> {
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
                    cmdLine.withNextEntry((v, a) -> {
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
                    cmdLine.withNextEntry((v, a) -> {
                        d.createDesktop = parseCond(v);
                    });
                    break;
                }
                default: {
                    if (cmdLine.isNextOption()) {
                        cmdLine.throwUnexpectedArgument();
                    } else if (d.switchWorkspaceLocation == null) {
                        d.switchWorkspaceLocation = cmdLine.next().flatMap(NArg::asString).get();
                    } else if (d.switchWorkspaceApi == null) {
                        d.switchWorkspaceApi = cmdLine.next().flatMap(NArg::asString).get();
                    } else if (cmdLine.isNextOption()) {
                        session.configureLast(cmdLine);
                    } else {
                        cmdLine.throwUnexpectedArgument();
                    }
                }
            }
        }
        if (cmdLine.isExecMode()) {
            SystemNdi ndi = createNdi();
            if (ndi == null) {
                if (d.ignoreUnsupportedOs) {
                    return;
                }
                throw new NExecutionException(NMsg.ofC("platform not supported : %s ", NWorkspace.of().getOs()), NExecutionException.ERROR_2);
            }
            if (d.switchWorkspaceLocation != null || d.switchWorkspaceApi != null) {
                NdiScriptOptions oo = new NdiScriptOptions();
                oo.getLauncher().setSwitchWorkspaceLocation(d.switchWorkspaceLocation);
                oo.getLauncher().setCreateDesktopLauncher(d.createDesktop);
                oo.getLauncher().setMenuCategory(d.menuCategory);
                oo.getLauncher().setCreateMenuLauncher(d.createMenu);
                oo.getLauncher().setShortcutName(d.shortcutName);
                ndi.switchWorkspace(oo);
            }
        }

    }

    private void printResults(PathInfo[] result) {
        NSession session=NSession.of();
        if (session.isTrace()) {
            result = Arrays.stream(result).filter(x -> x.getStatus() != PathInfo.Status.DISCARDED).toArray(PathInfo[]::new);
            if (session.isPlainTrace()) {
                int namesSize = Arrays.stream(result).mapToInt(x -> x.getPath().getName().length()).max().orElse(1);
                for (PathInfo ndiScriptInfo : result) {
                    NTexts txt = NTexts.of();
                    NOut.resetLine().println(NMsg.ofC(
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
                NOut.println(result);
            }
        }
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        if (cmdLine.next("add launcher", "lna").isPresent()) {
            runAddLauncher(cmdLine);
            return true;
        } else if (cmdLine.next("remove launcher", "lnrm").isPresent()) {
            runRemoveLauncher(cmdLine);
            return true;
        } else if (cmdLine.next("switch", "lnsw").isPresent()) {
            runSwitch(cmdLine);
            return true;
        }
        return false;
    }
}
