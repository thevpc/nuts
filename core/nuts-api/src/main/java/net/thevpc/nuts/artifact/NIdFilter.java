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
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.rpi.NIdFilterRPI;
import net.thevpc.nuts.util.NFilter;

/**
 * Class for filtering Artifact Ids
 *
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NIdFilter extends NFilter {
    //////// COMMON START

    /**
     * Creates a new instance of of nonnull.
     *
     * @param filter filter
     * @return of nonnull result
     */
    static NIdFilter ofNonnull(NFilter filter){
        return NIdFilterRPI.of().nonnull(filter);
    }

    /**
     * Creates a new instance of of always.
     *
     * @return of always result
     */
    static NIdFilter ofAlways(){
        return NIdFilterRPI.of().always();
    }

    /**
     * Creates a new instance of of never.
     *
     * @return of never result
     */
    static NIdFilter ofNever(){
        return NIdFilterRPI.of().never();
    }

    /**
     * Creates a new instance of of all.
     *
     * @param others others
     * @return of all result
     */
    static NIdFilter ofAll(NFilter... others){
        return NIdFilterRPI.of().all(others);
    }

    /**
     * Creates a new instance of of any.
     *
     * @param others others
     * @return of any result
     */
    static NIdFilter ofAny(NFilter... others){
        return NIdFilterRPI.of().any(others);
    }

    /**
     * Creates a new instance of of not.
     *
     * @param other other
     * @return of not result
     */
    static NIdFilter ofNot(NFilter other){
        return NIdFilterRPI.of().not(other);
    }

    /**
     * Creates a new instance of of none.
     *
     * @param others others
     * @return of none result
     */
    static NIdFilter ofNone(NFilter... others){
        return NIdFilterRPI.of().none(others);
    }

    /**
     * Creates a new instance of of from.
     *
     * @param a a
     * @return of from result
     */
    static NIdFilter ofFrom(NFilter a){
        return NIdFilterRPI.of().from(a);
    }

    /**
     * Creates a new instance of of as.
     *
     * @param a a
     * @return of as result
     */
    static NIdFilter ofAs(NFilter a){
        return NIdFilterRPI.of().as(a);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @return of result
     */
    static NIdFilter of(String expression){
        return NIdFilterRPI.of().parse(expression);
    }

    //////// COMMON END

    //////// FACTORY START

    /**
     * Creates a new instance of of value.
     *
     * @param id id
     * @return of value result
     */
    static NIdFilter ofValue(NId id){
        return NIdFilterRPI.of().byValue(id);
    }

    /**
     * Creates a new instance of of default version.
     *
     * @param defaultVersion default version
     * @return of default version result
     */
    static NIdFilter ofDefaultVersion(Boolean defaultVersion){
        return NIdFilterRPI.of().byDefaultVersion(defaultVersion);
    }


    /**
     * Creates a new instance of of name.
     *
     * @param names names
     * @return of name result
     */
    static NIdFilter ofName(String... names){
        return NIdFilterRPI.of().byName(names);
    }

    //////// FACTORY END

    /**
     * return true when the id is to be accepted
     *
     * @param id id to check
     * @return true when the id is to be accepted
     */
    boolean acceptId(NId id);

    /**
     * Or.
     *
     * @param other other
     * @return or result
     */
    NIdFilter or(NIdFilter other);

    /**
     * And.
     *
     * @param other other
     * @return and result
     */
    NIdFilter and(NIdFilter other);

    @Override
    NIdFilter neg();

}
