package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsSession;

import java.io.*;

public class NanoDBDefaultOutputStream implements NanoDBOutputStream {
    private DataOutputStream dos;
    private NanoDBOutputStreamCounter counter;
    private NutsSession session;

    public NanoDBDefaultOutputStream(OutputStream out,NutsSession session) {
        counter = new NanoDBOutputStreamCounter(out);
        dos = new DataOutputStream(counter);
        this.session=session;
    }

    @Override
    public void writeLob(String name, InputStream in) {
        try {
            byte[] x = new byte[1024 * 12];
            while (true) {
                int v = in.read(x);
                if (v <= 0) {
                    dos.writeInt(-1);
                    break;
                } else {
                    dos.writeInt(v);
                    dos.write(v);
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public long getPosition() {
            return counter.getCounter();
    }

    @Override
    public void writeBoolean(boolean val) {
        try {
            dos.writeBoolean(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeByte(int val) {
        try {
            dos.writeByte(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeShort(int val) {
        try {
            dos.writeShort(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeChar(int val) {
        try {
            dos.writeChar(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeInt(int val) {
        try {
            dos.writeInt(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeLong(long val) {
        try {
            dos.writeLong(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeFloat(float val) {
        try {
            dos.writeFloat(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeDouble(double val) {
        try {
            dos.writeDouble(val);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeBytes(String str) {
        try {
            dos.writeBytes(str);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeChars(String str) {
        try {
            dos.writeChars(str);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void writeUTF(String str) {
        try {
            dos.writeUTF(str);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void close() {
        try {
            dos.close();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    @Override
    public void flush() {
        try {
            dos.flush();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

//    @Override
//    public void writeObject(Object obj) {
//        try {
//            dos.writeObject(obj);
//        } catch (IOException ex) {
//            throw new NutsIOException(session,ex);
//        }
//    }
}
