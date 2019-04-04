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
public interface NutsRepositoryDeploymentOptions {

    Path getContent();

    NutsDescriptor getDescriptor();

    NutsId getId();

    String getRepository();

    boolean isForce();

    boolean isOffline();

    boolean isTrace();

    boolean isTransitive();

    NutsRepositoryDeploymentOptions setContent(Path content);

    NutsRepositoryDeploymentOptions setDescriptor(NutsDescriptor descriptor);

    NutsRepositoryDeploymentOptions setForce(boolean force);

    NutsRepositoryDeploymentOptions setId(NutsId id);

    NutsRepositoryDeploymentOptions setOffline(boolean offline);

    NutsRepositoryDeploymentOptions setRepository(String repository);

    NutsRepositoryDeploymentOptions setTrace(boolean trace);

    NutsRepositoryDeploymentOptions setTransitive(boolean transitive);
    
    NutsRepositoryDeploymentOptions copy();
    
}
