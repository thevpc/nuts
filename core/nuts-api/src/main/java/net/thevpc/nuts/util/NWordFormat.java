package net.thevpc.nuts.util;

public enum NWordFormat {
    CAPITALIZED,
    UPPERCASE,
    LOWERCASE,
    UNCAPITALIZED;

    public String formatWord(String value) {
        if (value != null) {
            switch (this) {
                case UPPERCASE:
                    return value.toUpperCase();
                case LOWERCASE:
                    return value.toLowerCase();
                case CAPITALIZED: {
                    if (!value.isEmpty()) {
                        char[] c = value.toCharArray();
                        c[0] = Character.toUpperCase(c[0]);
                        for (int i = 1; i < value.length(); i++) {
                            c[i] = Character.toLowerCase(c[i]);
                        }
                        return new String(c);
                    }
                    break;
                }
                case UNCAPITALIZED: {
                    if (!value.isEmpty()) {
                        char[] c = value.toCharArray();
                        c[0] = Character.toLowerCase(c[0]);
                        return new String(c);
                    }
                    break;
                }
            }
        }
        throw new UnsupportedOperationException();
    }
}
