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

import net.thevpc.nuts.spi.NDefaultScorableContext;

/**
 * Represents the context for evaluating the score of a {@link NScorable} instance.
 * <p>
 * A scorable context may optionally carry criteria that influence scoring. These criteria
 * can be any object, and it is up to the implementation of {@link NScorable#getScore(NScorableContext)}
 * to interpret them appropriately.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * NScorableContext ctx = NScorableContext.of(myCriteriaObject);
 * int score = someScorable.getScore(ctx);
 * }</pre>
 * </p>
 * <p>
 * Note: If no criteria are needed, {@link #of()} can be used to create an empty/default context.
 * </p>
 * @since 0.8.7
 */
public interface NScorableContext {

    /**
     * Creates an empty/default scorable context.
     *
     * @return a new {@link NScorableContext} instance with no criteria
     */
    static NScorableContext of() {
        return new NDefaultScorableContext();
    }

    /**
     * Creates a scorable context carrying the given criteria.
     *
     * @param any the criteria object; can be any type
     * @return a new {@link NScorableContext} instance with the specified criteria
     */
    static NScorableContext of(Object any) {
        return new NDefaultScorableContext(any);
    }

    /**
     * Returns the criteria object cast to the expected type.
     *
     * @param <T> the expected type
     * @return the criteria cast to {@code T}
     * @throws ClassCastException if the criteria is not compatible with {@code T}
     */
    <T> T getCriteria();

    /**
     * Returns the criteria object cast to the specified type.
     *
     * @param <T>      the expected type
     * @param expected the expected class of the criteria
     * @return the criteria cast to {@code T}, or {@code null} if not compatible
     */
    <T> T getCriteria(Class<T> expected);
}
