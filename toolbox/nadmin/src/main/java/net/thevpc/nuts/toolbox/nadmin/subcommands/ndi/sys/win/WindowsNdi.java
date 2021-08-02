package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.win;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.OptionalWindows;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.WinShellHelper;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.WindowFreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.AppShortcutTarget;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.BaseSystemNdi;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

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

    public PathInfo[] persistConfig2(String switchWorkspaceLocation, NutsId apiId, String rcPath) {
        String apiVersion = apiId.getVersion().toString();
        List<PathInfo> result = new ArrayList<>();
        PathInfo[] desktopSpecificVersionShortcutPath;
        if (rcPath == null) {
            result.addAll(Arrays.asList(desktopSpecificVersionShortcutPath = configurePathShortcut(AppShortcutTarget.DESKTOP, true, apiVersion, switchWorkspaceLocation, "nuts-cmd-%v")));
            result.addAll(Arrays.asList(configurePathShortcut(AppShortcutTarget.DESKTOP, false, apiVersion, switchWorkspaceLocation, "nuts-cmd-%v")));
            result.addAll(Arrays.asList(configurePathShortcut(AppShortcutTarget.MENU, true, apiVersion, switchWorkspaceLocation, "nuts-cmd-%v")));
            result.addAll(Arrays.asList(configurePathShortcut(AppShortcutTarget.MENU, false, apiVersion, switchWorkspaceLocation, "nuts-cmd-%v")));
            NutsPrintStream out = context.getSession().out();
            NutsTextManager factory = context.getWorkspace().text();
            if (context.getSession().isPlainTrace()) {
                out.printf("```error ATTENTION``` To run any nuts command you should use the pre-configured shell at \"%s\".%n",
                        desktopSpecificVersionShortcutPath[0].getPath()
                );
            }
            return result.toArray(new PathInfo[0]);
        } else {
            return configurePathShortcut(AppShortcutTarget.MENU, false, apiVersion, switchWorkspaceLocation, rcPath);
        }
    }

    @Override
    public String toCommentLine(String line) {
        return ":: " + line;
    }

    @Override
    public boolean isComments(String line) {
        return line.startsWith(":: ");
    }

    @Override
    public String trimComments(String line) {
        if(line.startsWith(":: ")){
            return line.substring(2).trim();
        }
        throw new IllegalArgumentException("not a comment");
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

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new WindowFreeDesktopEntryWriter(getOsDesktopPath(), context.getSession());
    }


    public Path getNutsStartScriptPath(String apiVersion, String switchWorkspaceLocation) {
        return getNutsAppsFolder(apiVersion, switchWorkspaceLocation).resolve(getExecFileName("start-nuts"));
    }

    public PathInfo[] configurePathShortcut(AppShortcutTarget appShortcutTarget, boolean linkVersion, String apiVersion, String switchWorkspaceLocation, String preferredName) {
        try {
            if (preferredName == null || preferredName.isEmpty()) {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("missing invalid name"));
            }
            Path startNutsFile = getNutsStartScriptPath(apiVersion, switchWorkspaceLocation);
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
                    PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                    ;
                    sl.build();
                    return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, Paths.get(path), ss)};
                } else {
                    String path = null;
                    switch (appShortcutTarget) {
                        case DESKTOP: {
                            if (!linkVersion) {
                                path = desktopFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                                sl.setPath(path);
                                PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                                ;
                                sl.build();
                                return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, Paths.get(path), ss)};
                            } else {
                                path = desktopFolder + File.separator + "nuts-cmd.lnk";
                                sl.setPath(path);
                                PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                                ;
                                sl.build();
                                return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, Paths.get(path), ss)};
                            }
                        }
                        case MENU: {
                            if (!linkVersion) {
                                path = menuFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                                sl.setPath(path);
                                PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                                ;
                                sl.build();
                                return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU, Paths.get(path), ss)};
                            } else {
                                path = menuFolder + File.separator + "nuts-cmd.lnk";
                                sl.setPath(path);
                                PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                                ;
                                sl.build();
                                return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU, Paths.get(path), ss)};
                            }
                        }
                    }
                }
            } else {
                String path = null;
                switch (appShortcutTarget) {
                    case DESKTOP: {
                        if (!linkVersion) {
                            path = desktopFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                            sl.setPath(path);
                            PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                            ;
                            sl.build();
                            return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, Paths.get(path), ss)};
                        } else {
                            path = desktopFolder + File.separator + "nuts-cmd.lnk";
                            sl.setPath(path);
                            sl.build();
                            return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_SHORTCUT, Paths.get(path), PathInfo.Status.OVERRIDDEN)};
                        }
                    }
                    case MENU: {
                        if (!linkVersion) {
                            path = menuFolder + File.separator + "nuts-cmd-" + apiVersion + ".lnk";
                            sl.setPath(path);
                            PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                            ;
                            sl.build();
                            return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU, Paths.get(path), PathInfo.Status.OVERRIDDEN)};
                        } else {
                            path = menuFolder + File.separator + "nuts-cmd.lnk";
                            sl.setPath(path);
                            PathInfo.Status ss = Files.isRegularFile(Paths.get(path)) ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED;
                            ;
                            sl.build();
                            return new PathInfo[]{new PathInfo(NdiScriptInfo.Type.DESKTOP_MENU, Paths.get(path), PathInfo.Status.OVERRIDDEN)};
                        }
                    }
                }
            }

            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unsupported"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    public PathInfo createNutsRC(boolean force) {
        Path apiConfigFile = getNutsRcPath(null, null);
        final NutsWorkspace ws = context.getWorkspace();
        String NUTS_JAR_PATH = ws.search()
                .setSession(context.getSession().copy().setTrace(false))
                .addId(ws.getApiId()).getResultPaths().required();

        /**
         * "#!/bin/sh\n" +
         *                         "# This File is generated by nuts nadmin companion tool.\n" +
         *                         "# Do not edit it manually. All changes will be lost when nadmin runs again\n" +
         *                         "# This file aims to prepare bash environment against current nuts\n" +
         *                         "# workspace installation.\n" +
         *                         "#\n"
         */
        StringBuilder goodNdiRc = new StringBuilder();
        goodNdiRc.append("@ECHO OFF").append(CRLF);
        TreeSet<String> exports = new TreeSet<>();
        exports.addAll(Arrays.asList("NUTS_VERSION", "NUTS_WORKSPACE", "NUTS_JAR", "PATH"));
        goodNdiRc.append("SET \"NUTS_VERSION=" + ws.getApiVersion()).append( "\"").append(CRLF);
        goodNdiRc.append("SET \"NUTS_WORKSPACE=" + ws.locations().getWorkspaceLocation().toString()).append( "\"").append(CRLF);
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            goodNdiRc.append("SET \"NUTS_WORKSPACE_" + value + "=" + ws.locations().getStoreLocation(value)).append( "\"").append(CRLF);
            exports.add("NUTS_WORKSPACE_" + value);
        }
        if (NUTS_JAR_PATH.startsWith(ws.locations().getStoreLocation(NutsStoreLocation.LIB))) {
            goodNdiRc.append("SET \"NUTS_JAR=${NUTS_WORKSPACE_LIB}" + NUTS_JAR_PATH.toString().substring(
                    ws.locations().getStoreLocation(NutsStoreLocation.LIB).length()
            )).append( "\"").append(CRLF);
        } else {
            goodNdiRc.append("SET \"NUTS_JAR=" + NUTS_JAR_PATH).append( "\"").append(CRLF);
        }

        goodNdiRc.append("SET \"PATH=${NUTS_WORKSPACE_APPS}" + getNadminAppsFolder().toString().substring(
                ws.locations().getStoreLocation(NutsStoreLocation.APPS).length()
        ) + ":${PATH}").append( "\"").append(CRLF);

        goodNdiRc.append("export " + String.join(" ", exports) + " \n");

        return addFileLine(NdiScriptInfo.Type.NUTS_RC, apiConfigFile, COMMENT_LINE_CONFIG_HEADER,
                goodNdiRc.toString(),
                force, null);
    }

    @Override
    public void configurePath(boolean persistentConfig) {
        Path ndiAppsFolder = Paths.get(context.getAppsFolder());
        //Path nadminConfigFolder = context.getConfigFolder();
        NutsWorkspace ws = context.getWorkspace();
        NutsWorkspaceConfigManager wsconfig = ws.config();
        Path apiConfigFolder = Paths.get(ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.APPS));
        Path startNutsFile = apiConfigFolder.resolve(getExecFileName("start-nuts"));
        Path apiConfigFile = apiConfigFolder.resolve(getExecFileName(".nuts-batrc"));
        Path nadminConfigFile = ndiAppsFolder.resolve(getExecFileName(".nadmin-batrc"));
        List<String> updatedNames = new ArrayList<>();

        PathInfo rr = addFileLine(NdiScriptInfo.Type.NUTS_WITH_ENV, apiConfigFile, COMMENT_LINE_CONFIG_HEADER,
                getCallScriptCommand(nadminConfigFile.toString()),
                context.getSession().isYes(), null);

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
        if (saveFile(nadminConfigFile, goodNdiRc, context.getSession().isYes())) {
            updatedNames.add(nadminConfigFile.getFileName().toString());
        }

        if (saveFile(startNutsFile,
                toCommentLine("This File is generated by nuts nadmin companion tool." + CRLF)
                        + toCommentLine("Do not edit it manually. All changes will be lost when nadmin runs again" + CRLF)
                        + toCommentLine("This file aims to run " + getExecFileName(".nuts-batrc") + " file." + CRLF)
                        + toCommentLine("workspace installation." + CRLF)
                        + toCommentLine("" + CRLF)
                        + "@ECHO OFF" + CRLF
                        + "cmd.exe /k \"" + apiConfigFile.toString() + "\"", context.getSession().isYes())) {
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
                context.getSession().out().resetLine().printf((context.getSession().isPlainTrace() ? "force " : "") + "updating %s to point to workspace %s%n",
                        formattedUpdatedNames,
                        ws.text().forStyled(ws.locations().getWorkspaceLocation(), NutsTextStyle.path())
                );
            }
            if (persistentConfig) {
                persistConfig(null, null, null);
            }
        }
    }


    private Path getOsDesktopPath() {
        return new File(System.getProperty("user.home"), "Desktop").toPath();
    }

}
