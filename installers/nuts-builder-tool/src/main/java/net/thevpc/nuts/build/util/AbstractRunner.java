package net.thevpc.nuts.build.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NArrays;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AbstractRunner {
    protected NSession session;
    protected NPrintStream out;
    protected NPath INIT_FOLDER;
    protected NPath CURRENT_FOLDER;
    private String remoteSshConnexion;
    private boolean timestampTrace = true;

    public AbstractRunner(NSession session) {
        this.session = session;
        this.out = this.session.out();
        INIT_FOLDER = NPath.ofUserDirectory(this.session);
        CURRENT_FOLDER = INIT_FOLDER;
    }


    public String readString(NPath path) {
        return new String(path.readBytes());
    }

    public void writeString(NPath path, String str) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("write", String.valueOf(path), str);
        }
        if (session.isDry()) {
            return;
        }
        path.writeString(str);
    }

    public void mvn(String... args) {
        exec("mvn", args);
    }

    public void mvnInstall() {
        exec("mvn", "install");
    }

    public void ng(String... args) {
        exec("ng", args);
    }

    public void ngBuild() {
        exec("ng", "build");
    }

    public NPath path(String path) {
        return NPath.of(path, session);
    }

    public void ssh(String... cmd) {
        exec("ssh", cmd);
    }

    public String sshAsString(String... cmd) {
        return execAsString(NArrays.concat("ssh", cmd));
    }

    public void scp(String... cmd) {
        exec("scp", cmd);
    }

    public void exec(String cmd) {
        exec(new String[]{cmd});
    }

    public void exec(String cmd, String arg) {
        exec(new String[]{cmd, arg});
    }

    public void exec(String cmd, String... cmds) {
        exec(NArrays.concat(cmd, cmds));
    }

    public void mkdir(NPath path) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("mkdir", "-p", path.toString());
        }
        if (!session.isDry()) {
            path.mkdirs();
        }
    }

    public void rmDir(NPath path) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("rm", "-r", path.toString());
        }
        if (!session.isDry()) {
            path.deleteTree();
        }
    }

    public void rm(NPath path) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("rm", path.toString());
        }
        if (!session.isDry()) {
            path.delete();
        }
    }

    public void cp(NPath from, NPath to) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("cp", from.toString(), to.toString());
        }
        if (!session.isDry()) {
            from.copyTo(to, NPathOption.REPLACE_EXISTING);
        }
    }

    public void exec(String... cmd) {
        if (session.isDry() || session.isTrace()) {
            traceCmd(cmd);
        }
//        String out =
        NExecCommand.of(session)
                .addCommand(cmd)
                .setFailFast(true)
                .setExecutionType(NExecutionType.SYSTEM)
                .setDirectory(CURRENT_FOLDER)
                .run()
//                .setRedirectErrorStream()
//                .grabOutputString()
//                .getOutputString()
        ;
//        session.out().println(out.trim());
    }

    public String execAsString(String... cmd) {
        if (session.isDry() || session.isTrace()) {
            traceCmd(cmd);
        }
        return NExecCommand.of(session)
                .addCommand(cmd)
                .setFailFast(true)
                .setExecutionType(NExecutionType.SYSTEM)
                .setDirectory(CURRENT_FOLDER)
                .redirectErrorStream()
                .grabOutputString()
                .run()
                .getOutputString();
    }

    public void copyWithHeader(NPath from, NPath to, String header) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("copy-with-header", from.toString(), to.toString(), "**header...**");
        }
        try (PrintStream out = to.getPrintStream()) {
            if (header != null) {
                out.println(header);
            }
            from.copyToPrintStream(out);
        }
    }

    public void sed(String fromExpr, String to, NPath path) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("sed", fromExpr, to, path.toString());
        }
        NPath p = NPath.ofTempFile("temp",session);
        try (PrintStream out = p.getPrintStream()) {
            try (BufferedReader br = path.getBufferedReader()) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.println(
                            line.replaceAll(fromExpr, to)
                    );
                }
            }
            p.moveTo(path, NPathOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void trace(String message, Map<String, ?> vars) {
        out.print("[trace ]");
        if (timestampTrace) {
            out.print("[" + new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date()) + "] ");
        }
        out.println(NMsg.ofV(message, vars));
    }

    public void traceCmd(String... cmdLine) {
        trace(NCmdLine.of(cmdLine));
    }

    public void trace(NCmdLine cmdLine) {
        if (session.isDry()) {
            out.print("[dry] ");
        } else {
            out.print("[trace] ");
        }
        if (timestampTrace) {
            out.print("[" + new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(new Date()) + "] ");
        }
        out.println(cmdLine);
    }

    public void trace(String message) {
        trace(message, new HashMap<>());
    }

    public void echo(String message, Map<String, ?> vars) {
        out.println(NMsg.ofV(message, vars));
    }


    public void echo(String message) {
        out.println(NMsg.ofV(message, new HashMap<>()));
    }


    public void sleep(int seconds) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("sleep", String.valueOf(seconds));
        }
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void cdInitial() {
        cd(INIT_FOLDER);
    }

    public String date(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    public void cd(NPath p) {
        if (session.isDry() || session.isTrace()) {
            traceCmd("cd", String.valueOf(p));
        }
        CURRENT_FOLDER = p;
    }

    public NOptional<String> getRemoteSshConnexion() {
        return NOptional.of(remoteSshConnexion);
    }

    public AbstractRunner setRemoteSshConnexion(String remoteSshConnexion) {
        this.remoteSshConnexion = remoteSshConnexion;
        return this;
    }


    public void scpPushFile(String fromLocal, String toRemote) {
        scp(fromLocal, getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void pushFolder(NPath fromLocal, String toRemote) {
        scp("-r", fromLocal.toString(), getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void pushFolder(String fromLocal, String toRemote) {
        scp("-r", fromLocal, getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void pushFile(NPath fromLocal, String toRemote) {
        pushFile(fromLocal.toString(), toRemote);
    }

    public void pushFile(String fromLocal, String toRemote) {
        scp(fromLocal, getRemoteSshConnexion().get() + ":" + toRemote);
    }


    public void remoteDeleteFolder(String path) {
        remoteExec("rm -R " + path);
    }

    public void remoteDeleteFile(String path) {
        remoteExec("rm " + path);
    }

    public void remoteCopyFolder(String fromRemote, String toRemote) {
        remoteExec("cp -r " + fromRemote + " " + toRemote);
        //scp("-r", getRemoteSshConnexion().get() + ":" + fromRemote, getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void remoteCopyFile(String fromRemote, String toRemote) {
        remoteExec("cp " + fromRemote + " " + toRemote);
        //scp(getRemoteSshConnexion().get() + ":" + fromRemote, getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void remoteExec(String command) {
        ssh(getRemoteSshConnexion().get(), command);
    }

    public void remoteKill(long pid) {
        ssh("kill", "-9", String.valueOf(pid));
    }

    public JpsResult[] remoteJps() {
        String jps = sshAsString(getRemoteSshConnexion().get(), "jps", "-lmv");
        String[] rows = jps.split("\n");
        List<JpsResult> results = new ArrayList<>();
        for (String row : rows) {
            try {
                NCmdLine line = NCmdLine.parseDefault(row).orNull();
                NArg nArg = line.peek().orNull();
                if (nArg.isLong()) {
                    long pid = line.next().get().asLong().get();
                    String clz = line.next().get().asString().get();
                    JpsResult r = new JpsResult(pid, clz, line.toStringArray());
                    if ("org.apache.catalina.startup.Bootstrap".equals(r.getClassName())) {
                        r.setType("Tomcat");
                        for (String s : r.getCmd()) {
                            if (s.startsWith("-Dcatalina.base=")) {
                                r.setDiscriminator(s.substring("-Dcatalina.base=".length()));
                                break;
                            }
                        }
                    }
                    results.add(r);
                }
            } catch (Exception e) {
                //just ignore
            }
        }
        return results.toArray(new JpsResult[0]);
    }

}
