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

    /**
     * Creates a new instance of of nonnull.
     *
     * @param filter filter
     * @return of nonnull result
     */
    static NDefinitionFilter ofNonnull(NFilter filter){
        return NDefinitionFilterRPI.of().nonnull(filter);
    }

    /**
     * Creates a new instance of of always.
     *
     * @return of always result
     */
    static NDefinitionFilter ofAlways(){
        return NDefinitionFilterRPI.of().always();
    }

    /**
     * Creates a new instance of of never.
     *
     * @return of never result
     */
    static NDefinitionFilter ofNever(){
        return NDefinitionFilterRPI.of().never();
    }

    /**
     * Creates a new instance of of all.
     *
     * @param others others
     * @return of all result
     */
    static NDefinitionFilter ofAll(NFilter... others){
        return NDefinitionFilterRPI.of().all(others);
    }

    /**
     * Creates a new instance of of any.
     *
     * @param others others
     * @return of any result
     */
    static NDefinitionFilter ofAny(NFilter... others){
        return NDefinitionFilterRPI.of().any(others);
    }

    /**
     * Creates a new instance of of not.
     *
     * @param other other
     * @return of not result
     */
    static NDefinitionFilter ofNot(NFilter other){
        return NDefinitionFilterRPI.of().not(other);
    }

    /**
     * Creates a new instance of of none.
     *
     * @param others others
     * @return of none result
     */
    static NDefinitionFilter ofNone(NFilter... others){
        return NDefinitionFilterRPI.of().none(others);
    }

    /**
     * Creates a new instance of of from.
     *
     * @param a a
     * @return of from result
     */
    static NDefinitionFilter ofFrom(NFilter a){
        return NDefinitionFilterRPI.of().from(a);
    }

    /**
     * Creates a new instance of of as.
     *
     * @param a a
     * @return of as result
     */
    static NDefinitionFilter ofAs(NFilter a){
        return NDefinitionFilterRPI.of().as(a);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @return of result
     */
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

    /**
     * Creates a new instance of of os family.
     *
     * @param values values
     * @return of os family result
     */
    static NDefinitionFilter ofOsFamily(NOsFamily... values){
        return NDefinitionFilterRPI.of().byOsFamily(values);
    }

    /**
     * Creates a new instance of of arch family.
     *
     * @param values values
     * @return of arch family result
     */
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

    /**
     * Creates a new instance of of desktop environment.
     *
     * @param values values
     * @return of desktop environment result
     */
    static NDefinitionFilter ofDesktopEnvironment(String... values){
        return NDefinitionFilterRPI.of().byDesktopEnvironment(values);
    }

    /**
     * Creates a new instance of of desktop environment.
     *
     * @param values values
     * @return of desktop environment result
     */
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

    /**
     * Creates a new instance of of name.
     *
     * @param values values
     * @return of name result
     */
    static NDefinitionFilter ofName(String... values){
        return NDefinitionFilterRPI.of().byName(values);
    }

    /**
     * Creates a new instance of of env.
     *
     * @param values values
     * @return of env result
     */
    static NDefinitionFilter ofEnv(Map<String, String> values){
        return NDefinitionFilterRPI.of().byEnv(values);
    }

    /**
     * Creates a new instance of of packaging.
     *
     * @param values values
     * @return of packaging result
     */
    static NDefinitionFilter ofPackaging(String... values){
        return NDefinitionFilterRPI.of().byPackaging(values);
    }

    /**
     * Creates a new instance of of os.
     *
     * @param values values
     * @return of os result
     */
    static NDefinitionFilter ofOs(NId... values){
        return NDefinitionFilterRPI.of().byOs(values);
    }

    /**
     * Creates a new instance of of platform family.
     *
     * @param values values
     * @return of platform family result
     */
    static NDefinitionFilter ofPlatformFamily(NExecutionEngineFamily... values){
        return NDefinitionFilterRPI.of().byPlatformFamily(values);
    }

    /**
     * Creates a new instance of of platform.
     *
     * @param values values
     * @return of platform result
     */
    static NDefinitionFilter ofPlatform(NId... values){
        return NDefinitionFilterRPI.of().byPlatform(values);
    }

    /**
     * Creates a new instance of of os.
     *
     * @param values values
     * @return of os result
     */
    static NDefinitionFilter ofOs(String... values){
        return NDefinitionFilterRPI.of().byOs(values);
    }


    /**
     * Creates a new instance of of packaging.
     *
     * @param values values
     * @return of packaging result
     */
    static NDefinitionFilter ofPackaging(Collection<String> values){
        return NDefinitionFilterRPI.of().byPackaging(values);
    }

    /**
     * Creates a new instance of of platform.
     *
     * @param values values
     * @return of platform result
     */
    static NDefinitionFilter ofPlatform(Collection<String> values){
        return NDefinitionFilterRPI.of().byPlatform(values);
    }

    /**
     * Creates a new instance of of desktop environment.
     *
     * @param values values
     * @return of desktop environment result
     */
    static NDefinitionFilter ofDesktopEnvironment(Collection<String> values){
        return NDefinitionFilterRPI.of().byDesktopEnvironment(values);
    }

    /**
     * Creates a new instance of of flag.
     *
     * @param values values
     * @return of flag result
     */
    static NDefinitionFilter ofFlag(NDescriptorFlag... values){
        return NDefinitionFilterRPI.of().byFlag(values);
    }

    /**
     * Creates a new instance of of flag.
     *
     * @param values values
     * @return of flag result
     */
    static NDefinitionFilter ofFlag(Collection<NDescriptorFlag> values){
        return NDefinitionFilterRPI.of().byFlag(values);
    }

    /**
     * Creates a new instance of of effective flag.
     *
     * @param values values
     * @return of effective flag result
     */
    static NDefinitionFilter ofEffectiveFlag(NDescriptorFlag... values){
        return NDefinitionFilterRPI.of().byEffectiveFlag(values);
    }

    /**
     * Creates a new instance of of effective flag.
     *
     * @param values values
     * @return of effective flag result
     */
    static NDefinitionFilter ofEffectiveFlag(Collection<NDescriptorFlag> values){
        return NDefinitionFilterRPI.of().byEffectiveFlag(values);
    }

    /**
     * Creates a new instance of of extension.
     *
     * @param value value
     * @return of extension result
     */
    static NDefinitionFilter ofExtension(NVersion value){
        return NDefinitionFilterRPI.of().byExtension(value);
    }

    /**
     * Creates a new instance of of runtime.
     *
     * @param value value
     * @return of runtime result
     */
    static NDefinitionFilter ofRuntime(NVersion value){
        return NDefinitionFilterRPI.of().byRuntime(value);
    }

    /**
     * Creates a new instance of of companion.
     *
     * @param value value
     * @return of companion result
     */
    static NDefinitionFilter ofCompanion(NVersion value){
        return NDefinitionFilterRPI.of().byCompanion(value);
    }

    /**
     * Creates a new instance of of api version.
     *
     * @param value value
     * @return of api version result
     */
    static NDefinitionFilter ofApiVersion(NVersion value){
        return NDefinitionFilterRPI.of().byApiVersion(value);
    }

    /**
     * Creates a new instance of of boot version.
     *
     * @param value value
     * @return of boot version result
     */
    static NDefinitionFilter ofBootVersion(NVersion value){
        return NDefinitionFilterRPI.of().byBootVersion(value);
    }

    /**
     * Creates a new instance of of locked ids.
     *
     * @param values values
     * @return of locked ids result
     */
    static NDefinitionFilter ofLockedIds(String... values){
        return NDefinitionFilterRPI.of().byLockedIds(values);
    }

    /**
     * Creates a new instance of of locked ids.
     *
     * @param values values
     * @return of locked ids result
     */
    static NDefinitionFilter ofLockedIds(NId... values){
        return NDefinitionFilterRPI.of().byLockedIds(values);
    }

    /**
     * Creates a new instance of of version.
     *
     * @param value value
     * @return of version result
     */
    static NDefinitionFilter ofVersion(String value){
        return NDefinitionFilterRPI.of().byVersion(value);
    }

    /**
     * Creates a new instance of of version.
     *
     * @param value value
     * @return of version result
     */
    static NDefinitionFilter ofVersion(NVersion value){
        return NDefinitionFilterRPI.of().byVersion(value);
    }

    /**
     * Creates a new instance of of version.
     *
     * @param value value
     * @return of version result
     */
    static NDefinitionFilter ofVersion(NVersionFilter value){
        return NDefinitionFilterRPI.of().byVersion(value);
    }

    /**
     * Creates a new instance of of installed.
     *
     * @param value value
     * @return of installed result
     */
    static NDefinitionFilter ofInstalled(boolean value){
        return NDefinitionFilterRPI.of().byInstalled(value);
    }

    /**
     * Creates a new instance of of installed or required.
     *
     * @param value value
     * @return of installed or required result
     */
    static NDefinitionFilter ofInstalledOrRequired(boolean value){
        return NDefinitionFilterRPI.of().byInstalledOrRequired(value);
    }

    /**
     * Creates a new instance of of required.
     *
     * @param value value
     * @return of required result
     */
    static NDefinitionFilter ofRequired(boolean value){
        return NDefinitionFilterRPI.of().byRequired(value);
    }

    /**
     * Creates a new instance of of default version.
     *
     * @param value value
     * @return of default version result
     */
    static NDefinitionFilter ofDefaultVersion(boolean value){
        return NDefinitionFilterRPI.of().byDefaultVersion(value);
    }

    /**
     * Creates a new instance of of obsolete.
     *
     * @param value value
     * @return of obsolete result
     */
    static NDefinitionFilter ofObsolete(boolean value){
        return NDefinitionFilterRPI.of().byObsolete(value);
    }

    /**
     * Creates a new instance of of deployed.
     *
     * @param value value
     * @return of deployed result
     */
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


    /**
     * Or.
     *
     * @param other other
     * @return or result
     */
    NDefinitionFilter or(NDefinitionFilter other);

    /**
     * And.
     *
     * @param other other
     * @return and result
     */
    NDefinitionFilter and(NDefinitionFilter other);

    /**
     * Neg.
     *
     * @return neg result
     */
    NDefinitionFilter neg();
}
