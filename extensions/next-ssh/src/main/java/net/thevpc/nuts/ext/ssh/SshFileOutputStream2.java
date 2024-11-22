package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NConnexionString;

import java.io.IOException;
import java.io.OutputStream;

public class SshFileOutputStream2 extends OutputStream {
    private NWorkspace workspace;
    private NPath temp;
    private NConnexionString path;
    private boolean mkdirs;
    private OutputStream tempOS;

    public SshFileOutputStream2(NConnexionString path, NWorkspace workspace, boolean mkdirs) {
        super();
        this.workspace = workspace;
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
        NSession session = workspace.currentSession();
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
