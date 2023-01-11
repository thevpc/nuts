/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package net.thevpc.nuts.toolbox.nbackup;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.cmdline.NCommandLineContext;
import net.thevpc.nuts.cmdline.NCommandLineProcessor;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTextStyle;

import java.util.Objects;

/**
 * @author vpc
 */
public class NBackup implements NApplication {

    public static void main(String[] args) {
        new NBackup().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext applicationContext) {
        NSession session = applicationContext.getSession();
        session.out().println(NMsg.ofC("%s Backup Tool.", NMsg.ofStyled("Nuts", NTextStyle.keyword())));
        applicationContext.processCommandLine(new NCommandLineProcessor() {

            @Override
            public boolean onCmdNextNonOption(NArg nonOption, NCommandLine commandLine, NCommandLineContext context) {
                NArg a = commandLine.next().get();
                switch (a.toString()) {
                    case "pull": {
                        runPull(commandLine, applicationContext);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onCmdExec(NCommandLine commandLine, NCommandLineContext context) {
                //
            }
        });
    }

    public void runPull(NCommandLine commandLine, NApplicationContext applicationContext) {
        commandLine.process(new NCommandLineProcessor() {
            private Options options = new Options();

            @Override
            public void onCmdInitParsing(NCommandLine commandLine, NCommandLineContext context) {
                NPath configFile = getConfigFile();
                Config config = null;
                if (configFile.isFile()) {
                    try {
                        config = NElements.of(commandLine.getSession()).parse(
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
                return applicationContext.getConfigFolder().resolve("backup.json");
            }

            @Override
            public boolean onCmdNextOption(NArg option, NCommandLine commandLine, NCommandLineContext context) {
                if (commandLine.withNextString((v, a, s) -> {
                    options.config.setRemoteServer(v);
                }, "--server")) {
                    return true;
                } else if (commandLine.withNextString((v, a, s) -> {
                    options.config.setRemoteUser(v);
                }, "--user")) {
                    return true;
                } else if (commandLine.withNextString((v, a, s) -> {
                    options.config.setLocalPath(v);
                }, "--local")) {
                    return true;
                } else if (commandLine.withNextString((v, a, s) -> {
                    addPath(v);
                }, "--add-path")) {
                    return true;
                } else if (commandLine.withNextString((v, a, s) -> {
                    options.config.getPaths().removeIf(x -> Objects.equals(String.valueOf(x).trim(), v.trim()));
                }, "--remove-path")) {
                    return true;
                } else if (commandLine.withNextBoolean((v, a, s) -> {
                    options.config.getPaths().clear();
                }, "--clear-paths")) {
                    return true;
                } else if (commandLine.withNextBoolean((v, a, s) -> {
                    options.cmd = Cmd.SAVE;
                }, "--save")) {
                    return true;
                } else if (commandLine.withNextBoolean((v, a, s) -> {
                    options.cmd = Cmd.SHOW;
                }, "--show")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean onCmdNextNonOption(NArg nonOption, NCommandLine commandLine, NCommandLineContext context) {
                NArg a = commandLine.next().get();
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
            public void onCmdExec(NCommandLine commandLine, NCommandLineContext context) {
                Config config = options.config;
                if (config == null) {
                    config = new Config();
                }
                NSession session = applicationContext.getSession();
                session.out().println(NMsg.ofC("Config File %s", getConfigFile()));

                switch (options.cmd) {
                    case SAVE: {
                        NElements.of(commandLine.getSession()).setValue(config).print(getConfigFile());
                        break;
                    }
                    case SHOW: {
                        NElements.of(commandLine.getSession()).setValue(config).println();
                        break;
                    }
                    case RUN: {
                        if (config.getPaths().isEmpty()) {
                            commandLine.throwMissingArgumentByName("path");
                        }
                        if (NBlankable.isBlank(config.getRemoteUser())) {
                            commandLine.throwMissingArgumentByName("--user");
                        }
                        if (NBlankable.isBlank(config.getRemoteServer())) {
                            commandLine.throwMissingArgumentByName("--server");
                        }
                        if (NBlankable.isBlank(config.getLocalPath())) {
                            commandLine.throwMissingArgumentByName("--local");
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
                session.out().println(NCommandLine.of(cmd));
                NExecCommand.of(session).addCommand(cmd).setFailFast(true).run();
            }
        }, applicationContext);
    }
}
