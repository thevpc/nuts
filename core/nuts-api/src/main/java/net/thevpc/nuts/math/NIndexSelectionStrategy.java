package net.thevpc.nuts.math;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Function;

/**
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 17 août 2007 10:03:51
 */
public enum NIndexSelectionStrategy implements NEnum {
    FIRST,
    LAST,
    BALANCED

    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * Default constructor
     */
    NIndexSelectionStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NIndexSelectionStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NIndexSelectionStrategy.class, new Function<NEnumUtils.EnumValue, NOptional<NIndexSelectionStrategy>>() {
            @Override
            public NOptional<NIndexSelectionStrategy> apply(NEnumUtils.EnumValue s) {
                String normalizedValue = s.getNormalizedValue();
                return null;
            }
        });
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
