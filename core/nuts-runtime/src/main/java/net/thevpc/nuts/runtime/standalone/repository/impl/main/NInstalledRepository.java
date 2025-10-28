package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependencyScope;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NInstallInformation;
import net.thevpc.nuts.command.NInstallStatus;
import net.thevpc.nuts.core.NRepository;
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

    NInstallInformation require(NDefinition id, NId[] forId, NDependencyScope scope);
    NInstallInformation deploy(NDefinition id);

    void unrequire(NId id, NId forId, NDependencyScope scope);

    NStream<NInstallLogRecord> findLog();
}
