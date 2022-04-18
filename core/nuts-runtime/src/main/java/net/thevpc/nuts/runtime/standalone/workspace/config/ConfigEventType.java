package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsApiUtils;

public enum ConfigEventType  implements NutsEnum {
    API, RUNTIME, BOOT, MAIN, SECURITY;
    private String id;

    ConfigEventType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsOptional<ConfigEventType> parse(String value) {
        return NutsApiUtils.parse(value, ConfigEventType.class);
    }

}
