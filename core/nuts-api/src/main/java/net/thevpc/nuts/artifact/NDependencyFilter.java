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

import net.thevpc.nuts.internal.rpi.NDependencyFilterRPI;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NDesktopEnvironmentFamily;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.util.NFilter;

import java.util.Collection;

/**
 * Dependency filter
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NDependencyFilter extends NFilter {
    //////// COMMON START

    /**
     * Creates a new instance of of nonnull.
     *
     * @param filter filter
     * @return of nonnull result
     */
    static NDependencyFilter ofNonnull(NFilter filter){
        return NDependencyFilterRPI.of().nonnull(filter);
    }

    /**
     * Creates a new instance of of always.
     *
     * @return of always result
     */
    static NDependencyFilter ofAlways(){
        return NDependencyFilterRPI.of().always();
    }

    /**
     * Creates a new instance of of never.
     *
     * @return of never result
     */
    static NDependencyFilter ofNever(){
        return NDependencyFilterRPI.of().never();
    }

    /**
     * Creates a new instance of of all.
     *
     * @param others others
     * @return of all result
     */
    static NDependencyFilter ofAll(NFilter... others){
        return NDependencyFilterRPI.of().all(others);
    }

    /**
     * Creates a new instance of of any.
     *
     * @param others others
     * @return of any result
     */
    static NDependencyFilter ofAny(NFilter... others){
        return NDependencyFilterRPI.of().any(others);
    }

    /**
     * Creates a new instance of of not.
     *
     * @param other other
     * @return of not result
     */
    static NDependencyFilter ofNot(NFilter other){
        return NDependencyFilterRPI.of().not(other);
    }

    /**
     * Creates a new instance of of none.
     *
     * @param others others
     * @return of none result
     */
    static NDependencyFilter ofNone(NFilter... others){
        return NDependencyFilterRPI.of().none(others);
    }

    /**
     * Creates a new instance of of from.
     *
     * @param a a
     * @return of from result
     */
    static NDependencyFilter ofFrom(NFilter a){
        return NDependencyFilterRPI.of().from(a);
    }

    /**
     * Creates a new instance of of as.
     *
     * @param a a
     * @return of as result
     */
    static NDependencyFilter ofAs(NFilter a){
        return NDependencyFilterRPI.of().as(a);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @return of result
     */
    static NDependencyFilter of(String expression){
        return NDependencyFilterRPI.of().parse(expression);
    }

    //////// COMMON END

    //////// FACTORY START

    /**
     * accept only dependencies that match the given scope
     * @param scopes accepted scope
     * @return a filter that accepts only dependencies that match the given scope
     */
    static NDependencyFilter ofScope(NDependencyScopePattern...scopes){
        return NDependencyFilterRPI.of().byScope(scopes);
    }

    /**
     * accept only dependencies that match the given scope
     * @param scope accepted scope
     * @return a filter that accepts only dependencies that match the given scope
     */
    static NDependencyFilter ofScope(NDependencyScope scope){
        return NDependencyFilterRPI.of().byScope(scope);
    }

    /**
     * accept only dependencies that match the given scopes
     * @param scopes accepted scopes list
     * @return a filter that accepts only dependencies that match the given scopes
     */
    static NDependencyFilter ofScope(NDependencyScope... scopes){
        return NDependencyFilterRPI.of().byScope(scopes);
    }

    /**
     * accept only dependencies that match the given scopes
     * @param scopes accepted scope list
     * @return a filter that accepts only dependencies that match the given scopes
     */
    static NDependencyFilter ofScope(Collection<NDependencyScope> scopes){
        return NDependencyFilterRPI.of().byScope(scopes);
    }

    /**
     * accept only dependencies that match the given optional state
     * @param optional accepted scope state. null matches any optional value
     * @return a filter that accepts only dependencies that match the given scope
     */
    static NDependencyFilter ofOptional(Boolean optional){
        return NDependencyFilterRPI.of().byOptional(optional);
    }

    /**
     * return a new filter that accepts all of {@code filter} but the given exclusions
     * @param filter base filter
     * @param exclusions excluded dependencies
     * @return return a new filter that accepts all of {@code filter} but the given exclusions
     */
    static NDependencyFilter ofExclude(NDependencyFilter filter, String[] exclusions){
        return NDependencyFilterRPI.of().byExclude(filter,exclusions);
    }

    /**
     * accept only dependencies that match the given archs
     * @param archs accepted archs list
     * @return a filter that accepts only dependencies that match the given archs
     */
    static NDependencyFilter ofArch(Collection<NArchFamily> archs){
        return NDependencyFilterRPI.of().byArch(archs);
    }

    /**
     * accept only dependencies that match the given arch
     * @param arch accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    static NDependencyFilter ofArch(NArchFamily arch){
        return NDependencyFilterRPI.of().byArch(arch);
    }

    /**
     * accept only dependencies that match the given archs
     * @param archs accepted arch list
     * @return a filter that accepts only dependencies that match the given archs
     */
    static NDependencyFilter ofArch(NArchFamily... archs){
        return NDependencyFilterRPI.of().byArch(archs);
    }

    /**
     * accept only dependencies that match the given arch
     * @param arch accepted arch
     * @return a filter that accepts only dependencies that match the given arch
     */
    static NDependencyFilter ofArch(String arch){
        return NDependencyFilterRPI.of().byArch(arch);
    }

    /**
     * accept only dependencies that match the given OSes
     * @param os accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    static NDependencyFilter ofOs(Collection<NOsFamily> os){
        return NDependencyFilterRPI.of().byOs(os);
    }

    /**
     * accept only dependencies that match the given OS
     * @param os accepted OS
     * @return a filter that accepts only dependencies that match the given OS
     */
    static NDependencyFilter ofOs(String os){
        return NDependencyFilterRPI.of().byOs(os);
    }

    /**
     * accept only dependencies that match the given OsDist
     * @param osDist accepted OsDist
     * @return a filter that accepts only dependencies that match the given OsDist
     */
    static NDependencyFilter ofOsDist(String osDist){
        return NDependencyFilterRPI.of().byOsDist(osDist);
    }

    /**
     * accept only dependencies that match the given OsDist list
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    static NDependencyFilter ofOsDist(String ...osDists){
        return NDependencyFilterRPI.of().byOsDist(osDists);
    }

    /**
     * accept only dependencies that match the given OsDist list
     * @param osDists accepted OsDist list
     * @return a filter that accepts only dependencies that match the given OsDist list
     */
    static NDependencyFilter ofOsDist(Collection<String> osDists){
        return NDependencyFilterRPI.of().byOsDist(osDists);
    }

    /**
     * accept only dependencies that match the given OS
     * @param os accepted OS
     * @return a filter that accepts only dependencies that match the given OS
     */
    static NDependencyFilter ofOs(NOsFamily os){
        return NDependencyFilterRPI.of().byOs(os);
    }

    /**
     * accept only dependencies that match the given OSes
     * @param os accepted OS list
     * @return a filter that accepts only dependencies that match the given OSes
     */
    static NDependencyFilter ofOs(NOsFamily... os){
        return NDependencyFilterRPI.of().byOs(os);
    }


    /**
     * accept only dependencies that match the current Desktop Environment
     * @return a filter that accepts only dependencies that match the current Desktop Environment
     */
    static NDependencyFilter ofCurrentDesktop(){
        return NDependencyFilterRPI.of().byCurrentDesktop();
    }

    /**
     * accept only dependencies that match the current Architecture
     * @return a filter that accepts only dependencies that match the current Architecture
     */
    static NDependencyFilter ofCurrentArch(){
        return NDependencyFilterRPI.of().byCurrentArch();
    }

    /**
     * accept only dependencies that match the current OS
     * @return a filter that accepts only dependencies that match the current OS
     */
    static NDependencyFilter ofCurrentOs(){
        return NDependencyFilterRPI.of().byCurrentOs();
    }

    /**
     * accept only dependencies that have a regular dependency type (such as "jar" in java)
     * @return a filter that accepts only dependencies that have a regular dependency type (such as "jar" in java)
     */
    static NDependencyFilter ofRegularType(){
        return NDependencyFilterRPI.of().byRegularType();
    }

    /**
     * accept only dependencies that match the current environment (OS, arch, etc...)
     * @return a filter that accept only dependencies that match the current environment (OS, arch, etc...)
     */
    static NDependencyFilter ofCurrentEnv(){
        return NDependencyFilterRPI.of().byCurrentEnv();
    }

    /**
     * create filter that accepts only dependencies required for runtime execution.
     *
     * equivalent to {@code ofRunnable(false)}
     *
     * @return filter that accepts only dependencies required for runtime execution
     */
    static NDependencyFilter ofRunnable(){
        return NDependencyFilterRPI.of().byRunnable();
    }

    /**
     * create filter that accepts only dependencies required for runtime execution.
     *
     * equivalent to {@code
     * ofScope(NutsDependencyScopePattern.RUN)
     *                 .and(byOptional(optional?null:false))
     *                 .and(byRegularType())
     *                 .and(byCurrentEnv())
     *                 }
     * @param optional optional
     * @return filter that accepts only dependencies required for runtime execution
     */
    static NDependencyFilter ofRunnable(boolean optional){
        return NDependencyFilterRPI.of().byRunnable(optional);
    }

    /**
     * Creates a new instance of of runnable.
     *
     * @param optional optional
     * @param anyEnv any env
     * @return of runnable result
     */
    static NDependencyFilter ofRunnable(boolean optional,boolean anyEnv){
        return NDependencyFilterRPI.of().byRunnable(optional,anyEnv);
    }

    /**
     * accept only dependencies that match the given Desktop Environment
     * @param de accepted Desktop Environment
     * @return a filter that accepts only dependencies that match the given Desktop Environment
     */
    static NDependencyFilter ofDesktop(NDesktopEnvironmentFamily de){
        return NDependencyFilterRPI.of().byDesktop(de);
    }

    /**
     * accept only dependencies that match any of the given Desktop Environment
     * @param de accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Desktop Environment
     */
    static NDependencyFilter ofDesktop(NDesktopEnvironmentFamily... de){
        return NDependencyFilterRPI.of().byDesktop(de);
    }

    /**
     * Creates a new instance of of desktop.
     *
     * @param de de
     * @return of desktop result
     */
    static NDependencyFilter ofDesktop(Collection<NDesktopEnvironmentFamily> de){
        return NDependencyFilterRPI.of().byDesktop(de);
    }

    /**
     * accept only dependencies that match any of the given Platform
     * @param values accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    static NDependencyFilter ofPlatform(NExecutionEngineFamily... values){
        return NDependencyFilterRPI.of().byPlatform(values);
    }

    /**
     * accept only dependencies that match any of the given Platform
     * @param values accepted Desktop Environment list
     * @return a filter that accepts only dependencies that match any of the given Platform
     */
    static NDependencyFilter ofPlatform(String... values){
        return NDependencyFilterRPI.of().byPlatform(values);
    }

    /**
     * accept only dependencies that match the given type
     * @param type accepted type
     * @return a filter that accepts only dependencies that match the given type
     */
    static NDependencyFilter ofType(String type){
        return NDependencyFilterRPI.of().byType(type);
    }


    //////// FACTORY END




    /**
     * return true if the {@code dependency} is accepted
     *
     * @param dependency dependency id
     * @param from       parent (dependent) id
     * @return true if the {@code dependency} is accepted
     */
    boolean acceptDependency(NDependency dependency, NId from);

    /**
     * Or.
     *
     * @param other other
     * @return or result
     */
    NDependencyFilter or(NDependencyFilter other);

    /**
     * And.
     *
     * @param other other
     * @return and result
     */
    NDependencyFilter and(NDependencyFilter other);

    /**
     * Neg.
     *
     * @return neg result
     */
    NDependencyFilter neg();
}
