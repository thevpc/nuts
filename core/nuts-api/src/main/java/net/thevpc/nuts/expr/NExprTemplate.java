package net.thevpc.nuts.expr;

import net.thevpc.nuts.io.NInputSource;

import java.io.*;

/**
 * NExprTemplate interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExprTemplate {

    /**
     * With jsp style.
     *
     * @return with jsp style result
     */
    NExprTemplate withJspStyle();

    /**
     * With moustache style.
     *
     * @return with moustache style result
     */
    NExprTemplate withMoustacheStyle();

    /**
     * With bash style.
     *
     * @return with bash style result
     */
    NExprTemplate withBashStyle();

    /**
     * With boundaries.
     *
     * @param start start
     * @param stop stop
     * @return with boundaries result
     */
    NExprTemplate withBoundaries(String start, String stop);

    /**
     * Process.
     *
     * @param inputStream input stream
     * @param outputStream output stream
     * @return process result
     */
    NExprTemplate process(InputStream inputStream, OutputStream outputStream);

    /**
     * Process.
     *
     * @param inputStream input stream
     * @param outputStream output stream
     * @return process result
     */
    NExprTemplate process(Reader inputStream, Writer outputStream);

    /**
     * Process.
     *
     * @param inputStream input stream
     * @param outputStream output stream
     * @return process result
     */
    NExprTemplate process(Reader inputStream, PrintStream outputStream);

    /**
     * Process string.
     *
     * @param string string
     * @return process string result
     */
    String processString(String string);

    /**
     * Compile.
     *
     * @param inputStream input stream
     * @return compile result
     */
    NExprCompiledTemplate compile(InputStream inputStream);

    /**
     * Compile.
     *
     * @param inputStream input stream
     * @return compile result
     */
    NExprCompiledTemplate compile(Reader inputStream);

    /**
     * Compile.
     *
     * @param string string
     * @return compile result
     */
    NExprCompiledTemplate compile(String string);

    /**
     * Compile.
     *
     * @param source source
     * @return compile result
     */
    NExprCompiledTemplate compile(NInputSource source);
}
