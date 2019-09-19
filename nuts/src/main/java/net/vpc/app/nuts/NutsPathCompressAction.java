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
public interface NutsPathCompressAction {

    List<Object> getSources();

    NutsPathCompressAction addSource(InputStream source);

    NutsPathCompressAction addSource(File source);

    NutsPathCompressAction addSource(Path source);

    NutsPathCompressAction addSource(URL source);

    Object getTarget();

    NutsPathCompressAction setTarget(OutputStream target);

    NutsPathCompressAction setTarget(Path target);

    NutsPathCompressAction setTarget(File target);

    NutsPathCompressAction to(OutputStream target);

    NutsPathCompressAction to(String target);

    NutsPathCompressAction to(Path target);

    NutsPathCompressAction to(File target);

    NutsPathCompressAction setTarget(String target);

    NutsPathCompressAction to(Object target);

    NutsSession getSession();

    NutsPathCompressAction session(NutsSession session);

    NutsPathCompressAction setSession(NutsSession session);

    void run();

    NutsPathCompressAction monitorable(boolean monitorable);

    NutsPathCompressAction monitorable();

    boolean isMonitorable();

    NutsPathCompressAction setMonitorable(boolean monitorable);

    boolean isIncludeDefaultMonitorFactory();

    NutsPathCompressAction setIncludeDefaultMonitorFactory(boolean value);

    NutsPathCompressAction includeDefaultMonitorFactory(boolean value);

    NutsPathCompressAction includeDefaultMonitorFactory();

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
    NutsPathCompressAction setProgressMonitorFactory(NutsInputStreamProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathCompressAction progressMonitorFactory(NutsInputStreamProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathCompressAction setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsPathCompressAction progressMonitor(NutsProgressMonitor value);

    boolean isSafeCopy();

    NutsPathCompressAction setSafeCopy(boolean safeCopy);

    NutsPathCompressAction safeCopy();

    NutsPathCompressAction safeCopy(boolean safeCopy);

    NutsPathCompressAction skipRoot(boolean value);

    NutsPathCompressAction skipRoot();

    boolean isSkipRoot();

    NutsPathCompressAction setSkipRoot(boolean value);
}
