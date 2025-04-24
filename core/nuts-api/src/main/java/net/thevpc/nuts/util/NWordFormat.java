package net.thevpc.nuts.util;

public enum NWordFormat implements NEnum {
    CAPITALIZED,
    UPPERCASE,
    LOWERCASE,
    UNCAPITALIZED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NWordFormat() {
        // cannot call NNameFormat because of cyclic dependency!
        //this.id = NNameFormat.ID_NAME.format(name());
        this.id = name().toLowerCase();
    }

    public static NOptional<NWordFormat> parse(String value) {
        return NEnumUtils.parseEnum(value, NWordFormat.class);
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

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
