package net.thevpc.nuts.ext.ssh;

import com.jcraft.jsch.*;
import net.thevpc.nuts.NLiteral;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NStringMapFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class SShConnection implements AutoCloseable {

    Session sshSession;
    NSession nSession;
    private boolean redirectErrorStream;
    private boolean failFast;
    private PrintStream out = new PrintStream(new NonClosableOutputStream(System.out));
    private PrintStream err = new PrintStream(new NonClosableOutputStream(System.err));
    private InputStream in;
    private List<SshListener> listeners = new ArrayList<>();

    public SShConnection(String address, InputStream in, OutputStream out, OutputStream err, NSession sshSession) {
        this(NConnexionString.of(address).get(), in, out, err, sshSession);
    }

    public SShConnection(NConnexionString address, InputStream in, OutputStream out, OutputStream err, NSession sshSession) {
        init(address.getUser(), address.getHost(),
                NLiteral.of(address.getPort()).asInt().orElse(-1),
                NStringMapFormat.URL_FORMAT.parse(address.getQueryString())
                        .orElse(Collections.emptyMap()).get("key-file"),
                address.getPassword(), in, out, err, sshSession);
    }

    public SShConnection(String user, String host, int port, String keyFilePath, String keyPassword, InputStream in, OutputStream out, OutputStream err, NSession sshSession) {
        init(user, host, port, keyFilePath, keyPassword, in, out, err, sshSession);
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

    private void init(String user, String host, int port, String keyFilePath, String keyPassword, InputStream in0, OutputStream out0, OutputStream err0, NSession session) {
        this.nSession = session;
        this.out = new PrintStream(new NonClosableOutputStream(out0));
        this.err = new PrintStream(new NonClosableOutputStream(err0));
        this.in = in0;
        try {
            JSch jsch = new JSch();

            if (keyFilePath == null && keyPassword == null) {
                keyFilePath = new File(System.getProperty("user.home"), ".ssh/id_rsa").getPath();
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
                    new NConnexionString().setUser(user).setHost(host).setPort(String.valueOf(port)).setPassword(keyPassword).setQueryString(
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
                    while(true) {
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
        execStringCommand("rm " + (R ? "-R" : "") + " " + from);
    }

    public void mkdir(String from, boolean p) {
        execStringCommand("mkdir " + (p ? "-p" : "") + " " + from);
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
                // but it is not accessible with JavaAPI ;-<
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
    }

    public InputStream prepareStream(File file) throws FileNotFoundException {
        FileInputStream in = new FileInputStream(file);
        NMsg path = NMsg.ofStyled(file.getPath(), NTextStyle.path());
        for (SshListener listener : listeners) {
            InputStream v = listener.monitorInputStream(in, file.length(), path);
            if (v != null) {
                return v;
            }
        }
        return in;
    }
}
