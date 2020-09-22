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
package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * I/O Action that help monitored uncompress of one or multiple resource types.
 * @author vpc
 * @since 0.5.8
 */
public interface NutsIOUncompressAction {
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
    NutsIOUncompressAction setFormat(String format);

    /**
     * update format option
     * @param option option name
     * @param value value
     * @return {@code this} instance
     */
    NutsIOUncompressAction setFormatOption(String option,Object value);

    /**
     * return format option
     * @param option option name
     * @return option value
     */
    Object getFormatOption(String option);

    /**
     * source to uncompress
     * @return source to uncompress
     */
    Object getSource();

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction setSource(InputStream source);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction setSource(File source);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction setSource(Path source);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction setSource(URL source);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction from(InputStream source);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction from(File source);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction from(Path source);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction from(URL source);

    /**
     * target to uncompress to
     * @return target to uncompress to
     */
    Object getTarget();

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOUncompressAction setTarget(Path target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOUncompressAction setTarget(File target);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction from(String source);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOUncompressAction to(String target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOUncompressAction to(Path target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOUncompressAction to(File target);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOUncompressAction setTarget(String target);

    /**
     * update source to uncompress from
     * @param source source to uncompress from
     * @return {@code this} instance
     */
    NutsIOUncompressAction from(Object source);

    /**
     * update target
     * @param target target
     * @return {@code this} instance
     */
    NutsIOUncompressAction to(Object target);

    /**
     * return current session
     * @return current session
     */
    NutsSession getSession();

    /**
     * update current session
     * @param session current session
     * @return {@code this} instance
     */
    NutsIOUncompressAction setSession(NutsSession session);

    /**
     * run this uncompress action
     * @return {@code this} instance
     */
    NutsIOUncompressAction run();

    /**
     * switch log progress flag to {@code value}.
     * @param value value
     * @return {@code this} instance
     */
    NutsIOUncompressAction logProgress(boolean value);

    /**
     * switch log progress flag to to true.
     * @return {@code this} instance
     */
    NutsIOUncompressAction logProgress();

    /**
     * true if log progress flag is armed
     * @return true if log progress flag is armed
     */
    boolean isLogProgress();

    /**
     * switch log progress flag to {@code value}.
     * @param value value
     * @return {@code this} instance
     */
    NutsIOUncompressAction setLogProgress(boolean value);

    /**
     * set skip root flag to {@code value}
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOUncompressAction skipRoot(boolean value);

    /**
     * set skip root flag to {@code true}
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOUncompressAction skipRoot();

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
    NutsIOUncompressAction setSkipRoot(boolean value);

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
    NutsIOUncompressAction setProgressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOUncompressAction progressMonitorFactory(NutsProgressFactory value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOUncompressAction setProgressMonitor(NutsProgressMonitor value);

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    NutsIOUncompressAction progressMonitor(NutsProgressMonitor value);

    /**
     * return true if safe flag is armed
     * @return true if safe flag is armed
     */
    boolean isSafe();

    /**
     * switch safe flag to {@code value}
     * @param value value
     * @return {@code this} instance
     */
    NutsIOUncompressAction setSafe(boolean value);

    /**
     * arm safe flag
     * @return {@code this} instance
     */
    NutsIOUncompressAction safe();

    /**
     * switch safe flag to {@code value}
     * @param value value
     * @return {@code this} instance
     */
    NutsIOUncompressAction safe(boolean value);
}
