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

import net.thevpc.nuts.*;

/**
 *
 * @author thevpc
 */
public class NutsHomeLocationsMap {

    private Map<NutsHomeLocation, String> locations;

    public NutsHomeLocationsMap(Map<NutsHomeLocation, String> locations) {
        this.locations = locations;
    }

    public String get(NutsHomeLocation location) {
        if (locations != null) {
            if (location != null) {
                return locations.get(location);
            }
        }
        return null;
    }

    public NutsHomeLocationsMap set(Map<NutsHomeLocation, String> locations) {
        return set(new NutsHomeLocationsMap(locations));
    }

    public NutsHomeLocationsMap set(NutsHomeLocationsMap other) {
        if (other != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = other.get(NutsHomeLocation.of(null, location));
                if (!NutsBlankable.isBlank(v)) {
                    set(NutsHomeLocation.of(null, location), v);
                }
            }
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                for (NutsOsFamily osFamily : NutsOsFamily.values()) {
                    String v = other.get(NutsHomeLocation.of(osFamily, location));
                    if (!NutsBlankable.isBlank(v)) {
                        set(NutsHomeLocation.of(osFamily, location), v);
                    }
                }
            }
        }
        return this;
    }

    public NutsHomeLocationsMap set(NutsHomeLocation type, String value) {
        NutsStoreLocation storeLocation = type.getStoreLocation();
        if (storeLocation != null) {
            if (NutsBlankable.isBlank(value)) {
                if (locations != null) {
                    locations.remove(type);
                }
            } else {
                if (locations == null) {
                    locations = new HashMap<>();
                }
                locations.put(type, value);
            }
        }
        return this;
    }

    public Map<NutsHomeLocation, String> toMap() {
        Map<NutsHomeLocation, String> map = new HashMap<>();
        if (locations != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = get(NutsHomeLocation.of(null, location));
                if (!NutsBlankable.isBlank(v)) {
                    map.put(NutsHomeLocation.of(null, location), v);
                }
            }
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                for (NutsOsFamily osFamily : NutsOsFamily.values()) {
                    String v = get(NutsHomeLocation.of(osFamily, location));
                    if (!NutsBlankable.isBlank(v)) {
                        map.put(NutsHomeLocation.of(osFamily, location), v);
                    }
                }
            }
        }
        return map;
    }

    public Map<NutsHomeLocation, String> toMapOrNull() {
        Map<NutsHomeLocation, String> m = toMap();
        if (m.isEmpty()) {
            return null;
        }
        return m;
    }
}
