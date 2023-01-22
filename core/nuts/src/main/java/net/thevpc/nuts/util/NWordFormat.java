package net.thevpc.nuts.util;

public enum NWordFormat {
    CAPITALIZED,
    UPPERCASE,
    LOWERCASE,
    UNCAPITALIZED;

    public String formatWord(String value) {
        if(value!=null) {
            switch (this) {
                case UPPERCASE:
                    return value.toUpperCase();
                case LOWERCASE:
                    return value.toLowerCase();
                case CAPITALIZED: {
                    if (value.length() > 0) {
                        char[] c = value.toCharArray();
                        c[0] = Character.toUpperCase(c[0]);
                        return new String(c);
                    }
                    break;
                }
                case UNCAPITALIZED: {
                    if (value.length() > 0) {
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
