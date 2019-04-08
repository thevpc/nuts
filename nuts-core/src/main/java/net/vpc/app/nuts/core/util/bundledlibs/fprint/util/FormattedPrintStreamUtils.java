package net.vpc.app.nuts.core.util.bundledlibs.fprint.util;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.vpc.app.nuts.core.util.bundledlibs.fprint.parser.FDocNode;
import net.vpc.app.nuts.core.util.bundledlibs.fprint.parser.FormattedPrintStreamNodePartialParser;

public class FormattedPrintStreamUtils {

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final Pattern printfPattern = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");
    private static final Logger log = Logger.getLogger(FormattedPrintStreamUtils.class.getName());

    public static String filterText(String text) {
        if (text == null) {
            text = "";
        }
        FormattedPrintStreamNodePartialParser pp = new FormattedPrintStreamNodePartialParser();
        StringBuilder sb = new StringBuilder();
        try {
            pp.take(text);
            pp.forceEnding();
            FDocNode tn = null;
            while ((tn = pp.consumeFDocNode()) != null) {
                escape(tn, sb);
            }
            return sb.toString();
        } catch (Exception ex) {
            log.log(Level.FINEST, "Error parsing : \n" + text, ex);
            return text;
        }
    }

    private static void escape(FDocNode tn, StringBuilder sb) {
        if (tn instanceof FDocNode.Plain) {
            sb.append(((FDocNode.Plain) tn).getValue());
        } else if (tn instanceof FDocNode.List) {
            for (FDocNode fDocNode : ((FDocNode.List) tn).getValues()) {
                escape(fDocNode, sb);
            }
        } else if (tn instanceof FDocNode.Typed) {
            escape(((FDocNode.Typed) tn).getNode(), sb);
        } else if (tn instanceof FDocNode.Escaped) {
            sb.append(((FDocNode.Escaped) tn).getValue());
        } else {
            throw new IllegalArgumentException("Unsupported");
        }
    }

    public static String escapeText(String str) {
        if (str == null) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            switch (c) {
                case '\"':
                case '\'':
                case '`':
                case '$':
                case '£':
                case '§':
                case '_':
                case '~':
                case '%':
                case '¤':
                case '@':
                case '^':
                case '#':
                case '¨':
                case '=':
                case '*':
                case '+':
                case '(':
                case '[':
                case '{':
                case '<':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String format(Locale locale, String format, Object... args) {

        StringBuilder sb = new StringBuilder();
        Matcher m = printfPattern.matcher(format);
        int x = 0;
        for (int i = 0, len = format.length(); i < len;) {
            if (m.find(i)) {
                // Anything between the start of the string and the beginning
                // of the format specifier is either fixed text or contains
                // an invalid format string.
                if (m.start() != i) {
                    //checkText(s, i, m.start());
                    sb.append(format.substring(i, m.start()));
                }
                Object arg = x < args.length ? args[x] : "MISSING_ARG_" + x;
                sb.append(escapeText(format0(locale, m.group(), arg)));
                x++;
                i = m.end();
            } else {
                sb.append(format.substring(i));
                break;
            }
        }
        return sb.toString();
    }

    private static String format0(Locale locale, String format0, Object arg) {
        StringBuilder sb = new StringBuilder();
        new Formatter(sb, locale).format(format0, new Object[]{arg});
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
