/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NFilter;

/**
 * Filters helper
 *
 * @author thevpc
 * @app.category Config
 * @since 0.8.0
 */
public interface NFilters extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NFilters of() {
       return NExtensions.of(NFilters.class);
    }


    /**
     * Nonnull.
     *
     * @param type type
     * @param filter filter
     * @return nonnull result
     */
    <T extends NFilter> T nonnull(Class<T> type, NFilter filter);

    /**
     * Always.
     *
     * @param type type
     * @return always result
     */
    <T extends NFilter> T always(Class<T> type);

    /**
     * Never.
     *
     * @param type type
     * @return never result
     */
    <T extends NFilter> T never(Class<T> type);

    /**
     * All.
     *
     * @param type type
     * @param others others
     * @return all result
     */
    <T extends NFilter> T all(Class<T> type, NFilter... others);

    /**
     * All.
     *
     * @param others others
     * @return all result
     */
    <T extends NFilter> T all(NFilter... others);

    /**
     * Any.
     *
     * @param type type
     * @param others others
     * @return any result
     */
    <T extends NFilter> T any(Class<T> type, NFilter... others);

    /**
     * Not.
     *
     * @param other other
     * @return not result
     */
    <T extends NFilter> T not(NFilter other);

    /**
     * Not.
     *
     * @param type type
     * @param other other
     * @return not result
     */
    <T extends NFilter> T not(Class<T> type, NFilter other);

    /**
     * Any.
     *
     * @param others others
     * @return any result
     */
    <T extends NFilter> T any(NFilter... others);

    /**
     * None.
     *
     * @param type type
     * @param others others
     * @return none result
     */
    <T extends NFilter> T none(Class<T> type, NFilter... others);

    /**
     * None.
     *
     * @param others others
     * @return none result
     */
    <T extends NFilter> T none(NFilter... others);

    /**
     * convert {@code filter} to {@code toFilterInterface} or throw error
     *
     * @param toFilterInterface one of the valid interfaces of
     *                          {@code NutsFilter}
     * @param filter            filter instance
     * @param <T>               filter type
     * @return instance of {@code T} converted from {@code filter} or error
     */
    <T extends NFilter> T to(Class<T> toFilterInterface, NFilter filter);

    /**
     * convert {@code filter} to {@code toFilterInterface} or throw error
     *
     * @param toFilterInterface one of the valid interfaces of
     *                          {@code NutsFilter}
     * @param filter            filter instance
     * @param <T>               filter type
     * @return instance of {@code T} converted from {@code filter} or null
     */
    <T extends NFilter> T as(Class<T> toFilterInterface, NFilter filter);

    /**
     * Detect type.
     *
     * @param nFilter n filter
     * @return detect type result
     */
    Class<? extends NFilter> detectType(NFilter nFilter);
}
