package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;

public interface NInstalledRepository extends NRepository {

    boolean isDefaultVersion(NId id);

    NIterator<NInstallInformation> searchInstallInformation();

    String getDefaultVersion(NId id);

    void setDefaultVersion(NId id);

    NInstallInformation getInstallInformation(NId id);

    NInstallStatus getInstallStatus(NId id);

    void install(NId id, NId forId);

    NInstallInformation install(NDefinition id);

    void uninstall(NDefinition id);

    NInstallInformation require(NDefinition id, boolean deploy, NId[] forId, NDependencyScope scope);

    void unrequire(NId id, NId forId, NDependencyScope scope);

    NStream<NInstallLogRecord> findLog();
}
