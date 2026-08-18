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
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.rpi.NDefinitionFilterRPI;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NDesktopEnvironmentFamily;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.util.NFilter;

import java.util.Collection;
import java.util.Map;

/**
 * Descriptor filter
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NDefinitionFilter extends NFilter {

    //////// COMMON START

    static NDefinitionFilter ofNonnull(NFilter filter){
        return NDefinitionFilterRPI.of().nonnull(filter);
    }

    static NDefinitionFilter ofAlways(){
        return NDefinitionFilterRPI.of().always();
    }

    static NDefinitionFilter ofNever(){
        return NDefinitionFilterRPI.of().never();
    }

    static NDefinitionFilter ofAll(NFilter... others){
        return NDefinitionFilterRPI.of().all(others);
    }

    static NDefinitionFilter ofAny(NFilter... others){
        return NDefinitionFilterRPI.of().any(others);
    }

    static NDefinitionFilter ofNot(NFilter other){
        return NDefinitionFilterRPI.of().not(other);
    }

    static NDefinitionFilter ofNone(NFilter... others){
        return NDefinitionFilterRPI.of().none(others);
    }

    static NDefinitionFilter ofFrom(NFilter a){
        return NDefinitionFilterRPI.of().from(a);
    }

    static NDefinitionFilter ofAs(NFilter a){
        return NDefinitionFilterRPI.of().as(a);
    }

    static NDefinitionFilter of(String expression){
        return NDefinitionFilterRPI.of().parse(expression);
    }

    //////// COMMON END

    //////// FACTORY START

    /**
     * accept only dependencies that match the given archs
     *
     * @param values accepted archs list
     * @return a filter that accepts only dependencies that match the given archs
     */
    static NDefinitionFilter ofArch(Collection<NArchFamily> values){
        return NDefinitionFilterRPI.of().byArch(values);
    }

    /**
     * accept only dependencies that match the given archs
     *
     * @param values accepted arch list
     * @return a filter that accepts only dependencies that match the given archs
     */
    static NDefinitionFilter ofArch(NArchFamily... values){
        return NDefinitionFilterRPI.of().byArch(values);
    }

    /**
     * accept only dependencies that match the given arch
     *
     * @param values accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    static NDefinitionFilter ofArch(String... values){
        return NDefinitionFilterRPI.of().byArch(values);
    }

    /**
     * accept only dependencies that match the given OSes
     *
     * @param values accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    static NDefinitionFilter ofOsFamily(Collection<NOsFamily> values){
        return NDefinitionFilterRPI.of().byOsFamily(values);
    }

    static NDefinitionFilter ofOsFamily(NOsFamily... values){
        return NDefinitionFilterRPI.of().byOsFamily(values);
    }

    static NDefinitionFilter ofArchFamily(NArchFamily... values){
        return NDefinitionFilterRPI.of().byArchFamily(values);
    }


    /**
     * accept only dependencies that match the given OsDist list
     *
     * @param values accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    static NDefinitionFilter ofOsDist(String... values){
        return NDefinitionFilterRPI.of().byOsDist(values);
    }

    /**
     * accept only dependencies that match the given OsDist list
     *
     * @param values accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    static NDefinitionFilter ofOsDist(Collection<String> values){
        return NDefinitionFilterRPI.of().byOsDist(values);
    }


    /**
     * accept only dependencies that match the current Desktop Environment
     *
     * @return a filter that accepts only dependencies that match the current Desktop Environment
     */
    static NDefinitionFilter ofCurrentDesktopEnvironmentFamily(){
        return NDefinitionFilterRPI.of().byCurrentDesktopEnvironmentFamily();
    }

    /**
     * accept only dependencies that match the current Architecture
     *
     * @return a filter that accepts only dependencies that match the current Architecture
     */
    static NDefinitionFilter ofCurrentArch(){
        return NDefinitionFilterRPI.of().byCurrentArch();
    }

    /**
     * accept only dependencies that match the current OS
     *
     * @return a filter that accepts only dependencies that match the current OS
     */
    static NDefinitionFilter ofCurrentOsFamily(){
        return NDefinitionFilterRPI.of().byCurrentOsFamily();
    }

    /**
     * accept only dependencies that match the current environment (OS, arch, etc...)
     *
     * @return a filter that accept only dependencies that match the current environment (OS, arch, etc...)
     */
    static NDefinitionFilter ofCurrentEnv(){
        return NDefinitionFilterRPI.of().byCurrentEnv();
    }

    /**
     * accept only dependencies that match the given Desktop Environment
     *
     * @param values accepted Desktop Environment
     * @return a filter that accepts only dependencies that match the given Desktop Environment
     */
    static NDefinitionFilter ofDesktopEnvironmentFamily(NDesktopEnvironmentFamily... values){
        return NDefinitionFilterRPI.of().byDesktopEnvironmentFamily(values);
    }

    static NDefinitionFilter ofDesktopEnvironment(String... values){
        return NDefinitionFilterRPI.of().byDesktopEnvironment(values);
    }

    static NDefinitionFilter ofDesktopEnvironment(NId... values){
        return NDefinitionFilterRPI.of().byDesktopEnvironment(values);
    }

    /**
     * accept only dependencies that match any of the given Platform
     *
     * @param values accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    static NDefinitionFilter ofPlatform(String... values){
        return NDefinitionFilterRPI.of().byPlatform(values);
    }


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
    static NDefinitionFilter ofDefaultVersion(Boolean value){
        return NDefinitionFilterRPI.of().byDefaultVersion(value);
    }

    static NDefinitionFilter ofName(String... values){
        return NDefinitionFilterRPI.of().byName(values);
    }

    static NDefinitionFilter ofEnv(Map<String, String> values){
        return NDefinitionFilterRPI.of().byEnv(values);
    }

    static NDefinitionFilter ofPackaging(String... values){
        return NDefinitionFilterRPI.of().byPackaging(values);
    }

    static NDefinitionFilter ofOs(NId... values){
        return NDefinitionFilterRPI.of().byOs(values);
    }

    static NDefinitionFilter ofPlatformFamily(NExecutionEngineFamily... values){
        return NDefinitionFilterRPI.of().byPlatformFamily(values);
    }

    static NDefinitionFilter ofPlatform(NId... values){
        return NDefinitionFilterRPI.of().byPlatform(values);
    }

    static NDefinitionFilter ofOs(String... values){
        return NDefinitionFilterRPI.of().byOs(values);
    }


    static NDefinitionFilter ofPackaging(Collection<String> values){
        return NDefinitionFilterRPI.of().byPackaging(values);
    }

    static NDefinitionFilter ofPlatform(Collection<String> values){
        return NDefinitionFilterRPI.of().byPlatform(values);
    }

    static NDefinitionFilter ofDesktopEnvironment(Collection<String> values){
        return NDefinitionFilterRPI.of().byDesktopEnvironment(values);
    }

    static NDefinitionFilter ofFlag(NDescriptorFlag... values){
        return NDefinitionFilterRPI.of().byFlag(values);
    }

    static NDefinitionFilter ofFlag(Collection<NDescriptorFlag> values){
        return NDefinitionFilterRPI.of().byFlag(values);
    }

    static NDefinitionFilter ofEffectiveFlag(NDescriptorFlag... values){
        return NDefinitionFilterRPI.of().byEffectiveFlag(values);
    }

    static NDefinitionFilter ofEffectiveFlag(Collection<NDescriptorFlag> values){
        return NDefinitionFilterRPI.of().byEffectiveFlag(values);
    }

    static NDefinitionFilter ofExtension(NVersion value){
        return NDefinitionFilterRPI.of().byExtension(value);
    }

    static NDefinitionFilter ofRuntime(NVersion value){
        return NDefinitionFilterRPI.of().byRuntime(value);
    }

    static NDefinitionFilter ofCompanion(NVersion value){
        return NDefinitionFilterRPI.of().byCompanion(value);
    }

    static NDefinitionFilter ofApiVersion(NVersion value){
        return NDefinitionFilterRPI.of().byApiVersion(value);
    }

    static NDefinitionFilter ofBootVersion(NVersion value){
        return NDefinitionFilterRPI.of().byBootVersion(value);
    }

    static NDefinitionFilter ofLockedIds(String... values){
        return NDefinitionFilterRPI.of().byLockedIds(values);
    }

    static NDefinitionFilter ofLockedIds(NId... values){
        return NDefinitionFilterRPI.of().byLockedIds(values);
    }

    static NDefinitionFilter ofVersion(String value){
        return NDefinitionFilterRPI.of().byVersion(value);
    }

    static NDefinitionFilter ofVersion(NVersion value){
        return NDefinitionFilterRPI.of().byVersion(value);
    }

    static NDefinitionFilter ofVersion(NVersionFilter value){
        return NDefinitionFilterRPI.of().byVersion(value);
    }

    static NDefinitionFilter ofInstalled(boolean value){
        return NDefinitionFilterRPI.of().byInstalled(value);
    }

    static NDefinitionFilter ofInstalledOrRequired(boolean value){
        return NDefinitionFilterRPI.of().byInstalledOrRequired(value);
    }

    static NDefinitionFilter ofRequired(boolean value){
        return NDefinitionFilterRPI.of().byRequired(value);
    }

    static NDefinitionFilter ofDefaultVersion(boolean value){
        return NDefinitionFilterRPI.of().byDefaultVersion(value);
    }

    static NDefinitionFilter ofObsolete(boolean value){
        return NDefinitionFilterRPI.of().byObsolete(value);
    }

    static NDefinitionFilter ofDeployed(boolean value){
        return NDefinitionFilterRPI.of().byDeployed(value);
    }


    //////// FACTORY END

    /**
     * return true if definition is accepted
     *
     * @param descriptor descriptor
     * @return true if descriptor is accepted
     */
    boolean acceptDefinition(NDefinition descriptor);


    NDefinitionFilter or(NDefinitionFilter other);

    NDefinitionFilter and(NDefinitionFilter other);

    NDefinitionFilter neg();
}
