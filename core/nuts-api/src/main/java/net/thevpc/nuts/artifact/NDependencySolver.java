/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.internal.rpi.NDependencySolverRPI;

import java.util.List;

/**
 * Nuts descriptors solver responsible to building traversable dependency tree
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public interface NDependencySolver {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NDependencySolver of() {
        return NDependencySolverRPI.of().createSolver();
    }

    /**
     * Creates a new instance of of.
     *
     * @param solverName solver name
     * @return of result
     */
    static NDependencySolver of(String solverName) {
        return NDependencySolverRPI.of().createSolver(solverName);
    }

    /**
     * Solver names.
     *
     * @return solver names result
     */
    static List<String> solverNames() {
        return NDependencySolverRPI.of().solverNames();
    }


    /**
     * Adds add.
     *
     * @param def def
     * @return add result
     */
    NDependencySolver add(NDefinition def);

    /**
     * Adds add.
     *
     * @param dependency dependency
     * @return add result
     */
    NDependencySolver add(NDependency dependency);

    /**
     * Adds add.
     *
     * @param dependency dependency
     * @param def def
     * @return add result
     */
    NDependencySolver add(NDependency dependency, NDefinition def);

    /**
     * Dependency filter.
     *
     * @param dependencyFilter dependency filter
     * @return dependency filter result
     */
    NDependencySolver dependencyFilter(NDependencyFilter dependencyFilter);

    /**
     * Repository filter.
     *
     * @param repositoryFilter repository filter
     * @return repository filter result
     */
    NDependencySolver repositoryFilter(NRepositoryFilter repositoryFilter);

    /**
     * Solve.
     *
     * @return solve result
     */
    NDependencies solve();

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Checks if is ignore current environment.
     *
     * @return is ignore current environment result
     */
    boolean isIgnoreCurrentEnvironment();

    /**
     * Ignore current environment.
     *
     * @param ignoreCurrentEnvironment ignore current environment
     * @return ignore current environment result
     */
    NDependencySolver ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment);

}
