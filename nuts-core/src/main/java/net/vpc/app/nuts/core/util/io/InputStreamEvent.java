/**
 * ====================================================================
 *            vpc-common-io : common reusable library for
 *                          input/output
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.io;

/**
 *
 * @author vpc
 */
public class InputStreamEvent {
    
    private final Object source;
    private final String sourceName;
    private final long globalCount;
    private final long globalMillis;
    private final long partialCount;
    private final long partialMillis;
    private final long length;
    private final Throwable exception;

    public InputStreamEvent(Object source, String sourceName, long globalCount, long globalMillis, long partialCount, long partialMillis,long length,Throwable exception) {
        this.source = source;
        this.length = length;
        this.sourceName = sourceName;
        this.globalCount = globalCount;
        this.globalMillis = globalMillis;
        this.partialCount = partialCount;
        this.partialMillis = partialMillis;
        this.exception = exception;
    }

    public Throwable getException() {
        return exception;
    }

    public long getLength() {
        return length;
    }

    
    public Object getSource() {
        return source;
    }

    public String getSourceName() {
        return sourceName;
    }

    public long getGlobalCount() {
        return globalCount;
    }

    public long getGlobalMillis() {
        return globalMillis;
    }

    public long getPartialCount() {
        return partialCount;
    }

    public long getPartialMillis() {
        return partialMillis;
    }
    
}
