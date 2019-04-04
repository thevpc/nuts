/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.nio.file.Path;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsWorkspaceRepositoryManager;

/**
 *
 * @author vpc
 */
public interface NutsWorkspaceRepositoryManagerExt extends NutsWorkspaceRepositoryManager {

    public void removeAllRepositories();

    NutsRepository wireRepository(NutsRepository repository);

    Path getRepositoriesRoot();
}
