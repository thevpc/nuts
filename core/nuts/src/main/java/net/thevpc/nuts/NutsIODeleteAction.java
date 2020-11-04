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
 * Copyright (C) 2016-2020 thevpc
 * <br>
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
package net.thevpc.nuts;

import java.io.File;
import java.nio.file.Path;

/**
 * I/O Action that help monitored delete.
 *
 * @author vpc
 * @since 0.5.8
 * @category Input Output
 */
public interface NutsIODeleteAction {
    /**
     * return target to delete
     * @return return target to delete
     */
    Object getTarget();

    /**
     * update target to delete
     * @param target target
     * @return {@code this} instance
     */
    NutsIODeleteAction setTarget(Object target);

    /**
     * update target to delete
     * @param target target
     * @return {@code this} instance
     */
    NutsIODeleteAction setTarget(File target);

    /**
     * update target to delete
     * @param target target
     * @return {@code this} instance
     */
    NutsIODeleteAction setTarget(Path target);

    /**
     * update target to delete
     * @param target target
     * @return {@code this} instance
     */
    NutsIODeleteAction target(Object target);

    /**
     * current session
     * @return current session
     */
    NutsSession getSession();

    /**
     * update session
     * @param session session
     * @return {@code this} instance
     */
    NutsIODeleteAction setSession(NutsSession session);

    /**
     * run delete action and return {@code this}
     * @return {@code this} instance
     */
    NutsIODeleteAction run();

    /**
     * return true if fail fast.
     * When fail fast flag is armed, the first
     * error that occurs will throw an {@link java.io.UncheckedIOException}
     * @return true if fail fast
     */
    boolean isFailFast();

    /**
     * set fail fast flag
     * @return {@code this} instance
     */
    NutsIODeleteAction failFast();

    /**
     * update fail fast flag
     * @param failFast value
     * @return {@code this} instance
     */
    NutsIODeleteAction setFailFast(boolean failFast);

    /**
     * update fail fast flag
     * @param failFast value
     * @return {@code this} instance
     */
    NutsIODeleteAction failFast(boolean failFast);
}
