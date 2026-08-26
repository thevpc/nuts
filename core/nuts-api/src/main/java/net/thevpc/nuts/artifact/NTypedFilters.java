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
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NFilter;

/**
 * Filters helper
 *
 * @author thevpc
 * @app.category Config
 * @since 0.8.0
 */
public interface NTypedFilters<T extends NFilter> extends NComponent {
    /**
     * Nonnull.
     *
     * @param filter filter
     * @return nonnull result
     */
    T nonnull(NFilter filter);

    /**
     * Always.
     *
     * @return always result
     */
    T always();

    /**
     * Never.
     *
     * @return never result
     */
    T never();

    /**
     * All.
     *
     * @param others others
     * @return all result
     */
    T all(NFilter... others);

    /**
     * Any.
     *
     * @param others others
     * @return any result
     */
    T any(NFilter... others);

    /**
     * Not.
     *
     * @param other other
     * @return not result
     */
    T not(NFilter other);

    /**
     * None.
     *
     * @param others others
     * @return none result
     */
    T none(NFilter... others);

    /**
     * From.
     *
     * @param a a
     * @return from result
     */
    T from(NFilter a);

    /**
     * As.
     *
     * @param a a
     * @return as result
     */
    T as(NFilter a);

    /**
     * Parse.
     *
     * @param expression expression
     * @return parse result
     */
    T parse(String expression);
}
