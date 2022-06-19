/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

/**
 * @author thevpc
 */
public class DefaultNutsProgressEvent implements NutsProgressEvent {

    private final Object source;
    private final NutsMessage message;
    private final long globalCount;
    private final NutsDuration globalDuration;
    private final long partialCount;
    private final NutsDuration partialDuration;
    private final long length;
    private final Throwable exception;
    private final NutsSession session;
    private final double progress;
    private final boolean indeterminate;
    private final NutsProgressEventType state;

    public DefaultNutsProgressEvent(Object source, NutsMessage message,
                                    long globalCount, long globalDurationNanos, Double progress,
                                    long partialCount, long partialDurationNanos,
                                    long length, Throwable exception, NutsSession session, Boolean indeterminate, NutsProgressEventType state) {
        this.source = source;
        this.length = length;
        this.message = message;
        this.globalCount = globalCount;
        this.globalDuration = NutsDuration.ofNanos(globalDurationNanos);
        this.partialCount = partialCount;
        this.partialDuration = NutsDuration.ofNanos(partialDurationNanos);
        this.exception = exception;
        this.session = session;
        this.state = state;
        // when percent is -1, percent will be evaluation
        if (progress == null) {
            if (length > 0) {
                progress = (globalCount * 1.0 / length);
            } else {
                progress = Double.NaN;
            }
        }
        this.progress = progress;
        if (indeterminate==null) {
            indeterminate = Double.isNaN(progress);
        }
        this.indeterminate = indeterminate;
    }

    @Override
    public NutsProgressEventType getState() {
        return state;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public Throwable getError() {
        return exception;
    }

    public long getMaxValue() {
        return length;
    }

    public Object getSource() {
        return source;
    }

    public NutsMessage getMessage() {
        return message;
    }

    public long getCurrentCount() {
        return globalCount;
    }

    @Override
    public NutsDuration getDuration() {
        return globalDuration;
    }

    @Override
    public NutsDuration getPartialDuration() {
        return partialDuration;
    }

    public long getPartialCount() {
        return partialCount;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public boolean isIndeterminate() {
        return indeterminate;
    }
}
