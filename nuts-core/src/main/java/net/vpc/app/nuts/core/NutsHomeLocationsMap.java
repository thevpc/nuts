/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsOsFamily;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsWorkspaceOptions;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class NutsHomeLocationsMap {

    private Map<String, String> locations;

    public NutsHomeLocationsMap(Map<String, String> locations) {
        this.locations = locations;
    }

    public String get(NutsOsFamily osFamily, NutsStoreLocation location) {
        if (locations != null) {
            if (location != null) {
                return locations.get(NutsWorkspaceOptions.createHomeLocationKey(osFamily, location));
            }
        }
        return null;
    }

    public void set(Map<String, String> locations) {
        set(new NutsHomeLocationsMap(locations));
    }

    public void set(NutsHomeLocationsMap other) {
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
    }

    public void set(NutsOsFamily osFamily, NutsStoreLocation location, String value) {
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
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        if (locations != null) {
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                String v = get(null, location);
                if (!CoreStringUtils.isBlank(v)) {
                    map.put(NutsWorkspaceOptions.createHomeLocationKey(null, location), v);
                }
            }
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                for (NutsOsFamily osFamily : NutsOsFamily.values()) {
                    String v = get(osFamily, location);
                    if (!CoreStringUtils.isBlank(v)) {
                        map.put(NutsWorkspaceOptions.createHomeLocationKey(osFamily, location), v);
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
