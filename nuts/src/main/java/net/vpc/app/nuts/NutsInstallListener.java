package net.vpc.app.nuts;

public interface NutsInstallListener extends NutsListener {
    default void onInstall(NutsFile nutsFile, boolean update, NutsSession session) {

    }
}
