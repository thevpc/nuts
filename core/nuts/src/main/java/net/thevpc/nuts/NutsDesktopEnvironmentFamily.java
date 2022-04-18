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
import net.thevpc.nuts.spi.NutsSelectorOp;

public enum NutsDesktopEnvironmentFamily implements NutsEnum {
    HEADLESS,
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

    public static NutsOptional<NutsDesktopEnvironmentFamily> parse(String value) {
        return NutsApiUtils.parse(value, NutsDesktopEnvironmentFamily.class, s -> {
            switch (s.toLowerCase()) {
                case "win":
                case "windows":
                case "windowsshell":
                    return NutsOptional.of(WINDOWS_SHELL);
                case "mac":
                case "macos":
                case "macaqua":
                case "macosaqua":
                case "aqua":
                    return NutsOptional.of(MACOS_AQUA);
                case "kde":
                case "plasma":
                    return NutsOptional.of(KDE);
                case "gnome":
                    return NutsOptional.of(GNOME);
                case "unknown":
                    return NutsOptional.of(UNKNOWN);
            }
            return null;
        });
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
