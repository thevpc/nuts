package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NContentType;

/**
 * NElementUtils class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public final class NElementUtils {
    /**
     * N element utils.
     *
     * @return n element utils result
     */
    private NElementUtils(){}

    /**
     * Checks if is valid element name char.
     *
     * @param c c
     * @param start start
     * @return is valid element name char result
     */
    public static boolean isValidElementNameChar(char c, boolean start) {
        if (start) {
            if (!Character.isJavaIdentifierStart(c)
                    && c != '.'
                    && c != ':'
                    && c != '@'
            ) {
                return false;
            }
        } else {
            if (!Character.isJavaIdentifierPart(c)
                    && c != '.'
                    && c != '-'
                    && c != ':'
                    && c != '@'
            ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if is valid element name char.
     *
     * @param c c
     * @param start start
     * @param contentType content type
     * @return is valid element name char result
     */
    public static boolean isValidElementNameChar(char c, boolean start, NContentType contentType) {
        if (contentType == null) {
            return isValidElementNameChar(c, start);
        }
        switch (contentType) {
            case XML: {
                if (start) {
                    if (!Character.isJavaIdentifierStart(c)
                            && c != '.'
                            && c != ':'
                    ) {
                        return false;
                    }
                } else {
                    if (!Character.isJavaIdentifierPart(c)
                            && c != '.'
                            && c != '-'
                            && c != ':'
                    ) {
                        return false;
                    }
                }
            }
            case TSON: {
                if (start) {
                    if (!Character.isJavaIdentifierStart(c)
                            && c != '.'
                            && c != '@'
                    ) {
                        return false;
                    }
                } else {
                    if (!Character.isJavaIdentifierPart(c)
                            && c != '.'
                            && c != '-'
                            && c != '@'
                    ) {
                        return false;
                    }
                }
            }
            default: {
                return true;
            }
        }
    }

    /**
     * Checks if is element name.
     *
     * @param name name
     * @return is element name result
     */
    public static boolean isElementName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        int len = name.length();
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (i == 0) {
                if (!isElementNameStart(c, true, true)) {
                    return false;
                }
            } else {
                if (!isElementNamePart(c, true, true)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * true when {@code c} can start an element name. ASCII letters, '_' and '$'
     * are handled inline; other characters fall back to the JDK identifier rules.
     */
    private static boolean isElementNameStart(char c, boolean allowAt, boolean allowColon) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$') {
            return true;
        }
        if (c == '.') {
            return true;
        }
        if (allowAt && c == '@') {
            return true;
        }
        if (allowColon && c == ':') {
            return true;
        }
        return c >= 128 && Character.isJavaIdentifierStart(c);
    }

    /**
     * true when {@code c} can continue an element name. ASCII letters, digits,
     * '_' and '$' are handled inline; other characters fall back to the JDK
     * identifier rules.
     */
    private static boolean isElementNamePart(char c, boolean allowAt, boolean allowColon) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '$') {
            return true;
        }
        if (c == '.' || c == '-') {
            return true;
        }
        if (allowAt && c == '@') {
            return true;
        }
        if (allowColon && c == ':') {
            return true;
        }
        // preserve Character.isJavaIdentifierPart semantics for the remaining
        // chars (notably identifier-ignorable ASCII control chars, and Unicode)
        return Character.isJavaIdentifierPart(c);
    }

    /**
     * Checks if is element name.
     *
     * @param name name
     * @param contentType content type
     * @return is element name result
     */
    public static boolean isElementName(String name, NContentType contentType) {
        if (contentType == null) {
            return isElementName(name);
        }
        if (name == null || name.isEmpty()) {
            return false;
        }
        int len = name.length();
        switch (contentType) {
            case XML: {
                //wont call isValidElementNameChar for performance
                for (int i = 0; i < len; i++) {
                    char c = name.charAt(i);
                    if (i == 0) {
                        if (!isElementNameStart(c, false, true)) {
                            return false;
                        }
                    } else {
                        if (!isElementNamePart(c, false, true)) {
                            return false;
                        }
                    }
                }
                break;
            }
            case JSON: {
                //wont call isValidElementNameChar for performance
                for (int i = 0; i < len; i++) {
                    char c = name.charAt(i);
                    if (i == 0) {
                        if (!isElementNameStart(c, true, false)) {
                            return false;
                        }
                    } else {
                        if (!isElementNamePart(c, true, false)) {
                            return false;
                        }
                    }
                }
                break;
            }
        }
        return true;
    }
}
