package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import java.util.List;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspaceBootConfig;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.CreateNutsScriptCommand;

public interface SystemNdi {
    WorkspaceAndApiVersion persistConfig(String switchWorkspaceLocation, String apiVersion, String preferredName);

    void configurePath(boolean persistentConfig);

    List<NdiScriptInfo> createNutsScript(NdiScriptOptions options);

    void removeNutsScript(String id, NutsSession session);

    void addNutsWorkspaceScript(String preferredScriptName, String switchWorkspaceLocation, String apiVersion);

    void switchWorkspace(String switchWorkspaceLocation, String apiVersion);

    boolean isNutsBootId(NutsId id);

    List<NdiScriptInfo> createBootScript(String preferredName, String apiVersion, boolean force, boolean trace, boolean includeEnv);

    List<NdiScriptInfo> createNutsScript(
            CreateNutsScriptCommand cmd,
//            List<String> idsToInstall, String switchWorkspaceLocation, String linkName, Boolean persistentConfig,
//            ArrayList<String> executorOptions, boolean env, boolean fetch, NutsExecutionType execType,
            NutsApplicationContext context);
}
