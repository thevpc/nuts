/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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
 * @category Base
 */
public interface NutsInstallStatusFilterManager extends NutsTypedFilters<NutsInstallStatusFilter>{
//    public static final NutsInstallStatusFilter ANY=of(null, null, null, null);
//
//    public static final NutsInstallStatusFilter INSTALLED=of(true, null, null, null);
//    public static final NutsInstallStatusFilter NOT_INSTALLED=of(false, null, null, null);
//    public static final NutsInstallStatusFilter REQUIRED=of(null, true, null, null);
//    public static final NutsInstallStatusFilter NOT_REQUIRED=of(null, false, null, null);
//    public static final NutsInstallStatusFilter OBSOLETE=of(null, null, true, null);
//    public static final NutsInstallStatusFilter NOT_OBSOLETE=of(null, null, false, null);
//
//    public static final NutsInstallStatusFilter DEFAULT_VALUE=of(null, null, null, true);
//    public static final NutsInstallStatusFilter NOT_DEFAULT_VALUE=of(null, null, null, false);
//
//    public static final NutsInstallStatusFilter DEPLOYED =INSTALLED.or(REQUIRED).to(NutsInstallStatusFilter.class);
//
//    public static final NutsInstallStatusFilter NOT_DEPLOYED =DEPLOYED.neg().to(NutsInstallStatusFilter.class);


    NutsInstallStatusFilter byInstalled();

    NutsInstallStatusFilter byNotInstalled();

    NutsInstallStatusFilter byRequired();

    NutsInstallStatusFilter byNotRequired();

    NutsInstallStatusFilter byDefaultValue();

    NutsInstallStatusFilter byNotDefaultValue();

    NutsInstallStatusFilter byObsolete();

    NutsInstallStatusFilter byNotObsolete();

    NutsInstallStatusFilter byDeployed();

    NutsInstallStatusFilter byNotDeployed();

}
