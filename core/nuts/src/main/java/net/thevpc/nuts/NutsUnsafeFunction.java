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

/**
 * Unsafe function is a function that can throw any arbitrary exception
 * @param <T> In
 * @param <R> Out
 */
public interface NutsUnsafeFunction<T, R> extends NutsUnsafeFunctionBase<T, R>, NutsDescribable{
    static <T, V> NutsUnsafeFunction<T, V> of(NutsUnsafeFunctionBase<T, V> o, String descr) {
        return NutsDescribables.ofUnsafeFunction(o, session -> NutsElements.of(session).ofString(descr));
    }

    static <T, V> NutsUnsafeFunction<T, V> of(NutsUnsafeFunctionBase<T, V> o, NutsElement descr) {
        return NutsDescribables.ofUnsafeFunction(o, e -> descr);
    }

    static <T, V> NutsUnsafeFunction<T, V> of(NutsUnsafeFunctionBase<T, V> o, Function<NutsSession, NutsElement> descr) {
        return NutsDescribables.ofUnsafeFunction(o, descr);
    }

}
