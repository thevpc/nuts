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
package net.thevpc.nuts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * NutsHomeLocation is a compound enumeration that associates OS family to store location.
 * It is used as a key to define path layout for a given OsFamily and a StoreLocation.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.3
 */
public class NutsHomeLocation implements NutsEnum {
    private static final Map<String, NutsHomeLocation> CACHE = new HashMap<>();
    private final NutsOsFamily osFamily;
    private final NutsStoreLocation storeLocation;

    private NutsHomeLocation(NutsOsFamily osFamily, NutsStoreLocation storeLocation) {
        this.osFamily = osFamily;
        this.storeLocation = storeLocation;
    }

    public static NutsHomeLocation of(NutsOsFamily osFamily, NutsStoreLocation storeLocation) {
        String key = (osFamily == null ? "system" : osFamily.id()) + "_" + (storeLocation == null ? "system" : storeLocation.id());
        NutsHomeLocation instance = CACHE.get(key);
        if (instance == null) {
            synchronized (CACHE) {
                instance = CACHE.get(key);
                if (instance == null) {
                    instance = new NutsHomeLocation(osFamily, storeLocation);
                    CACHE.put(key, instance);
                }
            }
        }
        return instance;
    }

    public static NutsHomeLocation parseLenient(String value) {
        return parseLenient(value, NutsHomeLocation.of(null, null));
    }

    public static NutsHomeLocation parseLenient(String value, NutsHomeLocation emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsHomeLocation parseLenient(String e, NutsHomeLocation emptyValue, NutsHomeLocation errorValue) {
        if (e == null) {
            e = "";
        } else {
            e = e.trim().toLowerCase();
        }
        switch (e.toLowerCase()) {
            case "": {
                return emptyValue;
            }
            default: {
                e = e.toLowerCase().replace(':', '_').replace('-', '_');
                int i = e.lastIndexOf('_');
                if (i >= 0) {
                    String s1 = e.substring(0, i);
                    String s2 = e.substring(i + 1);
                    NutsOsFamily osf = s1.equals("system") ? null : NutsOsFamily.parse(s1, null, null);
                    NutsStoreLocation loc = s2.equals("system") ? null : NutsStoreLocation.parse(s2, null, null);
                    if (osf == null) {
                        if (!s1.equals("system") && s1.length() > 0) {
                            return errorValue;
                        }
                    }
                    if (loc == null) {
                        if (!s2.equals("system") && s2.length() > 0) {
                            return errorValue;
                        }
                    }
                    return of(osf, loc);
                }
                return errorValue;
            }
        }
    }

    public static NutsHomeLocation parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsHomeLocation parse(String value, NutsHomeLocation emptyValue, NutsSession session) {
        NutsHomeLocation v = parseLenient(value, emptyValue, null);
        if (v == null) {
            if (!NutsBlankable.isBlank(value)) {
                if (session == null) {
                    throw new NutsBootException(NutsMessage.cstyle("invalid value %s of type %s", value, NutsHomeLocation.class.getName()));
                }
                throw new NutsParseException(session, NutsMessage.cstyle("invalid value %s of type %s", value, NutsHomeLocation.class.getName()));
            }
        }
        return v;
    }

    /**
     * OS family
     *
     * @return OS family
     */
    public NutsOsFamily getOsFamily() {
        return osFamily;
    }

    /**
     * Store Location
     *
     * @return Store Location
     */
    public NutsStoreLocation getStoreLocation() {
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
        NutsHomeLocation that = (NutsHomeLocation) o;
        return osFamily == that.osFamily && storeLocation == that.storeLocation;
    }

    @Override
    public String toString() {
        return name();
    }

    /**
     * Returns the name of this pseudo enum constant as a concatenation ('_' separated) of OsFamily and StoreLocation.
     * if any of osFamily or storeLocation is null, it is replaced by 'SYSTEM'
     *
     * @return the name of this pseudo enum constant
     */
    public String name() {
        return (osFamily == null ? "SYSTEM" : osFamily.name()) + "_" + (storeLocation == null ? "SYSTEM" : storeLocation.name());
    }

    /**
     * Returns the id of this pseudo enum constant as a concatenation ('-' separated) of OsFamily and StoreLocation ids.
     * if any of osFamily or storeLocation is null, it is replaced by 'system'
     *
     * @return the id of this pseudo enum constant
     */
    public String id() {
        return (osFamily == null ? "system" : osFamily.id()) + "-" + (storeLocation == null ? "system" : storeLocation.id());
    }
}
