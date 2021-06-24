package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

class SshNutsPath implements NutsPathSPI {
    private SshPath path;
    private NutsSession session;
    private SshListener listener;

    public SshNutsPath(SshPath path, NutsSession session) {
        this.path = path;
        this.session = session;
    }

    @Override
    public String asString() {
        return path.toString();
    }

    @Override
    public InputStream inputStream() {
        return new SshFileInputStream(path);
    }

    @Override
    public OutputStream outputStream() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported output stream for %s",toString()));
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    public void delete(boolean recurse) {
        try (SShConnection session = new SShConnection(path.toAddress())
                .addListener(listener)
        ) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents) {
        try (SShConnection c = new SShConnection(path.toAddress())
                .addListener(listener)
        ) {
            c.mkdir(path.getPath(), parents);
        }
    }

    @Override
    public String location() {
        return path.getPath();
    }

    @Override
    public boolean exists() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported exists for %s",toString()));
    }

    @Override
    public Instant lastModifiedInstant() {
        return null;
    }
}
