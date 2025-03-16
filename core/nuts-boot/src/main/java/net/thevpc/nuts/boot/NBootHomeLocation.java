/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * NutsHomeLocation is a compound enumeration that associates OS family to store location.
 * It's used as a key to define path layout for a given OsFamily and a StoreLocation.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.3
 */
public class NBootHomeLocation {
    private static final Map<String, NBootHomeLocation> CACHE = new HashMap<>();
    private final String osFamily;
    private final String storeLocation;

    private NBootHomeLocation(String osFamily, String storeLocation) {
        this.osFamily = osFamily;
        this.storeLocation = storeLocation;
    }

    public static NBootHomeLocation of(String osFamily, String storeLocation) {
        String key = NBootUtils.enumId(osFamily == null ? "system" : osFamily) + "_" + NBootUtils.enumId(storeLocation == null ? "system" : storeLocation);
        NBootHomeLocation instance = CACHE.get(key);
        if (instance == null) {
            synchronized (CACHE) {
                instance = CACHE.get(key);
                if (instance == null) {
                    instance = new NBootHomeLocation(osFamily == null ? null : NBootUtils.enumName(osFamily), storeLocation == null ? null : NBootUtils.enumName(storeLocation));
                    CACHE.put(key, instance);
                }
            }
        }
        return instance;
    }

    public static NBootHomeLocation parse(String value) {
        if (value == null) {
            value = "";
        } else {
            value = value.trim().toLowerCase();
        }
        if (value.isEmpty()) {
            return null;
        }
        String e = value.replace(':', '_').replace('-', '_');
        String finalValue = value;
        int i = e.lastIndexOf('_');
        if (i >= 0) {
            String s1 = e.substring(0, i);
            String s2 = e.substring(i + 1);
            String osf = s1.equals("system") ? null : s1;
            String loc = s2.equals("system") ? null : s2;
            if (osf == null) {
                if (!s1.equals("system") && s1.length() > 0) {
                    throw new NBootException(NBootMsg.ofC(NBootHomeLocation.class.getSimpleName() + " invalid value : %s", finalValue));
                }
            }
            if (loc == null) {
                if (!s2.equals("system") && s2.length() > 0) {
                    throw new NBootException(NBootMsg.ofC(NBootHomeLocation.class.getSimpleName() + " invalid value : %s", finalValue));
                }
            }
            return of(osf, loc);
        }
        throw new IllegalArgumentException(NBootMsg.ofC(NBootHomeLocation.class.getSimpleName() + " invalid value : %s", finalValue).toString());
    }


    /**
     * OS family
     *
     * @return OS family
     */
    public String getOsFamily() {
        return osFamily;
    }

    /**
     * Store Location
     *
     * @return Store Location
     */
    public String getStoreLocation() {
        return storeLocation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(osFamily, storeLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NBootHomeLocation that = (NBootHomeLocation) o;
        return Objects.equals(osFamily, that.osFamily) && Objects.equals(storeLocation, that.storeLocation);
    }

    @Override
    public String toString() {
        return name();
    }

    /**
     * Returns the name of this pseudo enum constant as a concatenation ('_' separated) of OsFamily and StoreLocation.
     * if any of osFamily or storeLocation is null, It's replaced by 'SYSTEM'
     *
     * @return the name of this pseudo enum constant
     */
    public String name() {
        return ((osFamily == null ? "SYSTEM" : NBootUtils.enumName(osFamily)) + "_" + (storeLocation == null ? "SYSTEM" : NBootUtils.enumName(storeLocation))).toUpperCase();
    }

    /**
     * Returns the id of this pseudo enum constant as a concatenation ('-' separated) of OsFamily and StoreLocation ids.
     * if any of osFamily or storeLocation is null, It's replaced by 'system'
     *
     * @return the id of this pseudo enum constant
     */
    public String id() {
        return (osFamily == null ? "system" : NBootUtils.enumId(osFamily)) + "-" + (storeLocation == null ? "system" : NBootUtils.enumId(storeLocation));
    }
}
