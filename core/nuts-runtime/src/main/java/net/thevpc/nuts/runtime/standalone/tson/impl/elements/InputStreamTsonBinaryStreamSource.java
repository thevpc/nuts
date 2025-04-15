package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import java.io.*;
import java.util.Base64;

public class InputStreamTsonBinaryStreamSource extends TsonBinaryStreamSource {
    private ByteArrayOutputStream memoryBuffer = new ByteArrayOutputStream();
    private OutputStream persistentBuffer;
    private int MAX_MEM_LEN = 10 * 8096;
    private long length;
    private boolean inMemory = true;
    private File tempFile;

    public void set(InputStream inputStream) {
        byte[] b = new byte[8096];
        int r;
        while (true) {
            try {
                if (!((r = inputStream.read(b)) > 0)) break;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            push(b, 0, r);
        }
    }

    public void pushBase64(String str) {
        byte[] y = Base64.getDecoder().decode(str);
        push(y, 0, y.length);
    }

    public void push(byte[] buffer, int offset, int len) {
        try {
            if (inMemory) {
                if (length + len < MAX_MEM_LEN) {
                    this.memoryBuffer.write(buffer, offset, len);
                } else {
                    tempFile = File.createTempFile("tsonbin", ".temp");
                    persistentBuffer = new FileOutputStream(tempFile);
                    inMemory = false;
                    persistentBuffer.write(memoryBuffer.toByteArray());
                    this.memoryBuffer = null;
                }
            } else {
                persistentBuffer.write(memoryBuffer.toByteArray());
            }
            length += len;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public InputStreamTsonBinaryStreamSource() {

    }

    public InputStreamTsonBinaryStreamSource(InputStream inputStream) {
        set(inputStream);
    }

    @Override
    public InputStream open() {
        if (inMemory) {
            return new ByteArrayInputStream(memoryBuffer.toByteArray());
        }
        try {
            if (persistentBuffer != null) {
                persistentBuffer.close();
                persistentBuffer = null;
            }
            return new FileInputStream(tempFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
