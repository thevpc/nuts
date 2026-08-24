package net.thevpc.nuts.io;

/**
 * NStringWriter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NStringWriter {
    /**
     * Write.
     *
     * @param text text
     */
    void write(char text);

    /**
     * Write.
     *
     * @param text text
     */
    void write(String text);

    /**
     * Write.
     *
     * @param text text
     * @param offset offset
     * @param len len
     */
    void write(char[] text, int offset, int len);
}
