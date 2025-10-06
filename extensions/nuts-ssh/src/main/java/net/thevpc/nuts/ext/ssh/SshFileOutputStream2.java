package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnexionString;

import java.io.IOException;
import java.io.OutputStream;

public class SshFileOutputStream2 extends OutputStream {
    private NPath temp;
    private NConnexionString path;
    private boolean mkdirs;
    private OutputStream tempOS;

    public SshFileOutputStream2(NConnexionString path, boolean mkdirs) {
        super();
        this.path = path;
        this.mkdirs = mkdirs;
        this.temp = NPath.ofTempFile();
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
        NSession session = NSession.of();
        try (SShConnection connection = new SShConnection(path
                , session.in()
                , session.out().asOutputStream()
                , session.err().asOutputStream()

        )) {
            connection.copyLocalToRemote(temp.toString(), path.getPath(), mkdirs);
        } finally {
            this.temp.delete();
        }
    }
}
