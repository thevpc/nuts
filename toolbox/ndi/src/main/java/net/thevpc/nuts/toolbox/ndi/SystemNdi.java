package net.thevpc.nuts.toolbox.ndi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspaceBootConfig;

import java.io.IOException;

public interface SystemNdi {
    boolean persistConfig(NutsWorkspaceBootConfig bootConfig, String apiVersion, NutsSession session);

    void configurePath(NutsSession session, boolean persistentConfig);

    NdiScriptnfo[] createNutsScript(NdiScriptOptions options);

    void removeNutsScript(String id, NutsSession session);

    void switchWorkspace(String switchWorkspaceLocation, String apiVersion);
}
