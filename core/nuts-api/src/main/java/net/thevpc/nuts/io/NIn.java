package net.thevpc.nuts.io;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.text.NMsg;

/**
 * General purpose Input utility class
 * @since 0.8.6
 */
public class NIn {
    /**
     * Read line.
     *
     * @param prompt prompt
     * @return read line result
     */
    public static String readLine(NMsg prompt) {
        return NTerminal.of().readLine(prompt);
    }

    /**
     * Read line.
     *
     * @return read line result
     */
    public static String readLine() {
        return NTerminal.of().readLine(null);
    }

    /**
     * Read password.
     *
     * @param prompt prompt
     * @return read password result
     */
    public static char[] readPassword(NMsg prompt) {
        return NTerminal.of().readPassword(prompt);
    }

    /**
     * Read password.
     *
     * @return read password result
     */
    public static char[] readPassword() {
        return NTerminal.of().readPassword(null);
    }


    /**
     * Read literal.
     *
     * @param prompt prompt
     * @return read literal result
     */
    public static NLiteral readLiteral(NMsg prompt) {
        return NLiteral.of(NTerminal.of().readLine(prompt));
    }

    /**
     * Read literal.
     *
     * @return read literal result
     */
    public static NLiteral readLiteral() {
        return NLiteral.of(NTerminal.of().readLine(null));
    }

    /**
     * Ask.
     *
     * @return ask result
     */
    public static <T> NAsk<T> ask() {
        return NAsk.of();
    }
}
