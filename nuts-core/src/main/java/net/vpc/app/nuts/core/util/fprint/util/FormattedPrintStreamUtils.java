package net.vpc.app.nuts.core.util.fprint.util;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.vpc.app.nuts.core.util.fprint.parser.FormattedPrintStreamNodePartialParser;

public class FormattedPrintStreamUtils {

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final Pattern PRINTF_PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    /**
     * extract plain text from formatted text
     *
     * @param text
     * @return
     */
    public static String filterText(String text) {
        return FormattedPrintStreamNodePartialParser.filterText0(text);
    }

    /**
     * transform plain text to formatted text so that the result is rendered as
     * is
     *
     * @param text
     * @return
     */
    public static String escapeText(String text) {
        return FormattedPrintStreamNodePartialParser.escapeText0(text);
    }

    public static String format(Locale locale, String format, Object... args) {
        StringBuilder sb = new StringBuilder();
        Matcher m = PRINTF_PATTERN.matcher(format);
        int x = 0;
        for (int i = 0, len = format.length(); i < len;) {
            if (m.find(i)) {
                if (m.start() != i) {
                    sb.append(format.substring(i, m.start()));
                }
                Object arg = x < args.length ? args[x] : "MISSING_ARG_" + x;
                String g = m.group();

                if (g.endsWith("N")) {
                    //escape %
                    char[] s=String.valueOf(arg).toCharArray();
                    for (int j = 0; j < s.length; j++) {
                        char c = s[j];
                        if(c=='%'){
                            sb.append('\\');
                        }
                        sb.append(c);
                    }
                } else {
                    sb.append(escapeText(format0(locale, g, arg)));
                }
                x++;
                i = m.end();
            } else {
                sb.append(format.substring(i));
                break;
            }
        }
        return sb.toString();
    }

    public static String format0(Locale locale, String format0, Object... args) {
        StringBuilder sb = new StringBuilder();
        new Formatter(sb, locale).format(format0, args);
        return sb.toString();
    }

    public static String fillString(char x, int width) {
        char[] cc = new char[width];
        Arrays.fill(cc, x);
        return new String(cc);
    }

    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }
}
