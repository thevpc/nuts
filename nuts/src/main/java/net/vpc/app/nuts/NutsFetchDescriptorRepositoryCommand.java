/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 */
public interface NutsFetchDescriptorRepositoryCommand extends NutsRepositoryCommand {

    NutsFetchDescriptorRepositoryCommand setId(NutsId id);

    NutsId getId();

    @Override
    public NutsFetchDescriptorRepositoryCommand run();

    @Override
    public NutsFetchDescriptorRepositoryCommand session(NutsRepositorySession session);

    @Override
    public NutsFetchDescriptorRepositoryCommand setSession(NutsRepositorySession session);

    NutsDescriptor getResult();

    NutsFetchDescriptorRepositoryCommand id(NutsId id);

}
