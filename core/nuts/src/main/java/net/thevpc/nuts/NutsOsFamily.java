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

import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

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


    private static final NutsOsFamily _curr = parse(System.getProperty("os.name")).orElse(UNKNOWN);
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsOsFamily() {
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    public static NutsOptional<NutsOsFamily> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsOsFamily.class, s -> {
            String e = s.getNormalizedValue();
            switch (e) {
                case "W":
                case "WIN":
                case "WINDOWS":
                    return NutsOptional.of(WINDOWS);
                case "L":
                case "LINUX":
                    return NutsOptional.of(LINUX);
                case "M":
                case "MAC":
                case "MACOS":
                    return NutsOptional.of(MACOS);
                case "U":
                case "UNIX":
                    return NutsOptional.of(UNIX);
                case "unknown":
                    return NutsOptional.of(UNKNOWN);
            }
            if (e.startsWith("LINUX")) {
                return NutsOptional.of(NutsOsFamily.LINUX);
            }
            if (e.startsWith("WIN")) {
                return NutsOptional.of(NutsOsFamily.WINDOWS);
            }
            if (e.startsWith("MAC")) {
                return NutsOptional.of(NutsOsFamily.MACOS);
            }
            if (e.startsWith("SUNOS")) {
                return NutsOptional.of(NutsOsFamily.UNIX);
            }
            if (e.startsWith("FREEBSD")) {
                return NutsOptional.of(NutsOsFamily.UNIX);
            }
            //process plexus os families
            switch (e) {
                case "DOS":
                    return NutsOptional.of(NutsOsFamily.WINDOWS);
                case "NETWARE":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "OS_2":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "TANDEM":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "ZOS":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "OS_400":
                    return NutsOptional.of(NutsOsFamily.UNIX);
                case "OPENVMS":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
            }
            return null;
        });
    }


    public static NutsOsFamily getCurrent() {
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
