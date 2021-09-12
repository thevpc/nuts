/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.template;

import java.util.Arrays;
import java.util.function.Function;

/**
 *
 * @author thevpc
 */
class _StringUtils {

//    public static boolean isStartsWithWords(String string, String portions) {
//        return isStartsWithWords(string.split(" "), portions);
//    }
    public static String sortLines(String string) {
        String[] all = string.split("\n");
        Arrays.sort(all);
        return String.join("\n", all);
    }

    public static String consumeWord(String string) {
        string = string.trim();
        if (string.isEmpty()) {
            return null;
        }
        int i = 0;
        for (i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(c)) {
                    break;
                }
            } else {
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
            }
        }
        if (i == 0) {
            return null;
        }
        return string.substring(0, i);
    }

    public static String consumeWords(String string, String... portions) {
        String i = string;
        for (String portion : portions) {
            if (!isStartsWithWord(i, portion)) {
                return null;
            }
            i = i.substring(portion.length()).trim();
        }
        return i;
    }

    public static boolean isStartsWithWords(String string, String... portions) {
        String i = string;
        for (String portion : portions) {
            if (!isStartsWithWord(i, portion)) {
                return false;
            }
            i = i.substring(portion.length()).trim();
        }
        return true;
    }

    public static boolean isStartsWithWord(String string, String portion) {
        return isStartsWithWord(string, portion, 0);
    }

    public static boolean isStartsWithWord(String string, String portion, int pos) {
        if (string.startsWith(portion, pos)) {
            if (string.length() == portion.length()) {
                return true;
            }
            char c = string.charAt(portion.length() + pos);
            if (Character.isJavaIdentifierPart(c)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static String replacePlaceHolders(String s, String prefix, String suffix, Function<String,String> converter) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < s.length()) {
            int u = s.indexOf(prefix, i);
            if (u < 0) {
                sb.append(substring(s, i, s.length()));
                i = u;
                break;
            } else {
                sb.append(substring(s, i, u));
                i = u + prefix.length();
                u = s.indexOf(suffix, i);
                if (u <= 0) {
                    String var = substring(s, i, s.length());
                    sb.append(converter.apply(var));
                    i = u;
                    break;
                } else {
                    String var = substring(s, i, u);
                    sb.append(converter.apply(var));
                    i = u + suffix.length();
                }
            }
        }
        return sb.toString();
    }
    public static String substring(String full, int from, int to) {
        if (full == null) {
            full = "";
        }
        if (from < 0) {
            from = 0;
        }
        if (to >= full.length()) {
            to = full.length();
        }
        if (to <= from) {
            return "";
        }
        return full.substring(from, to);
    }


    public static String literalToString(Object literal) {
        if (literal == null) {
            return "null";
        }
        if (literal instanceof String) {
            String theString = (String) literal;
            int len = theString.length();
            int bufLen = len * 2;
            if (bufLen < 0) {
                bufLen = Integer.MAX_VALUE;
            }
            StringBuilder sb = new StringBuilder(bufLen + 2);
            sb.append("\"");
            boolean escapeUnicode = true;
            for (int x = 0; x < len; x++) {
                char cc = theString.charAt(x);
                // Handle common case first, selecting largest block that
                // avoids the specials below
                if ((cc > 61) && (cc < 127)) {
                    if (cc == '\\') {
                        sb.append('\\');
                        sb.append('\\');
                        continue;
                    }
                    sb.append(cc);
                    continue;
                }
                switch (cc) {
                    case ' ':
//                        if (x == 0 || escapeSpace) {
//                            outBuffer.append('\\');
//                        }
                        sb.append(' ');
                        break;
                    case '\t':
                        sb.append('\\');
                        sb.append('t');
                        break;
                    case '\n':
                        sb.append('\\');
                        sb.append('n');
                        break;
                    case '\r':
                        sb.append('\\');
                        sb.append('r');
                        break;
                    case '\f':
                        sb.append('\\');
                        sb.append('f');
                        break;
//                    case '=': // Fall through
//                    case ':': // Fall through
//                    case '#': // Fall through
//                    case '!':
//                        outBuffer.append('\\');
//                        outBuffer.append(aChar);
//                        break;
                    default:
                        if (((cc < 0x0020) || (cc > 0x007e)) & escapeUnicode) {
                            sb.append('\\');
                            sb.append('u');
                            sb.append(toHex((cc >> 12) & 0xF));
                            sb.append(toHex((cc >> 8) & 0xF));
                            sb.append(toHex((cc >> 4) & 0xF));
                            sb.append(toHex(cc & 0xF));
                        } else {
                            sb.append(cc);
                        }
                }
            }

            sb.append("\"");
            return sb.toString();
        }
        if (literal instanceof Character) {
            return "'" + literal + '\'';
        }
        return String.valueOf(literal);
    }


    /**
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * A table of hex digits
     */
    private static final char[] hexDigit = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

}
