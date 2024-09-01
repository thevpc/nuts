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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.xtra.execentries;

import java.util.Objects;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NExecutionEntry;

/**
 *
 * @author thevpc
 */
public class DefaultNExecutionEntry implements NExecutionEntry {

    private final String name;
    private final boolean defaultEntry;
    private final boolean app;

    public DefaultNExecutionEntry(String name, boolean defaultEntry, boolean app) {
        if (NBlankable.isBlank(name)) {
            throw new IllegalArgumentException("empty name");
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
    public int compareTo(NExecutionEntry o) {
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
        if (!(obj instanceof NExecutionEntry)) {
            return false;
        }
        final NExecutionEntry other = (NExecutionEntry) obj;
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
