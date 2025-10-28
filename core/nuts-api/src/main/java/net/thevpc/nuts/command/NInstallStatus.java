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

    private NInstallStatus(boolean deployed, boolean installed, boolean required, boolean obsolete, boolean defaultVersion) {
        this.deployed = deployed;
        this.installed = installed;
        this.required = required;
        this.obsolete = obsolete;
        this.defaultVersion = defaultVersion;
    }

    private static NInstallStatus[] _buildNutsInstallStatusArray() {
        NInstallStatus[] ALL = new NInstallStatus[32];
        for (int i = 0; i < 32; i++) {
            ALL[i] = new NInstallStatus(
                    (i & 0x1) != 0,
                    (i & 0x1) != 0,
                    (i & 0x2) != 0,
                    (i & 0x4) != 0,
                    (i & 0x8) != 0
            );
        }
        return ALL;
    }

    public static NInstallStatus of(boolean deployed,boolean installed, boolean required, boolean obsolete, boolean defaultVersion) {
        return ALL[
                (deployed ? 1 : 0) * 1
                + (installed ? 1 : 0) * 2
                        + (required ? 1 : 0) * 4
                        + (obsolete ? 1 : 0) * 8
                        + (defaultVersion ? 1 : 0) * 16
                ];
    }

    public boolean isNonDeployed() {
        return !isInstalled() && !isRequired();
    }

    public boolean isInstalledOrRequired() {
        return isInstalled() || isRequired();
    }

    public boolean isDeployed() {
        return deployed;
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    public NInstallStatus withInstalled(boolean installed) {
        return of(deployed,installed, required, obsolete, defaultVersion);
    }
    public NInstallStatus withDeployed(boolean deployed) {
        return of(deployed,installed, required, obsolete, defaultVersion);
    }

    public NInstallStatus withRequired(boolean required) {
        return of(deployed,installed, required, obsolete, defaultVersion);
    }

    public NInstallStatus withObsolete(boolean obsolete) {
        return of(deployed,installed, required, obsolete, defaultVersion);
    }

    public NInstallStatus withDefaultVersion(boolean defaultVersion) {
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
