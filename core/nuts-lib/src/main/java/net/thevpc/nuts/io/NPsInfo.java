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
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.io;

import java.time.Instant;
import java.util.Set;

/**
 * System Process Information
 *
 * @app.category Base
 */
public interface NPsInfo {
    /**
     * Process Id in string representation
     *
     * @return Process Id in string representation
     */
    String getPid();

    /**
     * Process Name.
     * This should represent Fully Qualified Java Main Class Name for java processes.
     *
     * @return Process Name or Java Class Name for Java Processes
     */
    String getName();

    NpsStatus getStatus();

    NpsType getType();

    Set<String> getStatusFlags();

    /**
     * Process Title / Window Title if available
     *
     * @return Process Title / Window Title if available
     */
    String getTitle();

    /**
     * Process command line
     *
     * @return Process command line
     */
    String getCmdLine();

    /**
     * Process parsed command line (when applicable)
     *
     * @return parsed Process command line
     */
    String[] getCmdLineArgs();

    String getUser();

    double getPercentCpu();

    double getPercentMem();

    long getVirtualMemorySize();

    long getResidentSetSize();

    String getTerminal();

    Instant getStartTime();

    long getTime();
}
