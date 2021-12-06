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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;

import java.util.Collection;

/**
 * Dependencies filter factory
 *
 * @author thevpc
 * @app.category Base
 */
@NutsComponentScope(NutsComponentScopeType.SESSION)
public interface NutsDependencyFilters extends NutsTypedFilters<NutsDependencyFilter> {

    /**
     * return a new session bound instance of NutsDependencyFilters
     * @param session session
     * @return a new session bound instance of NutsDependencyFilters
     */
    static NutsDependencyFilters of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsDependencyFilters.class, true, null);
    }

    /**
     * accept only dependencies that match the given scope
     * @param scope accepted scope
     * @return a filter that accepts only dependencies that match the given scope
     */
    NutsDependencyFilter byScope(NutsDependencyScopePattern scope);

    /**
     * accept only dependencies that match the given scope
     * @param scope accepted scope
     * @return a filter that accepts only dependencies that match the given scope
     */
    NutsDependencyFilter byScope(NutsDependencyScope scope);

    /**
     * accept only dependencies that match the given scopes
     * @param scopes accepted scopes list
     * @return a filter that accepts only dependencies that match the given scopes
     */
    NutsDependencyFilter byScope(NutsDependencyScope... scopes);

    /**
     * accept only dependencies that match the given scopes
     * @param scopes accepted scope list
     * @return a filter that accepts only dependencies that match the given scopes
     */
    NutsDependencyFilter byScope(Collection<NutsDependencyScope> scopes);

    /**
     * accept only dependencies that match the given optional state
     * @param optional accepted scope state. null matches any optional value
     * @return a filter that accepts only dependencies that match the given scope
     */
    NutsDependencyFilter byOptional(Boolean optional);

    /**
     * return a new filter that accepts all of {@code filter} but the given exclusions
     * @param filter base filter
     * @param exclusions excluded dependencies
     * @return return a new filter that accepts all of {@code filter} but the given exclusions
     */
    NutsDependencyFilter byExclude(NutsDependencyFilter filter, String[] exclusions);

    /**
     * accept only dependencies that match the given archs
     * @param archs accepted archs list
     * @return a filter that accepts only dependencies that match the given archs
     */
    NutsDependencyFilter byArch(Collection<NutsArchFamily> archs);

    /**
     * accept only dependencies that match the given arch
     * @param arch accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    NutsDependencyFilter byArch(NutsArchFamily arch);

    /**
     * accept only dependencies that match the given archs
     * @param archs accepted arch list
     * @return a filter that accepts only dependencies that match the given archs
     */
    NutsDependencyFilter byArch(NutsArchFamily... archs);

    /**
     * accept only dependencies that match the given arch
     * @param arch accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    NutsDependencyFilter byArch(String arch);

    /**
     * accept only dependencies that match the given OSes
     * @param os accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    NutsDependencyFilter byOs(Collection<NutsOsFamily> os);

    /**
     * accept only dependencies that match the given OS
     * @param os accepted OS
     * @return a filter that accepts only dependencies that match the given OS
     */
    NutsDependencyFilter byOs(String os);

    /**
     * accept only dependencies that match the given OsDist
     * @param osDist accepted OsDist
     * @return a filter that accepts only dependencies that match the given OsDist
     */
    NutsDependencyFilter byOsDist(String osDist);

    /**
     * accept only dependencies that match the given OsDist list
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    NutsDependencyFilter byOsDist(String ...osDists);

    /**
     * accept only dependencies that match the given OsDist list
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    NutsDependencyFilter byOsDist(Collection<String> osDists);

    /**
     * accept only dependencies that match the given OS
     * @param os accepted OS
     * @return a filter that accepts only dependencies that match the given OS
     */
    NutsDependencyFilter byOs(NutsOsFamily os);

    /**
     * accept only dependencies that match the given OSes
     * @param os accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    NutsDependencyFilter byOs(NutsOsFamily... os);


    /**
     * accept only dependencies that match the current Desktop Environment
     * @return a filter that accepts only dependencies that match the current Desktop Environment
     */
    NutsDependencyFilter byCurrentDesktop();

    /**
     * accept only dependencies that match the current Architecture
     * @return a filter that accepts only dependencies that match the current Architecture
     */
    NutsDependencyFilter byCurrentArch();

    /**
     * accept only dependencies that match the current OS
     * @return a filter that accepts only dependencies that match the current OS
     */
    NutsDependencyFilter byCurrentOs();

    /**
     * accept only dependencies that have a regular dependency type (such as "jar" in java)
     * @return a filter that accepts only dependencies that have a regular dependency type (such as "jar" in java)
     */
    NutsDependencyFilter byRegularType();

    /**
     * accept only dependencies that match the current environment (OS, arch, etc...)
     * @return a filter that accept only dependencies that match the current environment (OS, arch, etc...)
     */
    NutsDependencyFilter byCurrentEnv();

    /**
     * create filter that accepts only dependencies required for runtime execution.
     *
     * equivalent to {@code byRunnable(false)}
     *
     * @return filter that accepts only dependencies required for runtime execution
     */
    NutsDependencyFilter byRunnable();

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
    NutsDependencyFilter byRunnable(boolean optional);

    /**
     * accept only dependencies that match the given Desktop Environment
     * @param de accepted Desktop Environment
     * @return a filter that accepts only dependencies that match the given Desktop Environment
     */
    NutsDependencyFilter byDesktop(NutsDesktopEnvironmentFamily de);

    /**
     * accept only dependencies that match any of the given Desktop Environment
     * @param de accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Desktop Environment
     */
    NutsDependencyFilter byDesktop(NutsDesktopEnvironmentFamily... de);

    /**
     * accept only dependencies that match any of the given Platform
     * @param pf accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    NutsDependencyFilter byPlatform(NutsPlatformFamily... pf);

    /**
     * accept only dependencies that match any of the given Platform
     * @param pf accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    NutsDependencyFilter byPlatform(String... pf);

    /**
     * accept only dependencies that match the given type
     * @param type accepted type
     * @return a filter that accepts only dependencies that match the given type
     */
    NutsDependencyFilter byType(String type);

}
