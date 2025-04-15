package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import java.io.*;

public class ReaderTsonCharStreamSource extends TsonCharStreamSource {
    private ByteArrayOutputStream memoryBuffer = new ByteArrayOutputStream();
    private Writer memoryBufferWriter = new OutputStreamWriter(memoryBuffer);
    private Writer persistentBuffer;
    private int MAX_MEM_LEN = 10 * 8096;
    private long length;
    private boolean inMemory = true;
    private File tempFile;

    public void set(Reader inputStream) {
        char[] b = new char[8096];
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

    public void push(char[] buffer, int offset, int len) {
        try {
            if (inMemory) {
                if (length + len < MAX_MEM_LEN) {
                    this.memoryBufferWriter.write(buffer, offset, len);
                } else {
                    tempFile = File.createTempFile("tsonchar", ".temp");
                    persistentBuffer = new FileWriter(tempFile);
                    inMemory = false;
                    memoryBuffer.flush();
                    persistentBuffer.write(new String(memoryBuffer.toByteArray()));
                    this.memoryBuffer = null;
                }
            } else {
                memoryBuffer.flush();
                persistentBuffer.write(new String(memoryBuffer.toByteArray()));
            }
            length += len;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ReaderTsonCharStreamSource() {

    }

    public ReaderTsonCharStreamSource(Reader inputStream) {
        set(inputStream);
    }

    @Override
    public Reader open() {
        try {
            if (inMemory) {
                memoryBuffer.flush();
                return new StringReader(new String(memoryBuffer.toByteArray()));
            }
            if (persistentBuffer != null) {
                persistentBuffer.close();
                persistentBuffer = null;
            }
            return new FileReader(tempFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
