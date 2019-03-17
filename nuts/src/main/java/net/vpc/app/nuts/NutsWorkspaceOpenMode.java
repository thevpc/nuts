package net.vpc.app.nuts;

public enum NutsWorkspaceOpenMode {
    /**
     * Open or Create. Default Mode.
     * If the workspace is found, it will be created otherwise it will be opened
     */
    OPEN_OR_CREATE,
    /**
     * Create Workspace (if not found) or throw Error (if found)
     */
    CREATE_NEW,
    /**
     * Open Workspace (if found) or throw Error (if not found)
     */
    OPEN_EXISTING
}
