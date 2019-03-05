package net.vpc.app.nuts;

public enum NutsWorkspaceOpenMode {
    /**
     * Open or Create. Default Mode.
     * If the workspace is found, it will be created otherwise it will be opened
     */
    DEFAULT,
    /**
     * Create Workspace (if not found) or throw Error (if found)
     */
    CREATE,
    /**
     * Open Workspace (if found) or throw Error (if not found)
     */
    OPEN
}
