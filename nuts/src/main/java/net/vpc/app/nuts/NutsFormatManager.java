package net.vpc.app.nuts;

public interface NutsFormatManager {
    NutsIdFormat createIdFormat();

    NutsWorkspaceVersionFormat createWorkspaceVersionFormat();

    NutsWorkspaceInfoFormat createWorkspaceInfoFormat();

}
