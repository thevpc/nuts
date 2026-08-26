package net.thevpc.nuts.text;

import net.thevpc.nuts.util.*;

import java.util.function.Function;

/**
 * NNewLineMode enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
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

  /**
   * N new line mode.
   */
    NNewLineMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NNewLineMode> parse(String value) {
        if(value != null) {
            switch (value) {
                case "\n":return NOptional.of(NNewLineMode.LF);
                case "\r":return NOptional.of(NNewLineMode.CR);
                case "\r\n":return NOptional.of(NNewLineMode.CRLF);
            }
        }
        return NEnumUtils.parseEnum(value, NNewLineMode.class, new Function<NEnumUtils.NEnumCandidate, NOptional<NNewLineMode>>() {
            @Override
            public NOptional<NNewLineMode> apply(NEnumUtils.NEnumCandidate enumValue) {
                switch (enumValue.value()) {
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

    /**
     * System.
     *
     * @return system result
     */
    public static NNewLineMode system() {
        if (autoValue == null) {
          /**
           * Synchronized.
           *
           * @param NNewLineMode.class n new line mode.class
           */
            synchronized (NNewLineMode.class) {
                if (autoValue == null) {
                    autoValue = parse(System.getProperty("line.separator", "\n")).orElse(LF);
                }
            }
        }
        return autoValue;
    }

    /**
     * Normalize.
     *
     * @return normalize result
     */
    public NNewLineMode normalize() {
        if (this == AUTO) {
            /**
             * System.
             *
             * @return system result
             */
            return system();
        }
        return this;
    }

    /**
     * Value.
     *
     * @return value result
     */
    public String value() {
        switch (this) {
            case LF:
                return "\n";
            case CRLF:
                return "\r\n";
            case CR:
                return "\r";
            case AUTO: {
                /**
                 * Normalize.
                 *
                 * @param ).value( ).value(
                 * @return normalize result
                 */
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
