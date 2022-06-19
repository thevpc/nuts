package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsEnum;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

public enum ConfigEventType  implements NutsEnum {
    API, RUNTIME, BOOT, MAIN, SECURITY;
    private String id;

    ConfigEventType() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsOptional<ConfigEventType> parse(String value) {
        return NutsStringUtils.parseEnum(value, ConfigEventType.class);
    }

}
