package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsEnum;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

public enum NutsInstallLogAction implements NutsEnum {
    INSTALL,
    UNINSTALL,
    REQUIRE,
    UNREQUIRE,
    DEPLOY,
    UNDEPLOY;
    private final String id;

    NutsInstallLogAction() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    public static NutsOptional<NutsInstallLogAction> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsInstallLogAction.class);
    }


    @Override
    public String id() {
        return id;
    }
}
