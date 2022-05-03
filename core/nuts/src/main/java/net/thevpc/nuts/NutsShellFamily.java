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

import net.thevpc.nuts.util.NutsUtils;

/**
 * Supported Shell Families
 *
 * @author bacali95
 * @app.category Base
 * @since 0.8.3
 */
public enum NutsShellFamily implements NutsEnum {
    /**
     * Posix Shell Family
     */
    SH,
    /**
     * Bash Shell Family
     */
    BASH,
    /**
     * Csh Shell Family
     */
    CSH,
    /**
     * Ksh Shell Family
     */
    KSH,
    /**
     * Zsh Shell Family
     */
    ZSH,
    /**
     * Fish Shell Family
     */
    FISH,
    /**
     * Windows cmd standard shell
     */
    WIN_CMD,
    /**
     * Windows power shell
     */
    WIN_POWER_SHELL,
    /**
     * Uncategorized Shell Family
     */
    UNKNOWN;


    private static final NutsShellFamily _curr = _resolveCurrent();
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsShellFamily() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    private static NutsShellFamily _resolveCurrent() {
        switch (NutsOsFamily.getCurrent()) {
            case WINDOWS: {
                return WIN_CMD;
            }
            case LINUX:
            case UNIX: {
                return parse(System.getenv("SHELL")).orElse(BASH);
            }
            case MACOS: {
                return parse(System.getenv("SHELL")).orElse(ZSH);
            }
        }
        return UNKNOWN;
    }

    public static NutsOptional<NutsShellFamily> parse(String value) {
        return NutsUtils.parseEnum(value, NutsShellFamily.class, s -> {
            String[] parts = s.trim().toLowerCase().split("/");
            if (parts.length > 0) {
                s = parts[parts.length - 1];
            } else {
                s = "";
            }
            switch (s) {
                case "":
                    return NutsOptional.ofEmpty(session -> NutsMessage.ofCstyle("%s is empty",NutsShellFamily.class.getSimpleName()));
                case "sh":
                    return NutsOptional.of(SH);
                case "bash":
                    return NutsOptional.of(BASH);
                case "csh":
                    return NutsOptional.of(CSH);
                case "ksh":
                    return NutsOptional.of(KSH);
                case "zsh":
                    return NutsOptional.of(ZSH);
                case "fish":
                    return NutsOptional.of(FISH);
                case "windows_cmd":
                case "win_cmd":
                case "cmd":
                case "win":
                    return NutsOptional.of(WIN_CMD);
                case "windows_power_shell":
                case "windows_powershell":
                case "win_power_shell":
                case "win_powershell":
                case "power_shell":
                case "powershell":
                    return NutsOptional.of(WIN_POWER_SHELL);
            }
            return null;
        });
    }


    public static NutsShellFamily getCurrent() {
        return _curr;
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
