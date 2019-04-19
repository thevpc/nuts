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

    public NutsId getId() {
        return id;
    }


    public NutsRepositoryUndeploymentOptions setId(NutsId id) {
        this.id = id;
        return this;
    }

    public String getRepository() {
        return repository;
    }


    public NutsRepositoryUndeploymentOptions setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public boolean isTrace() {
        return trace;
    }


    public NutsRepositoryUndeploymentOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isForce() {
        return force;
    }


    public NutsRepositoryUndeploymentOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

    public NutsRepositoryUndeploymentOptions transitive() {
        return transitive(true);
    }

    public NutsRepositoryUndeploymentOptions setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    public boolean isTransitive() {
        return transitive;
    }
    
    public NutsRepositoryUndeploymentOptions setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }


    public boolean isOffline() {
        return offline;
    }

    public NutsRepositoryUndeploymentOptions id(NutsId id) {
        return setId(id);
    }
    public NutsRepositoryUndeploymentOptions repository(String repository) {
        return setRepository(repository);
    }
    public NutsRepositoryUndeploymentOptions trace() {
        return trace(true);
    }

    public NutsRepositoryUndeploymentOptions trace(boolean trace) {
        return setTrace(trace);
    }
    public NutsRepositoryUndeploymentOptions force() {
        return force(true);
    }

    public NutsRepositoryUndeploymentOptions force(boolean force) {
        return setForce(force);
    }
    public NutsRepositoryUndeploymentOptions offline() {
        return offline(true);
    }

    public NutsRepositoryUndeploymentOptions offline(boolean offline) {
        return setOffline(offline);
    }
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
