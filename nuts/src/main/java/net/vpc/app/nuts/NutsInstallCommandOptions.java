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
public class NutsInstallCommandOptions {

    private boolean trace = true;
    private boolean force = false;

    public boolean isTrace() {
        return trace;
    }

    public NutsInstallCommandOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsInstallCommandOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

}
