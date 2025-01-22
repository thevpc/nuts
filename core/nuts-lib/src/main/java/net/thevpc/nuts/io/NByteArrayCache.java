package net.thevpc.nuts.io;

import java.io.*;
import java.nio.file.Files;

public class NByteArrayCache implements Closeable {
    private boolean persisted;
    private int maxSize;
    private OutputStream out;
    private File file;
    private long currSize;

    public NByteArrayCache() {
        this(10 * 1024 * 1024);
    }

    public NByteArrayCache(int maxSize) {
        this.maxSize = maxSize;
    }

    public long size() {
        return currSize;
    }

    public long maxSize() {
        return maxSize;
    }

    public NByteArrayCache write(byte[] data) {
        try {
            if (persisted) {
                out.write(data);
                currSize += data.length;
            } else {
                if (out == null) {
                    out = new ByteArrayOutputStream();
                }
                out.write(data);
                currSize += data.length;
                if (currSize > maxSize) {
                    if (file == null) {
                        file = Files.createTempFile("temp", ".temp").toFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file, true);
                    fos.write(((ByteArrayOutputStream) out).toByteArray());
                    out = fos;
                    persisted = true;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public NByteArrayCache copyTo(OutputStream out) {
        try {
            if(this.out !=null) {
                if (!persisted) {
                    out.write(((ByteArrayOutputStream) this.out).toByteArray());
                } else {
                    this.out.close();
                    this.out = null;
                    try (InputStream fis = new FileInputStream(file)) {
                        NIOUtils.copy(fis, out);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    @Override
    public void close()  {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
