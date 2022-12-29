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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

/**
 * Supported Operating System Families
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public enum NOsFamily implements NEnum {
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


    private static final NOsFamily _curr = parse(System.getProperty("os.name")).orElse(UNKNOWN);
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NOsFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NOsFamily> parse(String value) {
        return NStringUtils.parseEnum(value, NOsFamily.class, s -> {
            String e = s.getNormalizedValue();
            switch (e) {
                case "W":
                case "WIN":
                case "WINDOWS":
                    return NOptional.of(WINDOWS);
                case "L":
                case "LINUX":
                    return NOptional.of(LINUX);
                case "M":
                case "MAC":
                case "MACOS":
                    return NOptional.of(MACOS);
                case "U":
                case "UNIX":
                    return NOptional.of(UNIX);
                case "unknown":
                    return NOptional.of(UNKNOWN);
            }
            if (e.startsWith("LINUX")) {
                return NOptional.of(NOsFamily.LINUX);
            }
            if (e.startsWith("WIN")) {
                return NOptional.of(NOsFamily.WINDOWS);
            }
            if (e.startsWith("MAC")) {
                return NOptional.of(NOsFamily.MACOS);
            }
            if (e.startsWith("SUNOS")) {
                return NOptional.of(NOsFamily.UNIX);
            }
            if (e.startsWith("FREEBSD")) {
                return NOptional.of(NOsFamily.UNIX);
            }
            //process plexus os families
            switch (e) {
                case "DOS":
                    return NOptional.of(NOsFamily.WINDOWS);
                case "NETWARE":
                    return NOptional.of(NOsFamily.UNKNOWN);
                case "OS_2":
                    return NOptional.of(NOsFamily.UNKNOWN);
                case "TANDEM":
                    return NOptional.of(NOsFamily.UNKNOWN);
                case "ZOS":
                    return NOptional.of(NOsFamily.UNKNOWN);
                case "OS_400":
                    return NOptional.of(NOsFamily.UNIX);
                case "OPENVMS":
                    return NOptional.of(NOsFamily.UNKNOWN);
            }
            return null;
        });
    }


    public static NOsFamily getCurrent() {
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
