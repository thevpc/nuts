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
public class NutsDeployOptions {

    private boolean trace = true;
    private boolean force = false;

    public boolean isTrace() {
        return trace;
    }

    public NutsDeployOptions setTrace(boolean traceEnabled) {
        this.trace = traceEnabled;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsDeployOptions setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

}
