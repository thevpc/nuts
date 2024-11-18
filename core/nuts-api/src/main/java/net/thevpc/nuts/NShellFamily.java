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

import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.util.*;

import java.util.List;

/**
 * Supported Shell Families
 *
 * @author bacali95
 * @app.category Base
 * @since 0.8.3
 */
public enum NShellFamily implements NEnum {
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


    private static final NShellFamily _curr = _resolveCurrent();
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NShellFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    private static NShellFamily _resolveCurrent() {
        switch (NOsFamily.getCurrent()) {
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

    public static NOptional<NShellFamily> parse(String value) {
        return NEnumUtils.parseEnum(value, NShellFamily.class, s -> {
            String n = null;
            if (s.getValue().contains("/")) {
                List<String> parts = NStringUtils.split(s.getValue().trim().toUpperCase(), "/",true,true);
                if (parts.size() > 0) {
                    n = parts.get(parts.size() - 1);
                } else {
                    n = "";
                }
            } else {
                n = s.getNormalizedValue();
            }
            switch (n) {
                case "":
                    return NOptional.ofEmpty(() -> NMsg.ofC("%s is empty", NShellFamily.class.getSimpleName()));
                case "SH":
                    return NOptional.of(SH);
                case "BASH":
                    return NOptional.of(BASH);
                case "CSH":
                    return NOptional.of(CSH);
                case "KSH":
                    return NOptional.of(KSH);
                case "ZSH":
                    return NOptional.of(ZSH);
                case "FISH":
                    return NOptional.of(FISH);
                case "WINDOWS_CMD":
                case "WIN_CMD":
                case "CMD":
                case "WIN":
                    return NOptional.of(WIN_CMD);
                case "WINDOWS_POWER_SHELL":
                case "WINDOWS_POWERSHELL":
                case "WIN_POWER_SHELL":
                case "WIN_POWERSHELL":
                case "POWER_SHELL":
                case "POWERSHELL":
                    return NOptional.of(WIN_POWER_SHELL);
            }
            return null;
        });
    }


    public static NShellFamily getCurrent() {
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
