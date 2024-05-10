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
    public void run(NSession session) {
        session.out().println(NMsg.ofC("%s Backup Tool.", NMsg.ofStyled("Nuts", NTextStyle.keyword())));
        session.runAppCmdLine(new NCmdLineRunner() {

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                NArg a = cmdLine.next().get();
                switch (a.toString()) {
                    case "pull": {
                        runPull(cmdLine, session);
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

    public void runPull(NCmdLine cmdLine, NSession session) {
        cmdLine.forEachPeek(new NCmdLineRunner() {
            private Options options = new Options();

            @Override
            public void init(NCmdLine cmdLine, NCmdLineContext context) {
                NPath configFile = getConfigFile();
                Config config = null;
                if (configFile.isRegularFile()) {
                    try {
                        config = NElements.of(cmdLine.getSession()).parse(
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
                return session.getAppConfFolder().resolve("backup.json");
            }

            @Override
            public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                if (cmdLine.withNextEntry((v, a, s) -> {
                    options.config.setRemoteServer(v);
                }, "--server")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a, s) -> {
                    options.config.setRemoteUser(v);
                }, "--user")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a, s) -> {
                    options.config.setLocalPath(v);
                }, "--local")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a, s) -> {
                    addPath(v);
                }, "--add-path")) {
                    return true;
                } else if (cmdLine.withNextEntry((v, a, s) -> {
                    options.config.getPaths().removeIf(x -> Objects.equals(String.valueOf(x).trim(), v.trim()));
                }, "--remove-path")) {
                    return true;
                } else if (cmdLine.withNextFlag((v, a, s) -> {
                    options.config.getPaths().clear();
                }, "--clear-paths")) {
                    return true;
                } else if (cmdLine.withNextFlag((v, a, s) -> {
                    options.cmd = Cmd.SAVE;
                }, "--save")) {
                    return true;
                } else if (cmdLine.withNextFlag((v, a, s) -> {
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
                session.out().println(NMsg.ofC("Config File %s", getConfigFile()));

                switch (options.cmd) {
                    case SAVE: {
                        NElements.of(cmdLine.getSession()).setValue(config).print(getConfigFile());
                        break;
                    }
                    case SHOW: {
                        NElements.of(cmdLine.getSession()).setValue(config).println();
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
                        session.out().println(NMsg.ofC("Using local path %s", NMsg.ofStyled(config.getLocalPath(), NTextStyle.path())));
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
                NPath.of(localPath, session).getParent().mkdirs();
                session.out().println(NMsg.ofC("[%s] Backup %s from %s.",
                        NMsg.ofStyled(config.getRemoteServer(), NTextStyle.warn()),
                        NMsg.ofStyled(name, NTextStyle.keyword()),
                        NMsg.ofStyled(remotePath, NTextStyle.path())
                ));
                session.out().println(NCmdLine.of(cmd));
                NExecCmd.of(session).addCommand(cmd).failFast().run();
            }
        }, new DefaultNCmdLineContext(session));
    }
}
