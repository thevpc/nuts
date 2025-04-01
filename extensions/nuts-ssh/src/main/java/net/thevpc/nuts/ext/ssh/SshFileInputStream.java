package net.thevpc.nuts.ext.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NConnexionString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SshFileInputStream extends DynamicInputStream {
    private final String from;
    private boolean init;
    private long filesize;
    private byte[] buf;
    private Channel channel;
    private OutputStream out;
    private InputStream in;
    private boolean closeConnection;
    private SShConnection connection;
    public SshFileInputStream(NConnexionString path) {
        super(4096);
        this.from = path.getPath();
        init = false;
        filesize = 0L;
        buf = new byte[1024];
        this.closeConnection = true;
        NSession session = NSession.of();
        SShConnection connection = new SShConnection(path
                ,session.in()
                ,session.out().asOutputStream()
                ,session.err().asOutputStream()
        );
        this.connection = connection;
    }

    SshFileInputStream(SShConnection connection, String path, boolean closeConnection) {
        super(4096);
        this.from = path;
        init = false;
        filesize = 0L;
        buf = new byte[1024];
        this.closeConnection = closeConnection;
        this.connection = connection;
    }

    @Override
    protected boolean requestMore() throws IOException {
        if (!init) {
            init = true;
            // exec 'scp -f rfile' remotely
            String command = "scp -f " + from;
            try {
                channel = connection.sshSession.openChannel("exec");
            } catch (JSchException e) {
                throw new IOException(e);
            }
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            out = channel.getOutputStream();
            in = channel.getInputStream();

            try {
                channel.connect();
            } catch (JSchException e) {
                throw new IOException(e);
            }


            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            int c = SShConnection.checkAck(in);
            if (c != 'C') {
                return false;
            }

            // readAll '0644 '
            in.read(buf, 0, 5);

            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    break;
                }
                if (buf[0] == ' ') break;
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

            //System.out.println("file-size=" + filesize + ", file=" + file);

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
        }
        int foo;
        if (buf.length < filesize) foo = buf.length;
        else foo = (int) filesize;
        foo = in.read(buf, 0, foo);
        if (foo < 0) {
            // error
            return false;
        } else {
            this.push(buf, 0, foo);
            filesize -= foo;
            if (filesize == 0L) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        if (SShConnection.checkAck(in) != 0) {
            //System.exit(0);
            if (closeConnection) {
                connection.close();
            }
            return;
        }

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
        channel.disconnect();
        if (closeConnection) {
            connection.close();
        }
    }
}
