/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.toolbox.derby;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vpc
 */
public class DerbyMain extends NutsApplication {

    private Path derbyBinHome = Paths.get(".");
    private String derbyVersion = null;
    private Path derbyDataHome = null;
    private Command cmd = Command.start;
    private String host = null;
    private int port = -1;
    private SSLMode sslmode = null;
    private String extraArg = null;
    private NutsApplicationContext appContext;

    public enum SSLMode {
        off, basic, peerAuthentication
    }

    public enum Command {
        start, shutdown, sysinfo, help, ping, trace, runtimeinfo, maxthreads, timeslice, logconnections
    }

    public static void main(String[] args) {
        new DerbyMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        this.appContext = appContext;
        NutsWorkspace ws=appContext.getWorkspace();
        NutsCommand cmdLine = appContext.commandLine();
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.nextString("--derby-version")) != null) {
                derbyVersion = a.getValue().getString();
            } else if ((a = cmdLine.nextString("--db")) != null) {
                derbyDataHome = ws.io().path(getAbsoluteFile(a.getValue().getString(), appContext.getVarFolder().toString()));
            } else if ((a = cmdLine.nextString("--netbeans")) != null) {
                derbyDataHome = ws.io().path(System.getProperty("user.home") + "/.netbeans-derby");
            } else if ((a = cmdLine.nextString("-h", "--host")) != null) {
                host = a.getValue().getString();
            } else if ((a = cmdLine.nextString("-p", "--port")) != null) {
                port = a.getValue().getInt();
            } else if ((a = cmdLine.nextString("-ssl", "--ssl")) != null) {
                sslmode = SSLMode.valueOf(a.getValue().getString());
            } else if ((a = cmdLine.next("start")) != null) {
                cmd = Command.start;
            } else if ((a = cmdLine.next("sys", "sysinfo")) != null) {
                cmd = Command.sysinfo;
            } else if ((a = cmdLine.next("ping")) != null) {
                cmd = Command.ping;
            } else if ((a = cmdLine.next("rt", "runtime")) != null) {
                cmd = Command.runtimeinfo;
            } else if ((a = cmdLine.nextString("trace")) != null) {
                cmd = Command.trace;
                extraArg = a.getValue().getString();
            } else if ((a = cmdLine.nextString("trace-directory")) != null) {
                cmd = Command.trace;
                extraArg = a.getValue().getString();
            } else if ((a = cmdLine.nextString("max-threads")) != null) {
                cmd = Command.maxthreads;
                extraArg = a.getValue().getString();
            } else if ((a = cmdLine.nextString("time-slice")) != null) {
                cmd = Command.timeslice;
                extraArg = a.getValue().getString();
            } else if ((a = cmdLine.nextString("log-connections")) != null) {
                cmd = Command.logconnections;
                extraArg = a.getValue().getString();
            } else if ((a = cmdLine.next("stop", "shutdown")) != null) {
                cmd = Command.shutdown;
            } else {
                cmdLine.setCommandName("derby").unexpectedArgument();
            }
        }
        List<String> command = new ArrayList<>();
        List<String> executorOptions = new ArrayList<>();
        derbyBinHome = ws.config().getStoreLocation(resolveNutsId(), NutsStoreLocation.PROGRAMS).resolve("lib");
        String v = derbyVersion;
        Path h = derbyDataHome;
        if (v == null) {
            NutsId best = ws.search().addId("org.apache.derby:derbynet").duplicates(false).latest().getResultIds().singleton();
            v = best.getVersion().toString();
        }
        if (h == null) {
            h = appContext.getVarFolder().resolve("derby-db");
        }
        Path derby = download("org.apache.derby:derby#" + v);
        Path derbynet = download("org.apache.derby:derbynet#" + v);
        Path derbyoptionaltools = download("org.apache.derby:derbyoptionaltools#" + v);
        Path derbyclient = download("org.apache.derby:derbyclient#" + v);
        Path derbytools = download("org.apache.derby:derbytools#" + v);
        //use named jar because derby does test upon jar names at runtime (what a shame !!!)
        command.add("org.apache.derby:derbytools#" + v);
        executorOptions.add(
                "--classpath=" + derby + ":" + derbynet + ":" + derbyclient + ":" + derbytools + ":" + derbyoptionaltools
        );
        if (appContext.isVerbose()) {
            executorOptions.add("--show-command");
        }
        executorOptions.add("--main-class=org.apache.derby.drda.NetworkServerControl");
        executorOptions.add("-Dderby.system.home=" + h);

        if (host != null) {
            command.add("-h");
            command.add(host);
        }
        if (port != -1) {
            command.add("-p");
            command.add(String.valueOf(port));
        }
        if (sslmode != null) {
            command.add("-ssl");
            command.add(String.valueOf(sslmode));
        }
        command.add(cmd.toString());
        if (extraArg != null) {
            command.add(extraArg);
        }

        ws
                .exec()
                .executorOptions(executorOptions)
                .command(command)
                .failFast()
                .run().getResult();
    }

    private Path download(String id) {
        final NutsId iid = appContext.getWorkspace().parser().parseId(id);
        Path downloadBaseFolder = derbyBinHome.resolve(iid.getVersion().getValue());
        Path targetFile = downloadBaseFolder.resolve(iid.getName() + ".jar");
        if (!Files.exists(targetFile)) {
            appContext.getWorkspace().fetch().location(targetFile).id(id).getResultPath();
            if (appContext.isVerbose()) {
                appContext.getSession().getTerminal().out().println("downloading " + id + " to " + targetFile);
            }
        } else {
            if (appContext.isVerbose()) {
                appContext.getSession().getTerminal().out().println("using " + id + " form " + targetFile);
            }
        }
        return targetFile;
    }

    public NutsId resolveNutsId() {
        return appContext.getAppId();
    }

    /**
     * should promote this to FileUtils !!
     *
     * @param path
     * @param cwd
     * @return
     */
    public static String getAbsoluteFile(String path, String cwd) {
        if (new File(path).isAbsolute()) {
            return path;
        }
        if (cwd == null) {
            cwd = System.getProperty("user.dir");
        }
        switch (path) {
            case "~":
                return System.getProperty("user.home");
            case ".": {
                File file = new File(cwd);
                try {
                    return file.getCanonicalPath();
                } catch (IOException ex) {
                    return file.getAbsolutePath();
                }
            }
            case "..": {
                File file = new File(cwd, "..");
                try {
                    return file.getCanonicalPath();
                } catch (IOException ex) {
                    return file.getAbsolutePath();
                }
            }
        }
        int j = -1;
        char[] chars = path.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/' || chars[i] == '\\') {
                j = i;
                break;
            }
        }
        if (j > 0) {
            switch (path.substring(0, j)) {
                case "~":
                    String e = path.substring(j + 1);
                    if (e.isEmpty()) {
                        return System.getProperty("user.home");
                    }
                    File file = new File(System.getProperty("user.home"), e);
                    try {
                        return file.getCanonicalPath();
                    } catch (IOException ex) {
                        return file.getAbsolutePath();
                    }
            }
        }
        File file = new File(cwd, path);
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            return file.getAbsolutePath();
        }
    }
}
