package net.thevpc.nuts.runtime.standalone.tson.util;

import java.io.*;

public class Base64EncoderAdapter extends Reader {
    private static final char[] BASE64_CHARS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    private InputStream in;
    private char[] newline = {'\n'};
    private char[] outBuffer;
    private int outBufferWriteIndex = 0;
    private int outBufferReadIndex = 0;
    private byte[] inBuffer = new byte[3];
    private int inBufferIndex = 0;

    private int lineMax;
    private int lineLength;
    private byte[] tempBuffer;


    public Base64EncoderAdapter(InputStream in) {
        this(in, 76, -1);
    }

    public Base64EncoderAdapter(InputStream in, int lineMax) {
        this(in, lineMax, -1);
    }

    public Base64EncoderAdapter(InputStream in, int lineMax, int bufferSize) {
        this.in = in;
        this.lineMax = lineMax;
        if (bufferSize <= 0) {
            bufferSize = 1;
        }
        outBuffer = new char[bufferSize * 4 / 3 + 4 + 3];
        tempBuffer = new byte[bufferSize];
    }


    public static String toBase64(byte[] bytes) {
        return toBase64(bytes, -1);
    }

    public static String toBase64(byte[] bytes, int linemax) {
        Base64EncoderAdapter r = new Base64EncoderAdapter(new ByteArrayInputStream(bytes), linemax);
        char[] chars = new char[1024];
        int i;
        StringBuilder sb = new StringBuilder();
        try {
            while ((i = r.read(chars)) != 0) {
                sb.append(chars, 0, i);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }

    boolean pushByte(byte b) {
        inBuffer[inBufferIndex++] = b;
        if (inBufferIndex == 3) {
            int bits = (inBuffer[0] & 0xff) << 16 |
                    (inBuffer[1] & 0xff) << 8 |
                    (inBuffer[2] & 0xff);
            pushChar(BASE64_CHARS[(bits >>> 18) & 0x3f]);
            pushChar(BASE64_CHARS[(bits >>> 12) & 0x3f]);
            pushChar(BASE64_CHARS[(bits >>> 6) & 0x3f]);
            pushChar(BASE64_CHARS[bits & 0x3f]);
            inBufferIndex = 0;
            return true;
        }
        return false;
    }

    private void pushChar(char c) {
        if (outBufferWriteIndex >= outBuffer.length - 3) {
            if (outBufferReadIndex > 0) {
                int len = outBufferWriteIndex - outBufferReadIndex;
                System.arraycopy(outBuffer, outBufferReadIndex, outBuffer, 0, len);
                outBufferReadIndex = 0;
                outBufferWriteIndex = len;

            } else {
                throw new IllegalArgumentException("Buffer overflow");
            }
        }
        outBuffer[outBufferWriteIndex++] = c;
        lineLength++;
        if (lineLength >= lineMax) {
            for (char b : newline) {
                outBuffer[outBufferWriteIndex++] = b;
            }
            lineLength = 0;
        }
    }

    private void finish() {
        int b0 = inBuffer[0] & 0xff;
        switch (inBufferIndex) {
            case 1: {
                pushChar(BASE64_CHARS[b0 >> 2]);
                pushChar(BASE64_CHARS[(b0 << 4) & 0x3f]);
                pushChar('=');
                pushChar('=');
                inBufferIndex = 0;
                break;
            }
            case 2: {
                pushChar(BASE64_CHARS[b0 >> 2]);
                int b1 = inBuffer[1] & 0xff;
                pushChar(BASE64_CHARS[(b0 << 4) & 0x3f | (b1 >> 4)]);
                pushChar(BASE64_CHARS[(b1 << 2) & 0x3f]);
                pushChar('=');
                inBufferIndex = 0;
                break;
            }
        }
    }

    private int dataReady() {
        if (outBufferReadIndex >= outBufferWriteIndex) {
            while (true) {
                int c = 0;
                try {
                    c = in.read(tempBuffer);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (c <= 0) {
                    finish();
                    break;
                } else {
                    for (int i = 0; i < c; i++) {
                        pushByte(tempBuffer[i]);
                    }
                    if ((outBufferWriteIndex - outBufferReadIndex) > 0) {
                        break;
                    }
                }
            }
        }
        return (outBufferWriteIndex - outBufferReadIndex);
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        if (len > 0) {
            int r = dataReady();
            if (r > 0) {
                if (r <= len) {
                    len = r;
                }
                System.arraycopy(outBuffer, outBufferReadIndex, cbuf, off, len);
                outBufferReadIndex += len;
                return len;
            }
        }
        return 0;
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
