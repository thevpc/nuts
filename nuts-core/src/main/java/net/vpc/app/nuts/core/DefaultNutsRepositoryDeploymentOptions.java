/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.nio.file.Path;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsRepositoryDeploymentOptions;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositoryDeploymentOptions implements NutsRepositoryDeploymentOptions {

    private NutsId id;
    private Path content;
    private NutsDescriptor descriptor;
    private String repository;
    private boolean trace = true;
    private boolean force = false;
    private boolean offline = false;
    private boolean transitive = true;

    public NutsId getId() {
        return id;
    }

    public NutsRepositoryDeploymentOptions setId(NutsId id) {
        this.id = id;
        return this;
    }

    public Path getContent() {
        return content;
    }

    public NutsRepositoryDeploymentOptions setContent(Path content) {
        this.content = content;
        return this;
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    public NutsRepositoryDeploymentOptions setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public String getRepository() {
        return repository;
    }

    public NutsRepositoryDeploymentOptions setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public NutsRepositoryDeploymentOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsRepositoryDeploymentOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

    public boolean isOffline() {
        return offline;
    }

    public NutsRepositoryDeploymentOptions setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    public boolean isTransitive() {
        return transitive;
    }

    public NutsRepositoryDeploymentOptions setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NutsRepositoryDeploymentOptions copy() {
        return new DefaultNutsRepositoryDeploymentOptions()
                .setContent(content)
                .setDescriptor(descriptor)
                .setForce(force)
                .setId(id)
                .setOffline(offline)
                .setRepository(repository)
                .setTrace(trace)
                .setTransitive(transitive);

    }

}
