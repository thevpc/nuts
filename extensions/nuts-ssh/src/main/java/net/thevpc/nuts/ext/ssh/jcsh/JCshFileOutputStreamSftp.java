package net.thevpc.nuts.ext.ssh.jcsh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import net.thevpc.nuts.ext.ssh.SshConnection;
import net.thevpc.nuts.ext.ssh.SshConnectionAdapter;
import net.thevpc.nuts.ext.ssh.SshConnectionPool;
import net.thevpc.nuts.net.NConnectionString;

import java.io.IOException;
import java.io.OutputStream;

public class JCshFileOutputStreamSftp extends OutputStream {
    private final SshConnection connection;
    private final String remotePath;
    private final boolean mkdirs;
    private final boolean closeConnection;

    private boolean started = false;
    private boolean closed = false;

    private ChannelSftp sftp;
    private OutputStream out;

    public JCshFileOutputStreamSftp(NConnectionString path, boolean mkdirs, boolean closeConnection) {
        this.connection = SshConnectionPool.of().acquire(path);
        this.remotePath = path.getPath();
        this.mkdirs = mkdirs;
        this.closeConnection = closeConnection;
    }

    public JCshFileOutputStreamSftp(SshConnection connection, String remotePath, boolean mkdirs, boolean closeConnection) {
        this.connection = connection;
        this.remotePath = remotePath;
        this.mkdirs = mkdirs;
        this.closeConnection = closeConnection;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        if (closed) return;
        if (!started) startSftpUpload();
        out.write(buf, off, len);
    }

    private void startSftpUpload() throws IOException {
        try {
            started = true;

            JCshConnection base = JCshConnection.unwrap(connection);
            sftp = (ChannelSftp) base.sshSession.openChannel("sftp");
            sftp.connect();

            // mkdirs if needed
            if (mkdirs) {
                createRemoteDirs(sftp, remotePath);
            }

            // now open the file for writing (OVERWRITE mode)
            out = sftp.put(remotePath, ChannelSftp.OVERWRITE);

        } catch (JSchException e) {
            throw new IOException("Failed to start SFTP upload", e);
        } catch (SftpException e) {
            throw new IOException("Failed to start SFTP upload", e);
        }
    }

    private void createRemoteDirs(ChannelSftp sftp, String path) {
        String parent = path.contains("/")
                ? path.substring(0, path.lastIndexOf('/'))
                : null;

        if (parent == null || parent.isEmpty()) return;

        String[] parts = parent.split("/");
        StringBuilder sb = new StringBuilder();

        for (String p : parts) {
            if (p.isEmpty()) continue; // skip leading slash
            sb.append('/').append(p);
            try {
                sftp.stat(sb.toString());
            } catch (SftpException e) {
                try {
                    sftp.mkdir(sb.toString());
                } catch (SftpException ignored) {
                    // directory creation may race — ignore
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;

        IOException err = null;

        if (out != null) {
            try { out.flush(); } catch (IOException e) { err = e; }
            try { out.close(); } catch (IOException e) { if (err == null) err = e; }
        }

        if (sftp != null && sftp.isConnected()) {
            sftp.disconnect();
        }

        if (closeConnection) {
            try { connection.close(); } catch (Exception ignored) {}
        }

        if (err != null) throw err;
    }
}
