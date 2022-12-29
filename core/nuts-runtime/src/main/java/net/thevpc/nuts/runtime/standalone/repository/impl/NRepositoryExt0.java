/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NSession;

/**
 *
 * @author thevpc
 */
public interface NRepositoryExt0 {

    static NRepositoryExt0 of(NRepository repo) {
        return (NRepositoryExt0) repo;
    }

    NPath getIdBasedir(NId id, NSession session);

    String getIdFilename(NId id, NSession session);
}
