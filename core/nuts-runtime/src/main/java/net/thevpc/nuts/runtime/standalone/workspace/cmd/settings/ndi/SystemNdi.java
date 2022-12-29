package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;

public interface SystemNdi {
//    PathInfo[] persistConfigGlobal(NutsEnvInfo env, boolean createDesktop, boolean createMenu);
//
//    PathInfo[] persistConfigSpecial(String name, String fileName, NutsEnvInfo env, boolean createDesktop, boolean createMenu, boolean createShortcut);

    PathInfo[] createArtifactScript(NdiScriptOptions options);

    void removeNutsScript(String id, String switchWorkspaceLocation, NSession session);

//    void addNutsWorkspaceScript(String preferredScriptName, NutsEnvInfo env);

    PathInfo[] switchWorkspace(NdiScriptOptions options);

    boolean isNutsBootId(NId id);

    PathInfo[] addScript(
            NdiScriptOptions options,
            String[] all);
}
