/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repos;

import java.nio.file.Path;
import java.util.Iterator;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIndexStore;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositorySupportedAction;
import net.vpc.app.nuts.NutsDeployRepositoryCommand;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsPushRepositoryCommand;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsRepositoryUndeployCommand;

/**
 *
 * @author vpc
 */
public interface NutsRepositoryExt extends NutsRepositoryExt0{

    static NutsRepositoryExt of(NutsRepository repo) {
        return (NutsRepositoryExt) repo;
    }

    NutsIndexStore getIndexStore();

    void pushImpl(NutsPushRepositoryCommand command);

    NutsDescriptor deployImpl(NutsDeployRepositoryCommand command);

    void undeployImpl(NutsRepositoryUndeployCommand command);

    void checkAllowedFetch(NutsId id, NutsRepositorySession session);

    NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session);

    Iterator<NutsId> searchVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session);

    NutsContent fetchContentImpl(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session);

    Iterator<NutsId> searchImpl(final NutsIdFilter filter, NutsRepositorySession session);

    NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session);

    boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode);
}
