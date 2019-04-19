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

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsRepositoryDeploymentOptions setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public Path getContent() {
        return content;
    }

    @Override
    public NutsRepositoryDeploymentOptions setContent(Path content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsRepositoryDeploymentOptions setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsRepositoryDeploymentOptions setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsRepositoryDeploymentOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsRepositoryDeploymentOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsRepositoryDeploymentOptions setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsRepositoryDeploymentOptions setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public NutsRepositoryDeploymentOptions id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsRepositoryDeploymentOptions repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsRepositoryDeploymentOptions trace() {
        return trace(true);
    }

    @Override
    public NutsRepositoryDeploymentOptions trace(boolean trace) {
        return setTrace(trace);
    }

    @Override
    public NutsRepositoryDeploymentOptions force() {
        return force(true);
    }

    @Override
    public NutsRepositoryDeploymentOptions force(boolean force) {
        return setForce(force);
    }

    @Override
    public NutsRepositoryDeploymentOptions offline() {
        return offline(true);
    }

    @Override
    public NutsRepositoryDeploymentOptions offline(boolean offline) {
        return setOffline(offline);
    }

    @Override
    public NutsRepositoryDeploymentOptions transitive(boolean transitive) {
        return setTransitive(transitive);
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
