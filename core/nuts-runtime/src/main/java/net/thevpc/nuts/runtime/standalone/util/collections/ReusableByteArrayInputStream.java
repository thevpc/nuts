package net.thevpc.nuts.runtime.standalone.util.collections;

import java.io.InputStream;

public class ReusableByteArrayInputStream extends InputStream {
    private byte[] buf;
    private int pos;
    private int count;

    public ReusableByteArrayInputStream() {
        this.buf = new byte[0];
    }

    public void setBuffer(byte[] buf, int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
    }

    @Override
    public int read() {
        return (pos < count) ? (buf[pos++] & 0xFF) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (pos >= count) return -1;
        int toRead = Math.min(len, count - pos);
        System.arraycopy(buf, pos, b, off, toRead);
        pos += toRead;
        return toRead;
    }

    @Override
    public int available() {
        return count - pos;
    }
}
