package net.thevpc.nuts;

import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

/**
 * General purpose Input utility class
 * @since 0.8.6
 */
public class NIn {
    public static String readLine(NMsg prompt) {
        return NSession.of().getTerminal().readLine(prompt);
    }

    public static String readLine() {
        return NSession.of().getTerminal().readLine(null);
    }

    public static char[] readPassword(NMsg prompt) {
        return NSession.of().getTerminal().readPassword(prompt);
    }

    public static char[] readPassword() {
        return NSession.of().getTerminal().readPassword(null);
    }


    public static NLiteral readLiteral(NMsg prompt) {
        return NLiteral.of(NSession.of().getTerminal().readLine(prompt));
    }

    public static NLiteral readLiteral() {
        return NLiteral.of(NSession.of().getTerminal().readLine(null));
    }

    public static <T> NAsk<T> ask() {
        return NAsk.of();
    }
}
