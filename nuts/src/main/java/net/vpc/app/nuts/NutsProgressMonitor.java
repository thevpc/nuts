/**
 * ====================================================================
 *            vpc-common-io : common reusable library for
 *                          input/output
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

/**
 * Monitor handles events from copy, compress and delete actions
 * @author vpc
 * @since 0.5.8
 * @category Base
 */
public interface NutsProgressMonitor {

    /**
     * called when the action starts
     * @param event event
     */
    void onStart(NutsProgressEvent event);

    /**
     * called when the action terminates
     * @param event event
     */
    void onComplete(NutsProgressEvent event);

    /**
     * called when the action does a step forward and return
     * true if the progress was handled of false otherwise.
     *
     * @param event event
     * @return true if the progress was handled. In that case, a
     * mark point is registered to compute partial time and speed.
     */
    boolean onProgress(NutsProgressEvent event);

}
