package net.thevpc.nuts.lib.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NConnexionString;

import java.io.*;

public class SshFileOutputStream extends OutputStream {
    private SShConnection connection;
    private NSession session;
    private String to;
    private boolean mkdirs;
    private boolean failFast;
    private boolean started = false;
    private boolean ended = false;
    private long filesize;

    private OutputStream out;
    private InputStream in;
    private Channel channel;

    public SshFileOutputStream(NConnexionString path, NSession nSession, boolean mkdirs, boolean failFast, long filesize) {
        super();
        this.session = nSession;
        this.connection = new SShConnection(path, nSession);
        this.mkdirs = mkdirs;
        this.to = path.getPath();
        this.failFast = failFast;
        this.filesize = filesize;
    }

    @Override
    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte) b;
        this.write(buf, 0, 1);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        try {
            if (ended) {
                return;
            }
            if (!started) {
                if (_start()) return;
            }
            out.write(buffer, offset, length); //out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (JSchException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    private boolean _start() throws JSchException, IOException {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("missing target path");
        }
        if (mkdirs) {
            boolean off = failFast;
            failFast = false;
            if (to.endsWith("/")) {
                connection.mkdir(to, true);
            } else if (to.contains("/")) {
                String p = to.substring(0, to.lastIndexOf('/'));
                if (p.length() > 0) {
                    connection.mkdir(p, true);
                }
            }
            failFast = off;
        }
        boolean ptimestamp = true;

        // exec 'scp -t rfile' remotely
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + to;
        channel = connection.sshSession.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        out = channel.getOutputStream();
        in = channel.getInputStream();

        channel.connect();

        if (connection.checkAck(in) != 0) {
            ended = true;
            connection.close();
            return true;
        }


        long lastModified = 0;
        if (ptimestamp) {
            command = "T" + (lastModified / 1000) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += (" " + (lastModified / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (connection.checkAck(in) != 0) {
                ended = true;
                connection.close();
                return true;
            }
        }

        // send "C0644 filesize filename", where filename should not include '/'
        command = "C0644 " + filesize + " ";
        command += NPath.of(to, session).getName();

        command += "\n";
        out.write(command.getBytes());
        out.flush();

        if (connection.checkAck(in) != 0) {
            ended = true;
            connection.close();
            return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        byte[] buf = new byte[1];
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        if (connection.checkAck(in) != 0) {
            return;
        }
        out.close();

        channel.disconnect();
        this.connection.close();
        super.close();
    }
}
