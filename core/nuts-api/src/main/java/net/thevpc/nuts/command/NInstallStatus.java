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
package net.thevpc.nuts.command;

import java.util.Objects;

/**
 * Package installation status.
 * Possible combinations are :
 * <ul>
 *    <li>NOT_INSTALLED</li>
 *    <li>REQUIRED</li>
 *    <li>INSTALLED</li>
 *    <li>INSTALLED REQUIRED</li>
 *    <li>REQUIRED OBSOLETE</li>
 *    <li>INSTALLED OBSOLETE</li>
 *    <li>INSTALLED REQUIRED OBSOLETE</li>
 * </ul>
 *
 * @app.category Base
 */
public class NInstallStatus {

    private static final NInstallStatus[] ALL = _buildNutsInstallStatusArray();

    public static final NInstallStatus NONE = of(false,false, false, false, false);
    public static final NInstallStatus REQUIRED = of(false,false, true, false, false);
    private final boolean deployed;
    private final boolean installed;
    private final boolean required;
    private final boolean obsolete;
    private final boolean defaultVersion;

    /**
     * N install status.
     *
     * @param deployed deployed
     * @param installed installed
     * @param required required
     * @param obsolete obsolete
     * @param defaultVersion default version
     * @return n install status result
     */
    private NInstallStatus(boolean deployed, boolean installed, boolean required, boolean obsolete, boolean defaultVersion) {
        this.deployed = deployed;
        this.installed = installed;
        this.required = required;
        this.obsolete = obsolete;
        this.defaultVersion = defaultVersion;
    }

    /**
     * _build nuts install status array.
     *
     * @return _build nuts install status array result
     */
    private static NInstallStatus[] _buildNutsInstallStatusArray() {
        NInstallStatus[] ALL = new NInstallStatus[32];
        for (int i = 0; i < 32; i++) {
            ALL[i] = new NInstallStatus(
                    (i & 0x1) != 0,
                    (i & 0x2) != 0,
                    (i & 0x4) != 0,
                    (i & 0x8) != 0,
                    (i & 0x10) != 0
            );
        }
        return ALL;
    }

    /**
     * Creates a new instance of of.
     *
     * @param deployed deployed
     * @param installed installed
     * @param required required
     * @param obsolete obsolete
     * @param defaultVersion default version
     * @return of result
     */
    public static NInstallStatus of(boolean deployed,boolean installed, boolean required, boolean obsolete, boolean defaultVersion) {
        return ALL[
                (deployed ? 1 : 0) * 1
                + (installed ? 1 : 0) * 2
                        + (required ? 1 : 0) * 4
                        + (obsolete ? 1 : 0) * 8
                        + (defaultVersion ? 1 : 0) * 16
                ];
    }

    /**
     * Checks if is non deployed.
     *
     * @return is non deployed result
     */
    public boolean isNonDeployed() {
        return !isInstalled() && !isRequired();
    }

    /**
     * Checks if is installed or required.
     *
     * @return is installed or required result
     */
    public boolean isInstalledOrRequired() {
        /**
         * Checks if is installed.
         *
         * @param isRequired( is required(
         * @return is installed result
         */
        return isInstalled() || isRequired();
    }

    /**
     * Checks if is deployed.
     *
     * @return is deployed result
     */
    public boolean isDeployed() {
        return deployed;
    }

    /**
     * Checks if is installed.
     *
     * @return is installed result
     */
    public boolean isInstalled() {
        return installed;
    }

    /**
     * Checks if is required.
     *
     * @return is required result
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Checks if is obsolete.
     *
     * @return is obsolete result
     */
    public boolean isObsolete() {
        return obsolete;
    }

    /**
     * Checks if is default version.
     *
     * @return is default version result
     */
    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    /**
     * With installed.
     *
     * @param installed installed
     * @return with installed result
     */
    public NInstallStatus withInstalled(boolean installed) {
        /**
         * Creates a new instance of of.
         *
         * @param deployed deployed
         * @param installed installed
         * @param required required
         * @param obsolete obsolete
         * @param defaultVersion default version
         * @return of result
         */
        return of(deployed,installed, required, obsolete, defaultVersion);
    }
    /**
     * With deployed.
     *
     * @param deployed deployed
     * @return with deployed result
     */
    public NInstallStatus withDeployed(boolean deployed) {
        /**
         * Creates a new instance of of.
         *
         * @param deployed deployed
         * @param installed installed
         * @param required required
         * @param obsolete obsolete
         * @param defaultVersion default version
         * @return of result
         */
        return of(deployed,installed, required, obsolete, defaultVersion);
    }

    /**
     * With required.
     *
     * @param required required
     * @return with required result
     */
    public NInstallStatus withRequired(boolean required) {
        /**
         * Creates a new instance of of.
         *
         * @param deployed deployed
         * @param installed installed
         * @param required required
         * @param obsolete obsolete
         * @param defaultVersion default version
         * @return of result
         */
        return of(deployed,installed, required, obsolete, defaultVersion);
    }

    /**
     * With obsolete.
     *
     * @param obsolete obsolete
     * @return with obsolete result
     */
    public NInstallStatus withObsolete(boolean obsolete) {
        /**
         * Creates a new instance of of.
         *
         * @param deployed deployed
         * @param installed installed
         * @param required required
         * @param obsolete obsolete
         * @param defaultVersion default version
         * @return of result
         */
        return of(deployed,installed, required, obsolete, defaultVersion);
    }

    /**
     * With default version.
     *
     * @param defaultVersion default version
     * @return with default version result
     */
    public NInstallStatus withDefaultVersion(boolean defaultVersion) {
        /**
         * Creates a new instance of of.
         *
         * @param deployed deployed
         * @param installed installed
         * @param required required
         * @param obsolete obsolete
         * @param defaultVersion default version
         * @return of result
         */
        return of(deployed,installed, required, obsolete, defaultVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deployed,installed, required, obsolete, defaultVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NInstallStatus that = (NInstallStatus) o;
        return deployed == that.deployed && installed == that.installed && required == that.required && obsolete == that.obsolete && defaultVersion == that.defaultVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (installed) {
            sb.append("installed");
        }
        if (deployed) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("deployed");
        }
        if (required) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("required");
        }
        if (defaultVersion) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("defaultVersion");
        }
        if (obsolete) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("obsolete");
        }
        if (sb.length() == 0) {
            sb.append("not-deployed");
        }
        return sb.toString();
    }
}
