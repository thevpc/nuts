package net.thevpc.nuts.io;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.text.NMsg;

/**
 * General purpose Input utility class
 * @since 0.8.6
 */
public class NIn {
    public static String readLine(NMsg prompt) {
        return NTerminal.of().readLine(prompt);
    }

    public static String readLine() {
        return NTerminal.of().readLine(null);
    }

    public static char[] readPassword(NMsg prompt) {
        return NTerminal.of().readPassword(prompt);
    }

    public static char[] readPassword() {
        return NTerminal.of().readPassword(null);
    }


    public static NLiteral readLiteral(NMsg prompt) {
        return NLiteral.of(NTerminal.of().readLine(prompt));
    }

    public static NLiteral readLiteral() {
        return NLiteral.of(NTerminal.of().readLine(null));
    }

    public static <T> NAsk<T> ask() {
        return NAsk.of();
    }
}
