/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.repos;

import java.nio.file.Path;
import java.util.Iterator;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsRepositorySupportedAction;

/**
 *
 * @author thevpc
 */
public interface NutsRepositoryExt extends NutsRepositoryExt0{

    static NutsRepositoryExt of(NutsRepository repo) {
        return (NutsRepositoryExt) repo;
    }

    NutsIndexStore getIndexStore();

    void pushImpl(NutsPushRepositoryCommand command);

    NutsDescriptor deployImpl(NutsDeployRepositoryCommand command);

    void undeployImpl(NutsRepositoryUndeployCommand command);

    void checkAllowedFetch(NutsId id, NutsSession session);

    NutsDescriptor fetchDescriptorImpl(NutsId id, NutsFetchMode fetchMode, NutsSession session);

    Iterator<NutsId> searchVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session);

    NutsContent fetchContentImpl(NutsId id, NutsDescriptor descriptor, Path localPath, NutsFetchMode fetchMode, NutsSession session);

    Iterator<NutsId> searchImpl(final NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session);

    NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session);

    boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode, NutsSession session);
}
