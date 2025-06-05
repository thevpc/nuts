package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NNameSelectorStrategy implements NEnum {
    CASE_SENSITIVE,
    CASE_INSENSITIVE,
    FORMAT_INSENSITIVE;

    private String id;

    NNameSelectorStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NNameSelectorStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NNameSelectorStrategy.class);
    }
}
