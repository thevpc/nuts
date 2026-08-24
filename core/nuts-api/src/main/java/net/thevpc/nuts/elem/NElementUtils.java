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
            /**
             * Checks if is valid element name char.
             *
             * @param c c
             * @param start start
             * @return is valid element name char result
             */
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
        if (name == null) {
            return false;
        }
        if (name.isEmpty()) {
            return false;
        }
        char[] charArray = name.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(c)
                        && c != '.'
                        && c != '@'
                        && c != ':'
                ) {
                    return false;
                }
            } else {
                if (!Character.isJavaIdentifierPart(c)
                        && c != '.'
                        && c != '-'
                        && c != '@'
                        && c != ':'
                ) {
                    return false;
                }
            }
        }
        return true;
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
            /**
             * Checks if is element name.
             *
             * @param name name
             * @return is element name result
             */
            return isElementName(name);
        }
        if (name == null) {
            return false;
        }
        if (name.isEmpty()) {
            return false;
        }
        char[] charArray = name.toCharArray();
        switch (contentType) {
            case XML: {
                //wont call isValidElementNameChar for performance
                for (int i = 0; i < charArray.length; i++) {
                    char c = charArray[i];
                    if (i == 0) {
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
                break;
            }
            case JSON: {
                //wont call isValidElementNameChar for performance
                for (int i = 0; i < charArray.length; i++) {
                    char c = charArray[i];
                    if (i == 0) {
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
                break;
            }
        }
        return true;
    }
}
