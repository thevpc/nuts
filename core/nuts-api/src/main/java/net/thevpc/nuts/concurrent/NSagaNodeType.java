package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * @since 0.8.7
 */
public enum NSagaNodeType implements NEnum {
    STEP,
    IF,
    WHILE,
    SUITE,
    UNDO;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NSagaNodeType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NSagaNodeType> parse(String value) {
        return NEnumUtils.parseEnum(value, NSagaNodeType.class);
    }

    @Override
    public String id() {
        return id;
    }
}
