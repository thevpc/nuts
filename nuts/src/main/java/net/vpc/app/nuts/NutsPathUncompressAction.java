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
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * @author vpc
 * @since 0.5.4
 */
public interface NutsPathUncompressAction {

    Object getSource();

    NutsPathUncompressAction setSource(InputStream source);

    NutsPathUncompressAction setSource(File source);

    NutsPathUncompressAction setSource(Path source);

    NutsPathUncompressAction setSource(URL source);

    NutsPathUncompressAction from(InputStream source);

    NutsPathUncompressAction from(File source);

    NutsPathUncompressAction from(Path source);

    NutsPathUncompressAction from(URL source);

    Object getTarget();

    NutsPathUncompressAction setTarget(Path target);

    NutsPathUncompressAction setTarget(File target);

    NutsPathUncompressAction from(String source);

    NutsPathUncompressAction to(String target);

    NutsPathUncompressAction to(Path target);

    NutsPathUncompressAction to(File target);

    NutsPathUncompressAction setTarget(String target);

    NutsPathUncompressAction from(Object source);

    NutsPathUncompressAction to(Object target);

    NutsSession getSession();

    NutsPathUncompressAction session(NutsSession session);

    NutsPathUncompressAction setSession(NutsSession session);

    void run();

    NutsPathUncompressAction monitorable(boolean monitorable);

    NutsPathUncompressAction monitorable();

    boolean isMonitorable();

    NutsPathUncompressAction setMonitorable(boolean monitorable);

    NutsPathUncompressAction skipRoot(boolean value);

    NutsPathUncompressAction skipRoot();

    boolean isSkipRoot();

    NutsPathUncompressAction setSkipRoot(boolean value);

    boolean isIncludeDefaultMonitorFactory();

    NutsPathUncompressAction setIncludeDefaultMonitorFactory(boolean value);

    NutsPathUncompressAction includeDefaultMonitorFactory(boolean value);

    NutsPathUncompressAction includeDefaultMonitorFactory();

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NutsInputStreamProgressFactory getProgressMonitorFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathUncompressAction setProgressMonitorFactory(NutsInputStreamProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathUncompressAction progressMonitorFactory(NutsInputStreamProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathUncompressAction setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathUncompressAction progressMonitor(NutsProgressMonitor value);

    boolean isSafeCopy();

    NutsPathUncompressAction setSafeCopy(boolean safeCopy);

    NutsPathUncompressAction safeCopy();

    NutsPathUncompressAction safeCopy(boolean safeCopy);
}
