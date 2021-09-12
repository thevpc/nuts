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
 * Supported Shell Families
 *
 * @author bacali95
 * @app.category Base
 * @since 0.8.3
 */
public enum NutsShellFamily implements NutsEnum {
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
     * Uncategorized Shell Family
     */
    UNKNOWN;


    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;
    private static final NutsShellFamily _curr = parseLenient(System.getenv("SHELL"), UNKNOWN, UNKNOWN);

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
            if(parts.length>0) {
                e = parts[parts.length - 1];
            }else{
                e="";
            }
        }
        switch (e) {
            case "":
                return emptyValue;
            case "sh":
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
        }
        return errorValue;
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
