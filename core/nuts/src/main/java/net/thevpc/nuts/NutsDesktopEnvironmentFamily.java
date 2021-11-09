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

import net.thevpc.nuts.boot.NutsApiUtils;

public enum NutsDesktopEnvironmentFamily implements NutsEnum {
    WINDOWS_SHELL,
    MACOS_AQUA,
    KDE,
    GNOME,
    LXDE,
    LXQT,
    XFCE,
    MATE,

    CDE,
    OPENBOX,

    LUMINA,
    UNITY,
    UBUNTU,
    PANTHEON,
    CINNAMON,
    DEEPIN,
    BUDGIE,
    ENLIGHTENMENT,
    AWESOME,
    I3,

    UNKNOWN,
    NONE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsDesktopEnvironmentFamily() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsDesktopEnvironmentFamily parseLenient(String e) {
        return parseLenient(e, UNKNOWN);
    }

    public static NutsDesktopEnvironmentFamily parseLenient(String e, NutsDesktopEnvironmentFamily emptyOrErrorValue) {
        return parseLenient(e, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsDesktopEnvironmentFamily parseLenient(String e, NutsDesktopEnvironmentFamily emptyValue, NutsDesktopEnvironmentFamily errorValue) {
        if (e == null) {
            e = "";
        } else {
            e = e.toLowerCase().trim().replace("-", "").replace("_", "");
        }
        switch (e) {
            case "":
                return emptyValue;
            case "win":
            case "windows":
            case "windowsshell":
                return WINDOWS_SHELL;
            case "mac":
            case "macos":
            case "macaqua":
            case "macosaqua":
            case "aqua":
                return MACOS_AQUA;
            case "kde":
            case "plasma":
                return KDE;
            case "gnome":
                return GNOME;
            case "unknown":
                return UNKNOWN;
            default: {
                try {
                    return NutsDesktopEnvironmentFamily.valueOf(e.toUpperCase());
                } catch (Exception ex) {
                    //ignore error
                }
            }
        }
        return errorValue;
    }

    public static NutsDesktopEnvironmentFamily parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsDesktopEnvironmentFamily parse(String value, NutsDesktopEnvironmentFamily emptyValue, NutsSession session) {
        NutsDesktopEnvironmentFamily v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsDesktopEnvironmentFamily.class, session);
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
