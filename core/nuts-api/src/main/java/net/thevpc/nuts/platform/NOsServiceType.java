package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * NOsServiceType enum.
 *
 * @author thevpc
 * @since 0.8.0
 */
public enum NOsServiceType implements NEnum {
    INITD,
    SYSTEMD,
    DEFAULT,
    UNSUPPORTED;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    public static NOptional<NOsServiceType> parse(String value) {
        return NEnumUtils.parseEnum(value, NOsServiceType.class, s -> {
            String n = s.normalizedValue();
            switch (n) {
                case "INITD":
                    return NOptional.of(INITD);

                case "SYSTEMD":
                    return NOptional.of(SYSTEMD);

                case "UNSUPPORTED":
                    return NOptional.of(UNSUPPORTED);
                case "DEFAULT":
                    return NOptional.of(DEFAULT);
            }
            return null;
        });
    }

    /**
     * default constructor
     */
    NOsServiceType() {
        this.id = NNameFormat.ID_NAME.format(name());
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
