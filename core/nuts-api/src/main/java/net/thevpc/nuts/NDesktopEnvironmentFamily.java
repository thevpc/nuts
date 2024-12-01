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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NDesktopEnvironmentFamily implements NEnum {
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

    NDesktopEnvironmentFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NDesktopEnvironmentFamily> parse(String value) {
        return NEnumUtils.parseEnum(value, NDesktopEnvironmentFamily.class, s -> {
            switch (s.getNormalizedValue()) {
                case "WIN":
                case "WINDOWS":
                case "WINDOWSSHELL":
                    return NOptional.of(WINDOWS_SHELL);
                case "MAC":
                case "MACOS":
                case "MACAQUA":
                case "MACOSAQUA":
                case "AQUA":
                    return NOptional.of(MACOS_AQUA);
                case "KDE":
                case "PLASMA":
                    return NOptional.of(KDE);
                case "GNOME":
                    return NOptional.of(GNOME);
                case "UNKNOWN":
                    return NOptional.of(UNKNOWN);
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
