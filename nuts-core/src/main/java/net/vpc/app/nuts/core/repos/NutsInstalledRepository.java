package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;

public interface NutsInstalledRepository extends NutsRepository {
    boolean isDefaultVersion(NutsId id, NutsSession session);

    String getDefaultVersion(NutsId id, NutsSession session);

    void setDefaultVersion(NutsId id, NutsSession session);

    NutsInstallInformation getInstallInformation(NutsId id, NutsSession session);

    boolean isInstalled(NutsId id, NutsSession session);

    NutsInstallInformation install(NutsDefinition id, NutsSession session);

    void uninstall(NutsId id, NutsSession session);

}
