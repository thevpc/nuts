package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NInstallLogAction implements NEnum {
    INSTALL,
    UNINSTALL,
    REQUIRE,
    UNREQUIRE,
    DEPLOY,
    UNDEPLOY;
    private final String id;

    NInstallLogAction() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NInstallLogAction> parse(String value) {
        return NEnumUtils.parseEnum(value, NInstallLogAction.class);
    }


    @Override
    public String id() {
        return id;
    }
}
