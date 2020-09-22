/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * classes that implement this class will have their method {@link #setWorkspace(NutsWorkspace)}
 * called upon its creation (by factory) with a non {@code null} argument to <strong>initialize</strong>.
 * They <strong>may</strong> accept a call with a {@code null}
 * argument later to <strong>dispose</strong> the instance.
 * @author vpc
 * @category SPI Base
 */
public interface NutsWorkspaceAware {

    /**
     * initialize or dispose the instance.
     * when workspace is not null, the instance should initialize it values
     * accordingly.
     * when workspace is null, the instance should dispose resources.
     * @param workspace workspace reference or null
     */
    void setWorkspace(NutsWorkspace workspace);

}
