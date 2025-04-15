package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.CharBuffer;

public class AppendableWriter extends Writer {

    protected Appendable sb;

    public AppendableWriter(Appendable sb) {
        this.sb = sb;
    }

    public static AppendableWriter of(StringBuilder a) {
        return new StringBuilderWriter((StringBuilder) a);
    }

    public static AppendableWriter of(StringBuffer a) {
        return new StringBufferWriter((StringBuffer) a);
    }

    public static AppendableWriter of(CharBuffer a) {
        return new CharBufferWriter((CharBuffer) a);
    }

    public static AppendableWriter of(Appendable a) {
        if (a instanceof StringBuilder) {
            return new StringBuilderWriter((StringBuilder) a);
        }
        if (a instanceof StringBuffer) {
            return new StringBufferWriter((StringBuffer) a);
        }
        if (a instanceof CharBuffer) {
            return new CharBufferWriter((CharBuffer) a);
        }
        return new AppendableWriter(a);
    }

    public String toString() {
        return sb.toString();
    }

    private static class StringBuilderWriter extends AppendableWriter {
        StringBuilder sb;

        public StringBuilderWriter(StringBuilder sb) {
            super(sb);
            this.sb = sb;
        }

        public void write(char[] bytes, int offset, int count) {
//            if ((offset < 0) || (offset > bytes.length) || (count < 0) ||
//                    ((offset + count) > bytes.length) || ((offset + count) < 0)) {
//                throw new IndexOutOfBoundsException();
//            } else if (count == 0) {
//                return;
//            }
            sb.append(bytes, offset, count);
        }
    }

    private static class StringBufferWriter extends AppendableWriter {
        StringBuffer sb;

        public StringBufferWriter(StringBuffer sb) {
            super(sb);
            this.sb = sb;
        }

        public void write(char[] bytes, int offset, int count) {
//            if ((offset < 0) || (offset > bytes.length) || (count < 0) ||
//                    ((offset + count) > bytes.length) || ((offset + count) < 0)) {
//                throw new IndexOutOfBoundsException();
//            } else if (count == 0) {
//                return;
//            }
            sb.append(bytes, offset, count);
        }
    }

    private static class CharBufferWriter extends AppendableWriter {
        CharBuffer sb;

        public CharBufferWriter(CharBuffer sb) {
            super(sb);
            this.sb = sb;
        }

        public void write(char[] bytes, int offset, int count) {
//            if ((offset < 0) || (offset > bytes.length) || (count < 0) ||
//                    ((offset + count) > bytes.length) || ((offset + count) < 0)) {
//                throw new IndexOutOfBoundsException();
//            } else if (count == 0) {
//                return;
//            }
            sb.put(bytes, offset, count);
        }
    }

    public void write(int c) {
        try {
            sb.append((char) c);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void write(String str) {
        try {
            sb.append(str);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void write(String str, int off, int len) {
        try {
            sb.append(str, off, off + len);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void write(char[] bytes, int offset, int count) {
//        if ((offset < 0) || (offset > bytes.length) || (count < 0) ||
//                ((offset + count) > bytes.length) || ((offset + count) < 0)) {
//            throw new IndexOutOfBoundsException();
//        } else if (count == 0) {
//            return;
//        }
        if (sb instanceof StringBuilder) {
            ((StringBuilder) sb).append(bytes, offset, count);
        } else {
            try {
                sb.append(new String(bytes,offset,count));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public AppendableWriter append(CharSequence sequence) {
        if (sequence == null) {
            write("null");
        } else {
            write(sequence.toString());
        }
        return this;
    }

    public AppendableWriter append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    public AppendableWriter append(char c) {
        write(c);
        return this;
    }


    public void flush() {
    }

    public void close() {
    }

}
