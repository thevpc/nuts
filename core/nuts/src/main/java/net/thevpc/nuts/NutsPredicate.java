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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Describable Predicate
 * @param <T> Type
 */
public interface NutsPredicate<T> extends Predicate<T>, NutsDescribable {
    static <T> NutsPredicate<T> of(Predicate<T> o, String descr){
        return NutsDescribables.ofPredicate(o,e->e.ofString(descr));
    }
    static <T> NutsPredicate<T> of(Predicate<T> o,NutsElement descr){
        return NutsDescribables.ofPredicate(o,e->descr);
    }
    static <T> NutsPredicate<T> of(Predicate<T> o, Function<NutsElements, NutsElement> descr){
        return NutsDescribables.ofPredicate(o,descr);
    }

    NutsPredicate<T> and(Predicate<? super T> other);

    @Override
    NutsPredicate<T> negate();

    @Override
    NutsPredicate<T> or(Predicate<? super T> other);
}
