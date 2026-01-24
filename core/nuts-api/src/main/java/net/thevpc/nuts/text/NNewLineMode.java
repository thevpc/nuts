package net.thevpc.nuts.text;

import net.thevpc.nuts.util.*;

import java.util.function.Function;

public enum NNewLineMode implements NEnum {
    LF,      // \n (Unix/Linux/macOS)
    CRLF,    // \r\n (Windows)
    CR,      // \r (Legacy Mac)
    AUTO     // Use System.lineSeparator()
    ;
    private static NNewLineMode autoValue;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NNewLineMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NNewLineMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NNewLineMode.class, new Function<NEnumUtils.EnumValue, NOptional<NNewLineMode>>() {
            @Override
            public NOptional<NNewLineMode> apply(NEnumUtils.EnumValue enumValue) {
                switch (enumValue.getValue()) {
                    case "\n": {
                        return NOptional.of(NNewLineMode.LF);
                    }
                    case "\r": {
                        return NOptional.of(NNewLineMode.CR);
                    }
                    case "\r\n": {
                        return NOptional.of(NNewLineMode.CRLF);
                    }
                }
                return null;
            }
        });
    }

    public static NNewLineMode system() {
        if (autoValue == null) {
            synchronized (NNewLineMode.class) {
                if (autoValue == null) {
                    autoValue = parse(System.getProperty("line.separator", "\n")).orElse(LF);
                }
            }
        }
        return autoValue;
    }

    public NNewLineMode normalize() {
        if (this == AUTO) {
            return system();
        }
        return this;
    }

    public String value() {
        switch (this) {
            case LF:
                return "\n";
            case CRLF:
                return "\r\n";
            case CR:
                return "\r";
            case AUTO: {
                return normalize().value();
            }
        }
        return "\n";
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

}
