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
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NDesktopEnvironmentFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NRuntimeDistributionFamily;

import java.util.Collection;
import java.util.Map;

/**
 * Dependencies filter factory
 *
 * @author thevpc
 * @app.category Base
 */
public interface NDefinitionFilterRPI extends NTypedFilters<NDefinitionFilter> {

    /**
     * return a new session bound instance of NutsDependencyFilters
     *
     * @return a new session bound instance of NutsDependencyFilters
     */
    static NDefinitionFilterRPI of() {
        return NExtensions.of(NDefinitionFilterRPI.class);
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

    /**
     * By os family.
     *
     * @param os os
     * @return by os family result
     */
    NDefinitionFilter byOsFamily(NOsFamily... os);

    /**
     * By arch family.
     *
     * @param values values
     * @return by arch family result
     */
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

    /**
     * By desktop environment.
     *
     * @param de de
     * @return by desktop environment result
     */
    NDefinitionFilter byDesktopEnvironment(String... de);

    /**
     * By desktop environment.
     *
     * @param de de
     * @return by desktop environment result
     */
    NDefinitionFilter byDesktopEnvironment(NId... de);

    /**
     * accept only dependencies that match any of the given Platform
     *
     * @param pf accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    NDefinitionFilter byPlatform(String... pf);


    /**
     * search for default versions status.
     * <ul>
     * <li>return true of only default values are searched for</li>
     * <li>return false of only default values are searched for</li>
     * <li>return null of both default values and non default ones are searched
     * for</li>
     * </ul>
     *
     * @return search for default versions status
     * @since v0.5.5
     */
    NDefinitionFilter byDefaultVersion(Boolean defaultVersion);

    /**
     * By name.
     *
     * @param names names
     * @return by name result
     */
    NDefinitionFilter byName(String... names);

    /**
     * By env.
     *
     * @param faceMap face map
     * @return by env result
     */
    NDefinitionFilter byEnv(Map<String, String> faceMap);

    /**
     * By packaging.
     *
     * @param values values
     * @return by packaging result
     */
    NDefinitionFilter byPackaging(String... values);

    /**
     * By os.
     *
     * @param values values
     * @return by os result
     */
    NDefinitionFilter byOs(NId... values);

    /**
     * By platform family.
     *
     * @param values values
     * @return by platform family result
     */
    NDefinitionFilter byPlatformFamily(NRuntimeDistributionFamily... values);

    /**
     * By platform.
     *
     * @param values values
     * @return by platform result
     */
    NDefinitionFilter byPlatform(NId... values);

    /**
     * By os.
     *
     * @param values values
     * @return by os result
     */
    NDefinitionFilter byOs(String... values);


    /**
     * By packaging.
     *
     * @param values values
     * @return by packaging result
     */
    NDefinitionFilter byPackaging(Collection<String> values);

    /**
     * By platform.
     *
     * @param values values
     * @return by platform result
     */
    NDefinitionFilter byPlatform(Collection<String> values);

    /**
     * By desktop environment.
     *
     * @param values values
     * @return by desktop environment result
     */
    NDefinitionFilter byDesktopEnvironment(Collection<String> values);

    /**
     * By flag.
     *
     * @param flags flags
     * @return by flag result
     */
    NDefinitionFilter byFlag(NDescriptorFlag... flags);

    /**
     * By flag.
     *
     * @param flags flags
     * @return by flag result
     */
    NDefinitionFilter byFlag(Collection<NDescriptorFlag> flags);

    /**
     * By effective flag.
     *
     * @param flags flags
     * @return by effective flag result
     */
    NDefinitionFilter byEffectiveFlag(NDescriptorFlag... flags);

    /**
     * By effective flag.
     *
     * @param flags flags
     * @return by effective flag result
     */
    NDefinitionFilter byEffectiveFlag(Collection<NDescriptorFlag> flags);

    /**
     * By extension.
     *
     * @param apiVersion api version
     * @return by extension result
     */
    NDefinitionFilter byExtension(NVersion apiVersion);

    /**
     * By runtime.
     *
     * @param apiVersion api version
     * @return by runtime result
     */
    NDefinitionFilter byRuntime(NVersion apiVersion);

    /**
     * By companion.
     *
     * @param apiVersion api version
     * @return by companion result
     */
    NDefinitionFilter byCompanion(NVersion apiVersion);

    /**
     * By api version.
     *
     * @param apiVersion api version
     * @return by api version result
     */
    NDefinitionFilter byApiVersion(NVersion apiVersion);

    /**
     * By boot version.
     *
     * @param apiVersion api version
     * @return by boot version result
     */
    NDefinitionFilter byBootVersion(NVersion apiVersion);

    /**
     * By locked ids.
     *
     * @param ids ids
     * @return by locked ids result
     */
    NDefinitionFilter byLockedIds(String... ids);

    /**
     * By locked ids.
     *
     * @param ids ids
     * @return by locked ids result
     */
    NDefinitionFilter byLockedIds(NId... ids);

    /**
     * By version.
     *
     * @param version version
     * @return by version result
     */
    NDefinitionFilter byVersion(String version);

    /**
     * By version.
     *
     * @param version version
     * @return by version result
     */
    NDefinitionFilter byVersion(NVersion version);

    /**
     * By version.
     *
     * @param version version
     * @return by version result
     */
    NDefinitionFilter byVersion(NVersionFilter version);

    /**
     * By installed.
     *
     * @param value value
     * @return by installed result
     */
    NDefinitionFilter byInstalled(boolean value);

    /**
     * By installed or required.
     *
     * @param value value
     * @return by installed or required result
     */
    NDefinitionFilter byInstalledOrRequired(boolean value);

    /**
     * By required.
     *
     * @param value value
     * @return by required result
     */
    NDefinitionFilter byRequired(boolean value);

    /**
     * By default version.
     *
     * @param value value
     * @return by default version result
     */
    NDefinitionFilter byDefaultVersion(boolean value);

    /**
     * By obsolete.
     *
     * @param value value
     * @return by obsolete result
     */
    NDefinitionFilter byObsolete(boolean value);

    /**
     * By deployed.
     *
     * @param value value
     * @return by deployed result
     */
    NDefinitionFilter byDeployed(boolean value);
}
