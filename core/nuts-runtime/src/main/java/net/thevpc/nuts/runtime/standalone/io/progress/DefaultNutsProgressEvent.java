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
 *
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
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NutsProgressEvent;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

/**
 * @author thevpc
 */
public class DefaultNutsProgressEvent implements NutsProgressEvent {

    private final Object source;
    private final String message;
    private final long globalCount;
    private final long globalMillis;
    private final long partialCount;
    private final long partialMillis;
    private final long length;
    private final Throwable exception;
    private final NutsSession session;
    private final float percent;
    private final boolean indeterminate;

    public DefaultNutsProgressEvent(Object source, String message, long globalCount, long globalMillis, long partialCount, long partialMillis, long length, Throwable exception, NutsSession session, boolean indeterminate) {
        this.source = source;
        this.length = length;
        this.message = message;
        this.globalCount = globalCount;
        this.globalMillis = globalMillis;
        this.partialCount = partialCount;
        this.partialMillis = partialMillis;
        this.exception = exception;
        this.session = session;
        this.indeterminate = indeterminate;
        if (length > 0) {
            percent = (float) (globalCount * 100.0 / length);
        } else {
            percent = 100;
        }
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

    public String getMessage() {
        return message;
    }

    public long getCurrentValue() {
        return globalCount;
    }

    public long getTimeMillis() {
        return globalMillis;
    }

    public long getPartialValue() {
        return partialCount;
    }

    public long getPartialMillis() {
        return partialMillis;
    }


    @Override
    public float getPercent() {
        return percent;
    }

    @Override
    public boolean isIndeterminate() {
        return indeterminate;
    }
}
