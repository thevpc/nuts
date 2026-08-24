package net.thevpc.nuts.io;


import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * NonClosablePrintStream class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NonClosablePrintStream extends PrintStream implements OutputStreamDelegate {
    private OutputStream delegated;

    /**
     * Non closable print stream.
     *
     * @param out out
     * @return non closable print stream result
     */
    public NonClosablePrintStream(OutputStream out) {
      /**
       * Super.
       *
       * @param asNNonClosableOutputStream(out) as n non closable output stream(out)
       */
        super(asNNonClosableOutputStream(out));
        this.delegated = out;
    }

    /**
     * Non closable print stream.
     *
     * @param out out
     * @param autoFlush auto flush
     * @return non closable print stream result
     */
    public NonClosablePrintStream(OutputStream out, boolean autoFlush) {
      /**
       * Super.
       *
       * @param asNNonClosableOutputStream(out) as n non closable output stream(out)
       * @param autoFlush auto flush
       */
        super(asNNonClosableOutputStream(out), autoFlush);
        this.delegated = out;
    }

    /**
     * Non closable print stream.
     *
     * @param out out
     * @param autoFlush auto flush
     * @param encoding encoding
     * @return non closable print stream result
     * @throws UnsupportedEncodingException if execution fails
     */
    public NonClosablePrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
      /**
       * Super.
       *
       * @param asNNonClosableOutputStream(out) as n non closable output stream(out)
       * @param autoFlush auto flush
       * @param encoding encoding
       */
        super(asNNonClosableOutputStream(out), autoFlush, encoding);
        this.delegated = out;
    }

    @Override
    public OutputStream delegateOutputStream() {
        return delegated;
    }

    /**
     * As n non closable output stream.
     *
     * @param out out
     * @return as n non closable output stream result
     */
    private static NonClosableOutputStream asNNonClosableOutputStream(OutputStream out) {
        if (out == null) {
            return null;
        }
        if (out instanceof NonClosableOutputStream) {
            return (NonClosableOutputStream) out;
        }
        return new NonClosableOutputStream(out);
    }

    @Override
    public void close() {
        //never close!!
      /**
       * Flush.
       */
        flush();
    }
}
