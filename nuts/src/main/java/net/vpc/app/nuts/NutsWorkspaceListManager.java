package net.vpc.app.nuts;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * @date 2019-03-02
 */
public interface NutsWorkspaceListManager {

    Map<String, NutsWorkspaceLocation> getWorkspaces();

    NutsWorkspaceListConfig getConfig();

    NutsWorkspaceListManager setConfig(NutsWorkspaceListConfig config);

    NutsWorkspace addWorkspace(String name);

    void save();

    boolean deleteWorkspace(String name);

    void onOffWorkspace(String name, Boolean value);
}
