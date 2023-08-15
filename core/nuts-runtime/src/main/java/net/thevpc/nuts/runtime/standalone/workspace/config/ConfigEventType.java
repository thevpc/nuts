package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum ConfigEventType  implements NEnum {
    API, RUNTIME, BOOT, MAIN, SECURITY;
    private String id;

    ConfigEventType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NOptional<ConfigEventType> parse(String value) {
        return NEnumUtils.parseEnum(value, ConfigEventType.class);
    }

}
