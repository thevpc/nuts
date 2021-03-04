package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsExecutionType;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspaceBootConfig;

public interface SystemNdi {
    WorkspaceAndApiVersion persistConfig(NutsWorkspaceBootConfig bootConfig, String apiVersion, String preferredName, NutsSession session);

    void configurePath(NutsSession session, boolean persistentConfig);

    List<NdiScriptnfo> createNutsScript(NdiScriptOptions options);

    void removeNutsScript(String id, NutsSession session);

    void addNutsWorkspaceScript(String preferredScriptName, String switchWorkspaceLocation, String apiVersion);

    void switchWorkspace(String switchWorkspaceLocation, String apiVersion);

    boolean isNutsBootId(NutsId id);

    List<NdiScriptnfo> createBootScript(String preferredName, String apiVersion, boolean force, boolean trace, boolean includeEnv);

    List<NdiScriptnfo> createNutsScript(List<String> idsToInstall, String switchWorkspaceLocation, String linkName, Boolean persistentConfig, ArrayList<String> executorOptions, boolean env, boolean fetch, NutsExecutionType execType, NutsApplicationContext context);
}
