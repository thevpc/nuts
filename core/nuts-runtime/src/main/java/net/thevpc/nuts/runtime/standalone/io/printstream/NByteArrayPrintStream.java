package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractMultiReadNInputSource;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.util.Arrays;

public class NByteArrayPrintStream extends NPrintStreamRaw implements NMemoryPrintStream {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    public NByteArrayPrintStream(NTerminalMode mode, NWorkspace workspace) {
        super(new ByteArrayOutputStream2(), mode, null, null, new Bindings(), null);
        getMetaData().setMessage(
                NMsg.ofNtf(NText.ofStyledPath("<memory-buffer>"))
        );
    }

    protected NByteArrayPrintStream(NTerminalMode mode, ByteArrayOutputStream2 bos, NWorkspace workspace) {
        super(bos, mode, null, null, new Bindings(), null);
        getMetaData().setMessage(
                NMsg.ofNtf(NText.ofStyledPath("<memory-buffer>"))
        );
    }

    protected NPrintStream printParsed(NText b) {
        switch (getTerminalMode()) {
            case FILTERED:{
                NText transformed = txt().transform(b,
                        new NTextTransformConfig()
                                .setFiltered(true)
                                .setNormalize(true)
                                .setFlatten(true)
                );
                print(transformed.toString());
                return this;
            }
        }
        print(b.toString());
        return this;
    }

    @Override
    public byte[] getBytes() {
        flush();
        return out2().toByteArray();
    }

    private ByteArrayOutputStream2 out2() {
        return (ByteArrayOutputStream2) out;
    }

    @Override
    public String toString() {
        flush();
        return out2().toString();
    }

    @Override
    public OutputStream getOutputStream() {
        return asOutputStream();
    }

    @Override
    public NInputSource asInputSource() {
        return new MyAbstractMultiReadNInputSource(this);
    }

    public static class InputStreamFromByteArrayOutputStream2 extends InputStream {
        protected ByteArrayOutputStream2 buf;
        protected int pos;
        protected int mark = 0;

        public InputStreamFromByteArrayOutputStream2(ByteArrayOutputStream2 buf) {
            this.buf = buf;
            this.pos = 0;
        }

        public synchronized int read() {
            if (pos < this.buf.size()) {
                return buf.read(pos++) & 0xff;
            }
            return -1;
        }

        public synchronized int read(byte[] b, int off, int len) {
            int r = buf.read(b, off, len, pos);
            if(r>0){
                pos+=r;
            }
            return r;
        }

        public synchronized long skip(long n) {
            int count = buf.size();
            long k = count - pos;
            if (n < k) {
                k = n < 0 ? 0 : n;
            }

            pos += k;
            return k;
        }

        public synchronized int available() {
            int count = buf.size();
            return count - pos;
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int readAheadLimit) {
            mark = pos;
        }

        public synchronized void reset() {
            pos = mark;
        }

        public void close() {
        }
    }

    public static class ByteArrayOutputStream2 extends OutputStream {

        protected byte buf[];
        protected int count;

        public ByteArrayOutputStream2() {
            this(32);
        }

        public ByteArrayOutputStream2(int size) {
            if (size < 0) {
                throw new IllegalArgumentException("Negative initial size: "
                        + size);
            }
            buf = new byte[size];
        }

        public InputStream asInputStream() {
            return new InputStreamFromByteArrayOutputStream2(this);
        }

        private void ensureCapacity(int minCapacity) {
            // overflow-conscious code
            if (minCapacity - buf.length > 0) {
                grow(minCapacity);
            }
        }


        private void grow(int minCapacity) {
            // overflow-conscious code
            int oldCapacity = buf.length;
            int newCapacity = oldCapacity << 1;
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = hugeCapacity(minCapacity);
            buf = Arrays.copyOf(buf, newCapacity);
        }


        public synchronized void write(int b) {
            ensureCapacity(count + 1);
            buf[count] = (byte) b;
            count += 1;
        }

        public synchronized void write(byte b[], int off, int len) {
            if ((off < 0) || (off > b.length) || (len < 0) ||
                    ((off + len) - b.length > 0)) {
                throw new IndexOutOfBoundsException();
            }
            ensureCapacity(count + len);
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }

        public synchronized void writeTo(OutputStream out) throws IOException {
            out.write(buf, 0, count);
        }

        public synchronized void reset() {
            count = 0;
        }

        public synchronized byte toByteArray()[] {
            return Arrays.copyOf(buf, count);
        }

        public synchronized int size() {
            return count;
        }

        public synchronized String toString() {
            return new String(buf, 0, count);
        }

        public synchronized String toString(String charsetName)
                throws UnsupportedEncodingException {
            return new String(buf, 0, count, charsetName);
        }


        public void close() {
        }

        public synchronized int available(int from) {
            int i = count - from;
            return i >= 0 ? i : 0;
        }

        public synchronized int read(int pointer) {
            if (pointer >= 0 && pointer < count) {
                return buf[pointer];
            }
            return -1;
        }

        public synchronized int read(byte[] buffer, int off, int len, int pointer) {
            if (buffer == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > buffer.length - off) {
                throw new IndexOutOfBoundsException();
            }

            if (pointer >= count) {
                return -1;
            }

            int avail = count - pointer;
            if (len > avail) {
                len = avail;
            }
            if (len <= 0) {
                return 0;
            }
            System.arraycopy(buf, pointer, buffer, off, len);
            return len;
        }

    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public static class MyAbstractMultiReadNInputSource extends AbstractMultiReadNInputSource {
        private NByteArrayPrintStream value;

        public MyAbstractMultiReadNInputSource(NByteArrayPrintStream value) {
            super();
            this.value = value;
        }

        public NByteArrayPrintStream getValue() {
            return value;
        }

        @Override
        public InputStream getInputStream() {
            return value.out2().asInputStream();
        }

        @Override
        public NContentMetadata getMetaData() {
            return value.getMetaData();
        }

        @Override
        public boolean isKnownContentLength() {
            return true;
        }

        @Override
        public long contentLength() {
            return value.out2().size();
        }
    }
}
