package net.thevpc.nuts.toolbox.ndi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspaceBootConfig;

public interface SystemNdi {
    WorkspaceAndApiVersion persistConfig(NutsWorkspaceBootConfig bootConfig, String apiVersion, String preferredName, NutsSession session);

    void configurePath(NutsSession session, boolean persistentConfig);

    NdiScriptnfo[] createNutsScript(NdiScriptOptions options);

    void removeNutsScript(String id, NutsSession session);

    void addNutsWorkspaceScript(String preferredScriptName, String switchWorkspaceLocation, String apiVersion);

    void switchWorkspace(String switchWorkspaceLocation, String apiVersion);
}
