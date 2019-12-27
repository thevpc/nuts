/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.util.Iterator;

/**
 *
 * @author vpc
 */
public interface NutsRepositoryExt0 {

    static NutsRepositoryExt0 of(NutsRepository repo) {
        return (NutsRepositoryExt0) repo;
    }

    String getIdBasedir(NutsId id);

    String getIdFilename(NutsId id);
}
