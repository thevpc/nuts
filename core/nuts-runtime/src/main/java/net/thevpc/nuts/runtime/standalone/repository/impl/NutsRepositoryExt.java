/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NutsRepositorySupportedAction;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;
import net.thevpc.nuts.util.NutsIterator;

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

    NutsIterator<NutsId> searchVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session);

    NutsPath fetchContentImpl(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session);

    NutsIterator<NutsId> searchImpl(final NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session);

    NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session);

    boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode, NutsSession session);
}
