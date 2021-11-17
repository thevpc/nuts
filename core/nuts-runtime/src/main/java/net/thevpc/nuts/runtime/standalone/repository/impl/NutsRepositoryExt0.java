/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author thevpc
 */
public interface NutsRepositoryExt0 {

    static NutsRepositoryExt0 of(NutsRepository repo) {
        return (NutsRepositoryExt0) repo;
    }

    NutsPath getIdBasedir(NutsId id, NutsSession session);

    String getIdFilename(NutsId id, NutsSession session);
}
