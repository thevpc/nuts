package net.thevpc.nuts.ext.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import net.thevpc.nuts.net.NConnectionString;

import java.io.IOException;
import java.io.InputStream;

public class SshFileInputStreamSftp extends DynamicInputStream {

    private final String remotePath;
    private final boolean closeConnection;
    private final SshConnection connection;

    private boolean init = false;

    private ChannelSftp sftp;
    private InputStream handle;

    private final byte[] buf = new byte[4096]; // bigger chunks for SFTP

    public SshFileInputStreamSftp(NConnectionString path) {
        super(4096);
        this.remotePath = path.getPath();
        this.closeConnection = true;
        this.connection = SshConnectionPool.of().acquire(path);
    }

    public SshFileInputStreamSftp(SshConnection connection, String path, boolean closeConnection) {
        super(4096);
        this.remotePath = path;
        this.closeConnection = closeConnection;
        this.connection = connection;
    }

    @Override
    protected boolean requestMore() throws IOException {
        try {
            if (!init) {
                init = true;

                // open SFTP
                JCshSshConnection baseConnection = (JCshSshConnection) ((SshConnectionAdapter) connection).connection;
                sftp = (ChannelSftp) baseConnection.sshSession.openChannel("sftp");
                sftp.connect();
                // open file handle
                handle = sftp.get(remotePath);
            }
            int count=handle.read(buf, 0, buf.length);
            if (count < 0) {
                return false;
            }

            push(buf, 0, count);
            return true;

        } catch (IOException e) {
            throw new IOException(e);
        } catch (JSchException e) {
            throw new IOException(e);
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (sftp != null && handle != null) {
                handle.close();
            }
        } catch (IOException ignore) {
            // safe to ignore
        }

        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
        }

        if (closeConnection) {
            connection.close();
        }
    }
}