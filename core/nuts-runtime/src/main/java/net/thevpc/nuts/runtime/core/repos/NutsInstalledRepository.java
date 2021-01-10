package net.thevpc.nuts.runtime.core.repos;

import net.thevpc.nuts.*;

import java.util.Iterator;
import java.util.Set;

public interface NutsInstalledRepository extends NutsRepository {
    boolean isDefaultVersion(NutsId id, NutsSession session);

    Iterator<NutsInstallInformation> searchInstallInformation(NutsSession session);

    String getDefaultVersion(NutsId id, NutsSession session);

    void setDefaultVersion(NutsId id, NutsSession session);

    NutsInstallInformation getInstallInformation(NutsId id, NutsSession session);

    NutsInstallStatus getInstallStatus(NutsId id, NutsSession session);

    void install(NutsId id, NutsSession session, NutsId forId);

    NutsInstallInformation install(NutsDefinition id, NutsSession session);
    void uninstall(NutsId id, NutsSession session);

    NutsInstallInformation require(NutsDefinition id, boolean deploy,NutsId[] forId, NutsDependencyScope scope, NutsSession session);

    void unrequire(NutsId id, NutsId forId, NutsDependencyScope scope, NutsSession session);

}
