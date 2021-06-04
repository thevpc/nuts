package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.UpdatedPaths;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.OptionalWindows;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.WinShellHelper;

public class WindowsNdi extends BaseSystemNdi {

    private static String CRLF = "\r\n";

    public WindowsNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    @Override
    protected String createNutsScriptCommand(NutsId fnutsId, NdiScriptOptions options) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" ");
        if (options.getExecType() != null) {
            command.append("--").append(options.getExecType().id());
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" %*");
        return command.toString();
    }

    @Override
    public String toCommentLine(String line) {
        return ":: " + line;
    }

    @Override
    protected String getCallScriptCommand(String path) {
        return "@CALL \"" + path + "\"";
    }

    @Override
    public String getExecFileName(String name) {
        return name + ".cmd";
    }

    @Override
    protected String getTemplateBodyName() {
        return "windows_template_body.text";
    }

    @Override
    protected String getTemplateNutsName() {
        return "windows_template_nuts.text";
    }

    public String configurePathShortcut(Target target, boolean linkVersion, String apiVersion, NutsWorkspaceBootConfig bootConfig, String preferredName, NutsSession session) {
        try {
            NutsWorkspace ws = context.getWorkspace();
            if (apiVersion == null) {
                throw new NutsIllegalArgumentException(context.getSession(), "missing nuts-api version to link to");
            }
            NutsId apiId = ws.getApiId().builder().setVersion(apiVersion).build();
            ws.fetch().setId(apiId).setFailFast(true).getResultDefinition();

            Path apiConfigFolder
                    = bootConfig != null ? Paths.get(bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS))
                            : Paths.get(ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS));
            Path startNutsFile = apiConfigFolder.resolve(getExecFileName("start-nuts"));
            if (!OptionalWindows.isAvailable()) {
                return null;
            }
            WinShellHelper sl = OptionalWindows.linkBuilder()
                    .setTarget(startNutsFile.toString())
                    .setWorkingDir(System.getProperty("user.home"));

            sl.setIconIndex(148);
            Path desktopFolder = Paths.get(System.getProperty("user.home")).resolve("Desktop");
            Path menuFolder = Paths.get(System.getProperty("user.home")).resolve("AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Nuts");
            Files.createDirectories(desktopFolder);
            Files.createDirectories(menuFolder);
            if (preferredName != null) {
                if (preferredName.isEmpty()) {
                    preferredName = "nuts-cmd-%v";
                }
                preferredName = preferredName.replace("%v", apiVersion);
                if (!preferredName.endsWith(".lnk")) {
                    preferredName = preferredName + ".lnk";
                }
                if (preferredName.contains("/") || preferredName.contains("\\")) {
                    String path = preferredName;
                    sl.setPath(path);
                    sl.build();
                    return path;
                } else {
                    String path = null;
                    switch (target) {
                        case DESKTOP: {
                            if (!linkVersion) {
                                path = desktopFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                                sl.setPath(path);
                                sl.build();
                                return path;
                            } else {
                                path = desktopFolder + File.separator + "nuts-cmd.lnk";
                                sl.setPath(path);
                                sl.build();
                                return path;
                            }
                        }
                        case MENU: {
                            if (!linkVersion) {
                                path = menuFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                                sl.setPath(path);
                                sl.build();
                                return path;
                            } else {
                                path = menuFolder + File.separator + "nuts-cmd.lnk";
                                sl.setPath(path);
                                sl.build();
                                return path;
                            }
                        }
                    }
                }
            } else {
                String path = null;
                switch (target) {
                    case DESKTOP: {
                        if (!linkVersion) {
                            path = desktopFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                                sl.setPath(path);
                                sl.build();
                            return path;
                        } else {
                            path = desktopFolder + File.separator + "nuts-cmd.lnk";
                                sl.setPath(path);
                                sl.build();
                            return path;
                        }
                    }
                    case MENU: {
                        if (!linkVersion) {
                            path = menuFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                                sl.setPath(path);
                                sl.build();
                            return path;
                        } else {
                            path = menuFolder + File.separator + "nuts-cmd.lnk";
                                sl.setPath(path);
                                sl.build();
                            return path;
                        }
                    }
                }
            }

            throw new NutsIllegalArgumentException(context.getSession(), "unsupported");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public UpdatedPaths persistConfig2(NutsWorkspaceBootConfig bootConfig, NutsId apiId, String rcPath, NutsSession session) {
        String apiVersion = apiId.getVersion().toString();
        if (rcPath == null) {
            String desktopGlobalShortcutPath = configurePathShortcut(Target.DESKTOP, true, apiVersion, bootConfig, null, session);
            String desktopSpecificVersionShortcutPath = configurePathShortcut(Target.DESKTOP, false, apiVersion, bootConfig, null, session);
            String menuGlobalShortcutPath = configurePathShortcut(Target.MENU, true, apiVersion, bootConfig, null, session);
            String menuSpecificVersionShortcutPath = configurePathShortcut(Target.MENU, false, apiVersion, bootConfig, null, session);
            PrintStream out = context.getSession().out();
            NutsTextManager factory = context.getWorkspace().text();
            if (session.isTrace()) {
                out.printf("```error ATTENTION``` To run any nuts command you should use the pre-configured shell at \"%s\".%n",
                        factory.forStyled(desktopSpecificVersionShortcutPath, NutsTextStyle.path()));
            }
            return new UpdatedPaths(
                    new String[]{desktopGlobalShortcutPath, desktopSpecificVersionShortcutPath, menuGlobalShortcutPath, menuSpecificVersionShortcutPath},
                    new String[0]
            );
        } else {
            String menuSpecificVersionShortcutPath = configurePathShortcut(Target.MENU, false, apiVersion, bootConfig, rcPath, session);
            return new UpdatedPaths(
                    new String[]{menuSpecificVersionShortcutPath},
                    new String[0]
            );
        }
    }

    @Override
    public void configurePath(NutsSession session, boolean persistentConfig) {
        Path ndiAppsFolder = Paths.get(context.getAppsFolder());
        //Path ndiConfigFolder = context.getConfigFolder();
        NutsWorkspace ws = context.getWorkspace();
        NutsWorkspaceConfigManager wsconfig = ws.config();
        Path apiConfigFolder = Paths.get(ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.APPS));
        Path startNutsFile = apiConfigFolder.resolve(getExecFileName("start-nuts"));
        Path apiConfigFile = apiConfigFolder.resolve(getExecFileName(".nuts-batrc"));
        Path ndiConfigFile = ndiAppsFolder.resolve(getExecFileName(".nadmin-batrc"));
        List<String> updatedNames = new ArrayList<>();

        removeFileCommented2Lines(apiConfigFile, "net.thevpc.nuts.toolbox.ndi configuration", true);
        removeFileCommented2Lines(apiConfigFile, "net.vpc.app.nuts configuration", true);
        if (addFileLine(apiConfigFile, "net.thevpc.nuts configuration",
                getCallScriptCommand(ndiConfigFile.toString()),
                session.isYes(), null, null)) {
            updatedNames.add(apiConfigFile.getFileName().toString());
        }

        String goodNdiRc = toCommentLine("This File is generated by nuts nadmin companion tool." + CRLF)
                + toCommentLine("Do not edit it manually. All changes will be lost when nadmin runs again" + CRLF)
                + toCommentLine("This file aims to prepare bash environment against current nuts" + CRLF)
                + toCommentLine("workspace installation." + CRLF)
                + toCommentLine("" + CRLF)
                + "@ECHO OFF" + CRLF
                + "SET \"NUTS_VERSION=" + ws.getApiVersion() + "\"" + CRLF
                + "SET \"NUTS_JAR=" + ws.search()
                        .setSession(context.getSession().copy().setTrace(false))
                        .addId(ws.getApiId()).getResultPaths().required()
                + "\"" + CRLF
                + "SET \"NUTS_WORKSPACE=" + ws.locations().getWorkspaceLocation().toString() + "\"" + CRLF
                + "SET \"PATH=" + ndiAppsFolder + ";%PATH%\"" + CRLF;
        if (saveFile(ndiConfigFile, goodNdiRc, session.isYes())) {
            updatedNames.add(ndiConfigFile.getFileName().toString());
        }

        if (saveFile(startNutsFile,
                toCommentLine("This File is generated by nuts nadmin companion tool." + CRLF)
                + toCommentLine("Do not edit it manually. All changes will be lost when nadmin runs again" + CRLF)
                + toCommentLine("This file aims to run " + getExecFileName(".nuts-batrc") + " file." + CRLF)
                + toCommentLine("workspace installation." + CRLF)
                + toCommentLine("" + CRLF)
                + "@ECHO OFF" + CRLF
                + "cmd.exe /k \"" + apiConfigFile.toString() + "\"", session.isYes())) {
            updatedNames.add(startNutsFile.getFileName().toString());
        }
        if (!updatedNames.isEmpty()) {
            if (context.getSession().isPlainTrace()) {
                NutsTextBuilder formattedUpdatedNames = context.getWorkspace().text().builder();
                for (String updatedName : updatedNames) {
                    if (formattedUpdatedNames.size() > 0) {
                        formattedUpdatedNames.append(", ");
                    }
                    formattedUpdatedNames.append(updatedName, NutsTextStyle.path());
                }
                context.getSession().out().printf((context.getSession().isPlainTrace() ? "force " : "") + "updating %s to point to workspace %s%n",
                        formattedUpdatedNames,
                        ws.text().forStyled(ws.locations().getWorkspaceLocation(), NutsTextStyle.path())
                );
            }
            if (persistentConfig) {
                persistConfig(null, null, null, session);
            }
        }
    }

    public enum Target {
        MENU,
        DESKTOP
    }
}
