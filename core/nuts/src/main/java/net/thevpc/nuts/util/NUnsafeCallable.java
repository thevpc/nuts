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

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Describable Runnable
 */
public interface NUnsafeCallable<T> extends NDescribable {

    static <T> NUnsafeCallable<T> of(NUnsafeCallable<T> o, String descr) {
        return NDescribables.ofUnsafeCallable(o, session -> NElements.of(session).ofString(descr));
    }

    static <T> NUnsafeCallable<T> of(NUnsafeCallable<T> o, NElement descr) {
        return NDescribables.ofUnsafeCallable(o, e -> descr);
    }

    static <T> NUnsafeCallable<T> of(NUnsafeCallable<T> o, Function<NSession, NElement> descr) {
        return NDescribables.ofUnsafeCallable(o, descr);
    }

    static <T> NUnsafeCallable<T> of(Callable<T> o, String descr) {
        return NDescribables.ofUnsafeCallable(o, session -> NElements.of(session).ofString(descr));
    }

    static <T> NUnsafeCallable<T> of(Callable<T> o, NElement descr) {
        return NDescribables.ofUnsafeCallable(o, e -> descr);
    }

    static <T> NUnsafeCallable<T> of(Callable<T> o, Function<NSession, NElement> descr) {
        return NDescribables.ofUnsafeCallable(o, descr);
    }

    T call() throws Exception;

    default NElement describe(NSession session) {
        return NElements.of(session).ofString(toString());
    }

}
