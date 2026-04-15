package net.thevpc.nuts.core;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NRunAsMode implements NEnum {
    CURRENT_USER,
    USER,
    ROOT,
    SUDO;
    private final String id;

    NRunAsMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NRunAsMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NRunAsMode.class);
    }

    @Override
    public String id() {
        return id;
    }

}
