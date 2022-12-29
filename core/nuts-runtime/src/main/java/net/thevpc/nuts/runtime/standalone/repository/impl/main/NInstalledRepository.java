package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

public interface NInstalledRepository extends NRepository {

    boolean isDefaultVersion(NId id, NSession session);

    NIterator<NInstallInformation> searchInstallInformation(NSession session);

    String getDefaultVersion(NId id, NSession session);

    void setDefaultVersion(NId id, NSession session);

    NInstallInformation getInstallInformation(NId id, NSession session);

    NInstallStatus getInstallStatus(NId id, NSession session);

    void install(NId id, NSession session, NId forId);

    NInstallInformation install(NDefinition id, NSession session);

    void uninstall(NDefinition id, NSession session);

    NInstallInformation require(NDefinition id, boolean deploy, NId[] forId, NDependencyScope scope, NSession session);

    void unrequire(NId id, NId forId, NDependencyScope scope, NSession session);

    NStream<NInstallLogRecord> findLog(NSession session);
}
