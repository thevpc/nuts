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
public class NutsInstallCompanionOptions {

    private boolean ask;
    private boolean force;
    private boolean trace;

    public boolean isAsk() {
        return ask;
    }

    public NutsInstallCompanionOptions setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NutsInstallCompanionOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public NutsInstallCompanionOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    

}
