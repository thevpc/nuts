package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.unix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.NameBuilder;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.NutsEnvInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.ReplaceString;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnyNixNdi extends BaseSystemNdi {
    public static final Pattern UNIX_USER_DIRS_PATTERN = Pattern.compile("^\\s*(?<k>[A-Z_]+)\\s*=\\s*(?<v>.*)$");
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/sh", "#!.*");

    public AnyNixNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public String getBashrcName() {
        return ".bashrc";
    }

    public PathInfo[] createCallNutsRC(String fileName, NutsEnvInfo env) {
        Path sysrcFile = null;
        if (fileName == null) {
            sysrcFile = getSysRC(env).path();
        } else {
            sysrcFile = Paths.get(NameBuilder.id(env.getNutsApiId(), fileName,null, env.getNutsApiDef().getDescriptor(), context.getSession()).buildName());
        }
        Path nutsRcPath = getNutsInit(env).path();
        //old configs
        sysrcFile = sysrcFile.toAbsolutePath();
        return new PathInfo[]{
                addFileLine(NdiScriptInfoType.NUTS,
                        env.getNutsApiId(),
                        sysrcFile,
                        getCommentLineConfigHeader(),
                        getCallScriptCommand(nutsRcPath.toString()),
                        getShebanSh())
        };
    }

    public NdiScriptInfo getSysRC(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return Paths.get(System.getProperty("user.home")).resolve(getBashrcName());
            }

            @Override
            public PathInfo create() {
                Path apiConfigFile = path();
                return addFileLine(NdiScriptInfoType.SYS_RC,
                        env.getNutsApiId(),
                        apiConfigFile, getCommentLineConfigHeader(),
                        getCallScriptCommand(getNutsInit(env).path().toString()),
                        getShebanSh());
            }
        };
    }



    //    @Override
    public boolean isComments(String line) {
        line = line.trim();
        return line.startsWith("#");
    }

    public String trimComments(String line) {
        line = line.trim();
        if (line.startsWith("#")) {
            while (line.startsWith("#")) {
                line = line.substring(1);
            }
            return line.trim();
        }
        throw new IllegalArgumentException("Not a comment: " + line);
    }

    @Override
    public String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" $NUTS_OPTIONS ");
        if (options.getExecType() != null) {
            command.append("--").append(options.getExecType().id());
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" \"$@\"");
        return command.toString();
    }

    public void onPostGlobal(NutsEnvInfo env, PathInfo[] updatedPaths) {
        NutsTextManager factory = context.getWorkspace().text();
        if (Arrays.stream(updatedPaths).anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED) && context.getSession().isTrace()) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().resetLine().printf("%s %s to point to workspace %s%n",
                        context.getSession().isYes() ?
                                factory.forStyled("force updating", NutsTextStyle.warn().append(NutsTextStyle.underlined())) :
                                factory.forStyled("force updating", NutsTextStyle.warn())
                        ,
                        factory.builder().appendJoined(", ",
                                Arrays.stream(updatedPaths).map(x ->
                                        factory.forStyled(x.getPath().getFileName().toString(), NutsTextStyle.path())).collect(Collectors.toList())),
                        factory.forStyled(context.getSession().getWorkspace().locations().getWorkspaceLocation(), NutsTextStyle.path())
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

    public String toCommentLine(String line) {
        return "# " + line;
    }

    @Override
    protected String getSetVarCommand(String name, String value) {
        return name +"=\"" + value + "\"";
    }

    @Override
    protected String getSetVarStaticCommand(String name, String value) {
        return name + "='" + value + "'";
    }

//    public PathInfo[] persistConfigGlobal(NutsEnvInfo env, boolean createDesktop, boolean createMenu) {
//        List<PathInfo> updatedPaths = new ArrayList<>();
//        if (createDesktop) {
//            updatedPaths.addAll(Arrays.asList(createLaunchTermShortcutGlobal(AppShortcutTarget.DESKTOP, env)));
//        }
//        if (createMenu) {
//            updatedPaths.addAll(Arrays.asList(createLaunchTermShortcutGlobal(AppShortcutTarget.MENU, env)));
//        }
//        updatedPaths.addAll(Arrays.asList(createSysRC(env)));
//        updatedPaths.addAll(Arrays.asList(createNutsEnv(env)));
//        updatedPaths.addAll(Arrays.asList(createNutsRC(env)));
//
//        return updatedPaths.toArray(new PathInfo[0]);
//    }

//    public PathInfo[] persistConfigSpecial(String name, String fileName, NutsEnvInfo env, boolean createDesktop, boolean createMenu, boolean createShortcut) {
//        if (name == null) {
//            name = "Nuts Terminal " + env.getNutsApiVersion();
//        }
//        if (fileName == null) {
//            fileName = env.getNutsApiId().getLongName().replace(':', '-').replace('#', '-');
//        }
//        List<PathInfo> updatedPaths = new ArrayList<>();
//        if (createDesktop) {
//            updatedPaths.addAll(Arrays.asList(createLaunchTermShortcut(AppShortcutTarget.DESKTOP, env, name, fileName)));
//        }
//        if (createMenu) {
//            updatedPaths.addAll(Arrays.asList(createLaunchTermShortcut(AppShortcutTarget.MENU, env, name, fileName)));
//        }
//        if (createShortcut) {
//            updatedPaths.addAll(Arrays.asList(createLaunchTermShortcut(AppShortcutTarget.SHORTCUT, env, name, fileName)));
//        }
//        updatedPaths.addAll(Arrays.asList(createNutsRC(env, true)));
////        configurePathShortcut(AppShortcutTarget.DESKTOP, true,nutsId.getVersion().toString(),bootConfig,fileName,session,updatedPaths,discardedPaths);
////        configurePathShortcut(AppShortcutTarget.MENU, true,nutsId.getVersion().toString(),bootConfig,fileName,session,updatedPaths,discardedPaths);
//        return updatedPaths.toArray(new PathInfo[0]);
//    }

    @Override
    protected String getCallScriptCommand(String path, String... args) {
        return "source \"" + path + "\" " + Arrays.stream(args).map(a -> dblQte(a)).collect(Collectors.joining(" "));
    }

    @Override
    public String getExecFileName(String name) {
        return name;
    }

    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new UnixFreeDesktopEntryWriter(context.getSession(), getOsDesktopPath());
    }

    public PathInfo[] createLaunchTermShortcut(AppShortcutTarget appShortcutTarget,
                                               NutsEnvInfo env,
                                               String name,
                                               String fileName
    ) {
//        String termCommand = "konsole";
//        Path homerc = getSysRCPath();
//        Path nrc = getNutsRcPath(env);
        String cmd = getNutsTerm(env).path().toString();
        //remove aby path
        fileName = NameBuilder.id(env.getNutsApiId(), fileName,name, env.getNutsApiDef().getDescriptor(), context.getSession())
                .buildName();
        if (name == null) {
            name = NameBuilder.label(env.getNutsApiId(), "Nuts Terminal%s%v%s%h",null, env.getNutsApiDef().getDescriptor(), context.getSession())
                    .buildName();
        }
        return createShortcut(appShortcutTarget,
                env.getNutsApiId(),
                fileName,
                FreeDesktopEntry.Group.desktopEntry(name, cmd, System.getProperty("user.home"))
                        .setIcon(resolveIcon(null, env.getNutsApiId()))
                        .setStartNotify(true)
                        .addCategory("/Utilities/Nuts")
                        .setGenericName(env.getNutsApiDef().getDescriptor().getGenericName())
                        .setComment(env.getNutsApiDef().getDescriptor().getDescription())
                        .setTerminal(true)
        );
    }

    protected ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    protected ReplaceString getCommentLineConfigHeader() {
        return COMMENT_LINE_CONFIG_HEADER;
    }

    @Override
    protected String getTemplateName(String name) {
        return "linux_template_" + name + ".text";
    }


//    private String simplifyPath(PathInfo x) {
//        Path p = x.getPath();
//        return null;
//    }

    //    @Override
    public String varRef(String v) {
        return "${" + v + "}";
    }

    public String getPathVarSep() {
        return ":";
    }

    private Path getOsDesktopPath() {
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
        return new File(System.getProperty("user.home"), "Desktop").toPath();
    }


    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            case "svg":
                return 10;
            case "png":
                return 8;
            case "jpg":
                return 6;
            case "jpeg":
                return 5;
            case "gif":
                return 4;
            case "ico":
                return 3;
        }
        return -1;
    }
}
