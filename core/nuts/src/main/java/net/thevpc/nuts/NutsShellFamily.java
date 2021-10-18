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

    private static NutsShellFamily _resolveCurrent() {
        switch (NutsOsFamily.getCurrent()) {
            case WINDOWS: {
                return WIN_CMD;
            }
            case LINUX:
            case UNIX: {
                return parseLenient(System.getenv("SHELL"), BASH, BASH);
            }
            case MACOS: {
                return parseLenient(System.getenv("SHELL"), ZSH, ZSH);
            }
        }
        return UNKNOWN;
    }

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsShellFamily() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsShellFamily parseLenient(String value) {
        return parseLenient(value, UNKNOWN);
    }

    public static NutsShellFamily parseLenient(String value, NutsShellFamily emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsShellFamily parseLenient(String e, NutsShellFamily emptyValue, NutsShellFamily errorValue) {
        if (e == null) {
            e = "";
        } else {
            String[] parts = e.trim().toLowerCase().split("/");
            if (parts.length > 0) {
                e = parts[parts.length - 1];
            } else {
                e = "";
            }
        }
        switch (e) {
            case "":
                return emptyValue;
            case "sh":
                return SH;
            case "bash":
                return BASH;
            case "csh":
                return CSH;
            case "ksh":
                return KSH;
            case "zsh":
                return ZSH;
            case "fish":
                return FISH;
            case "windows_cmd":
            case "win_cmd":
            case "cmd":
            case "win":
                return WIN_CMD;
            case "windows_power_shell":
            case "windows_powershell":
            case "win_power_shell":
            case "win_powershell":
            case "power_shell":
            case "powershell":
                return WIN_POWER_SHELL;
        }
        return errorValue;
    }

    public static NutsShellFamily getCurrent() {
        return _curr;
    }

    public static NutsShellFamily parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsShellFamily parse(String value, NutsShellFamily emptyValue, NutsSession session) {
        NutsShellFamily v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsShellFamily.class, session);
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
