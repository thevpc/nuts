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
public class NutsUninstallOptions {

    private boolean trace = true;
    private boolean erase = false;

    public boolean isTrace() {
        return trace;
    }

    public NutsUninstallOptions setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public boolean isErase() {
        return erase;
    }

    public NutsUninstallOptions setErase(boolean erase) {
        this.erase = erase;
        return this;
    }

}
