/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.spi.NDeployRepositoryCmd;
import net.thevpc.nuts.spi.NPushRepositoryCmd;
import net.thevpc.nuts.spi.NRepositoryUndeployCmd;
import net.thevpc.nuts.util.NIterator;

/**
 *
 * @author thevpc
 */
public interface NRepositoryExt extends NRepositoryExt0 {

    static NRepositoryExt of(NRepository repo) {
        return (NRepositoryExt) repo;
    }

    NIndexStore getIndexStore();

    void pushImpl(NPushRepositoryCmd command);

    NDescriptor deployImpl(NDeployRepositoryCmd command);

    void undeployImpl(NRepositoryUndeployCmd command);

    void checkAllowedFetch(NId id, NSession session);

    NDescriptor fetchDescriptorImpl(NId id, NFetchMode fetchMode, NSession session);

    NIterator<NId> searchVersionsImpl(NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session);

    NPath fetchContentImpl(NId id, NDescriptor descriptor, NFetchMode fetchMode, NSession session);

    NIterator<NId> searchImpl(final NIdFilter filter, NFetchMode fetchMode, NSession session);

    NId searchLatestVersion(NId id, NIdFilter filter, NFetchMode fetchMode, NSession session);

    boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode, NSession session);
}
