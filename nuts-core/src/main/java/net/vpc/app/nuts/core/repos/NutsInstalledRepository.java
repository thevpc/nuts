package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsInstallStatus;

import java.util.Iterator;

public interface NutsInstalledRepository extends NutsRepository {
    boolean isDefaultVersion(NutsId id, NutsSession session);

    Iterator<NutsInstallInformation> searchInstallInformation(NutsSession session);

    String getDefaultVersion(NutsId id, NutsSession session);

    void setDefaultVersion(NutsId id, NutsSession session);

    NutsInstallInformation getInstallInformation(NutsId id, NutsSession session);

    NutsInstallStatus getInstallStatus(NutsId id, NutsSession session);

    void install(NutsId id, NutsSession session, NutsId forId);

    NutsInstallInformation install(NutsDefinition id, NutsId forId, NutsSession session);

    void uninstall(NutsId id, NutsSession session);

}
