package net.thevpc.nuts.ext.ssh.jcsh;

import com.jcraft.jsch.*;
import net.thevpc.nuts.ext.ssh.*;
import net.thevpc.nuts.ext.ssh.util.SshUtils;
import net.thevpc.nuts.io.NonClosableInputStream;
import net.thevpc.nuts.io.NonClosableOutputStream;
import net.thevpc.nuts.io.NullOutputStream;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.*;

public class JCshConnection extends SshConnectionBase {

    public static final String USE = "use";
    Session sshSession;

    private boolean closed;
    private boolean useFtp = true;

    public static JCshConnection unwrap(SshConnection any) {
        if(any instanceof SshConnectionAdapter) {
            return unwrap(((SshConnectionAdapter) any).getConnection());
        }
        return (JCshConnection) any;
    }

    public JCshConnection(NConnectionString connectionString) {
        init(connectionString);
    }


    private void init(NConnectionString connectionString) {
        this.connectionString = connectionString;
        String user = connectionString.getUserName();
        String host = connectionString.getHost();
        int port = NLiteral.of(connectionString.getPort()).asInt().orElse(-1);
        String keyFilePath = connectionString.builder().getQueryParam(SshConnection.IDENTITY_FILE).orNull();
        String keyPassword = connectionString.getPassword();
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
                    NConnectionStringBuilder.of().setUserName(user).setHost(host).setPort(String.valueOf(port)).setPassword(keyPassword).setQueryString(
                            keyFilePath == null ? null : NStringMapFormat.URL_FORMAT
                                    .format(
                                            NMaps.of(SshConnection.IDENTITY_FILE, keyFilePath)
                                    )
                    ) + ")", e));
        }
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
            },"JCshConnectionChannelThread").start();
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

//    @Override
//    public byte[] readRemoteFile(String from) {
//        if (useFtp) {
//            return readRemoteFileSftp(from);
//        }
//        return readRemoteFileScp(from);
//    }

    protected byte[] readRemoteFileSftp(String from) {
        try {
            ChannelSftp sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            sftp.get(from, output);
            sftp.disconnect();
            return output.toByteArray();
        } catch (SftpException ex2) {
            throw new UncheckedIOException(new IOException(ex2));
        } catch (JSchException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    protected byte[] readRemoteFileScp(String from) {
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
                int c = SshUtils.checkAck(in);
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

                if (SshUtils.checkAck(in) != 0) {
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
        if (useFtp) {
            copyRemoteToLocalSftp(from, to, mkdir);
        } else {
            copyRemoteToLocalScp(from, to, mkdir);
        }
    }

    protected void copyRemoteToLocalSftp(String from, String to, boolean mkdir) {
        try {
            for (SshListener listener : listeners) {
                listener.onGet(from, to, mkdir);
            }

            // Ensure parent directories exist locally if requested
            if (mkdir) {
                File parent = new File(to).getParentFile();
                if (parent != null && !parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new IOException("Failed to create directories: " + parent);
                    }
                }
            }

            ChannelSftp sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();

            // If 'to' is a directory, append the filename
            File localFile = new File(to);
            if (localFile.isDirectory()) {
                String filename = new File(from).getName();
                localFile = new File(localFile, filename);
            }

            try (FileOutputStream fos = new FileOutputStream(localFile)) {
                sftp.get(from, fos);
            }

            sftp.disconnect();
        } catch (JSchException e) {
            throw new UncheckedIOException(new IOException("SSH error", e));
        } catch (SftpException e) {
            throw new UncheckedIOException(new IOException("SFTP error", e));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void copyRemoteToLocalScp(String from, String to, boolean mkdir) {
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
                int c = SshUtils.checkAck(in);
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

                if (SshUtils.checkAck(in) != 0) {
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
        if (useFtp) {
            return new JCshFileInputStreamSftp(_connectionStringBuilder().setPath(from).build());
        } else {
            return new JCshFileInputStreamScp(_connectionStringBuilder().setPath(from).build());
        }
    }

    private NConnectionStringBuilder _connectionStringBuilder() {
        return connectionString.builder().setQueryParam(USE, "jcsh");
    }

    @Override
    public OutputStream getOutputStream(String from) {
        if (useFtp) {
            return new JCshFileOutputStreamSftp(_connectionStringBuilder().setPath(from).build(), false, true);
        }
        return new JCshFileOutputStreamScp(_connectionStringBuilder().setPath(from).build(), false);
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
                boolean off = isFailFast();
                setFailFast(false);
                if (to.endsWith("/")) {
                    mkdir(to, true);
                } else if (to.contains("/")) {
                    String p = to.substring(0, to.lastIndexOf('/'));
                    if (p.length() > 0) {
                        mkdir(p, true);
                    }
                }
                setFailFast(off);
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

            if (SshUtils.checkAck(in) != 0) {
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
                if (SshUtils.checkAck(in) != 0) {
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

            if (SshUtils.checkAck(in) != 0) {
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

            if (SshUtils.checkAck(in) != 0) {
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

}
