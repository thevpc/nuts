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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.NHomeLocation;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.NOsFamily;

/**
 *
 * @author thevpc
 */
public class NHomeLocationsMap {

    private Map<NHomeLocation, String> locations;

    public NHomeLocationsMap(Map<NHomeLocation, String> locations) {
        this.locations = locations;
    }

    public String get(NHomeLocation location) {
        if (locations != null) {
            if (location != null) {
                return locations.get(location);
            }
        }
        return null;
    }

    public NHomeLocationsMap set(Map<NHomeLocation, String> locations) {
        return set(new NHomeLocationsMap(locations));
    }

    public NHomeLocationsMap set(NHomeLocationsMap other) {
        if (other != null) {
            for (NStoreType location : NStoreType.values()) {
                String v = other.get(NHomeLocation.of(null, location));
                if (!NBlankable.isBlank(v)) {
                    set(NHomeLocation.of(null, location), v);
                }
            }
            for (NStoreType location : NStoreType.values()) {
                for (NOsFamily osFamily : NOsFamily.values()) {
                    String v = other.get(NHomeLocation.of(osFamily, location));
                    if (!NBlankable.isBlank(v)) {
                        set(NHomeLocation.of(osFamily, location), v);
                    }
                }
            }
        }
        return this;
    }

    public NHomeLocationsMap set(NHomeLocation type, String value) {
        NStoreType storeLocation = type.getStoreLocation();
        if (storeLocation != null) {
            if (NBlankable.isBlank(value)) {
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

    public Map<NHomeLocation, String> toMap() {
        Map<NHomeLocation, String> map = new HashMap<>();
        if (locations != null) {
            for (NStoreType location : NStoreType.values()) {
                String v = get(NHomeLocation.of(null, location));
                if (!NBlankable.isBlank(v)) {
                    map.put(NHomeLocation.of(null, location), v);
                }
            }
            for (NStoreType location : NStoreType.values()) {
                for (NOsFamily osFamily : NOsFamily.values()) {
                    String v = get(NHomeLocation.of(osFamily, location));
                    if (!NBlankable.isBlank(v)) {
                        map.put(NHomeLocation.of(osFamily, location), v);
                    }
                }
            }
        }
        return map;
    }

    public Map<NHomeLocation, String> toMapOrNull() {
        Map<NHomeLocation, String> m = toMap();
        if (m.isEmpty()) {
            return null;
        }
        return m;
    }
}
