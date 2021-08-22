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

/**
 * Supported Operating System Families
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public enum NutsOsFamily implements NutsEnum {
    /**
     * Microsoft Window Operating system Family
     */
    WINDOWS,
    /**
     * Linux Distribution Operating system Family
     */
    LINUX,
    /**
     * Apple MacOS Operating system Family
     */
    MACOS,
    /**
     * Generic Unix Operating system Family
     */
    UNIX,
    /**
     * Uncategorized Operating system Family
     */
    UNKNOWN;


    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsOsFamily() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsOsFamily parseLenient(String value) {
        return parseLenient(value, UNKNOWN);
    }

    public static NutsOsFamily parseLenient(String value, NutsOsFamily emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsOsFamily parseLenient(String e, NutsOsFamily emptyValue, NutsOsFamily errorValue) {
        if (e == null) {
            e = "";
        } else {
            e = e.trim().toLowerCase();
        }
        switch (e.toLowerCase()) {
            case "": {
                return emptyValue;
            }
            case "win":
            case "windows":
                return WINDOWS;
            case "linux":
                return LINUX;
            case "macos":
                return MACOS;
            case "unix":
                return UNIX;
            case "unknown":
                return UNKNOWN;
        }
        return errorValue;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
