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

import net.thevpc.nuts.reserved.NutsReservedLangUtils;

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
        this.id = name().toLowerCase().replace('_', '-');
    }

    public static NutsOptional<NutsOsFamily> parse(String value) {
        return NutsReservedLangUtils.parseEnum(value, NutsOsFamily.class, s -> {
            String e = s.toLowerCase();
            switch (e) {
                case "w":
                case "win":
                case "windows":
                    return NutsOptional.of(WINDOWS);
                case "l":
                case "linux":
                    return NutsOptional.of(LINUX);
                case "m":
                case "mac":
                case "macos":
                    return NutsOptional.of(MACOS);
                case "u":
                case "unix":
                    return NutsOptional.of(UNIX);
                case "unknown":
                    return NutsOptional.of(UNKNOWN);
            }
            if (e.startsWith("linux")) {
                return NutsOptional.of(NutsOsFamily.LINUX);
            }
            if (e.startsWith("win")) {
                return NutsOptional.of(NutsOsFamily.WINDOWS);
            }
            if (e.startsWith("mac")) {
                return NutsOptional.of(NutsOsFamily.MACOS);
            }
            if (e.startsWith("sunos")) {
                return NutsOptional.of(NutsOsFamily.UNIX);
            }
            if (e.startsWith("freebsd")) {
                return NutsOptional.of(NutsOsFamily.UNIX);
            }
            //process plexus os families
            switch (e) {
                case "dos":
                    return NutsOptional.of(NutsOsFamily.WINDOWS);
                case "netware":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "os/2":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "tandem":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "zos":
                    return NutsOptional.of(NutsOsFamily.UNKNOWN);
                case "os/400":
                    return NutsOptional.of(NutsOsFamily.UNIX);
                case "openvms":
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
