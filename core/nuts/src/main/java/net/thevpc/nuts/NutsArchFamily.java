/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

/**
 * uniform platform architecture impl-note: list updated from
 * https://github.com/trustin/os-maven-plugin
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.1
 */
public enum NutsArchFamily implements NutsEnum {
    X86_32,
    X86_64,
    ITANIUM_32,
    SPARC_32,
    SPARC_64,
    ARM_32,
    ARM_64,
    AARCH_64,
    MIPS_32,
    MIPSEL_32,
    MIPS_64,
    MIPSEL_64,
    PPC_32,
    PPCLE_32,
    PPC_64,
    PPCLE_64,
    S390_32,
    S390_64,
    ITANIUM_64,
    UNKNOWN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsArchFamily() {
        this.id = name().toLowerCase();//.replace('_', '-');
    }

    /**
     * resolved platform architecture (from {@code System.getProperty("os.arch")})
     * or UNKNOWN
     * @return resolved platform architecture
     */
    public static NutsArchFamily getArchFamily() {
        return parseLenient(System.getProperty("os.arch"),UNKNOWN);
    }

    /**
     * parse string and return null if parse fails
     * @param arch value to parse
     * @return parsed instance or null
     */
    public static NutsArchFamily parseLenient(String arch) {
        return parseLenient(arch, null);
    }

    /**
     * parse string and return {@code emptyOrErrorValue} if parse fails
     * @param arch value to parse
     * @return parsed instance or {@code emptyOrErrorValue}
     */
    public static NutsArchFamily parseLenient(String arch, NutsArchFamily emptyOrErrorValue) {
        return parseLenient(arch, emptyOrErrorValue, emptyOrErrorValue);
    }

    /**
     *
     * parse string and return {@code emptyValue} when null or {@code errorValue} if parse fails
     * @param arch value to parse
     * @param emptyValue value when the value is null or empty
     * @param errorValue value when the value cannot be parsed
     * @return parsed value
     */
    public static NutsArchFamily parseLenient(String arch, NutsArchFamily emptyValue, NutsArchFamily errorValue) {
        arch = arch == null ? "" : arch.toLowerCase().replace('-', '_').trim();
        switch (arch) {
            case "x8632":
            case "x86":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
            case "ia32":
            case "x32":
            case "x86_32":
                return X86_32;

            case "x8664":
            case "amd64":
            case "ia32e":
            case "em64t":
            case "x64":
            case "x86_64":
                return X86_64;

            case "ia64n":
            case "itanium_32":
                return ITANIUM_32;

            case "sparc":
            case "sparc32":
            case "sparc_32":
                return SPARC_32;

            case "sparcv9":
            case "sparc64":
            case "sparc_64":
                return SPARC_64;

            case "arm":
            case "arm32":
            case "arm_32":
                return ARM_32;

            case "arm64": //merged with aarch64
            case "aarch64":
            case "aarch_64":
                return AARCH_64;

            case "mips":
            case "mips32":
            case "mips_32":
                return MIPS_32;

            case "mips_64":
                return MIPS_64;

            case "mipsel":
            case "mips32el":
            case "mipsel_32":
                return MIPSEL_32;

            case "mips64":
            case "mips64el":
            case "mipsel_64":
                return MIPSEL_64;

            case "ppc":
            case "ppc32":
            case "ppc_32":
                return PPC_32;

            case "ppc64":
            case "ppc_64":
                return PPC_64;

            case "ppcle":
            case "ppcle32":
            case "ppcle_32":
                return PPCLE_32;

            case "ppc64le":
            case "ppcle_64":
                return PPCLE_64;

            case "s390":
            case "s390_32":
                return S390_32;

            case "s390x":
            case "s390_64":
                return S390_64;

            case "arm_64":
                return ARM_64;

            case "ia64w":
            case "itanium64":
            case "itanium_64":
                return ITANIUM_64;
            case "unknown":
                return UNKNOWN;
            case "": {
                return emptyValue;
            }
            default: {
                if (arch.startsWith("ia64w") && arch.length() == 6) {
                    return ITANIUM_64;
                }
                return errorValue;
            }
        }
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
