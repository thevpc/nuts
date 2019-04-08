/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author vpc
 */
public enum NutsFetchStrategy implements Iterable<NutsFetchMode> {

    OFFLINE(true, NutsFetchMode.INSTALLED, NutsFetchMode.LOCAL),
    WIRED(true, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE),
    ONLINE(true, NutsFetchMode.INSTALLED, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE),
    ANYWHERE(false, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE),
    INSTALLED(true, NutsFetchMode.INSTALLED),
    LOCAL(true, NutsFetchMode.LOCAL),
    REMOTE(true, NutsFetchMode.REMOTE);

    private boolean stopFast;
    private NutsFetchMode[] all;

    private NutsFetchStrategy(boolean stopFast, NutsFetchMode... all) {
        this.stopFast = stopFast;
        this.all = Arrays.copyOf(all, all.length);
    }

    public boolean isStopFast() {
        return stopFast;
    }

    public NutsFetchMode[] modes() {
        return Arrays.copyOf(all, all.length);
    }

    @Override
    public Iterator<NutsFetchMode> iterator() {
        return Arrays.asList(all).iterator();
    }

}
