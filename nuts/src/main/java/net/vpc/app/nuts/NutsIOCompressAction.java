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
 * I/O Action that help monitored compress
 * of one or multiple resource types.
 * Default implementation should handle
 *
 * @author vpc
 * @since 0.5.4
 * @category Base
 */
public interface NutsIOCompressAction {

    /**
     * update format option
     * @param option option name
     * @param value value
     * @return {@code this} instance
     */
    NutsIOCompressAction setFormatOption(String option,Object value);

    /**
     * return format option
     * @param option option name
     * @return option value
     */
    Object getFormatOption(String option);

    /**
     * format
     * @return format
     */
    String getFormat();

    /**
     * update format
     * @param format format
     * @return {@code this} instance
     */
    NutsIOCompressAction setFormat(String format);

    /**
     * sources to compress
     *
     * @return sources to compress
     */
    List<Object> getSources();

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsIOCompressAction addSource(String source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsIOCompressAction addSource(InputStream source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsIOCompressAction addSource(File source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsIOCompressAction addSource(Path source);

    /**
     * add source to compress
     *
     * @param source source
     * @return {@code this} instance
     * @throws NutsUnsupportedArgumentException if unsupported type
     */
    NutsIOCompressAction addSource(URL source);

    /**
     * target to compress to
     *
     * @return target to compress to
     */
    Object getTarget();

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction setTarget(OutputStream target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction setTarget(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction setTarget(File target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction to(OutputStream target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction to(String target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction to(Path target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction to(File target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction setTarget(String target);

    /**
     * update target
     *
     * @param target target
     * @return {@code this} instance
     */
    NutsIOCompressAction to(Object target);

    /**
     * return current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * update current session
     *
     * @param session current session
     * @return {@code this} instance
     */
    NutsIOCompressAction setSession(NutsSession session);

    /**
     * run this Compress action
     *
     * @return {@code this} instance
     */
    NutsIOCompressAction run();

    /**
     * switch log progress flag to {@code value}.
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsIOCompressAction logProgress(boolean value);

    /**
     * switch log progress flag to to true.
     *
     * @return {@code this} instance
     */
    NutsIOCompressAction logProgress();

    /**
     * switch log progress flag to {@code value}.
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsIOCompressAction setLogProgress(boolean value);

    /**
     * true if log progress flag is armed
     *
     * @return true if log progress flag is armed
     */
    boolean isLogProgress();

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    NutsProgressFactory getProgressMonitorFactory();

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCompressAction setProgressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCompressAction progressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCompressAction setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCompressAction progressMonitor(NutsProgressMonitor value);

    /**
     * return true if safe copy flag is armed
     *
     * @return true if safe copy flag is armed
     */
    boolean isSafe();

    /**
     * switch safe copy flag to {@code value}
     *
     * @param value safe value
     * @return {@code this} instance
     */
    NutsIOCompressAction setSafe(boolean value);

    /**
     * arm safe copy flag
     *
     * @return {@code this} instance
     */
    NutsIOCompressAction safe();

    /**
     * switch safe copy flag to {@code value}
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsIOCompressAction safe(boolean value);

    /**
     * set skip root flag to {@code value}
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCompressAction skipRoot(boolean value);

    /**
     * set skip root flag to {@code true}
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCompressAction skipRoot();

    /**
     * return true if skip root flag is armed.
     *
     * @return true if skip root flag is armed
     * @since 0.5.8
     */
    boolean isSkipRoot();

    /**
     * set skip root flag to {@code value}
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOCompressAction setSkipRoot(boolean value);
}
