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
package net.thevpc.nuts;

/**
 * Progress event
 * @author thevpc
 * @since 0.5.8
 * @category Toolkit
 */
public interface NutsProgressEvent {

    /**
     * Nuts Session
     * @return Nuts Session
     */
    NutsSession getSession();

    /**
     * Nuts Workspace
     * @return Nuts Workspace
     */
    NutsWorkspace getWorkspace();

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
    NutsString getMessage();

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
