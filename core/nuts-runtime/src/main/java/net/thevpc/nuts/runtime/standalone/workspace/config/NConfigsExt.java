package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;

public interface NConfigsExt extends NConfigs {

    static NConfigsExt of(NConfigs wsc) {
        return (NConfigsExt) wsc;
    }

    DefaultNWorkspaceConfigModel getModel();
}
