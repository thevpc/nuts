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
package net.thevpc.nuts.runtime;

import java.util.HashMap;
import java.util.Map;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class NutsStoreLocationsMap {

    private Map<String, String> locations;

    public NutsStoreLocationsMap(Map<String, String> locations) {
        this.locations = locations;
    }

    public String get(NutsStoreLocation location) {
        if (locations != null) {
            if (location != null) {
                return locations.get(location.id());
            }
        }
        return null;
    }

    public NutsStoreLocationsMap set(Map<String, String> locations) {
        set(new NutsStoreLocationsMap(locations));
        return this;
    }

    public NutsStoreLocationsMap set(NutsStoreLocationsMap other) {
        if (other != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = other.get(location);
                if (!CoreStringUtils.isBlank(v)) {
                    set(location, v);
                }
            }
        }
        return this;
    }

    public NutsStoreLocationsMap set(NutsStoreLocation location, String value) {
        if (location != null) {
            if (CoreStringUtils.isBlank(value)) {
                if (locations != null) {
                    locations.remove(location.id());
                }
            } else {
                if (locations == null) {
                    locations = new HashMap<>();
                }
                locations.put(location.id(), value);
            }
        }
        return this;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        if (locations != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = get(location);
                if (!CoreStringUtils.isBlank(v)) {
                    map.put(location.id(), v);
                }
            }
        }
        return map;
    }

    public Map<String, String> toMapOrNull() {
        Map<String, String> m = toMap();
        if (m.isEmpty()) {
            return null;
        }
        return m;
    }
}
