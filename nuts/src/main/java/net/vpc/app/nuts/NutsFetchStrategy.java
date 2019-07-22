/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public enum NutsFetchStrategy implements Iterable<NutsFetchMode> {

    OFFLINE(true, NutsFetchMode.LOCAL),
    ONLINE(true, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE),
    ANYWHERE(false, NutsFetchMode.LOCAL, NutsFetchMode.REMOTE),
    INSTALLED(true, NutsFetchMode.INSTALLED),
    REMOTE(true, NutsFetchMode.REMOTE);

    private final boolean stopFast;
    private NutsFetchMode[] all;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

    NutsFetchStrategy(boolean stopFast, NutsFetchMode... all) {
        this.id = name().toLowerCase().replace('_', '-');
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
