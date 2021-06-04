package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsEnum;

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
}
