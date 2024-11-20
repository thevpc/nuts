package net.thevpc.nuts.build.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.lib.common.collections.NArrays;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.*;

import net.thevpc.nuts.build.NutsBuildRunnerContext;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.spi.NScopeType;

public abstract class AbstractRunner implements NCmdLineConfigurable {

    protected NPath INIT_FOLDER;
    protected NPath CURRENT_FOLDER;
    private String remoteSshConnexion;
    private boolean timestampTrace = true;
    private boolean preferRsync = true;
    private boolean explodedUpload = true;

    public AbstractRunner() {
        INIT_FOLDER = NPath.ofUserDirectory();
        CURRENT_FOLDER = INIT_FOLDER;
    }

    public void configureBeforeOptions(NCmdLine cmdLine) {

    }

    public void configureAfterOptions() {

    }

    public NutsBuildRunnerContext context() {
        NSession session = NSession.get();
        NutsBuildRunnerContext s = (NutsBuildRunnerContext) session.getProperty(NutsBuildRunnerContext.class.getName(), NScopeType.SESSION).orNull();
        if (s == null) {
            s = new NutsBuildRunnerContext();
            session.setProperty(NutsBuildRunnerContext.class.getName(), NScopeType.SESSION, s);
        }
        return s;
    }

    @Override
    public Object configure(boolean skipUnsupported, String... args) {
        configure(skipUnsupported, NCmdLine.of(args));
        return this;
    }

    public void configure(NCmdLine args) {

    }

    public abstract void run();

    public String readString(NPath path) {
        return new String(path.readBytes());
    }

    public void writeString(NPath path, String str) {
        NSession session = NSession.get();
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
        return NPath.of(path);
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
        NSession session = NSession.get();
        if (session.isDry() || session.isTrace()) {
            traceCmd("mkdir", "-p", path.toString());
        }
        if (!session.isDry()) {
            path.mkdirs();
        }
    }

    public void rmDir(NPath path) {
        NSession session = NSession.get();
        if (session.isDry() || session.isTrace()) {
            traceCmd("rm", "-r", path.toString());
        }
        if (!session.isDry()) {
            path.deleteTree();
        }
    }

    public void rm(NPath path) {
        NSession session = NSession.get();
        if (session.isDry() || session.isTrace()) {
            traceCmd("rm", path.toString());
        }
        if (!session.isDry()) {
            path.delete();
        }
    }

    public void cp(NPath from, NPath to) {
        NSession session = NSession.get();
        if (session.isDry() || session.isTrace()) {
            traceCmd("cp", from.toString(), to.toString());
        }
        if (!session.isDry()) {
            from.copyTo(to, NPathOption.REPLACE_EXISTING);
        }
    }

    public void exec(String... cmd) {
        //if (session.isDry() || session.isTrace()) {
        traceCmd(cmd);
        //}
//        String out =
        NExecCmd.of()
                .addCommand(cmd)
                .failFast()
                .system()
                .setDirectory(CURRENT_FOLDER)
                .run() //                .setRedirectErrorStream()
        //                .grabOutputString()
        //                .getOutputString()
        ;
//        session.out().println(out.trim());
    }

    public String execAsString(String... cmd) {
        NSession session = NSession.get();
        if (session.isDry() || session.isTrace()) {
            traceCmd(cmd);
        }
        return NExecCmd.of()
                .addCommand(cmd)
                .failFast()
                .system()
                .setDirectory(CURRENT_FOLDER)
                .run()
                .getGrabbedAllString();
    }

    public void copyWithHeader(NPath from, NPath to, String header) {
        NSession session = NSession.get();
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
        NSession session = NSession.get();
        if (session.isDry() || session.isTrace()) {
            traceCmd("sed", fromExpr, to, path.toString());
        }
        NPath p = NPath.ofTempFile("temp");
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
        NSession session = NSession.get();
        NPrintStream out = session.out();
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
        NSession session = NSession.get();
        NPrintStream out = session.out();
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
        NSession session = NSession.get();
        NPrintStream out = session.out();
        out.println(NMsg.ofV(message, vars));
    }

    public void echo(String message) {
        NSession session = NSession.get();
        NPrintStream out = session.out();
        out.println(NMsg.ofV(message, new HashMap<>()));
    }

    public void sleep(int seconds) {
        NSession session = NSession.get();
        NPrintStream out = session.out();
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
        NSession session = NSession.get();
        NPrintStream out = session.out();
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


    private String _parentPath(String toRemote) {
        int i = toRemote.lastIndexOf('/');
        return toRemote.substring(0, i + 1);
    }

    public void upload(NPath fromLocal, String toRemote) {
        upload(fromLocal.toString(), toRemote);
    }

    public void upload(NPath fromLocal, NPath toRemote) {
        upload(fromLocal.toString(), toRemote.toString());
    }

    public void remoteDeleteFolder(String path) {
        remoteExec("rm -R " + path);
    }

    public void remoteDeleteFile(String path) {
        remoteExec("rm " + path);
    }

    public void remoteCopyFolder(String fromRemote, String toRemote) {
        remoteExec("cp -r " + fromRemote + " " + _parentPath(toRemote));
        //scp("-r", getRemoteSshConnexion().get() + ":" + fromRemote, getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void remoteCopyFolder(NPath fromRemote, NPath toRemote) {
        remoteExec("cp -r " + fromRemote + " " + _parentPath(toRemote.toString()));
        //scp("-r", getRemoteSshConnexion().get() + ":" + fromRemote, getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void remoteCopyFile(String fromRemote, String toRemote) {
        remoteExec("cp " + fromRemote + " " + toRemote);
        //scp(getRemoteSshConnexion().get() + ":" + fromRemote, getRemoteSshConnexion().get() + ":" + toRemote);
    }

    public void remoteCopyFile(NPath fromRemote, NPath toRemote) {
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

    protected NPath removeThevpcMaven() {
        return NPath.of(context().home + "/srv/maven-thevpc/");
    }

    protected NPath remoteTheVpcNuts() {
//        return NPath.of(context().home + "/srv/tomcat-a/webapps-thevpc/ROOT/nuts/", session);
        return NPath.of(context().home + "/srv/tomcat-a/domain-webapps/thevpc.net/ROOT/nuts/");
    }

    protected NPath localMvn() {
        return NPath.of(Mvn.localMaven());
    }

    protected NPath removeMvn() {
        String remoteUser = "vpc";
        return NPath.of("/home/" + remoteUser + "/.m2/repository");
    }

    public void pushIdFiles(String... ids) {
        NPath m2Path = localMvn();
        NPath mavenThevpc = removeThevpcMaven();
        class PathWithPrio {

            String p;
            NPath path;
            int prio;

            public PathWithPrio(String p, NPath path, int prio) {
                this.p = p;
                this.path = path;
                this.prio = prio;
            }
        }
        List<PathWithPrio> todo = new ArrayList<>();
        for (String sid : ids) {
            NId id = NId.of(sid).get();
            NPath folder = localMvn().resolve(Mvn.folder(id));
            for (NPath sub : folder.list()) {
                if (sub.isRegularFile()) {
                    String name = sub.getName();
                    todo.add(new PathWithPrio(
                                    Mvn.folder(id),
                                    sub,
                                    MvnArtifactType.byFile(name).get().uploadPrio()
                            )
                    );
                }
            }
        }
        todo.sort(Comparator.comparing(x -> x.prio));
        for (PathWithPrio pathWithPrio : todo) {
            upload(pathWithPrio.path, m2Path.resolve(pathWithPrio.p).toString());
            remoteCopyFile(
                    m2Path.resolve(pathWithPrio.p).toString(),
                    mavenThevpc.resolve(pathWithPrio.p).toString()
            );
        }

    }

    public void upload(String rfrom, String to) {
        if (rfrom.endsWith("/*")) {
            String rfrom0 = rfrom.substring(0, rfrom.length() - 2);
            if (explodedUpload) {
                for (NPath p : NPath.of(rfrom0).list()) {
                    String rto = to + "/" + p.getName();
                    if (p.isDirectory()) {
                        remoteMkdirs(rto);
                        rto = NPath.of(rto).getParent().toString();
                    }
                    scpOrRsync(p.toString(), getRemoteSshConnexion().get() + ":" + rto);
                }
            } else {
                String rto = to;
                if (NPath.of(rfrom).isDirectory()) {
                    remoteMkdirs(rto);
                    rto = NPath.of(rto).getParent().toString();
                }
//                log(NMsg.ofC("##upload## %s to %s",
//                        NMsg.ofStyled(rfrom0, NTextStyle.path()),
//                        NMsg.ofStyled(connexion() + ":" + rto, NTextStyle.path())));
                scpOrRsync(rfrom0, getRemoteSshConnexion().get() + ":" + rto);
            }
        } else {
            String rto = NPath.of(to).toString();
            if (NPath.of(rfrom).isDirectory()) {
                remoteMkdirs(rto);
                rto = NPath.of(rto).getParent().toString();
            }
            scpOrRsync(rfrom, remoteSshConnexion + ":" + rto);
        }
    }

    public void remoteMkdirs(String path) {
        rexec("mkdir","-p",path);
    }

    private void scpOrRsync(String from, String to) {
        if (preferRsync) {
            exec(new String[]{"rsync", "-r", "-v", "-h", "-z", "--progress", from, to});
        } else {
            exec(new String[]{"scp", "-r", from, to});
        }
    }

    public void rexec(String... command) {
        NExecCmd.of().system()
                .addCommand(
                        "ssh",
                        remoteSshConnexion,
                        NCmdLine.of(command).format().filteredText()
                )
                .failFast()
                .run();
    }

}
