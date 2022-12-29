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

    public static final NInstallStatus NONE = of(false, false, false, false);
    public static final NInstallStatus INSTALLED = of(true, false, false, false);
    public static final NInstallStatus REQUIRED = of(false, true, false, false);
    public static final NInstallStatus OBSOLETE = of(false, false, true, false);
    public static final NInstallStatus DEFAULT_VALUE = of(false, false, false, true);
    private final boolean installed;
    private final boolean required;
    private final boolean obsolete;
    private final boolean defaultVersion;

    private NInstallStatus(boolean installed, boolean required, boolean obsolete, boolean defaultVersion) {
        this.installed = installed;
        this.required = required;
        this.obsolete = obsolete;
        this.defaultVersion = defaultVersion;
    }

    private static NInstallStatus[] _buildNutsInstallStatusArray() {
        NInstallStatus[] ALL = new NInstallStatus[16];
        for (int i = 0; i < 16; i++) {
            ALL[i] = new NInstallStatus(
                    (i & 0x1) != 0,
                    (i & 0x2) != 0,
                    (i & 0x4) != 0,
                    (i & 0x8) != 0
            );
        }
        return ALL;
    }

    public static NInstallStatus of(boolean installed, boolean required, boolean obsolete, boolean defaultVersion) {
        return ALL[
                (installed ? 1 : 0) * 1
                        + (required ? 1 : 0) * 2
                        + (obsolete ? 1 : 0) * 4
                        + (defaultVersion ? 1 : 0) * 8
                ];
    }

    public boolean isDeployed() {
        return isInstalled() || isRequired();
    }

    public boolean isNonDeployed() {
        return !isInstalled() && !isRequired();
    }

    public boolean isInstalledOrRequired() {
        return isInstalled() || isRequired();
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
        return of(installed, required, obsolete, defaultVersion);
    }

    public NInstallStatus withRequired(boolean required) {
        return of(installed, required, obsolete, defaultVersion);
    }

    public NInstallStatus withObsolete(boolean obsolete) {
        return of(installed, required, obsolete, defaultVersion);
    }

    public NInstallStatus withDefaultVersion(boolean defaultVersion) {
        return of(installed, required, obsolete, defaultVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(installed, required, obsolete, defaultVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NInstallStatus that = (NInstallStatus) o;
        return installed == that.installed && required == that.required && obsolete == that.obsolete && defaultVersion == that.defaultVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (installed) {
            sb.append("installed");
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
