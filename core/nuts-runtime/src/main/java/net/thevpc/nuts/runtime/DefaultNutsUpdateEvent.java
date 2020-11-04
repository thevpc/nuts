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
 *
 * Copyright (C) 2016-2020 thevpc
 *
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
package net.thevpc.nuts.runtime;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUpdateEvent;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 * @since 0.5.3
 */
public class DefaultNutsUpdateEvent implements NutsUpdateEvent {

    private final NutsDefinition oldDefinition;
    private final NutsDefinition definition;
    private final NutsSession session;
    private final boolean force;

    public DefaultNutsUpdateEvent(NutsDefinition oldDefinition,NutsDefinition definition, NutsSession session, boolean force) {
        this.oldDefinition = oldDefinition;
        this.definition = definition;
        this.session = session;
        this.force = force;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NutsDefinition getNewValue() {
        return definition;
    }

    @Override
    public NutsDefinition getOldValue() {
        return oldDefinition;
    }

    @Override
    public boolean isForce() {
        return force;
    }

}
