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
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * uniform platform architecture impl-note: list updated from
 * https://github.com/trustin/os-maven-plugin
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.1
 */
public enum NArchFamily implements NEnum {
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

    private static final NArchFamily _curr = parse(System.getProperty("os.arch")).orElse(UNKNOWN);
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NArchFamily() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NArchFamily> parse(String value) {
        return NEnumUtils.parseEnum(value, NArchFamily.class, s -> {
            String arch = s.getNormalizedValue();
            switch (arch) {
                case "X8632":
                case "X86":
                case "I386":
                case "I486":
                case "I586":
                case "I686":
                case "IA32":
                case "X32":
                case "X86_32":
                    return NOptional.of(X86_32);

                case "X8664":
                case "AMD64":
                case "IA32E":
                case "EM64T":
                case "X64":
                case "X86_64":
                    return NOptional.of(X86_64);

                case "IA64N":
                case "ITANIUM_32":
                    return NOptional.of(ITANIUM_32);

                case "SPARC":
                case "SPARC32":
                case "SPARC_32":
                    return NOptional.of(SPARC_32);

                case "SPARCV9":
                case "SPARC64":
                case "SPARC_64":
                    return NOptional.of(SPARC_64);

                case "ARM":
                case "ARM32":
                case "ARM_32":
                    return NOptional.of(ARM_32);

                case "ARM64": //merged with aarch64
                case "AARCH64":
                case "AARCH_64":
                    return NOptional.of(AARCH_64);

                case "MIPS":
                case "MIPS32":
                case "MIPS_32":
                    return NOptional.of(MIPS_32);

                case "MIPS_64":
                    return NOptional.of(MIPS_64);

                case "MIPSEL":
                case "MIPS32EL":
                case "MIPSEL_32":
                    return NOptional.of(MIPSEL_32);

                case "MIPS64":
                case "MIPS64EL":
                case "MIPSEL_64":
                    return NOptional.of(MIPSEL_64);

                case "PPC":
                case "PPC32":
                case "PPC_32":
                    return NOptional.of(PPC_32);

                case "PPC64":
                case "PPC_64":
                    return NOptional.of(PPC_64);

                case "PPCLE":
                case "PPCLE32":
                case "PPCLE_32":
                    return NOptional.of(PPCLE_32);

                case "PPC64LE":
                case "PPCLE_64":
                    return NOptional.of(PPCLE_64);

                case "S390":
                case "S390_32":
                    return NOptional.of(S390_32);

                case "S390X":
                case "S390_64":
                    return NOptional.of(S390_64);

                case "ARM_64":
                    return NOptional.of(ARM_64);

                case "IA64W":
                case "ITANIUM64":
                case "ITANIUM_64":
                    return NOptional.of(ITANIUM_64);
                case "UNKNOWN":
                    return NOptional.of(UNKNOWN);
                default: {
                    if (arch.startsWith("IA64W") && arch.length() == 6) {
                        return NOptional.of(ITANIUM_64);
                    }
                }
            }
            return null;
        });
    }

    public boolean is64Bit() {
        return getBits() == 64;
    }

    public boolean isArm() {
        switch (this) {
            case ARM_64:
            case ARM_32:
                return true;
            case AARCH_64:
            case ITANIUM_64:
            case MIPSEL_64:
            case MIPS_64:
            case PPCLE_64:
            case S390_64:
            case SPARC_64:
            case X86_64:
            case PPC_64:
            case ITANIUM_32:
            case MIPSEL_32:
            case MIPS_32:
            case PPCLE_32:
            case S390_32:
            case SPARC_32:
            case X86_32:
            case PPC_32:
                return false;
        }
        return false;
    }

    public boolean isX86() {
        switch (this) {
            case X86_64:
            case X86_32:
                return true;
            case ARM_64:
            case ARM_32:
            case AARCH_64:
            case ITANIUM_64:
            case MIPSEL_64:
            case MIPS_64:
            case PPCLE_64:
            case S390_64:
            case SPARC_64:
            case PPC_64:
            case ITANIUM_32:
            case MIPSEL_32:
            case MIPS_32:
            case PPCLE_32:
            case S390_32:
            case SPARC_32:
            case PPC_32:
                return false;
        }
        return false;
    }

    public int getBits() {
        switch (this) {
            case AARCH_64:
            case ARM_64:
            case ITANIUM_64:
            case MIPSEL_64:
            case MIPS_64:
            case PPCLE_64:
            case S390_64:
            case SPARC_64:
            case X86_64:
            case PPC_64:
                return 64;
            case ARM_32:
            case ITANIUM_32:
            case MIPSEL_32:
            case MIPS_32:
            case PPCLE_32:
            case S390_32:
            case SPARC_32:
            case X86_32:
            case PPC_32:
                return 32;
            case UNKNOWN: {
                return -1;
            }
        }
        return -1;
    }

    public static NArchFamily getCurrent() {
        return _curr;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    @Override
    public String id() {
        return id;
    }
}
