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
 *
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsExecutionContext;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.NutsId;

/**
 * An Executor Component is responsible of "executing" a nuts package
 * (package) Created by vpc on 1/7/17.
 *
 * @since 0.5.4
 * @app.category SPI Base
 */
public interface NutsExecutorComponent extends NutsComponent {

    /**
     * artifact id
     * @return artifact id
     */
    NutsId getId();

    /**
     * execute the artifact
     * @param executionContext executionContext
     * @throws NutsExecutionException when execution fails
     */
    void exec(NutsExecutionContext executionContext) throws NutsExecutionException;

    /**
     * performs a dry execution (simulation) avoiding any side effect and issuing trace to standard
     * output in order to log simulation workflow.
     * @param executionContext executionContext
     * @throws NutsExecutionException when execution fails
     */
    void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException;
}
