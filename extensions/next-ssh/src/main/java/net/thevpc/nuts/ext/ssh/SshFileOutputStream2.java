package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.common.str.NConnexionString;

import java.io.IOException;
import java.io.OutputStream;

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
        this.temp = NPath.ofTempFile(session);
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
        try (SShConnection connection = new SShConnection(path
                ,session.in()
                ,session.out().asOutputStream()
                ,session.err().asOutputStream()
                , session

        )) {
            connection.copyLocalToRemote(temp.toString(), path.getPath(), mkdirs);
        } finally {
            this.temp.delete();
        }
    }
}
