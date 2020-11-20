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
 *
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

/**
 * Filters helper
 *
 * @author vpc
 * %category Config
 * @since 0.8.0
 */
public interface NutsFilterManager {

    <T extends NutsFilter> T nonnull(Class<T> type, NutsFilter filter);

    <T extends NutsFilter> T always(Class<T> type);

    <T extends NutsFilter> T never(Class<T> type);

    <T extends NutsFilter> T all(Class<T> type, NutsFilter... others);

    <T extends NutsFilter> T all(NutsFilter... others);

    <T extends NutsFilter> T any(Class<T> type, NutsFilter... others);

    <T extends NutsFilter> T not(NutsFilter other);

    <T extends NutsFilter> T not(Class<T> type, NutsFilter other);

    <T extends NutsFilter> T any(NutsFilter... others);

    <T extends NutsFilter> T none(Class<T> type, NutsFilter... others);

    <T extends NutsFilter> T none(NutsFilter... others);

    /**
     * convert {@code filter} to {@code toFilterInterface} or throw error
     *
     * @param toFilterInterface one of the valid interfaces of {@code NutsFilter}
     * @param filter            filter instance
     * @param <T>               filter type
     * @return instance of {@code T} converted from {@code filter} or error
     */
    <T extends NutsFilter> T to(Class<T> toFilterInterface, NutsFilter filter);

    /**
     * convert {@code filter} to {@code toFilterInterface} or throw error
     *
     * @param toFilterInterface one of the valid interfaces of {@code NutsFilter}
     * @param filter            filter instance
     * @param <T>               filter type
     * @return instance of {@code T} converted from {@code filter} or null
     */
    <T extends NutsFilter> T as(Class<T> toFilterInterface, NutsFilter filter);

    Class<? extends NutsFilter> detectType(NutsFilter nutsFilter);

    NutsIdFilterManager id();

    NutsDependencyFilterManager dependency();

    NutsRepositoryFilterManager repository();

    NutsVersionFilterManager version();

    NutsDescriptorFilterManager descriptor();
}
