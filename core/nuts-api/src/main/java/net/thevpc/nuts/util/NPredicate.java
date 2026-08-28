/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.internal.NReservedNPredicateUtils;
import net.thevpc.nuts.spi.base.NPredicateImpl;

import java.util.function.Predicate;

/**
 * Describable Predicate
 *
 * @param <T> Type
 */
public interface NPredicate<T> extends Predicate<T>, NRedescribable<NPredicate<T>> {
//    /**
//     * Creates a new instance of of non null.
//     *
//     * @return of non null result
//     */
//    static <T> NPredicate<T> ofNonNull() {
//        /**
//         * Creates a new instance of of.
//         *
//         * @param Objects::nonNull objects::non null
//         * @param NElement.ofName("nonNull") n element.of name("non null")
//         * @return of result
//         */
//        return of(Objects::nonNull, NElement.ofName("nonNull"));
//    }
//
//    /**
//     * Creates a new instance of of null.
//     *
//     * @return of null result
//     */
//    static <T> NPredicate<T> ofNull() {
//        /**
//         * Creates a new instance of of.
//         *
//         * @param Objects::isNull objects::is null
//         * @param NElement.ofName("nonNull") n element.of name("non null")
//         * @return of result
//         */
//        return of(Objects::isNull, NElement.ofName("nonNull"));
//    }

    /**
     * Creates a new instance of of non blank.
     *
     * @return of non blank result
     */
    static <T> NPredicate<T> ofNonBlank() {
        /**
         * Creates a new instance of of.
         *
         * @param NBlankable::isNonBlank n blankable::is non blank
         * @param NElement.ofName("nonNull") n element.of name("non null")
         * @return of result
         */
        return of(NBlankable::isNonBlank, NElement.ofName("nonNull"));
    }

//    /**
//     * Creates a new instance of of blank.
//     *
//     * @return of blank result
//     */
//    static <T> NPredicate<T> ofBlank() {
//        /**
//         * Creates a new instance of of.
//         *
//         * @param NBlankable::isBlank n blankable::is blank
//         * @param NElement.ofName("nonNull") n element.of name("non null")
//         * @return of result
//         */
//        return of(NBlankable::isBlank, NElement.ofName("nonNull"));
//    }

    /**
     * Creates a new instance of of.
     *
     * @param o o
     * @return of result
     */
    static <T> NPredicate<T> of(Predicate<T> o) {
        /**
         * Creates a new instance of of.
         *
         * @param o o
         * @param null null
         * @return of result
         */
        return of(o, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param o o
     * @param description description
     * @return of result
     */
    static <T> NPredicate<T> of(Predicate<T> o, NElement description) {
        if (o == null) {
            return null;
        }
        if (o instanceof NPredicate<?>) {
            return (NPredicate<T>) o;
        }
        return new NPredicateImpl<>(o, description);
    }

    /**
     * Never.
     *
     * @return never result
     */
    @SuppressWarnings("unchecked")
    public static <T> NPredicate<T> ofNever() {
        return NReservedNPredicateUtils.never();
    }

    /**
     * Blank.
     *
     * @return blank result
     */
    @SuppressWarnings("unchecked")
    public static <T> NPredicate<T> ofBlank() {
        return NReservedNPredicateUtils.blank();
    }

    /**
     * Always.
     *
     * @return always result
     */
    @SuppressWarnings("unchecked")
    public static <T> NPredicate<T> ofAlways() {
        return NReservedNPredicateUtils.always();
    }

    /**
     * Checks if is null.
     *
     * @return is null result
     */
    @SuppressWarnings("unchecked")
    public static <T> NPredicate<T> ofNull() {
        return NReservedNPredicateUtils.isNull();
    }

    /**
     * Non null.
     *
     * @return non null result
     */
    @SuppressWarnings("unchecked")
    public static <T> NPredicate<T> ofNonNull() {
        return NReservedNPredicateUtils.nonNull();
    }





    /**
     * And.
     *
     * @param other other
     * @return and result
     */
    NPredicate<T> and(Predicate<? super T> other);

    @Override
    NPredicate<T> negate();

    @Override
    NPredicate<T> or(Predicate<? super T> other);
}
