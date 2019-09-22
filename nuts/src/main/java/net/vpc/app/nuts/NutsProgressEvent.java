/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
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
 * Progress event
 * @author vpc
 * @since 0.5.8
 */
public interface NutsProgressEvent {

    /**
     * Nuts Session
     * @return Nuts Session
     */
    NutsSession getSession();

    /**
     * error or null
     * @return error or null
     */
    Throwable getError();

    /**
     * progress max value or -1 if intermediate
     * @return progress max value
     */
    long getMaxValue();

    /**
     * progress current value
     * @return progress current value
     */
    long getCurrentValue();

    /**
     * progress value from the last mark point.
     * Mark point occurs when {@link NutsProgressMonitor#onProgress(NutsProgressEvent)} return false.
     * @return progress value from the last mark point.
     */
    long getPartialValue();

    /**
     * progress source object
     * @return progress source object
     */
    Object getSource();

    /**
     * event message
     * @return event message
     */
    String getMessage();

    /**
     * progress percentage ([0..100])
     * @return progress percentage ([0..100])
     */
    float getPercent();

    /**
     * progress time from the starting of the progress.
     * @return progress time from the starting of the progress.
     */
    long getTimeMillis();

    /**
     * progress time from the starting of the last mark point.
     * @return progress time from the starting of the last mark point.
     */
    long getPartialMillis();

    /**
     * when true, max value is unknown, and the progress is indeterminate
     * @return true when max value is unknown, and the progress is indeterminate
     */
    boolean isIndeterminate();

}
