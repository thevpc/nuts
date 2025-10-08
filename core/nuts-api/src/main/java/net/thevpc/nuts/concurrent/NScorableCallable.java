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
package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.spi.NScorable;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;

public interface NScorableCallable<T> extends NScorable {
    static <T> NScorableCallable<T> of(int score, T value) {
        return of(score, value, null);
    }

    static <T> NScorableCallable<T> of(int score, T value, Supplier<NMsg> emptyMessage) {
        return score <= 0 ? ofInvalid(emptyMessage) : new DefaultNScorableCallable<>(() -> value, score, emptyMessage);
    }

    static <T> NScorableCallable<T> of(int score, Supplier<T> supplier) {
        return of(score, supplier, null);
    }

    static <T> NScorableCallable<T> of(int score, Supplier<T> supplier, Supplier<NMsg> emptyMessage) {
        return (score <= 0 || supplier == null) ? ofInvalid(emptyMessage)
                : new DefaultNScorableCallable<>(supplier, score, emptyMessage)
                ;
    }

    static <T> NScorableCallable<T> ofValid(T value) {
        return of(DEFAULT_SCORE, value, null);
    }

    static <T> NScorableCallable<T> ofValid(Supplier<T> supplier) {
        return of(DEFAULT_SCORE, supplier, null);
    }

    static <T> NScorableCallable<T> ofValid(int score, T value) {
        return of((score <= 0) ? DEFAULT_SCORE : score, value, null);
    }

    static <T> NScorableCallable<T> ofValid(int score, Supplier<T> supplier) {
        return of((score <= 0) ? DEFAULT_SCORE : score, supplier, null);
    }

    @SuppressWarnings("unchecked")
    static <T> NScorableCallable<T> ofInvalid(Supplier<NMsg> emptyMessage) {
        return new DefaultNScorableCallable<>(null, UNSUPPORTED_SCORE, emptyMessage);
    }

    @SuppressWarnings("unchecked")
    static <T> NScorableCallable<T> ofInvalid(NMsg emptyMessage) {
        return new DefaultNScorableCallable<>(null, UNSUPPORTED_SCORE, emptyMessage == null ? null : () -> emptyMessage);
    }

    T call();

    default boolean isValid(NScorableContext scorableContext) {
        return getScore(scorableContext) > 0;
    }

    int getScore(NScorableContext scorableContext);

    NOptional<T> toOptional();
}
