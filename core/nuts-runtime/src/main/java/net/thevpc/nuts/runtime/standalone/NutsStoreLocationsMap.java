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
package net.thevpc.nuts.runtime.standalone;

import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsUtilStrings;

/**
 *
 * @author thevpc
 */
public class NutsStoreLocationsMap {

    private Map<NutsStoreLocation, String> locations;

    public NutsStoreLocationsMap(Map<NutsStoreLocation, String> locations) {
        this.locations = locations;
    }

    public String get(NutsStoreLocation location) {
        if (locations != null) {
            if (location != null) {
                return locations.get(location);
            }
        }
        return null;
    }

    public NutsStoreLocationsMap set(Map<NutsStoreLocation, String> locations) {
        set(new NutsStoreLocationsMap(locations));
        return this;
    }

    public NutsStoreLocationsMap set(NutsStoreLocationsMap other) {
        if (other != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = other.get(location);
                if (!NutsBlankable.isBlank(v)) {
                    set(location, v);
                }
            }
        }
        return this;
    }

    public NutsStoreLocationsMap set(NutsStoreLocation location, String value) {
        if (location != null) {
            if (NutsBlankable.isBlank(value)) {
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

    public Map<NutsStoreLocation, String> toMap() {
        Map<NutsStoreLocation, String> map = new HashMap<>();
        if (locations != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = get(location);
                if (!NutsBlankable.isBlank(v)) {
                    map.put(location, v);
                }
            }
        }
        return map;
    }

    public Map<NutsStoreLocation, String> toMapOrNull() {
        Map<NutsStoreLocation, String> m = toMap();
        if (m.isEmpty()) {
            return null;
        }
        return m;
    }
}
