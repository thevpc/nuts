package net.thevpc.nuts.ext.ssh;

import com.jcraft.jsch.*;
import net.thevpc.nuts.io.NPathType;
import net.thevpc.nuts.io.NullOutputStream;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SShConnection extends SShConnexionBase {

    Session sshSession;
    private boolean failFast;

    private OsProbeInfo probedInfo;
    private boolean closed;
    private NConnexionString connexionString;

    public SShConnection(String address) {
        init(NConnexionString.of(address));
    }

    public SShConnection(NConnexionString connexionString) {
        init(connexionString);
    }



    @Override
    public void reset() {
        failFast = false;
    }

    private void init(NConnexionString connexionString) {
        this.connexionString = connexionString;
        String user = connexionString.getUserName();
        String host = connexionString.getHost();
        int port = NLiteral.of(connexionString.getPort()).asInt().orElse(-1);
        String keyFilePath = NStringMapFormat.URL_FORMAT.parse(connexionString.getQueryString())
                .orElse(Collections.emptyMap()).get("key-file");
        String keyPassword = connexionString.getPassword();
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

//    public SShConnection redirectOutput(PrintStream out) {
//        this.out = out;
//        return this;
//    }

//    public boolean isGrabOutputString() {
//        return out instanceof SPrintStream;
//    }
//
//    public SShConnection grabOutputString() {
//        this.out = new SPrintStream();
//        return this;
//    }
//
//    public SShConnection grabOutputString(boolean grab) {
//        if (grab) {
//            this.out = new SPrintStream();
//        } else {
//            this.out = new PrintStream(new NonClosableOutputStream(System.out));
//        }
//        return this;
//    }

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



    @Override
    public int mv(String from, String to) {
        return execArrayCommandGrabbed("mv", from, to).code();
    }

    @Override
    public IOResult execArrayCommandGrabbed(String... command) {
        String sb = cmdArrayToString(command);
        return execStringCommandGrabbed(sb);
    }

    @Override
    public IOResult execStringCommandGrabbed(String command) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int r = execStringCommand(command, new IOBindings(
                null, bos,
                berr
        ));
        return new IOResult(
                r,
                bos.toByteArray(),
                berr.toByteArray()
        );
    }

    @Override
    public int execStringCommand(String command, IOBindings io) {
        if (io == null) {
            io = new IOBindings(null, null, null);
        }
        OutputStream out = new NonClosableOutputStream(io.out() == null ? NullOutputStream.INSTANCE : io.out());
        OutputStream err = (io.out() == io.err()) ? out : new NonClosableOutputStream(io.err() == null ? NullOutputStream.INSTANCE : io.err());
        InputStream in = io.in() == null ? new ByteArrayInputStream(new byte[0]) : new NonClosableInputStream(io.in());

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

            channel.setOutputStream(out);

            ((ChannelExec) channel).setErrStream(err);
            InputStream cin = channel.getInputStream();
            OutputStream cout = channel.getOutputStream();

            channel.connect();

            new Thread(() -> {
                byte[] tmp = new byte[1024];
                try {
                    while (true) {
                        while (in.available() > 0) {
                            int i = in.read(tmp, 0, tmp.length);
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
                    out.write(tmp, 0, i);
                }
                out.flush();
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
            throw new UncheckedIOException(new IOException("Ssh command exited with code " + status));
        }
        return status;
    }

    @Override
    public void rm(String from, boolean R) {
        NOsFamily nOsFamily = resolveOsFamily();
        switch (nOsFamily) {
            case WINDOWS: {

            }
        }
        execStringCommandGrabbed("rm " + (R ? "-R" : "") + " " + from);
    }

    @Override
    public NPathType type(String path) {
        NOsFamily nOsFamily = resolveOsFamily();
        if (nOsFamily.isWindow()) {
            //TODO
            return NPathType.NOT_FOUND;
        } else {
            IOResult ii = execArrayCommandGrabbed("file", "-b", "-E", path);
            int i = ii.code();
            if (i > 0) {
                return null;
            }
            String s = ii.outString();
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


    @Override
    public void mkdir(String from, boolean p) {
        NOsFamily nOsFamily = resolveOsFamily();
        switch (resolveShellFamily()) {
            case WIN_CMD: {
                execArrayCommandGrabbed("mkdir", ensureWindowPath(from));
                break;
            }
            case WIN_POWER_SHELL: {
                if (p) {
                    execArrayCommandGrabbed("mkdir", "-p", ensureWindowPath(from));
                } else {
                    execArrayCommandGrabbed("mkdir", ensureWindowPath(from));
                }
            }
            default: {
                if (p) {
                    execArrayCommandGrabbed("mkdir", "-p", from);
                } else {
                    execArrayCommandGrabbed("mkdir", from);
                }
            }
        }
    }

    private String ensureWindowPath(String from) {
        from = from.replace("/", "\\");
        if (from.matches("\\\\[a-zA-Z]:.*")) {
            from = from.substring(1);
        }
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
        if (getProbedInfo() != null) {
            shellFamily = getProbedInfo().shellFamily();
        }
        return shellFamily;
    }

    private OsProbeInfo getProbedInfo() {
        if (probedInfo == null) {
            OsProbeInfo osProbeInfo = OsProbeInfoCache.of().get(connexionString);
            osProbeInfo.tryUpdate();
            probedInfo = osProbeInfo;
        }
        return probedInfo;
    }

    private NOsFamily resolveOsFamily() {
        NOsFamily nOsFamily = NOsFamily.LINUX;
        if (getProbedInfo() != null) {
            nOsFamily = getProbedInfo().osFamily();
        }
        return nOsFamily;
    }

    @Override
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

    @Override
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

    @Override
    public InputStream getInputStream(String from) {
        return getInputStream(from, false);
    }

    @Override
    public InputStream getInputStream(String from, boolean closeConnection) {
        return new SshFileInputStream(this, from, closeConnection);
    }

    @Override
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
                out.write(ex.toString().getBytes());
            }
            channel.disconnect();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (JSchException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    @Override
    public boolean isAlive() {
        return !closed && sshSession.isConnected();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void close() {
        if (!closed) {
            sshSession.disconnect();
            closed = true;
        }
    }

    @Override
    public List<String> list(String path) {
        switch (resolveShellFamily()) {
            case WIN_CMD:
            case WIN_POWER_SHELL: {
                IOResult i = execArrayCommandGrabbed("powershell", "-c", "Get-ChildItem '" + ensureWindowPath(path) + "'");
                if (i.code() == 0) {
                    //continue parsing
                    String[] s = i.outString().split("[\n|\r]");
                    Pattern compiled = Pattern.compile("(?<f>[a-z-]{6})[ \t]*(?<d>[0-9-/]{8,10})[ \t]*(?<t>[0-9]{2}:[0-9]{2})[ \t]*(?<p>.*)");
                    return NStream.ofArray(s).map(
                            x -> {
                                x = x.trim();
                                if (x.length() > 0) {
                                    Matcher m = compiled.matcher(x);
                                    if (m.find()) {
                                        String cc = path;
                                        if (!cc.endsWith("/")) {
                                            cc += "/";
                                        }
                                        cc += m.group("p");
                                        if (m.group("f").startsWith("d")) {
                                            cc += "/";
                                        }
                                        return cc;
                                    }
                                }
                                return null;
                            }
                    ).filter(x -> x != null).collect(Collectors.toList());
                }
                break;
            }
            case SH:
            case BASH:
            case ZSH:
            case FISH:
            case KSH:
            case CSH: {
                IOResult i = execArrayCommandGrabbed("ls", path);
                if (i.code() == 0) {
                    String[] s = i.outString().split("[\n|\r]");
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

    @Override
    public long contentLength(String basePath) {
        IOResult i = execArrayCommandGrabbed("ls", "-l", basePath);
        if (i.code() != 0) {
            return -1;
        }
        String outputString = i.outString();
        String[] r = NStringUtils.trim(outputString).split(" ");
        if (r.length > 4) {
            NOptional<Long> size = NLiteral.of(r[4]).asLong();
            if (size.isPresent()) {
                return size.get();
            }
        }
        return -1;
    }

    @Override
    public String getContentEncoding(String basePath) {
        IOResult i = execArrayCommandGrabbed("file", "-bi", basePath);
        if (i.code() != 0) {
            return null;
        }
        String outputString = NStringUtils.trim(i.outString());
        Pattern p = Pattern.compile(".*charset=(?<cs>\\S*).*");
        Matcher m = p.matcher(outputString);
        if (m.find()) {
            return m.group("cs");
        }
        return null;
    }

    @Override
    public String getContentType(String basePath) {
        IOResult i = execArrayCommandGrabbed("file", "-bi", basePath);
        if (i.code() != 0) {
            return null;
        }
        String outputString = i.outString();
        String[] r = Arrays.stream(NStringUtils.trim(outputString).split("[ ;]")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
        if (r.length > 0) {
            return NStringUtils.trim(r[0]);
        }
        return null;
    }

    @Override
    public String getCharset(String basePath) {
        IOResult i = execArrayCommandGrabbed("file", "-bi", basePath);
        if (i.code() != 0) {
            return null;
        }
        String outputString = i.outString();
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

    @Override
    public void cp(String path, String path1, boolean b) {
        if (b) {
            execArrayCommandGrabbed("cp", path, path1);
        } else {
            execArrayCommandGrabbed("cp", "-r", path, path1);
        }
    }

    @Override
    public Channel openExecChannel(String command) {
        Channel channel = null;
        try {
            channel = sshSession.openChannel("exec");
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        ((ChannelExec) channel).setCommand(command);
        return channel;
    }

    @Override
    public List<String> walk(String path, boolean followLinks, int maxDepth) {
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
                IOResult i = execStringCommandGrabbed(cmd.toString());
                if (i.code() == 0) {
                    String[] s = i.outString().split("[\n|\r]");
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

    public byte[] getDigestWithCommand(String algo, String basePath) {
        String cmdsum = null;
        switch (algo) {
            case "SHA-1": {
                cmdsum = "sha1sum";
                break;
            }
            case "SHA-256": {
                cmdsum = "sha256sum";
                break;
            }
            case "SHA-224": {
                cmdsum = "sha224sum";
                break;
            }
            case "SHA-512": {
                cmdsum = "sha512sum";
                break;
            }
            case "MD5": {
                cmdsum = "md5sum";
                break;
            }
        }
        if (cmdsum != null) {
            IOResult r = execArrayCommandGrabbed(cmdsum, basePath);
            if (r.code() == 0) {
                String z = NStringUtils.trim(r.outString());
                int i = z.indexOf(' ');
                if (i > 0) {
                    z = z.substring(0, i);
                    return NHex.toBytes(z);
                }
            }
        }
        return null;
    }

}
