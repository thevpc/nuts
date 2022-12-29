package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;

public interface NWorkspaceConfigManagerExt extends NWorkspaceConfigManager {

    static NWorkspaceConfigManagerExt of(NWorkspaceConfigManager wsc) {
        return (NWorkspaceConfigManagerExt) wsc;
    }

    DefaultNWorkspaceConfigModel getModel();
}
