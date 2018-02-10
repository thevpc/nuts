package net.vpc.app.nuts;

public interface NutsWorkspaceImpl {

    /////////////////////////////////////////////////////////////////
    // PRIVATE API
    boolean initializeWorkspace(NutsBootWorkspace workspaceBoot, NutsWorkspaceFactory factory, String workspaceBootId, String workspaceRuntimeId, String workspace, ClassLoader workspaceClassLoader, NutsWorkspaceCreateOptions options);

    NutsWorkspace self();
}
