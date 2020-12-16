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
import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.core.CoreNutsWorkspaceOptions;

/**
 *
 * @author thevpc
 */
public class NutsHomeLocationsMap {

    private Map<String, String> locations;

    public NutsHomeLocationsMap(Map<String, String> locations) {
        this.locations = locations;
    }

    public String get(NutsOsFamily osFamily, NutsStoreLocation location) {
        if (locations != null) {
            if (location != null) {
                return locations.get(CoreNutsWorkspaceOptions.createHomeLocationKey(osFamily, location));
            }
        }
        return null;
    }

    public NutsHomeLocationsMap set(Map<String, String> locations) {
        return set(new NutsHomeLocationsMap(locations));
    }

    public NutsHomeLocationsMap set(NutsHomeLocationsMap other) {
        if (other != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = other.get(null, location);
                if (!CoreStringUtils.isBlank(v)) {
                    set(null, location, v);
                }
            }
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                for (NutsOsFamily osFamily : NutsOsFamily.values()) {
                    String v = other.get(osFamily, location);
                    if (!CoreStringUtils.isBlank(v)) {
                        set(osFamily, location, v);
                    }
                }
            }
        }
        return this;
    }

    public NutsHomeLocationsMap set(NutsOsFamily osFamily, NutsStoreLocation location, String value) {
        if (location != null) {
            if (CoreStringUtils.isBlank(value)) {
                if (locations != null) {
                    locations.remove(CoreNutsWorkspaceOptions.createHomeLocationKey(osFamily, location));
                }
            } else {
                if (locations == null) {
                    locations = new HashMap<>();
                }
                locations.put(CoreNutsWorkspaceOptions.createHomeLocationKey(osFamily, location), value);
            }
        }
        return this;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        if (locations != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = get(null, location);
                if (!CoreStringUtils.isBlank(v)) {
                    map.put(CoreNutsWorkspaceOptions.createHomeLocationKey(null, location), v);
                }
            }
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                for (NutsOsFamily osFamily : NutsOsFamily.values()) {
                    String v = get(osFamily, location);
                    if (!CoreStringUtils.isBlank(v)) {
                        map.put(CoreNutsWorkspaceOptions.createHomeLocationKey(osFamily, location), v);
                    }
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
