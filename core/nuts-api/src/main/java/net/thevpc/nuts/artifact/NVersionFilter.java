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

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.internal.rpi.NVersionFilterRPI;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.function.Supplier;

/**
 * version interval is a version filter that accepts interval ranges of
 * versions.
 * <p>
 * version intervals can be in one of the following forms
 * <pre>
 * [ version, ]
 * ] version, ] or ( version, ]
 * [ version, [ or [ version, )
 * ] version, [ or ] version, [
 *
 * [ ,version ]
 * ] ,version ] or ( ,version ]
 * [ ,version [ or [ ,version )
 * ] ,version [ or ] ,version [
 *
 * [ version1 , version2 ]
 * ] version1 , version2 ] or ( version1 , version2 ]
 * [ version1 , version2 [ or [ version1 , version2 )
 * ] version1 , version2 [ or ] version1 , version2 [
 *
 * comma or space separated intervals such as :
 *   [ version1 , version2 ], [ version1 , version2 ]
 *   [ version1 , version2 ]  [ version1 , version2 ]
 * </pre>
 * <p>
 * Created by vpc on 1/8/17.
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NVersionFilter extends NFilter {
    //////// COMMON START

    /**
     * Creates a new instance of of nonnull.
     *
     * @param filter filter
     * @return of nonnull result
     */
    static NVersionFilter ofNonnull(NFilter filter){
        return NVersionFilterRPI.of().nonnull(filter);
    }

    /**
     * Creates a new instance of of always.
     *
     * @return of always result
     */
    static NVersionFilter ofAlways(){
        return NVersionFilterRPI.of().always();
    }

    /**
     * Creates a new instance of of never.
     *
     * @return of never result
     */
    static NVersionFilter ofNever(){
        return NVersionFilterRPI.of().never();
    }

    /**
     * Creates a new instance of of all.
     *
     * @param others others
     * @return of all result
     */
    static NVersionFilter ofAll(NFilter... others){
        return NVersionFilterRPI.of().all(others);
    }

    /**
     * Creates a new instance of of any.
     *
     * @param others others
     * @return of any result
     */
    static NVersionFilter ofAny(NFilter... others){
        return NVersionFilterRPI.of().any(others);
    }

    /**
     * Creates a new instance of of not.
     *
     * @param other other
     * @return of not result
     */
    static NVersionFilter ofNot(NFilter other){
        return NVersionFilterRPI.of().not(other);
    }

    /**
     * Creates a new instance of of none.
     *
     * @param others others
     * @return of none result
     */
    static NVersionFilter ofNone(NFilter... others){
        return NVersionFilterRPI.of().none(others);
    }

    /**
     * Creates a new instance of of from.
     *
     * @param a a
     * @return of from result
     */
    static NVersionFilter ofFrom(NFilter a){
        return NVersionFilterRPI.of().from(a);
    }

    /**
     * Creates a new instance of of as.
     *
     * @param a a
     * @return of as result
     */
    static NVersionFilter ofAs(NFilter a){
        return NVersionFilterRPI.of().as(a);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @return of result
     */
    static NVersionFilter of(String expression){
        return NVersionFilterRPI.of().parse(expression);
    }

    //////// COMMON END


    //////// FACTORY START
    /**
     * Creates a new instance of of value.
     *
     * @param version version
     * @return of value result
     */
    static NOptional<NVersionFilter> ofValue(String version){
        return NVersionFilterRPI.of().byValue(version);
    }

    /**
     * Creates a new instance of of value.
     *
     * @param version version
     * @param comparator comparator
     * @return of value result
     */
    static NOptional<NVersionFilter> ofValue(String version, NVersionComparator comparator){
        return NVersionFilterRPI.of().byValue(version, comparator);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @param versionComparator version comparator
     * @return of result
     */
    static NVersionFilter of(String expression, NVersionComparator versionComparator){
        return NVersionFilterRPI.of().parse(expression, versionComparator);
    }
    //////// FACTORY END


    /**
     * true if the version is accepted by this instance filter
     *
     * @param version version to check
     * @return true if the version is accepted by this instance interval
     */
    boolean acceptVersion(NVersion version);

    /**
     * Or.
     *
     * @param other other
     * @return or result
     */
    NVersionFilter or(NVersionFilter other);

    /**
     * And.
     *
     * @param other other
     * @return and result
     */
    NVersionFilter and(NVersionFilter other);

    /**
     * Neg.
     *
     * @return neg result
     */
    NVersionFilter neg();

    /**
     * Intervals.
     *
     * @return intervals result
     */
    NOptional<List<NVersionInterval>> intervals();

    @Override
    default NElement describe() {
        return NFilter.super.describe();
    }

    /**
     * With description.
     *
     * @param description description
     * @return with description result
     */
    NFilter withDescription(Supplier<NElement> description);
}
