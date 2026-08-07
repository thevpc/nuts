package net.thevpc.nuts.runtime.standalone.util.collections;

import java.io.OutputStream;

public class ReusableByteArrayOutputStream extends OutputStream {
    private byte[] buf;
    private int count;

    public ReusableByteArrayOutputStream(int initialCapacity) {
        this.buf = new byte[initialCapacity];
    }

    public void reset() {
        this.count = 0;
    }

    public byte[] getBuffer() {
        return buf;
    }

    public int size() {
        return count;
    }

    @Override
    public void write(int b) {
        ensureCapacity(count + 1);
        buf[count++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - buf.length > 0) {
            byte[] newBuf = new byte[Math.max(buf.length << 1, minCapacity)];
            System.arraycopy(buf, 0, newBuf, 0, count);
            buf = newBuf;
        }
    }
}
