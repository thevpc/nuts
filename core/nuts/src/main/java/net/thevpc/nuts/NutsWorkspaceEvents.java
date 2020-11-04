package net.thevpc.nuts;

public interface NutsWorkspaceEvents {
    void removeRepositoryListener(NutsRepositoryListener listener);

    void addRepositoryListener(NutsRepositoryListener listener);

    NutsRepositoryListener[] getRepositoryListeners();

    void addUserPropertyListener(NutsMapListener<String, Object> listener);

    void removeUserPropertyListener(NutsMapListener<String, Object> listener);

    NutsMapListener<String, Object>[] getUserPropertyListeners();

    void removeWorkspaceListener(NutsWorkspaceListener listener);

    void addWorkspaceListener(NutsWorkspaceListener listener);

    NutsWorkspaceListener[] getWorkspaceListeners();

    void removeInstallListener(NutsInstallListener listener);

    void addInstallListener(NutsInstallListener listener);

    NutsInstallListener[] getInstallListeners();

}
