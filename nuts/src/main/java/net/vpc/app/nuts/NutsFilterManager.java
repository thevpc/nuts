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
 * <p>
 * Copyright (C) 2016-2020 thevpc
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Filters helper
 *
 * @author vpc
 * @category Config
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
