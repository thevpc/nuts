/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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

import net.thevpc.nuts.*;

import java.util.List;

/**
 * Nuts descriptors solver responsible to building traversable dependency tree
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public interface NDependencySolver {

    static NDependencySolver of(NSession session) {
        return NDependencySolvers.of(session).createSolver(session);
    }

    static NDependencySolver of(String solverName, NSession session) {
        return NDependencySolvers.of(session).createSolver(solverName, session);
    }

    static List<String> getSolverNames(NSession session) {
        return NDependencySolvers.of(session).getSolverNames(session);
    }


    NDependencySolver add(NDefinition def);

    NDependencySolver add(NDependency dependency);

    NDependencySolver add(NDependency dependency, NDefinition def);

    NDependencySolver setDependencyFilter(NDependencyFilter dependencyFilter);
    NDependencySolver setRepositoryFilter(NRepositoryFilter repositoryFilter);

    NDependencies solve();

    String getName();
}
