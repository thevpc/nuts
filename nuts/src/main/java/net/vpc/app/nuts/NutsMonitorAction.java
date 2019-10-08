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

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author vpc
 */
public interface NutsMonitorAction {

    NutsMonitorAction session(NutsSession s);

    NutsMonitorAction setSession(NutsSession s);

    NutsSession getSession();

    NutsMonitorAction name(String s);

    NutsMonitorAction setName(String s);

    String getName();

    NutsMonitorAction origin(Object s);

    NutsMonitorAction setOrigin(Object s);

    Object getOrigin();

    NutsMonitorAction length(long len);

    NutsMonitorAction setLength(long len);

    long getLength();

    NutsMonitorAction source(String path);

    NutsMonitorAction source(Path path);

    NutsMonitorAction source(File path);

    NutsMonitorAction source(InputStream path);

    NutsMonitorAction setSource(String path);

    NutsMonitorAction setSource(Path path);

    NutsMonitorAction setSource(File path);

    NutsMonitorAction setSource(InputStream path);

    InputStream create();

    boolean isLogProgress();

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction setLogProgress(boolean value);

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction logProgress(boolean value);

    /**
     * will include default factory (console) even if progressFactory is defined
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction logProgress();

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NutsProgressFactory getProgressFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction setProgressFactory(NutsProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction progressFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsMonitorAction progressMonitor(NutsProgressMonitor value);
}
