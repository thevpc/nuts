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
public class NutsPushOptions {

    private boolean trace = true;
    private boolean force = false;
    private boolean offline = false;
    private String repository;

    
    public boolean isTrace() {
        return trace;
    }

    public String getRepository() {
        return repository;
    }

    public NutsPushOptions setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public NutsPushOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsPushOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

    public boolean isOffline() {
        return offline;
    }

    public NutsPushOptions setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

}
