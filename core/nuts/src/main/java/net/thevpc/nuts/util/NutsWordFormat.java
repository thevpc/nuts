package net.thevpc.nuts.util;

public enum NutsWordFormat {
    CAPITALIZED,
    UPPERCASE,
    LOWERCASE,
    UNCAPITALIZED;

    public String formatWord(String value) {
        switch (this) {
            case UPPERCASE:
                return value.toUpperCase();
            case LOWERCASE:
                return value.toLowerCase();
            case CAPITALIZED: {
                char[] c = value.toCharArray();
                c[0] = Character.toUpperCase(c[0]);
                return new String(c);
            }
            case UNCAPITALIZED: {
                char[] c = value.toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                return new String(c);
            }
        }
        throw new UnsupportedOperationException();
    }
}
