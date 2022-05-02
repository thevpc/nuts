package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.reserved.NutsReservedLangUtils;

public enum NutsInstallLogAction implements NutsEnum {
    INSTALL,
    UNINSTALL,
    REQUIRE,
    UNREQUIRE,
    DEPLOY,
    UNDEPLOY;
    private final String id;

    NutsInstallLogAction() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsOptional<NutsInstallLogAction> parse(String value) {
        return NutsReservedLangUtils.parseEnum(value, NutsInstallLogAction.class);
    }


    @Override
    public String id() {
        return id;
    }
}
