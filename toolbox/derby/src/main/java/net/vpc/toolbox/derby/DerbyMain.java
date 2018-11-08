/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.toolbox.derby;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vpc
 */
public class DerbyMain extends NutsApplication{

    private File derbyBinHome = new File(".");
    private String derbyVersion = null;
    private String derbyDataHome = null;
    private Command cmd = Command.start;
    private NutsSession session;
    private String host = null;
    private int port = -1;
    private SSLMode sslmode = null;
    private String extraArg = null;
    private boolean verbose = false;
    private NutsWorkspace ws;

    public enum SSLMode {
        off, basic, peerAuthentication
    }

    public static enum Command {
        start, shutdown, sysinfo, help, ping, trace, runtimeinfo, maxthreads, timeslice, logconnections
    }

    public static void main(String[] args) {
        new DerbyMain().launchAndExit(args);
    }

    @Override
    public int launch(String[] args, NutsWorkspace ws) {
        this.ws = ws;
        session = ws.createSession();
        parseArgs(ws.getBootOptions().getApplicationArguments());
        return main();
    }

    public NutsId resolveNutsId(){
        if (ws == null) {
            ws = Nuts.openWorkspace();
        }
        NutsId r = ws.resolveNutsIdForClass(getClass());
        if (r == null) {
            r = ws.parseNutsId("net.vpc.app.nuts.toolbox:derby#1.2");
        }
        return r;
    }

    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--derby-version":
                case "-dv": {
                    i++;
                    derbyVersion = args[i];
                    break;
                }
                case "--db":
                case "-db": {
                    i++;
                    derbyDataHome = args[i];
                    break;
                }
                case "--version":
                case "-v": {
                    System.out.println(resolveNutsId());
                    break;
                }
                case "--verbose": {
                    verbose = true;
                    break;
                }
                case "start": {
                    cmd = Command.start;
                    break;
                }
                case "sys":
                case "sysinfo": {
                    cmd = Command.sysinfo;
                    break;
                }
                case "ping": {
                    cmd = Command.sysinfo;
                    break;
                }
                case "rt":
                case "runtimeinfo": {
                    cmd = Command.runtimeinfo;
                    break;
                }
                case "trace": {
                    cmd = Command.trace;
                    i++;
                    extraArg = args[i];
                    break;
                }
                case "tracedirectory": {
                    cmd = Command.trace;
                    i++;
                    extraArg = args[i];
                    break;
                }
                case "maxthreads": {
                    cmd = Command.maxthreads;
                    i++;
                    extraArg = args[i];
                    break;
                }
                case "timeslice": {
                    cmd = Command.timeslice;
                    i++;
                    extraArg = args[i];
                    break;
                }
                case "logconnections": {
                    cmd = Command.logconnections;
                    i++;
                    extraArg = args[i];
                    break;
                }
                case "stop":
                case "shutdown": {
                    cmd = Command.shutdown;
                    break;
                }
                case "-h":
                case "-host": {
                    i++;
                    host = args[i];
                    break;
                }
                case "-p":
                case "-port": {
                    i++;
                    port = Integer.parseInt(args[i]);
                    break;
                }
                case "-ssl": {
                    i++;
                    sslmode = sslmode.valueOf(args[i]);
                    break;
                }
                case "--help":
                case "-?": {
                    System.out.println(resolveNutsId() + " [--db <DB_HOME]> [--version <DB_VERSION>] [start|stop|sysinfo]");
                    System.out.println("\t default DB_HOME    : ~/.netbeans-derby");
                    System.out.println("\t default DB_VERSION : LATEST");
                    System.out.println("\t See NetworkServerControl command for full commands");
                    break;
                }
            }
        }
    }

    public int main() {
        derbyBinHome = new File(ws.getStoreRoot(resolveNutsId(), RootFolderType.PROGRAMS), "lib");
        String v = derbyVersion;
        String h = derbyDataHome;
        if (v == null) {
            NutsId best = ws.findOne(new NutsSearch().addId("org.apache.derby:derbynet").setLastestVersions(true), null);
            v = best.getVersion().toString();
        }
        if (h == null) {
            h = System.getProperty("user.home") + "/.netbeans-derby";
        }
        if (h.startsWith("~/") || h.startsWith("~\\")) {
            h = System.getProperty("user.home") + File.separatorChar + h.substring(2);
        }
        File derby = download("org.apache.derby:derby#" + v);
        File derbynet = download("org.apache.derby:derbynet#" + v);
        File derbyoptionaltools = download("org.apache.derby:derbyoptionaltools#" + v);
        File derbyclient = download("org.apache.derby:derbyclient#" + v);
        File derbytools = download("org.apache.derby:derbytools#" + v);
//        ws.exec("org.apache.derby:derbytools#" + v,
//                new String[]{
//                    "--nuts-np=org.apache.derby:derby#" + v,
//                    "--nuts-cp=" + derbynet.getPath(),
//                    "--nuts-np=org.apache.derby:derbyoptionaltools#" + v,
//                    "--nuts-np=org.apache.derby:derbyclient#" + v,
//                    //            "--nuts-np=org.apache.derby:derbynet#"+v,
//                    "--nuts-main-class=org.apache.derby.drda.NetworkServerControl",
//                    "--nuts-show-command",
//                    "--nuts-Dderby.system.home=/home/vpc/.netbeans-derby",
//                    "start",}, null, null);
//use named jar because derby test upon jar names at runtime (what a shame !!!)
        List<String> command = new ArrayList<>();
        command.add("org.apache.derby:derbytools#" + v);
        command.addAll(Arrays.asList("--nuts-cp=" + derby.getPath(),
                "--nuts-cp=" + derbynet.getPath(),
                "--nuts-cp=" + derbyclient.getPath(),
                "--nuts-cp=" + derbytools.getPath(),
                "--nuts-cp=" + derbyoptionaltools.getPath()
        ));
        if (verbose) {
            command.add("--nuts-show-command");
        }
        command.addAll(Arrays.asList(
                "--nuts-main-class=org.apache.derby.drda.NetworkServerControl",
                "--nuts-Dderby.system.home=" + h
        ));
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
        return ws.exec(command.toArray(new String[command.size()]), null, null,null);
    }

    private File download(String id) {
        final NutsId iid = ws.getExtensionManager().parseNutsId(id);
        File downloadBaseFolder = new File(derbyBinHome, iid.getVersion().getValue());
        File targetFile = new File(downloadBaseFolder, iid.getName() + ".jar");
        if (!targetFile.exists()) {
            ws.copyTo(id, targetFile.getPath(), null);
            if (verbose) {
                session.getTerminal().getOut().println("downloading " + id + " to " + targetFile);
            }
        } else {
            if (verbose) {
                session.getTerminal().getOut().println("using " + id + " form " + targetFile);
            }
        }
        return targetFile;
    }
}
