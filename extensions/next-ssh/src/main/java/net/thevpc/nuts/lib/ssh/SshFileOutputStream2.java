package net.thevpc.nuts.lib.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.util.NConnexionString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class SshFileOutputStream2 extends OutputStream {
    private NSession session;
    private NPath temp;
    private NConnexionString path;
    private boolean mkdirs;
    private OutputStream tempOS;

    public SshFileOutputStream2(NConnexionString path, NSession nSession, boolean mkdirs) {
        super();
        this.session = nSession;
        this.path = path;
        this.mkdirs = mkdirs;
        this.temp = NPaths.of(session).createTempFile();
        this.tempOS = this.temp.getOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte) b;
        this.write(buf, 0, 1);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        tempOS.write(buffer, offset, length);
    }

    @Override
    public void close() throws IOException {
        tempOS.close();
        try (SShConnection connection = new SShConnection(path, session)) {
            connection.copyLocalToRemote(temp.toString(), path.getPath(), mkdirs);
        } finally {
            this.temp.delete();
        }
    }
}
