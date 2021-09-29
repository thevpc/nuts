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

import net.thevpc.nuts.boot.NutsApiUtils;

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


    private static final NutsOsFamily _curr = parseLenient(System.getProperty("os.name"), UNKNOWN, UNKNOWN);
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
            case "mac":
            case "macos":
                return MACOS;
            case "unix":
                return UNIX;
            case "unknown":
                return UNKNOWN;
        }
        if (e.startsWith("linux")) {
            return NutsOsFamily.LINUX;
        }
        if (e.startsWith("win")) {
            return NutsOsFamily.WINDOWS;
        }
        if (e.startsWith("mac")) {
            return NutsOsFamily.MACOS;
        }
        if (e.startsWith("sunos")) {
            return NutsOsFamily.UNIX;
        }
        if (e.startsWith("freebsd")) {
            return NutsOsFamily.UNIX;
        }
        //process plexus os families
        switch (e) {
            case "dos":
                return NutsOsFamily.WINDOWS;
            case "netware":
                return NutsOsFamily.UNKNOWN;
            case "os/2":
                return NutsOsFamily.UNKNOWN;
            case "tandem":
                return NutsOsFamily.UNKNOWN;
            case "zos":
                return NutsOsFamily.UNKNOWN;
            case "os/400":
                return NutsOsFamily.UNIX;
            case "openvms":
                return NutsOsFamily.UNKNOWN;
        }
        return errorValue;
    }

    public static NutsOsFamily getCurrent() {
        return _curr;
    }

    public static NutsOsFamily parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsOsFamily parse(String value, NutsOsFamily emptyValue, NutsSession session) {
        NutsOsFamily v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v,value,NutsOsFamily.class,session);
        return v;
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
