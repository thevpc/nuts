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
package net.vpc.app.nuts.runtime.app;

import net.vpc.app.nuts.NutsArgumentCandidate;

/**
 * Default (simple) NutsArgumentCandidate implementation.
 * @author vpc
 * @since 0.5.5
 */
public class NutsDefaultArgumentCandidate implements NutsArgumentCandidate {

    private final String value;
    private final String display;

    /**
     *
     * @param value
     */
    public NutsDefaultArgumentCandidate(String value) {
        this.value = value;
        this.display = value;
    }

    public NutsDefaultArgumentCandidate(String value, String display) {
        this.value = value;
        this.display = display;
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public String getValue() {
        return value;
    }
}
