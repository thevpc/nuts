/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;

import java.util.Collection;
import java.util.Map;

/**
 * Dependencies filter factory
 *
 * @author thevpc
 * @app.category Base
 */
public interface NDefinitionFilters extends NTypedFilters<NDefinitionFilter> {

    /**
     * return a new session bound instance of NutsDependencyFilters
     *
     * @return a new session bound instance of NutsDependencyFilters
     */
    static NDefinitionFilters of() {
        return NExtensions.of(NDefinitionFilters.class);
    }

    /**
     * accept only dependencies that match the given archs
     *
     * @param archs accepted archs list
     * @return a filter that accepts only dependencies that match the given archs
     */
    NDefinitionFilter byArch(Collection<NArchFamily> archs);

    /**
     * accept only dependencies that match the given archs
     *
     * @param archs accepted arch list
     * @return a filter that accepts only dependencies that match the given archs
     */
    NDefinitionFilter byArch(NArchFamily... archs);

    /**
     * accept only dependencies that match the given arch
     *
     * @param arch accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    NDefinitionFilter byArch(String... arch);

    /**
     * accept only dependencies that match the given OSes
     *
     * @param os accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    NDefinitionFilter byOsFamily(Collection<NOsFamily> os);

    NDefinitionFilter byOsFamily(NOsFamily... os);

    NDefinitionFilter byArchFamily(NArchFamily... values);


    /**
     * accept only dependencies that match the given OsDist list
     *
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    NDefinitionFilter byOsDist(String... osDists);

    /**
     * accept only dependencies that match the given OsDist list
     *
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    NDefinitionFilter byOsDist(Collection<String> osDists);


    /**
     * accept only dependencies that match the current Desktop Environment
     *
     * @return a filter that accepts only dependencies that match the current Desktop Environment
     */
    NDefinitionFilter byCurrentDesktopEnvironmentFamily();

    /**
     * accept only dependencies that match the current Architecture
     *
     * @return a filter that accepts only dependencies that match the current Architecture
     */
    NDefinitionFilter byCurrentArch();

    /**
     * accept only dependencies that match the current OS
     *
     * @return a filter that accepts only dependencies that match the current OS
     */
    NDefinitionFilter byCurrentOsFamily();

    /**
     * accept only dependencies that match the current environment (OS, arch, etc...)
     *
     * @return a filter that accept only dependencies that match the current environment (OS, arch, etc...)
     */
    NDefinitionFilter byCurrentEnv();

    /**
     * accept only dependencies that match the given Desktop Environment
     *
     * @param de accepted Desktop Environment
     * @return a filter that accepts only dependencies that match the given Desktop Environment
     */
    NDefinitionFilter byDesktopEnvironmentFamily(NDesktopEnvironmentFamily... de);

    NDefinitionFilter byDesktopEnvironment(String... de);

    NDefinitionFilter byDesktopEnvironment(NId... de);

    /**
     * accept only dependencies that match any of the given Platform
     *
     * @param pf accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    NDefinitionFilter byPlatform(String... pf);


    NDefinitionFilter byDefaultVersion(Boolean defaultVersion);

    NDefinitionFilter byName(String... names);

    NDefinitionFilter byEnv(Map<String, String> faceMap);

    NDefinitionFilter byPackaging(String... values);

    NDefinitionFilter byOs(NId... values);

    NDefinitionFilter byPlatformFamily(NPlatformFamily... values);

    NDefinitionFilter byPlatform(NId... values);

    NDefinitionFilter byOs(String... values);


    NDefinitionFilter byPackaging(Collection<String> values);

    NDefinitionFilter byPlatform(Collection<String> values);

    NDefinitionFilter byDesktopEnvironment(Collection<String> values);

    NDefinitionFilter byFlag(NDescriptorFlag... flags);

    NDefinitionFilter byFlag(Collection<NDescriptorFlag> flags);

    NDefinitionFilter byEffectiveFlag(NDescriptorFlag... flags);

    NDefinitionFilter byEffectiveFlag(Collection<NDescriptorFlag> flags);

    NDefinitionFilter byExtension(NVersion apiVersion);

    NDefinitionFilter byRuntime(NVersion apiVersion);

    NDefinitionFilter byCompanion(NVersion apiVersion);

    NDefinitionFilter byApiVersion(NVersion apiVersion);

    NDefinitionFilter byBootVersion(NVersion apiVersion);

    NDefinitionFilter byLockedIds(String... ids);

    NDefinitionFilter byVersion(String version);

    NDefinitionFilter byVersion(NVersion version);

    NDefinitionFilter byVersion(NVersionFilter version);

    NDefinitionFilter byInstalled(boolean value);

    NDefinitionFilter byRequired(boolean value);

    NDefinitionFilter byDefaultValue(boolean value);

    NDefinitionFilter byObsolete(boolean value);

    NDefinitionFilter byDeployed(boolean value);
}
