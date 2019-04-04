package net.vpc.app.nuts;

import java.util.*;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * @date 2019-03-02
 */
public interface NutsWorkspaceListManager {

    List<NutsWorkspaceLocation> getWorkspaces();

    NutsWorkspaceLocation getWorkspaceLocation(String uuid);

    NutsWorkspaceListConfig getConfig();

    NutsWorkspaceListManager setConfig(NutsWorkspaceListConfig config);

    NutsWorkspace addWorkspace(String path);

    boolean removeWorkspace(String name);

    void onOffWorkspace(String name, Boolean value);
}
