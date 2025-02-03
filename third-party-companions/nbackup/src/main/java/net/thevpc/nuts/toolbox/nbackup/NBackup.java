/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package net.thevpc.nuts.toolbox.nbackup;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.Objects;

/**
 * @author vpc
 */
public class NBackup implements NApplication {

    public static void main(String[] args) {
        new NBackup().runAndExit(args);
    }

    @Override
    public void run() {
        NSession session = NSession.get().get();
        NOut.println(NMsg.ofC("%s Backup Tool.", NMsg.ofStyledKeyword("Nuts")));
        NApp.of().processCmdLine(new NCmdLineRunner() {

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                NArg a = cmdLine.next().get();
                switch (a.toString()) {
                    case "pull": {
                        runPull(cmdLine);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                //
            }
        });
    }

    public void runPull(NCmdLine cmdLine) {
        NSession session = NSession.get().get();
        cmdLine.forEachPeek(new NCmdLineRunner() {
            private Options options = new Options();

            @Override
            public void init(NCmdLine cmdLine, NCmdLineContext context) {
                NPath configFile = getConfigFile();
                Config config = null;
                if (configFile.isRegularFile()) {
                    try {
                        config = NElements.of().parse(
                                configFile, Config.class
                        );
                    } catch (Exception ex) {
                        //
                    }
                }
                if (config != null) {
                    options.config = config;
                }
            }

            private NPath getConfigFile() {
                return NApp.of().getConfFolder().resolve("backup.json");
            }

            @Override
            public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                if (cmdLine.withNextEntry((v, a) -> {
                    options.config.setRemoteServer(v);
                }, "--server")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a) -> {
                    options.config.setRemoteUser(v);
                }, "--user")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a) -> {
                    options.config.setLocalPath(v);
                }, "--local")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a) -> {
                    addPath(v);
                }, "--add-path")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a) -> {
                    options.config.getPaths().removeIf(x -> Objects.equals(String.valueOf(x).trim(), v.trim()));
                }, "--remove-path")) {
                    return true;
                } else if (cmdLine.withNextFlag((v, a) -> {
                    options.config.getPaths().clear();
                }, "--clear-paths")) {
                    return true;
                } else if (cmdLine.withNextFlag((v, a) -> {
                    options.cmd = Cmd.SAVE;
                }, "--save")) {
                    return true;
                } else if (cmdLine.withNextFlag((v, a) -> {
                    options.cmd = Cmd.SHOW;
                }, "--show")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                NArg a = cmdLine.next().get();
                addPath(a.toString());
                return true;
            }


            private void addPath(String a) {
                int i = a.indexOf('=');
                if (i > 0) {
                    options.config.getPaths().add(new DecoratedPath(a.substring(i + 1), a.substring(0, i)));
                } else {
                    options.config.getPaths().add(new DecoratedPath(a, null));
                }
            }

            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                Config config = options.config;
                if (config == null) {
                    config = new Config();
                }
                NSession session = NSession.get().get();
                NOut.println(NMsg.ofC("Config File %s", getConfigFile()));

                switch (options.cmd) {
                    case SAVE: {
                        NElements.of().setValue(config).print(getConfigFile());
                        break;
                    }
                    case SHOW: {
                        NElements.of().setValue(config).println();
                        break;
                    }
                    case RUN: {
                        if (config.getPaths().isEmpty()) {
                            cmdLine.throwMissingArgument("path");
                        }
                        if (NBlankable.isBlank(config.getRemoteUser())) {
                            cmdLine.throwMissingArgument("--user");
                        }
                        if (NBlankable.isBlank(config.getRemoteServer())) {
                            cmdLine.throwMissingArgument("--server");
                        }
                        if (NBlankable.isBlank(config.getLocalPath())) {
                            cmdLine.throwMissingArgument("--local");
                        }
                        NOut.println(NMsg.ofC("Using local path %s", NMsg.ofStyledPath(config.getLocalPath())));
                        for (DecoratedPath path : config.getPaths()) {
                            get(path, config, session);
                        }
                        break;
                    }
                }

            }

            private void get(DecoratedPath dpath, Config config, NSession session) {
                String localPath = config.getLocalPath();
                String remotePath = dpath.getPath();
                String name = dpath.getName();
                if (!remotePath.startsWith("/")) {
                    remotePath = "/home/" + config.getRemoteUser() + "/" + remotePath;
                }
                if (!remotePath.startsWith("/")) {
                    localPath += "/";
                }
                localPath += remotePath;
                String[] cmd = {
                        "rsync",
                        "-azP" + (session.isDry() ? "nv" : ""),
                        "--delete",
                        config.getRemoteUser() + "@" + config.getRemoteServer() + ":" + remotePath,
                        localPath};
                NPath.of(localPath).getParent().mkdirs();
                NOut.println(NMsg.ofC("[%s] Backup %s from %s.",
                        NMsg.ofStyledWarn(config.getRemoteServer()),
                        NMsg.ofStyledKeyword(name),
                        NMsg.ofStyledPath(remotePath)
                ));
                NOut.println(NCmdLine.of(cmd));
                NExecCmd.of().addCommand(cmd).failFast().run();
            }
        }, new DefaultNCmdLineContext(session));
    }
}
