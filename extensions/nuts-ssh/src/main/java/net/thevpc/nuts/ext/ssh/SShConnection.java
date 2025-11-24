package net.thevpc.nuts.ext.ssh;

import com.jcraft.jsch.*;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPathType;
import net.thevpc.nuts.net.DefaultNConnexionStringBuilder;
import net.thevpc.nuts.net.NConnexionString;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SShConnection implements AutoCloseable {

    Session sshSession;
    private boolean redirectErrorStream;
    private boolean failFast;
    private PrintStream out = new PrintStream(new NonClosableOutputStream(System.out));
    private PrintStream err = new PrintStream(new NonClosableOutputStream(System.err));
    private InputStream in;
    private NConnexionString connexionString;
    private List<SshListener> listeners = new ArrayList<>();
    private OsProbeInfo osProbeInfo;

    public SShConnection(String address, InputStream in, OutputStream out, OutputStream err) {
        this(NConnexionString.of(address), in, out, err);
    }

    public static SShConnection ofProbedSShConnection(String connexionString, InputStream in, OutputStream out, OutputStream err) {
        OsProbeInfo osProbeInfo = OsProbeInfoCache.of().get(connexionString);
        osProbeInfo.tryUpdate();
        return new SShConnection(connexionString
                , in
                , out
                , err
        ).setOsProbeInfo(osProbeInfo);
    }

    public static SShConnection ofProbedSShConnection(NConnexionString connexionString, InputStream in, OutputStream out, OutputStream err) {
        OsProbeInfo osProbeInfo = OsProbeInfoCache.of().get(connexionString);
        osProbeInfo.tryUpdate();
        return new SShConnection(connexionString
                , in
                , out
                , err
        ).setOsProbeInfo(osProbeInfo);
    }

    public SShConnection(NConnexionString connexionString, InputStream in, OutputStream out, OutputStream err) {
        init(connexionString, in, out, err);
    }

//    public SShConnection(String user, String host, int port, String keyFilePath, String keyPassword, InputStream in, OutputStream out, OutputStream err) {
//        init(user, host, port, keyFilePath, keyPassword, in, out, err);
//    }


    public OsProbeInfo getOsProbeInfo() {
        return osProbeInfo;
    }

    public SShConnection setOsProbeInfo(OsProbeInfo osProbeInfo) {
        this.osProbeInfo = osProbeInfo;
        return this;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    public SShConnection redirectErrorStream() {
        redirectErrorStream = true;
        return this;
    }

    public SShConnection setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    public void reset() {
        failFast = false;
        redirectErrorStream = false;
        out = new PrintStream(new NonClosableOutputStream(System.out));
        err = new PrintStream(new NonClosableOutputStream(System.err));
    }

    public SShConnection addListener(SshListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    public SShConnection removeListener(SshListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
        return this;
    }

    private void init(NConnexionString connexionString, InputStream in0, OutputStream out0, OutputStream err0) {
        String user = connexionString.getUserName();
        String host = connexionString.getHost();
        int port = NLiteral.of(connexionString.getPort()).asInt().orElse(-1);
        String keyFilePath = NStringMapFormat.URL_FORMAT.parse(connexionString.getQueryString())
                .orElse(Collections.emptyMap()).get("key-file");
        String keyPassword = connexionString.getPassword();
        this.out = new PrintStream(new NonClosableOutputStream(out0));
        this.err = new PrintStream(new NonClosableOutputStream(err0));
        this.in = in0;
        try {
            JSch jsch = new JSch();

            if (keyFilePath == null && keyPassword == null) {
                for (String name : new String[]{"id_rsa", "id_ecdsa", "id_ecdsa_sk", "id_ed25519", "id_ed25519_sk", "id_dsa"}) {
                    File f = new File(System.getProperty("user.home"), ".ssh/" + name);
                    if (f.isFile()) {
                        keyFilePath = f.getPath();
                        break;
                    }
                }
            }
            if (keyFilePath != null) {
                if (keyPassword != null) {
                    jsch.addIdentity(keyFilePath, keyPassword);
                } else {
                    jsch.addIdentity(keyFilePath);
                }
            }
            if (user == null || user.length() == 0) {
                user = System.getProperty("user.name");
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            if (port <= 0) {
                port = 22;
            }
            this.sshSession = jsch.getSession(user, host, port);
            if (keyPassword != null && keyPassword.length() > 0) {
                this.sshSession.setConfig("PreferredAuthentications", "password");
                this.sshSession.setPassword(keyPassword);
            }
            this.sshSession.setConfig(config);
            this.sshSession.connect();
        } catch (JSchException e) {
            //
            throw new UncheckedIOException(new IOException(e.getMessage() + " (" +
                    new DefaultNConnexionStringBuilder().setUserName(user).setHost(host).setPort(String.valueOf(port)).setPassword(keyPassword).setQueryString(
                            keyFilePath == null ? null : NStringMapFormat.URL_FORMAT
                                    .format(
                                            NMaps.of("key-file", keyFilePath)
                                    )
                    ) + ")", e));
        }
    }

    public boolean isFailFast() {
        return failFast;
    }

    public SShConnection failFast() {
        return setFailFast(true);
    }

    public SShConnection sailFast(boolean failFast) {
        return setFailFast(failFast);
    }

    public SShConnection setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public SShConnection redirectOutput(PrintStream out) {
        this.out = out;
        return this;
    }

    public boolean isGrabOutputString() {
        return out instanceof SPrintStream;
    }

    public SShConnection grabOutputString() {
        this.out = new SPrintStream();
        return this;
    }

    public SShConnection grabOutputString(boolean grab) {
        if (grab) {
            this.out = new SPrintStream();
        } else {
            this.out = new PrintStream(new NonClosableOutputStream(System.out));
        }
        return this;
    }

    public static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //         -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                //err.print(sb.toString());
            }
            if (b == 2) { // fatal error
                //err.print(sb.toString());
            }
        }
        return b;
    }

    public int exec(List<String> command) {
        return exec(command.toArray(new String[0]));
    }

    public int exec(String... command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            String s = command[i];

            //should add quotes ?
            if (s.isEmpty() || s.contains(" ")) {
                s = "\"" + s.replace("\"", "\\\"") + "\"";
            }
            sb.append(s);
        }
        return execStringCommand(sb.toString());
    }

    public int mv(String from, String to) {
        return execStringCommand("mv " + from + " " + to);
    }

    public int execStringCommand(String command) {
        int status = 205;
        for (SshListener listener : listeners) {
            listener.onExec(command);
        }
        try {
            Channel channel = sshSession.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // X Forwarding
            // channel.setXForwarding(true);
            //channel.setInputStream(new NonClosableInputStream(this.in0));
            //channel.setInputStream(null);

            channel.setOutputStream(new NonClosableOutputStream(out));

            //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
            //((ChannelExec)channel).setErrStream(fos);
            if (isRedirectErrorStream()) {
                ((ChannelExec) channel).setErrStream(new NonClosableOutputStream(out));
            } else {
                ((ChannelExec) channel).setErrStream(new NonClosableOutputStream(err));
            }
            InputStream cin = channel.getInputStream();
            OutputStream cout = channel.getOutputStream();

            channel.connect();

            new Thread(() -> {
                byte[] tmp = new byte[1024];
                try {
                    while (true) {
                        while (this.in.available() > 0) {
                            int i = this.in.read(tmp, 0, tmp.length);
                            if (i < 0) {
                                break;
                            }
                            cout.write(tmp, 0, i);
                        }
                        cout.flush();
                        if (channel.isClosed()) {
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ee) {
                            //
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }).start();
            byte[] tmp = new byte[1024];
            while (true) {
                while (cin.available() > 0) {
                    int i = cin.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (cin.available() > 0) {
                        continue;
                    }
                    status = channel.getExitStatus();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    //
                }
            }
            channel.disconnect();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (JSchException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
        if (isFailFast() && status != 0) {
            throw new UncheckedIOException(new IOException("Ssh command exited with code " + status + (isGrabOutputString() ? (" and message " + getOutputString().trim()) : "")));
        }
        return status;
    }

    public void rm(String from, boolean R) {
        NOsFamily nOsFamily = resolveOsFamily();
        switch (nOsFamily) {
            case WINDOWS: {

            }
        }
        execStringCommand("rm " + (R ? "-R" : "") + " " + from);
    }

    public NPathType type(String path) {
        NOsFamily nOsFamily = resolveOsFamily();
        if (nOsFamily.isWindow()) {
            //TODO
            return NPathType.NOT_FOUND;
        } else {
            int i = exec("file", "-b", "-E", path);
            if (i > 0) {
                return null;
            }
            String s = getOutputString();
            s = s.trim();
            if (s.startsWith("directory")) {
                return NPathType.DIRECTORY;
            }
            if (s.startsWith("fifo (named pipe)")) {
                return NPathType.NAMED_PIPE;
            }
            if (s.startsWith("character special")) {
                return NPathType.CHARACTER;
            }
            if (s.startsWith("symbolic link")) {
                return NPathType.SYMBOLIC_LINK;
            }
            if (s.startsWith("block special")) {
                return NPathType.BLOCK;
            }
            return NPathType.FILE;
        }
    }


    public void mkdir(String from, boolean p) {
        NOsFamily nOsFamily = resolveOsFamily();
        switch (resolveShellFamily()) {
            case WIN_CMD: {
                exec("mkdir", ensureWindowPath(from));
                break;
            }
            case WIN_POWER_SHELL: {
                if (p) {
                    exec("mkdir", "-p", ensureWindowPath(from));
                } else {
                    exec("mkdir", ensureWindowPath(from));
                }
            }
            default: {
                if (p) {
                    exec("mkdir", "-p", from);
                } else {
                    exec("mkdir", from);
                }
            }
        }
    }

    private String ensureWindowPath(String from) {
        return from;
    }

    private NShellFamily resolveShellFamily() {
        NOsFamily os = resolveOsFamily();
        NShellFamily shellFamily = NShellFamily.WIN_POWER_SHELL;
        switch (os) {
            case WINDOWS: {
                shellFamily = NShellFamily.WIN_POWER_SHELL;
                break;
            }
            case LINUX: {
                shellFamily = NShellFamily.BASH;
                break;
            }
            case MACOS: {
                shellFamily = NShellFamily.ZSH;
                break;
            }
            case UNIX: {
                shellFamily = NShellFamily.SH;
                break;
            }
            default: {
                shellFamily = NShellFamily.BASH;
            }
        }
        if (osProbeInfo != null) {
            shellFamily = osProbeInfo.shellFamily();
        }
        return shellFamily;
    }

    private NOsFamily resolveOsFamily() {
        NOsFamily nOsFamily = NOsFamily.LINUX;
        if (osProbeInfo != null) {
            nOsFamily = osProbeInfo.osFamily();
        }
        return nOsFamily;
    }

    public byte[] readRemoteFile(String from) {
        try {
//            for (SshListener listener : listeners) {
//                listener.onGet(from, to, mkdir);
//            }

//            if (new File(to).isDirectory()) {
//                prefix = to + File.separator;
//            }
            // exec 'scp -f rfile' remotely
            String command = "scp -f " + from;
            Channel channel = sshSession.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // readAll '0644 '
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') {
                        break;
                    }
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                String file = null;
                for (int i = 0; ; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

//                System.out.println("file-size=" + filesize + ", file=" + file);
                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
                fos = new ByteArrayOutputStream();
                // readAll a content of lfile
                int foo;
                while (true) {
                    if (buf.length < filesize) {
                        foo = buf.length;
                    } else {
                        foo = (int) filesize;
                    }
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L) {
                        break;
                    }
                }

                if (checkAck(in) != 0) {
                    return fos.toByteArray();
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }

            channel.disconnect();
            return fos.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (JSchException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public void copyRemoteToLocal(String from, String to, boolean mkdir) {
        try {
            for (SshListener listener : listeners) {
                listener.onGet(from, to, mkdir);
            }

            if (mkdir) {
                String pp = new File(to).getParent();
                if (pp != null) {
                    mkdir(pp, true);
                }
            }

            String prefix = null;

//            if (new File(to).isDirectory()) {
//                prefix = to + File.separator;
//            }
            // exec 'scp -f rfile' remotely
            String command = "scp -f " + from;
            Channel channel = sshSession.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // readAll '0644 '
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') {
                        break;
                    }
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                String file = null;
                for (int i = 0; ; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

//                System.out.println("file-size=" + filesize + ", file=" + file);
                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // readAll a content of lfile
                FileOutputStream fos = new FileOutputStream(prefix == null ? to : prefix + file);
                int foo;
                while (true) {
                    if (buf.length < filesize) {
                        foo = buf.length;
                    } else {
                        foo = (int) filesize;
                    }
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L) {
                        break;
                    }
                }

                if (checkAck(in) != 0) {
                    return;
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }

            channel.disconnect();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (JSchException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public InputStream getInputStream(String from) {
        return getInputStream(from, false);
    }

    private InputStream getInputStream(String from, boolean closeConnection) {
        return new SshFileInputStream(this, from, closeConnection);
    }

    public void copyLocalToRemote(String from, String to, boolean mkdirs) {
        try {
            if (from == null || from.trim().isEmpty()) {
                throw new IllegalArgumentException("missing source path");
            }
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("missing target path");
            }
            for (SshListener listener : listeners) {
                listener.onPut(from, to, mkdirs);
            }
            if (mkdirs) {
                boolean off = failFast;
                failFast = false;
                if (to.endsWith("/")) {
                    mkdir(to, true);
                } else if (to.contains("/")) {
                    String p = to.substring(0, to.lastIndexOf('/'));
                    if (p.length() > 0) {
                        mkdir(p, true);
                    }
                }
                failFast = off;
            }
            boolean ptimestamp = true;

            // exec 'scp -t rfile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + to;
            Channel channel = sshSession.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                return;
            }

            File _lfile = new File(from);

            if (ptimestamp) {
                command = "T" + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but It's not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    return;
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (from.lastIndexOf('/') > 0) {
                command += from.substring(from.lastIndexOf('/') + 1);
            } else {
                command += from;
            }

            command += "\n";
            out.write(command.getBytes());
            out.flush();

            if (checkAck(in) != 0) {
                return;
            }

            // send a content of lfile
            InputStream fis = prepareStream(new File(from));
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                out.write(buf, 0, len); //out.flush();
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            if (checkAck(in) != 0) {
                return;
            }
            out.close();

            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ex) {
                this.out.println(ex);
            }

            channel.disconnect();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (JSchException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public void close() {
        sshSession.disconnect();
    }

    public String getOutputString() {
        if (out instanceof SPrintStream) {
            return ((SPrintStream) out).getStringBuffer();
        }
        throw new UncheckedIOException(new IOException("Grab Output is not enabled. See grabOutputString()"));
    }

    public PrintStream getOut() {
        return out;
    }

    public SShConnection setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    public List<String> list(String path) {
        ensureGrabbed();
        switch (resolveShellFamily()) {
            case WIN_CMD:
            case WIN_POWER_SHELL: {
                int i = exec("powershell", "-c", "Get-ChildItem '" + ensureWindowPath(path) + "'");
                if (i == 0) {
                    //continue parsing
                    String[] s = getOutputString().split("[\n|\r]");
                    return NStream.ofArray(s).map(
                            x -> {
                                String cc = path;
                                if (!cc.endsWith("/")) {
                                    cc += "/";
                                }
                                cc += x;
                                return cc;
                            }
                    ).collect(Collectors.toList());
                }
                break;
            }
            case SH:
            case BASH:
            case ZSH:
            case FISH:
            case KSH:
            case CSH: {
                int i = exec("ls", path);
                if (i == 0) {
                    String[] s = getOutputString().split("[\n|\r]");
                    return NStream.ofArray(s).map(
                            x -> {
                                String cc = path;
                                if (!cc.endsWith("/")) {
                                    cc += "/";
                                }
                                cc += x;
                                return cc;
                            }
                    ).collect(Collectors.toList());

                }
            }
        }
        return new ArrayList<>();
    }

    private void ensureGrabbed() {
        if (!(out instanceof SPrintStream)) {
            throw new IllegalArgumentException("Grab Output is not enabled. See grabOutputString()");
        }
    }

    public long contentLength(String basePath) {
        ensureGrabbed();
        int i = execStringCommand("ls -l " + basePath);
        if (i != 0) {
            return -1;
        }
        String outputString = getOutputString();
        String[] r = NStringUtils.trim(outputString).split(" ");
        if (r.length > 4) {
            NOptional<Long> size = NLiteral.of(r[4]).asLong();
            if (size.isPresent()) {
                return size.get();
            }
        }
        return -1;
    }

    public String getContentEncoding(String basePath) {
        ensureGrabbed();
        int i = execStringCommand("file -bi " + basePath);
        if (i != 0) {
            return null;
        }
        String outputString = NStringUtils.trim(getOutputString());
        Pattern p = Pattern.compile(".*charset=(?<cs>\\S*).*");
        Matcher m = p.matcher(outputString);
        if (m.find()) {
            return m.group("cs");
        }
        return null;
    }

    public String getContentType(String basePath) {
        ensureGrabbed();
        int i = execStringCommand("file -bi " + basePath);
        if (i != 0) {
            return null;
        }
        String outputString = getOutputString();
        String[] r = Arrays.stream(NStringUtils.trim(outputString).split("[ ;]")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
        if (r.length > 0) {
            return NStringUtils.trim(r[0]);
        }
        return null;
    }

    public String getCharset(String basePath) {
        ensureGrabbed();
        int i = execStringCommand("file -bi " + basePath);
        if (i != 0) {
            return null;
        }
        String outputString = getOutputString();
        String[] r = Arrays.stream(NStringUtils.trim(outputString).split("[ ;]")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
        if (r.length > 1) {
            String v = NStringUtils.trim(r[1]);
            if (v.startsWith("charset=")) {
                v = v.substring("charset=".length()).trim();
            }
            return v;
        }
        return null;
    }

    public void cp(String path, String path1, boolean b) {
        if (b) {
            exec("cp", path, path1);
        } else {
            exec("cp", "-r", path, path1);
        }
    }

    public List<String> walk(String path, boolean followLinks, int maxDepth) {


        ensureGrabbed();
        switch (resolveShellFamily()) {
            case WIN_CMD:
            case WIN_POWER_SHELL: {
                // Get-ChildItem -Path C:\ -Depth 2 -Force | ForEach-Object { $_.FullName }
                // TODO
                break;
            }
            case SH:
            case BASH:
            case ZSH:
            case FISH:
            case KSH:
            case CSH: {
                StringBuilder cmd = new StringBuilder();
                cmd.append("find");
                cmd.append(" ").append(path);
                if (followLinks) {
                    //all
                } else {
                    cmd.append(" -type d,f");
                }
                if (maxDepth > 0 && maxDepth != Integer.MAX_VALUE) {
                    cmd.append(" -maxdepth ").append(maxDepth);
                }
                int i = execStringCommand(cmd.toString());
                if (i == 0) {
                    String[] s = getOutputString().split("[\n|\r]");
                    return Stream.of(s).map(
                            x -> {
                                String cc = path;
                                if (!cc.endsWith("/")) {
                                    cc += "/";
                                }
                                cc += x;
                                return cc;
                            }
                    ).collect(Collectors.toList());
                }
                break;
            }
        }
        return new ArrayList<>();
    }


    private static class SPrintStream extends PrintStream {

        private ByteArrayOutputStream out;

        public SPrintStream() {
            this(new ByteArrayOutputStream());
        }

        public SPrintStream(ByteArrayOutputStream out1) {
            super(out1);
            this.out = out1;
        }

        public String getStringBuffer() {
            flush();
            return new String(out.toByteArray());
        }

        @Override
        public String toString() {
            return getStringBuffer();
        }
    }

    public InputStream prepareStream(File file) throws FileNotFoundException {
        FileInputStream in = new FileInputStream(file);
        NMsg path = NMsg.ofStyledPath(file.getPath());
        for (SshListener listener : listeners) {
            InputStream v = listener.monitorInputStream(in, file.length(), path);
            if (v != null) {
                return v;
            }
        }
        return in;
    }

    public byte[] getDigestWithCommand(String cmd, String basePath, String algo) {
        ensureGrabbed();
        int r = execStringCommand(cmd + " " + basePath);
        if (r == 0) {
            String z = NStringUtils.trim(getOutputString());
            int i = z.indexOf(' ');
            if (i > 0) {
                z = z.substring(0, i);
                return NHex.toBytes(z);
            }
        }
        return null;
    }

}
