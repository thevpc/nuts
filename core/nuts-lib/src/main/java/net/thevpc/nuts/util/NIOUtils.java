package net.thevpc.nuts.util;

public class NIOUtils {

    public static boolean isValidFileNameChar(char c) {
        switch (c) {
            case '"':
            case '\'':
            case '`':
            case '?':
            case '*':
            case ':':
            case '%':
            case '|':
            case '<':
            case '>':
            case '/':
            case '\\':
            case '{':
            case '}':
            case '[':
            case ']':
            case '(':
            case ')':
            case '$': {
                return false;
            }
            default: {
                if (c < 32) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * return normalized file name by replacing any special character with a space and trimming the result
     *
     * @param name fine name to normalize
     * @return normalized string without accents
     */
    public static String normalizeFileName(String name) {
        char[] chars = NStringUtils.normalizeString(name).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '°': {
                    chars[i] = 'o';
                    break;
                }
                case '"':
                case '\'':
                case '`':
                case '?':
                case '*':
                case ':':
                case '%':
                case '|':
                case '<':
                case '>':
                case '/':
                case '\\':
                case '{':
                case '}':
                case '[':
                case ']':
                case '(':
                case ')':
                case '$': {
                    chars[i] = ' ';
                    break;
                }
                default: {
                    if (chars[i] < 32) {
                        chars[i] = ' ';
                    }
                }
            }
        }
        return new String(chars).trim();
    }

}
