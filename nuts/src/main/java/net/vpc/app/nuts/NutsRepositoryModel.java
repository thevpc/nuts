package net.vpc.app.nuts;

import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.util.Iterator;

public interface NutsRepositoryModel {
    int MIRRORING = 1;
    int LIB_READ = 2;
    int LIB_WRITE = 4;
    int LIB_OVERRIDE = 8;
    int CACHE_READ = 16;
    int CACHE_WRITE = 32;

    int LIB = LIB_READ | LIB_WRITE | LIB_OVERRIDE;
    int CACHE = CACHE_READ | CACHE_WRITE;


    String getName();

    default NutsStoreLocationStrategy getStoreLocationStrategy() {
        return null;
    }

    default int getDeployOrder() {
        return 100;
    }

    default String getUuid() {
        return null;
    }

    default int getMode() {
        return MIRRORING | LIB | CACHE;
    }

    default int getSpeed() {
        return NutsRepository.SPEED_FAST;
    }

    default String getRepositoryType() {
        return "custom";
    }

    default NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        Iterator<NutsId> allVersions = searchVersions(id, filter, session);
        NutsId a = null;
        while (allVersions != null && allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    default Iterator<NutsId> searchVersions(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        return null;
    }

    default NutsDescriptor fetchDescriptor(NutsId id, NutsRepositorySession session) {
        return null;
    }

    default NutsContent fetchContent(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        return null;
    }

    default Iterator<NutsId> search(NutsIdFilter filter, String[] roots, NutsRepositorySession session) {
        return null;
    }

    default void updateStatistics() {

    }

    default boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode) {
        return true;
    }
}
