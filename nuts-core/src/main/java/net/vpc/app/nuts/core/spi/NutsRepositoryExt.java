/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.NutsContentEvent;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIndexStoreClient;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositorySupportedAction;
import net.vpc.app.nuts.NutsDeployRepositoryCommand;
import net.vpc.app.nuts.NutsPushRepositoryCommand;
import net.vpc.app.nuts.NutsRepositoryUndeployCommand;

/**
 *
 * @author vpc
 */
public interface NutsRepositoryExt {

    static NutsRepositoryExt of(NutsRepository repo) {
        return (NutsRepositoryExt) repo;
    }

    String getIdFilename(NutsId id);

    int getFindSupportLevelCurrent(NutsRepositorySupportedAction supportedAction, NutsId id, NutsFetchMode mode);

    void fireOnUndeploy(NutsContentEvent evt);

    void fireOnDeploy(NutsContentEvent file);

    void fireOnInstall(NutsContentEvent evt);

    void fireOnPush(NutsContentEvent file);

    void fireOnAddRepository(NutsRepository repository);

    public void fireOnRemoveRepository(NutsRepository repository);

    NutsIndexStoreClient getIndexStoreClient();

    void pushImpl(NutsPushRepositoryCommand command);

    void deployImpl(NutsDeployRepositoryCommand command);

    void undeployImpl(NutsRepositoryUndeployCommand command);
}
