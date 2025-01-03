package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NPathExtensionType implements NEnum {
    SHORT,LONG,SMART;
    private final String id;

    NPathExtensionType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<NPathExtensionType> parse(String value) {
        return NEnumUtils.parseEnum(value, NPathExtensionType.class);
    }
}
