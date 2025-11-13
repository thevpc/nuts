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

import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.function.Supplier;
/**
 * Represents a callable value with an associated score.
 * <p>
 * The score indicates the "validity" or "priority" of the value. A score of zero or
 * less generally indicates an invalid value. This interface allows creation of
 * both valid and invalid scored callables, optionally providing a message describing
 * why a value is empty or invalid.
 *
 * @param <T> the type of value produced by the callable
 */
public interface NScoredCallable<T> extends NScorable {

    /**
     * Creates a scored callable with the given score and value.
     * <p>
     * If the score is less than or equal to zero, an invalid callable is returned.
     *
     * @param score the score of the callable
     * @param value the value
     * @param <T> the type of the value
     * @return a new {@code NScoredCallable} instance
     */
    static <T> NScoredCallable<T> of(int score, T value) {
        return of(score, value, null);
    }

    /**
     * Creates a scored callable with a score, value, and an optional empty message supplier.
     *
     * @param score the score of the callable
     * @param value the value
     * @param emptyMessage a supplier of a message if the value is empty or invalid
     * @param <T> the type of the value
     * @return a new {@code NScoredCallable} instance
     */
    static <T> NScoredCallable<T> of(int score, T value, Supplier<NMsg> emptyMessage) {
        return score <= 0 ? ofInvalid(emptyMessage) : new DefaultNScoredCallable<>(() -> value, score, emptyMessage);
    }

    /**
     * Creates a scored callable from a supplier and a score.
     *
     * @param score the score of the callable
     * @param supplier the supplier of the value
     * @param <T> the type of the value
     * @return a new {@code NScoredCallable} instance
     */
    static <T> NScoredCallable<T> of(int score, Supplier<T> supplier) {
        return of(score, supplier, null);
    }

    /**
     * Creates a scored callable from a supplier, score, and optional empty message supplier.
     *
     * @param score the score of the callable
     * @param supplier the supplier of the value
     * @param emptyMessage a supplier of a message if the value is empty or invalid
     * @param <T> the type of the value
     * @return a new {@code NScoredCallable} instance
     */
    static <T> NScoredCallable<T> of(int score, Supplier<T> supplier, Supplier<NMsg> emptyMessage) {
        return (score <= 0 || supplier == null) ? ofInvalid(emptyMessage)
                : new DefaultNScoredCallable<>(supplier, score, emptyMessage)
                ;
    }

    /**
     * Creates a valid scored callable using a default score.
     *
     * @param value the value
     * @param <T> the type of the value
     * @return a valid scored callable
     */
    static <T> NScoredCallable<T> ofValid(T value) {
        return of(DEFAULT_SCORE, value, null);
    }

    /**
     * Creates a valid scored callable using a default score and a supplier.
     *
     * @param supplier the supplier of the value
     * @param <T> the type of the value
     * @return a valid scored callable
     */
    static <T> NScoredCallable<T> ofValid(Supplier<T> supplier) {
        return of(DEFAULT_SCORE, supplier, null);
    }

    /**
     * Creates a valid scored callable with a specific score.
     * <p>
     * If the score is zero or negative, the default score is used.
     *
     * @param score the score
     * @param value the value
     * @param <T> the type of the value
     * @return a valid scored callable
     */
    static <T> NScoredCallable<T> ofValid(int score, T value) {
        return of((score <= 0) ? DEFAULT_SCORE : score, value, null);
    }

    /**
     * Creates a valid scored callable with a specific score and a supplier.
     * <p>
     * If the score is zero or negative, the default score is used.
     *
     * @param score the score
     * @param supplier the supplier of the value
     * @param <T> the type of the value
     * @return a valid scored callable
     */
    static <T> NScoredCallable<T> ofValid(int score, Supplier<T> supplier) {
        return of((score <= 0) ? DEFAULT_SCORE : score, supplier, null);
    }

    /**
     * Creates an invalid scored callable with a supplier for the empty message.
     *
     * @param emptyMessage the supplier providing a message
     * @param <T> the type of the value
     * @return an invalid scored callable
     */
    @SuppressWarnings("unchecked")
    static <T> NScoredCallable<T> ofInvalid(Supplier<NMsg> emptyMessage) {
        return new DefaultNScoredCallable<>(null, UNSUPPORTED_SCORE, emptyMessage);
    }

    /**
     * Creates an invalid scored callable with a fixed message.
     *
     * @param emptyMessage the message describing why the callable is invalid
     * @param <T> the type of the value
     * @return an invalid scored callable
     */
    @SuppressWarnings("unchecked")
    static <T> NScoredCallable<T> ofInvalid(NMsg emptyMessage) {
        return new DefaultNScoredCallable<>(null, UNSUPPORTED_SCORE, emptyMessage == null ? null : () -> emptyMessage);
    }

    /**
     * Computes or retrieves the value of this callable.
     *
     * @return the computed value
     */
    T call();

    /**
     * Returns the score of this callable in the given context.
     *
     * @param scorableContext the context for scoring
     * @return the score as an integer
     */
    int getScore(NScorableContext scorableContext);

}
