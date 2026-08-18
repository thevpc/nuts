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

    static NIdFilter ofNonnull(NFilter filter){
        return NIdFilterRPI.of().nonnull(filter);
    }

    static NIdFilter ofAlways(){
        return NIdFilterRPI.of().always();
    }

    static NIdFilter ofNever(){
        return NIdFilterRPI.of().never();
    }

    static NIdFilter ofAll(NFilter... others){
        return NIdFilterRPI.of().all(others);
    }

    static NIdFilter ofAny(NFilter... others){
        return NIdFilterRPI.of().any(others);
    }

    static NIdFilter ofNot(NFilter other){
        return NIdFilterRPI.of().not(other);
    }

    static NIdFilter ofNone(NFilter... others){
        return NIdFilterRPI.of().none(others);
    }

    static NIdFilter ofFrom(NFilter a){
        return NIdFilterRPI.of().from(a);
    }

    static NIdFilter ofAs(NFilter a){
        return NIdFilterRPI.of().as(a);
    }

    static NIdFilter of(String expression){
        return NIdFilterRPI.of().parse(expression);
    }

    //////// COMMON END

    //////// FACTORY START

    static NIdFilter ofValue(NId id){
        return NIdFilterRPI.of().byValue(id);
    }

    static NIdFilter ofDefaultVersion(Boolean defaultVersion){
        return NIdFilterRPI.of().byDefaultVersion(defaultVersion);
    }


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

    NIdFilter or(NIdFilter other);

    NIdFilter and(NIdFilter other);

    @Override
    NIdFilter neg();

}
