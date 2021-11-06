package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsSession;

import java.io.*;

class NanoDBDefaultInputStream implements NanoDBInputStream {
    private DataInputStream in;
    private NutsSession session;

    public NanoDBDefaultInputStream(InputStream in,NutsSession session) {
        this.in =(in instanceof DataInputStream)?(DataInputStream) in:new DataInputStream(in);
        this.session=session;
    }

    @Override
    public InputStream readLob(String name) {
        throw new UnsupportedOperationException("unsupported operation: readLob");
    }

    @Override
    public int readInt() {
        try {
            return in.readInt();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public long readLong() {
        try {
            return in.readLong();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public float readFloat() {
        try {
            return in.readFloat();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public double readDouble() {
        try {
            return in.readDouble();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public String readUTF() {
        try {
            return in.readUTF();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public byte readByte() {
        try {
            return in.readByte();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public boolean readBoolean() {
        try {
            return in.readBoolean();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }
//    @Override
//    public Object readObject() {
//        try {
//            return is.readObject();
//        } catch (IOException ex) {
//            throw new NutsIOException(session,ex);
//        } catch (ClassNotFoundException e) {
//            throw new NutsIOException(session,e);
//        }
//    }
}
