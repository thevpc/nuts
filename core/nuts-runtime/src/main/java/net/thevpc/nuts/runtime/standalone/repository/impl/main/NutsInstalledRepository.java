package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstallLogRecord;

import java.util.Iterator;

public interface NutsInstalledRepository extends NutsRepository {

    boolean isDefaultVersion(NutsId id, NutsSession session);

    Iterator<NutsInstallInformation> searchInstallInformation(NutsSession session);

    String getDefaultVersion(NutsId id, NutsSession session);

    void setDefaultVersion(NutsId id, NutsSession session);

    NutsInstallInformation getInstallInformation(NutsId id, NutsSession session);

    NutsInstallStatus getInstallStatus(NutsId id, NutsSession session);

    void install(NutsId id, NutsSession session, NutsId forId);

    NutsInstallInformation install(NutsDefinition id, NutsSession session);

    void uninstall(NutsDefinition id, NutsSession session);

    NutsInstallInformation require(NutsDefinition id, boolean deploy, NutsId[] forId, NutsDependencyScope scope, NutsSession session);

    void unrequire(NutsId id, NutsId forId, NutsDependencyScope scope, NutsSession session);

    NutsStream<NutsInstallLogRecord> findLog(NutsSession session);
}
