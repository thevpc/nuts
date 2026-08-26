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
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.artifact.NDependencySolver;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.List;

/**
 * NDependencySolverRPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDependencySolverRPI extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NDependencySolverRPI of() {
       return NExtensions.of(NDependencySolverRPI.class);
    }

    /**
     * Creates a new instance of create solver.
     *
     * @return create solver result
     */
    NDependencySolver createSolver();

    /**
     * Creates a new instance of create solver.
     *
     * @param solverName solver name
     * @return create solver result
     */
    NDependencySolver createSolver(String solverName);

    /**
     * Solver names.
     *
     * @return solver names result
     */
    List<String> solverNames();

}
