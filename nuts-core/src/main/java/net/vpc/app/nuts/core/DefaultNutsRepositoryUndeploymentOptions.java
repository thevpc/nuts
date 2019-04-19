/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsRepositoryUndeploymentOptions;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositoryUndeploymentOptions implements NutsRepositoryUndeploymentOptions {

    private NutsId id;
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
    public NutsRepositoryUndeploymentOptions setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsRepositoryUndeploymentOptions setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsRepositoryUndeploymentOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsRepositoryUndeploymentOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

    @Override
    public NutsRepositoryUndeploymentOptions transitive() {
        return transitive(true);
    }

    @Override
    public NutsRepositoryUndeploymentOptions setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsRepositoryUndeploymentOptions setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsRepositoryUndeploymentOptions id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsRepositoryUndeploymentOptions repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsRepositoryUndeploymentOptions trace() {
        return trace(true);
    }

    @Override
    public NutsRepositoryUndeploymentOptions trace(boolean trace) {
        return setTrace(trace);
    }

    @Override
    public NutsRepositoryUndeploymentOptions force() {
        return force(true);
    }

    @Override
    public NutsRepositoryUndeploymentOptions force(boolean force) {
        return setForce(force);
    }

    @Override
    public NutsRepositoryUndeploymentOptions offline() {
        return offline(true);
    }

    @Override
    public NutsRepositoryUndeploymentOptions offline(boolean offline) {
        return setOffline(offline);
    }

    @Override
    public NutsRepositoryUndeploymentOptions transitive(boolean transitive) {
        return setTransitive(transitive);
    }

    @Override
    public NutsRepositoryUndeploymentOptions copy() {
        return new DefaultNutsRepositoryUndeploymentOptions()
                .setForce(force)
                .setId(id)
                .setOffline(offline)
                .setRepository(repository)
                .setTrace(trace)
                .setTransitive(transitive);

    }

}
