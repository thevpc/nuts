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
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NDescribable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Describable Predicate
 * @param <T> Type
 */
public interface NPredicate<T> extends Predicate<T>, NDescribable {
    static <T> NPredicate<T> of(Predicate<T> o, String descr){
        return NDescribables.ofPredicate(o, session-> NElements.of(session).ofString(descr));
    }
    static <T> NPredicate<T> of(Predicate<T> o, NElement descr){
        return NDescribables.ofPredicate(o, e->descr);
    }
    static <T> NPredicate<T> of(Predicate<T> o, Function<NSession, NElement> descr){
        return NDescribables.ofPredicate(o,descr);
    }

    NPredicate<T> and(Predicate<? super T> other);

    @Override
    NPredicate<T> negate();

    @Override
    NPredicate<T> or(Predicate<? super T> other);
}
