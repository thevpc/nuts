/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.nio.file.Path;

/**
 *
 * @author vpc
 */
public interface NutsFetchContentRepositoryCommand extends NutsRepositoryCommand {

    NutsFetchContentRepositoryCommand setId(NutsId id);

    NutsId getId();

    @Override
    public NutsFetchContentRepositoryCommand run();

    @Override
    public NutsFetchContentRepositoryCommand session(NutsRepositorySession session);

    @Override
    public NutsFetchContentRepositoryCommand setSession(NutsRepositorySession session);

    NutsContent getResult();

    NutsFetchContentRepositoryCommand id(NutsId id);

    Path getLocalPath();

    NutsFetchContentRepositoryCommand localPath(Path localPath);

    NutsFetchContentRepositoryCommand setLocalPath(Path localPath);

    NutsDescriptor getDescriptor();

    NutsFetchContentRepositoryCommand setDescriptor(NutsDescriptor descriptor);

    NutsFetchContentRepositoryCommand descriptor(NutsDescriptor descriptor);

}
