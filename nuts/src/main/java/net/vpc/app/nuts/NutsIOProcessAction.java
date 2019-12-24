/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * I/O Action that help monitoring processes
 *
 * @author vpc
 * @since 0.5.8
 */
public interface NutsIOProcessAction {
    /**
     * process type to consider. Supported 'java'
     * @return process type to consider. Supported 'java'
     */
    String getType();

    /**
     * set process type to consider.
     * Supported 'java' or 'java#version'
     * @param processType new type
     * @return return {@code this} instance
     */
    NutsIOProcessAction setType(String processType);

    /**
     * set process type to consider.
     * Supported 'java' or 'java#version'
     * @param processType new type
     * @return return {@code this} instance
     */
    NutsIOProcessAction type(String processType);

    /**
     * list all processes of type {@link #getType()}
     * @return list all processes of type {@link #getType()}
     */
    NutsSearchResult<NutsProcessInfo> getResultList();

    /**
     * current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    NutsIOProcessAction session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    NutsIOProcessAction setSession(NutsSession session);

    /**
     * return true if fail fast.
     * When fail fast flag is armed, the first
     * error that occurs will throw an {@link java.io.UncheckedIOException}
     *
     * @return true if fail fast
     */
    boolean isFailFast();

    /**
     * update fail fast flag
     *
     * @param failFast value
     * @return {@code this} instance
     */
    NutsIOProcessAction setFailFast(boolean failFast);

    /**
     * update fail fast flag
     *
     * @param failFast value
     * @return {@code this} instance
     */
    NutsIOProcessAction failFast(boolean failFast);

    /**
     * set fail fast flag
     *
     * @return {@code this} instance
     */
    NutsIOProcessAction failFast();
}
