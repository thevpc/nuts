package net.thevpc.nuts.util;

import net.thevpc.nuts.format.NContentType;

public final class NElementUtils {
    private NElementUtils(){}

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

    public static boolean isValidElementName(String name) {
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

    public static boolean isValidElementName(String name, NContentType contentType) {
        if (contentType == null) {
            return isValidElementName(name);
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
