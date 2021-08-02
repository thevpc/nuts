package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.WorkspaceAndApiVersion;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.unix.UnixFreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.ReplaceString;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnyNixNdi extends BaseSystemNdi {
    public static final Pattern UNIX_USER_DIRS_PATTERN = Pattern.compile("^\\s*(?<k>[A-Z_]+)\\s*=\\s*(?<v>.*)$");
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/sh", "#!.*");

    public AnyNixNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    //    @Override
    public boolean isComments(String line) {
        line = line.trim();
        return line.startsWith("#");
    }

    //    @Override
    public String trimComments(String line) {
        line = line.trim();
        if (line.startsWith("#")) {
            while (line.startsWith("#")) {
                line = line.substring(1);
            }
            return line;
        }
        throw new IllegalArgumentException("Not a comment: " + line);
    }

    @Override
    public String createNutsScriptCommand(NutsId fnutsId, NdiScriptOptions options) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" $NUTS_OPTIONS ");
        if (options.getExecType() != null) {
            command.append("--").append(options.getExecType().id());
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" \"$@\"");
        return command.toString();
    }

    public PathInfo[] persistConfig2(String switchWorkspaceLocation,
                                     NutsId nutsId,
                                     String fileName) {
        List<PathInfo> updatedPaths = new ArrayList<>();
        if (fileName == null) {
            fileName = getSysRCPath().toString();
        }
        updatedPaths.addAll(Arrays.asList(persistConfig2_persistCallRC(switchWorkspaceLocation, nutsId, fileName)));
//        configurePathShortcut(AppShortcutTarget.DESKTOP, true,nutsId.getVersion().toString(),bootConfig,fileName,session,updatedPaths,discardedPaths);
//        configurePathShortcut(AppShortcutTarget.MENU, true,nutsId.getVersion().toString(),bootConfig,fileName,session,updatedPaths,discardedPaths);
        return updatedPaths.toArray(new PathInfo[0]);
    }

    public String toCommentLine(String line) {
        return "# " + line;
    }

    @Override
    protected String getCallScriptCommand(String path) {
        return "source \"" + path + "\"";
    }

    @Override
    public String getExecFileName(String name) {
        return name;
    }

    @Override
    protected String getTemplateBodyName() {
        return "linux_template_body.text";
    }

    @Override
    protected String getTemplateNutsName() {
        return "linux_template_nuts.text";
    }

    @NotNull
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new UnixFreeDesktopEntryWriter(context.getSession(), getOsDesktopPath());
    }

    public String getBashrcName() {
        return ".bashrc";
    }

    public Path getSysRCPath() {
        return Paths.get(System.getProperty("user.home")).resolve(getBashrcName());
    }

    public PathInfo[] persistConfig2_persistCallRC(String switchWorkspaceLocation,
                                                   NutsId nutsId,
                                                   String fileName) {
        Path sysrcFile = null;
        if (fileName == null) {
            throw new IllegalArgumentException("missing fileName");
        } else {
            if (fileName.contains("%v")) {
                fileName = fileName.replace("%v", nutsId.getVersion().toString());
            }
            sysrcFile = Paths.get(fileName);
        }
        Path apiConfigFile = getNutsRcPath(null, switchWorkspaceLocation);
        boolean force = context.getSession().isYes();
        //old configs
        sysrcFile = sysrcFile.toAbsolutePath();
        return new PathInfo[]{
                addFileLine(NdiScriptInfo.Type.NUTS_WITHOUT_ENV, sysrcFile,
                        COMMENT_LINE_CONFIG_HEADER,
                        getCallScriptCommand(apiConfigFile.toString()),
                        force, SHEBAN_SH)
        };
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
        TreeSet<String> exports = new TreeSet<>();
        exports.addAll(Arrays.asList("NUTS_VERSION", "NUTS_WORKSPACE", "NUTS_JAR", "PATH"));
        goodNdiRc.append("NUTS_VERSION='" + ws.getApiVersion() + "'\n");
        goodNdiRc.append("NUTS_WORKSPACE='" + ws.locations().getWorkspaceLocation().toString() + "'\n");
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            goodNdiRc.append("NUTS_WORKSPACE_" + value + "='" + ws.locations().getStoreLocation(value) + "'\n");
            exports.add("NUTS_WORKSPACE_" + value);
        }
        if (NUTS_JAR_PATH.startsWith(ws.locations().getStoreLocation(NutsStoreLocation.LIB))) {
            goodNdiRc.append("NUTS_JAR=\"${NUTS_WORKSPACE_LIB}" + NUTS_JAR_PATH.toString().substring(
                    ws.locations().getStoreLocation(NutsStoreLocation.LIB).length()
            ) + "\"\n");
        } else {
            goodNdiRc.append("NUTS_JAR='" + NUTS_JAR_PATH + "'\n");
        }

        goodNdiRc.append("PATH=\"${NUTS_WORKSPACE_APPS}" + getNadminAppsFolder().toString().substring(
                ws.locations().getStoreLocation(NutsStoreLocation.APPS).length()
        ) + ":${PATH}\"\n");

        goodNdiRc.append("export " + String.join(" ", exports) + " \n");

        return addFileLine(NdiScriptInfo.Type.NUTS_RC, apiConfigFile, COMMENT_LINE_CONFIG_HEADER,
                goodNdiRc.toString(),
                force, SHEBAN_SH);
    }

    @Override
    public void configurePath(boolean persistentConfig) {
//        Path ndiAppsFolder = Paths.get(context.getAppsFolder());
        final NutsWorkspace ws = context.getWorkspace();
//        Path apiAppsFolder = Paths.get(ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.APPS));
//        Path apiConfigFile = getNutsApiAppsFolder().resolve(getExecFileName(".nuts-bashrc"));
//        Path apiConfigFile = getNutsRcPath(nll,);
//        Path nadminConfigFile = ndiAppsFolder.resolve(getExecFileName(".nadmin-bashrc"));
//        Path nadminConfigFile = apiAppsFolder.resolve(getExecFileName(".nuts-env"));
        List<PathInfo> updatedPaths = new ArrayList<>();
//        List<String> updatedNames = new ArrayList<>();
        boolean force = context.getSession().isYes();

        if (persistentConfig) {
            WorkspaceAndApiVersion r = persistConfig(null, null, null);
            updatedPaths.addAll(r.getUpdatedPaths());
        }

        updatedPaths.add(createNutsRC(true));

        NutsTextManager factory = context.getWorkspace().text();

        if (updatedPaths.stream().anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED) && context.getSession().isTrace()) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().resetLine().printf("%s %s to point to workspace %s%n",
                        context.getSession().isYes() ?
                                factory.forStyled("force updating", NutsTextStyle.warn().append(NutsTextStyle.underlined())) :
                                factory.forStyled("force updating", NutsTextStyle.warn())
                        ,
                        factory.builder().appendJoined(", ",
                                updatedPaths.stream().map(x ->
                                        factory.forStyled(x.getPath().getFileName().toString(), NutsTextStyle.path())).collect(Collectors.toList())),
                        factory.forStyled(ws.locations().getWorkspaceLocation(), NutsTextStyle.path())
                );
            }
            context.getSession().getTerminal().ask()
                    .resetLine()
                    .forBoolean(
                            "```error ATTENTION``` You may need to re-run terminal or issue \"%s\" in your current terminal for new environment to take effect.%n"
                                    + "Please type 'ok' if you agree, 'why' if you need more explanation or 'cancel' to cancel updates.",
                            factory.forStyled(". ~/" + getBashrcName(), NutsTextStyle.path())
                    )
                    .setHintMessage("")
                    .setSession(context.getSession())
                    .setParser(new NutsQuestionParser<Boolean>() {
                        @Override
                        public Boolean parse(Object response, Boolean defaultValue, NutsQuestion<Boolean> question) {
                            if (response instanceof Boolean) {
                                return (Boolean) response;
                            }
                            if (response == null || ((response instanceof String) && response.toString().length() == 0)) {
                                response = defaultValue;
                            }
                            if (response == null) {
                                throw new NutsValidationException(context.getSession(), NutsMessage.cstyle("sorry... but you need to type 'ok', 'why' or 'cancel'"));
                            }
                            String r = response.toString();
                            if ("ok".equalsIgnoreCase(r)) {
                                return true;
                            }
                            if ("why".equalsIgnoreCase(r)) {
                                NutsPrintStream out = context.getSession().out();
                                out.resetLine();
                                out.printf("\\\"%s\\\" is a special file in your home that is invoked upon each interactive terminal launch.%n", factory.forStyled(getBashrcName(), NutsTextStyle.path()));
                                out.print("It helps configuring environment variables. ```sh nuts``` make usage of such facility to update your **PATH** env variable\n");
                                out.print("to point to current ```sh nuts``` workspace, so that when you call a ```sh nuts``` command it will be resolved correctly...\n");
                                out.printf("However updating \\\"%s\\\" does not affect the running process/terminal. So you have basically two choices :%n", factory.forStyled(getBashrcName(), NutsTextStyle.path()));
                                out.print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)%n");
                                out.printf(" - Or to run by your self the \\\"%s\\\" script (don\\'t forget the leading dot)%n", factory.forStyled(". ~/" + getBashrcName(), NutsTextStyle.path()));
                                throw new NutsValidationException(context.getSession(), NutsMessage.cstyle("Try again..."));
                            } else if ("cancel".equalsIgnoreCase(r) || "cancel!".equalsIgnoreCase(r)) {
                                throw new NutsUserCancelException(context.getSession());
                            } else {
                                throw new NutsValidationException(context.getSession(), NutsMessage.cstyle("sorry... but you need to type 'ok', 'why' or 'cancel'"));
                            }
                        }
                    })
                    .getValue();

        }
    }

//    private String simplifyPath(PathInfo x) {
//        Path p = x.getPath();
//        return null;
//    }

    private Path getOsDesktopPath() {
        switch (context.getWorkspace().env().getOsFamily()) {
            case UNIX:
            case LINUX:
            case MACOS: {
                File f = new File(System.getProperty("user.home"), ".config/user-dirs.dirs");
                if (f.exists()) {
                    try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (line.startsWith("#")) {
                                //ignore
                            } else {
                                Matcher m = UNIX_USER_DIRS_PATTERN.matcher(line);
                                if (m.find()) {
                                    String k = m.group("k");
                                    if (k.equals("XDG_DESKTOP_DIR")) {
                                        String v = m.group("v");
                                        v = v.trim();
                                        if (v.startsWith("\"")) {
                                            int last = v.indexOf('\"', 1);
                                            String s = v.substring(1, last);
                                            s = s.replace("$HOME", System.getProperty("user.home"));
                                            return Paths.get(s);
                                        } else {
                                            return Paths.get(v);
                                        }
                                    }
                                } else {
                                    //this is unexpected format!
                                    break;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }
        }
        return new File(System.getProperty("user.home"), "Desktop").toPath();
    }

}
