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
package net.thevpc.nuts.runtime.io;

import java.util.Objects;
import net.thevpc.nuts.NutsExecutionEntry;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsExecutionEntry implements NutsExecutionEntry {

    private final String name;
    private final boolean defaultEntry;
    private final boolean app;

    public DefaultNutsExecutionEntry(String name, boolean defaultEntry, boolean app) {
        if (CoreStringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Empty name");
        }
        this.name = name;
        this.defaultEntry = defaultEntry;
        this.app = app;
    }

    @Override
    public boolean isApp() {
        return app;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDefaultEntry() {
        return defaultEntry;
    }

    @Override
    public String toString() {
        return "NutsExecutionEntry{"
                + "name='" + name + '\''
                + ", app=" + app
                + ", defaultEntry=" + defaultEntry
                + '}';
    }

    @Override
    public int compareTo(NutsExecutionEntry o) {
        if (o == null) {
            return 1;
        }
        int x = name.compareTo(o.getName());
        if (x != 0) {
            return x;
        }
        x = Boolean.compare(defaultEntry, o.isDefaultEntry());
        if (x != 0) {
            return x;
        }
        x = Boolean.compare(app, o.isApp());
        if (x != 0) {
            return x;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + (this.defaultEntry ? 1 : 0);
        hash = 17 * hash + (this.app ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NutsExecutionEntry)) {
            return false;
        }
        final NutsExecutionEntry other = (NutsExecutionEntry) obj;
        if (this.defaultEntry != other.isDefaultEntry()) {
            return false;
        }
        if (this.app != other.isApp()) {
            return false;
        }
        if (!Objects.equals(this.name, other.getName())) {
            return false;
        }
        return true;
    }
}
