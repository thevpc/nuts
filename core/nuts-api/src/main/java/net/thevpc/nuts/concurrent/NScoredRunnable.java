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

import java.util.function.Supplier;

/**
 * Represents a {@link Runnable} with an associated score.
 * <p>
 * A {@code NScoredRunnable} can be valid or invalid based on its score.
 * Scores indicate the "priority" or "validity" of the runnable. A score of zero
 * or less generally indicates an invalid runnable. Optionally, an empty message
 * can describe why the runnable is invalid.
 */
public interface NScoredRunnable extends NScorable {

    /**
     * Creates a scored runnable with the given score and action.
     * <p>
     * If the score is zero or less, an invalid scored runnable is returned.
     *
     * @param score the score of the runnable
     * @param supplier the action to run
     * @return a new {@code NScoredRunnable} instance
     */
    static NScoredRunnable of(int score, Runnable supplier) {
        return of(score, supplier, null);
    }

    /**
     * Creates a scored runnable with the given score, action, and optional empty message.
     *
     * @param score the score of the runnable
     * @param supplier the action to run
     * @param emptyMessage a supplier providing a message if the runnable is invalid
     * @return a new {@code NScoredRunnable} instance
     */
    static NScoredRunnable of(int score, Runnable supplier, Supplier<NMsg> emptyMessage) {
        return (score <= 0 || supplier == null) ? ofInvalid(emptyMessage)
                : new DefaultNScoredRunnable(supplier, score, emptyMessage)
                ;
    }

    /**
     * Creates an invalid scored runnable with an optional empty message.
     *
     * @param emptyMessage a supplier providing a message if the runnable is invalid
     * @return an invalid {@code NScoredRunnable} instance
     */
    @SuppressWarnings("unchecked")
    static NScoredRunnable ofInvalid(Supplier<NMsg> emptyMessage) {
        return new DefaultNScoredRunnable(null, UNSUPPORTED_SCORE, emptyMessage);
    }

    /**
     * Executes the runnable action.
     */
    void run();


    /**
     * Returns the score of this runnable in the given context.
     *
     * @param scorableContext the context for scoring
     * @return the score as an integer
     */
    int getScore(NScorableContext scorableContext);
}
