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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NStoreType;

/**
 *
 * @author thevpc
 */
public class NStoreLocationsMap {

    private Map<NStoreType, String> locations;

    public NStoreLocationsMap(Map<NStoreType, String> locations) {
        this.locations = locations;
    }

    public String get(NStoreType location) {
        if (locations != null) {
            if (location != null) {
                return locations.get(location);
            }
        }
        return null;
    }

    public NStoreLocationsMap set(Map<NStoreType, String> locations) {
        set(new NStoreLocationsMap(locations));
        return this;
    }

    public NStoreLocationsMap set(NStoreLocationsMap other) {
        if (other != null) {
            for (NStoreType location : NStoreType.values()) {
                String v = other.get(location);
                if (!NBlankable.isBlank(v)) {
                    set(location, v);
                }
            }
        }
        return this;
    }

    public NStoreLocationsMap set(NStoreType location, String value) {
        if (location != null) {
            if (NBlankable.isBlank(value)) {
                if (locations != null) {
                    locations.remove(location);
                }
            } else {
                if (locations == null) {
                    locations = new HashMap<>();
                }
                locations.put(location, value);
            }
        }
        return this;
    }

    public Map<NStoreType, String> toMap() {
        Map<NStoreType, String> map = new HashMap<>();
        if (locations != null) {
            for (NStoreType location : NStoreType.values()) {
                String v = get(location);
                if (!NBlankable.isBlank(v)) {
                    map.put(location, v);
                }
            }
        }
        return map;
    }

    public Map<NStoreType, String> toMapOrNull() {
        Map<NStoreType, String> m = toMap();
        if (m.isEmpty()) {
            return null;
        }
        return m;
    }
}
