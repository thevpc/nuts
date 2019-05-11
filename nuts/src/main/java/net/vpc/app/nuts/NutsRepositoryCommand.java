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
public interface NutsRepositoryCommand {

    NutsRepositoryCommand setSession(NutsRepositorySession session);

    NutsRepositoryCommand session(NutsRepositorySession session);

    NutsRepositorySession getSession();

    NutsRepositoryCommand run();

}
