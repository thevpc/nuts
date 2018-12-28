package net.vpc.app.nuts;

public interface NutsInstallListener extends NutsListener {
    default void onInstall(NutsDefinition nutsDefinition, boolean update, NutsSession session) {

    }
}
