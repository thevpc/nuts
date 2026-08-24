package net.thevpc.nuts.io;

import java.io.*;
import java.nio.file.Files;

/**
 * NByteArrayCache class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NByteArrayCache implements Closeable {
    private boolean persisted;
    private int maxSize;
    private OutputStream out;
    private File file;
    private long currSize;

    /**
     * N byte array cache.
     *
     * @return n byte array cache result
     */
    public NByteArrayCache() {
      /**
       * This.
       *
       * @param 1024 1024
       */
        this(10 * 1024 * 1024);
    }

    /**
     * N byte array cache.
     *
     * @param maxSize max size
     * @return n byte array cache result
     */
    public NByteArrayCache(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Size.
     *
     * @return size result
     */
    public long size() {
        return currSize;
    }

    /**
     * Max size.
     *
     * @return max size result
     */
    public long maxSize() {
        return maxSize;
    }

    /**
     * Write.
     *
     * @param data data
     * @return write result
     */
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
            /**
             * Unchecked io exception.
             *
             * @param e e
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(e);
        }
        return this;
    }

    /**
     * Copy to.
     *
     * @param out out
     * @return copy to result
     */
    public NByteArrayCache copyTo(OutputStream out) {
        try {
            if(this.out !=null) {
                if (!persisted) {
                    out.write(((ByteArrayOutputStream) this.out).toByteArray());
                } else {
                    this.out.close();
                    this.out = null;
                  /**
                   * Try.
                   *
                   * @param FileInputStream(file) file input stream(file)
                   */
                    try (InputStream fis = new FileInputStream(file)) {
                        NIOUtils.copy(fis, out);
                    }
                }
            }
        } catch (IOException e) {
            /**
             * Unchecked io exception.
             *
             * @param e e
             * @return unchecked io exception result
             */
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
                /**
                 * Unchecked io exception.
                 *
                 * @param e e
                 * @return unchecked io exception result
                 */
                throw new UncheckedIOException(e);
            }
        }
    }
}
