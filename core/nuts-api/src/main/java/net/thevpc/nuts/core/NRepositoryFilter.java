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
package net.thevpc.nuts.core;

import net.thevpc.nuts.internal.rpi.NRepositoryFilterRPI;
import net.thevpc.nuts.util.NFilter;

/**
 * Created by vpc on 1/5/17.
 *
 * @app.category Base
 * @since 0.5.4
 */
public interface NRepositoryFilter extends NFilter {


    //////// COMMON START

    /**
     * Creates a new instance of of nonnull.
     *
     * @param filter filter
     * @return of nonnull result
     */
    static NRepositoryFilter ofNonnull(NFilter filter){
        return NRepositoryFilterRPI.of().nonnull(filter);
    }

    /**
     * Creates a new instance of of always.
     *
     * @return of always result
     */
    static NRepositoryFilter ofAlways(){
        return NRepositoryFilterRPI.of().always();
    }

    /**
     * Creates a new instance of of never.
     *
     * @return of never result
     */
    static NRepositoryFilter ofNever(){
        return NRepositoryFilterRPI.of().never();
    }

    /**
     * Creates a new instance of of all.
     *
     * @param others others
     * @return of all result
     */
    static NRepositoryFilter ofAll(NFilter... others){
        return NRepositoryFilterRPI.of().all(others);
    }

    /**
     * Creates a new instance of of any.
     *
     * @param others others
     * @return of any result
     */
    static NRepositoryFilter ofAny(NFilter... others){
        return NRepositoryFilterRPI.of().any(others);
    }

    /**
     * Creates a new instance of of not.
     *
     * @param other other
     * @return of not result
     */
    static NRepositoryFilter ofNot(NFilter other){
        return NRepositoryFilterRPI.of().not(other);
    }

    /**
     * Creates a new instance of of none.
     *
     * @param others others
     * @return of none result
     */
    static NRepositoryFilter ofNone(NFilter... others){
        return NRepositoryFilterRPI.of().none(others);
    }

    /**
     * Creates a new instance of of from.
     *
     * @param a a
     * @return of from result
     */
    static NRepositoryFilter ofFrom(NFilter a){
        return NRepositoryFilterRPI.of().from(a);
    }

    /**
     * Creates a new instance of of as.
     *
     * @param a a
     * @return of as result
     */
    static NRepositoryFilter ofAs(NFilter a){
        return NRepositoryFilterRPI.of().as(a);
    }

    /**
     * Creates a new instance of of.
     *
     * @param expression expression
     * @return of result
     */
    static NRepositoryFilter of(String expression){
        return NRepositoryFilterRPI.of().parse(expression);
    }


    //////// COMMON END

    //////// FACTORY START

    /**
     * Creates a new instance of of selector.
     *
     * @param names names
     * @return of selector result
     */
    static NRepositoryFilter ofSelector(String... names){
        return NRepositoryFilterRPI.of().bySelector(names);
    }

    /**
     * Creates a new instance of of name.
     *
     * @param names names
     * @return of name result
     */
    static NRepositoryFilter ofName(String... names){
        return NRepositoryFilterRPI.of().byName(names);
    }

    /**
     * Creates a new instance of of name selector.
     *
     * @param names names
     * @return of name selector result
     */
    static NRepositoryFilter ofNameSelector(String... names){
        return NRepositoryFilterRPI.of().byNameSelector(names);
    }

    /**
     * Creates a new instance of of uuid.
     *
     * @param uuids uuids
     * @return of uuid result
     */
    static NRepositoryFilter ofUuid(String... uuids){
        return NRepositoryFilterRPI.of().byUuid(uuids);
    }

    /**
     * Creates a new instance of of installed repo.
     *
     * @return of installed repo result
     */
    static NRepositoryFilter ofInstalledRepo(){
        return NRepositoryFilterRPI.of().installedRepo();
    }

    //////// FACTORY END

    /**
     * Accept repository.
     *
     * @param repository repository
     * @return accept repository result
     */
    boolean acceptRepository(NRepository repository);

    /**
     * Or.
     *
     * @param other other
     * @return or result
     */
    NRepositoryFilter or(NRepositoryFilter other);

    /**
     * And.
     *
     * @param other other
     * @return and result
     */
    NRepositoryFilter and(NRepositoryFilter other);

    /**
     * Neg.
     *
     * @return neg result
     */
    NRepositoryFilter neg();

}
