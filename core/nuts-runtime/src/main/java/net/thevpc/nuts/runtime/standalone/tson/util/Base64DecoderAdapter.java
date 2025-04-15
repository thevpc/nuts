package net.thevpc.nuts.runtime.standalone.tson.util;

import java.io.*;
import java.util.Arrays;

public class Base64DecoderAdapter extends InputStream {


    private static final char[] TO_BASE64_CHARS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    private static final int[] FROM_BASE64_BYTES = new int[256];

    static {
        Arrays.fill(FROM_BASE64_BYTES, -1);
        for (int i = 0; i < TO_BASE64_CHARS.length; i++)
            FROM_BASE64_BYTES[TO_BASE64_CHARS[i]] = i;
        FROM_BASE64_BYTES['='] = -2;
    }

    private Reader in;
    private byte[] outBuffer;
    private int outBufferWriteIndex = 0;
    private int outBufferReadIndex = 0;
    private char[] inBuffer = new char[4];
    private int inBufferIndex = 0;
    private char[] tempBuffer;


    public Base64DecoderAdapter(Reader in) {
        this(in, -1);
    }

    public Base64DecoderAdapter(Reader in, int outBufferSize) {
        this.in = in;
        if (outBufferSize < 10) {
            outBufferSize = 10;
        }
        outBuffer = new byte[outBufferSize + 4];
        tempBuffer = new char[outBufferSize];
    }


    public static byte[] fromBase64(String bytes) {
        Base64DecoderAdapter r = new Base64DecoderAdapter(new StringReader(bytes));
        byte[] buffer = new byte[1024];
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while ((i = r.read(buffer)) != 0) {
                out.write(buffer, 0, i);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }


    private boolean pushChar(char cc) {
        if (cc <= 32) {
            //ignore white chars
            return false;
        }
        inBuffer[inBufferIndex++] = cc;
        if (inBufferIndex == 4) {
            pushCharLast();
            return true;
        }
        return false;
    }

    private void pushCharLast() {
        int b0 = FROM_BASE64_BYTES[inBuffer[0] & 0xff];
        if (b0 < 0) {
            throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(b0, 16));
        }
        int b1 = FROM_BASE64_BYTES[inBuffer[1] & 0xff];
        if (b1 < 0) {
            if (b1 == -2) {
                pushByte((byte) (b0 << 18));
            } else {
                throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(b1, 16));
            }
        } else {
            int b2 = FROM_BASE64_BYTES[inBuffer[2] & 0xff];
            if (b2 < 0) {
                if (b2 == -2) {
                    int i = b0 << 18 | b1 << 12;
                    pushByte((byte) (i >> 16));
                    //pushByte((byte) 0);
                    //pushByte((byte) 0);
                } else {
                    throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(b2, 16));
                }
            } else {
                int b3 = FROM_BASE64_BYTES[inBuffer[3] & 0xff];
                if (b3 < 0) {
                    if (b3 == -2) {
                        int i = b0 << 18 | b1 << 12 | b2 << 6;
                        pushByte((byte) (i >> 16));
                        pushByte((byte) (i >> 8));
                        //pushByte((byte) 0);
                    } else {
                        throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(b3, 16));
                    }
                } else {
                    int i = b0 << 18 | b1 << 12 | b2 << 6 | b3;
                    pushByte((byte) (i >> 16));
                    pushByte((byte) (i >> 8));
                    pushByte((byte) i);
                }
            }
        }
        inBufferIndex = 0;
    }

    private void pushByte(byte c) {
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
    }

    private void finish() {
        switch (inBufferIndex) {
            case 0:
                return;
            case 1: {
                inBuffer[inBufferIndex++] = '=';
                inBuffer[inBufferIndex++] = '=';
                inBuffer[inBufferIndex++] = '=';
                pushCharLast();
                break;
            }
            case 2: {
                inBuffer[inBufferIndex++] = '=';
                inBuffer[inBufferIndex++] = '=';
                pushCharLast();
                break;
            }
            case 3: {
                inBuffer[inBufferIndex++] = '=';
                pushCharLast();
                break;
            }
        }
    }

    private int dataReady() {
        if (outBufferReadIndex >= outBufferWriteIndex) {
            while (true) {
                try {
                    int c = in.read(tempBuffer);
                    if (c <= 0) {
                        finish();
                        break;
                    } else {
                        for (int i = 0; i < c; i++) {
                            pushChar(tempBuffer[i]);
                        }
                        if ((outBufferWriteIndex - outBufferReadIndex) > 0) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return (outBufferWriteIndex - outBufferReadIndex);
    }

    @Override
    public int read() {
        int r = dataReady();
        if (r > 0) {
            byte b = outBuffer[outBufferReadIndex++];
            return b;
        }
        return -1;
    }

    @Override
    public int read(byte[] cbuf, int off, int len)  {
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
    public void close()  {
        try {
            in.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
