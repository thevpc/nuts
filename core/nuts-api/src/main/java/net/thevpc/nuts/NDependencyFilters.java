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

import net.thevpc.nuts.env.NArchFamily;
import net.thevpc.nuts.env.NDesktopEnvironmentFamily;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.env.NPlatformFamily;
import net.thevpc.nuts.ext.NExtensions;

import java.util.Collection;

/**
 * Dependencies filter factory
 *
 * @author thevpc
 * @app.category Base
 */
public interface NDependencyFilters extends NTypedFilters<NDependencyFilter> {

    /**
     * return a new session bound instance of NutsDependencyFilters
     *
     * @return a new session bound instance of NutsDependencyFilters
     */
    static NDependencyFilters of() {
       return NExtensions.of(NDependencyFilters.class);
    }

    /**
     * accept only dependencies that match the given scope
     * @param scope accepted scope
     * @return a filter that accepts only dependencies that match the given scope
     */
    NDependencyFilter byScope(NDependencyScopePattern scope);

    /**
     * accept only dependencies that match the given scope
     * @param scope accepted scope
     * @return a filter that accepts only dependencies that match the given scope
     */
    NDependencyFilter byScope(NDependencyScope scope);

    /**
     * accept only dependencies that match the given scopes
     * @param scopes accepted scopes list
     * @return a filter that accepts only dependencies that match the given scopes
     */
    NDependencyFilter byScope(NDependencyScope... scopes);

    /**
     * accept only dependencies that match the given scopes
     * @param scopes accepted scope list
     * @return a filter that accepts only dependencies that match the given scopes
     */
    NDependencyFilter byScope(Collection<NDependencyScope> scopes);

    /**
     * accept only dependencies that match the given optional state
     * @param optional accepted scope state. null matches any optional value
     * @return a filter that accepts only dependencies that match the given scope
     */
    NDependencyFilter byOptional(Boolean optional);

    /**
     * return a new filter that accepts all of {@code filter} but the given exclusions
     * @param filter base filter
     * @param exclusions excluded dependencies
     * @return return a new filter that accepts all of {@code filter} but the given exclusions
     */
    NDependencyFilter byExclude(NDependencyFilter filter, String[] exclusions);

    /**
     * accept only dependencies that match the given archs
     * @param archs accepted archs list
     * @return a filter that accepts only dependencies that match the given archs
     */
    NDependencyFilter byArch(Collection<NArchFamily> archs);

    /**
     * accept only dependencies that match the given arch
     * @param arch accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    NDependencyFilter byArch(NArchFamily arch);

    /**
     * accept only dependencies that match the given archs
     * @param archs accepted arch list
     * @return a filter that accepts only dependencies that match the given archs
     */
    NDependencyFilter byArch(NArchFamily... archs);

    /**
     * accept only dependencies that match the given arch
     * @param arch accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    NDependencyFilter byArch(String arch);

    /**
     * accept only dependencies that match the given OSes
     * @param os accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    NDependencyFilter byOs(Collection<NOsFamily> os);

    /**
     * accept only dependencies that match the given OS
     * @param os accepted OS
     * @return a filter that accepts only dependencies that match the given OS
     */
    NDependencyFilter byOs(String os);

    /**
     * accept only dependencies that match the given OsDist
     * @param osDist accepted OsDist
     * @return a filter that accepts only dependencies that match the given OsDist
     */
    NDependencyFilter byOsDist(String osDist);

    /**
     * accept only dependencies that match the given OsDist list
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    NDependencyFilter byOsDist(String ...osDists);

    /**
     * accept only dependencies that match the given OsDist list
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    NDependencyFilter byOsDist(Collection<String> osDists);

    /**
     * accept only dependencies that match the given OS
     * @param os accepted OS
     * @return a filter that accepts only dependencies that match the given OS
     */
    NDependencyFilter byOs(NOsFamily os);

    /**
     * accept only dependencies that match the given OSes
     * @param os accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    NDependencyFilter byOs(NOsFamily... os);


    /**
     * accept only dependencies that match the current Desktop Environment
     * @return a filter that accepts only dependencies that match the current Desktop Environment
     */
    NDependencyFilter byCurrentDesktop();

    /**
     * accept only dependencies that match the current Architecture
     * @return a filter that accepts only dependencies that match the current Architecture
     */
    NDependencyFilter byCurrentArch();

    /**
     * accept only dependencies that match the current OS
     * @return a filter that accepts only dependencies that match the current OS
     */
    NDependencyFilter byCurrentOs();

    /**
     * accept only dependencies that have a regular dependency type (such as "jar" in java)
     * @return a filter that accepts only dependencies that have a regular dependency type (such as "jar" in java)
     */
    NDependencyFilter byRegularType();

    /**
     * accept only dependencies that match the current environment (OS, arch, etc...)
     * @return a filter that accept only dependencies that match the current environment (OS, arch, etc...)
     */
    NDependencyFilter byCurrentEnv();

    /**
     * create filter that accepts only dependencies required for runtime execution.
     *
     * equivalent to {@code byRunnable(false)}
     *
     * @return filter that accepts only dependencies required for runtime execution
     */
    NDependencyFilter byRunnable();

    /**
     * create filter that accepts only dependencies required for runtime execution.
     *
     * equivalent to {@code
     * byScope(NutsDependencyScopePattern.RUN)
     *                 .and(byOptional(optional?null:false))
     *                 .and(byRegularType())
     *                 .and(byCurrentEnv())
     *                 }
     * @param optional optional
     * @return filter that accepts only dependencies required for runtime execution
     */
    NDependencyFilter byRunnable(boolean optional);

    /**
     * accept only dependencies that match the given Desktop Environment
     * @param de accepted Desktop Environment
     * @return a filter that accepts only dependencies that match the given Desktop Environment
     */
    NDependencyFilter byDesktop(NDesktopEnvironmentFamily de);

    /**
     * accept only dependencies that match any of the given Desktop Environment
     * @param de accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Desktop Environment
     */
    NDependencyFilter byDesktop(NDesktopEnvironmentFamily... de);

    NDependencyFilter byDesktop(Collection<NDesktopEnvironmentFamily> de);

    /**
     * accept only dependencies that match any of the given Platform
     * @param pf accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    NDependencyFilter byPlatform(NPlatformFamily... pf);

    /**
     * accept only dependencies that match any of the given Platform
     * @param pf accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    NDependencyFilter byPlatform(String... pf);

    /**
     * accept only dependencies that match the given type
     * @param type accepted type
     * @return a filter that accepts only dependencies that match the given type
     */
    NDependencyFilter byType(String type);

}
