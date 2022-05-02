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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NutsIterable;
import net.thevpc.nuts.util.NutsStream;

/**
 * Package dependencies representation
 *
 * @author thevpc
 * @since 0.8.2
 */
public interface NutsDependencies extends NutsIterable<NutsDependency> {
    /**
     * sources of this dependencies
     * @return sources of this dependencies
     */
    NutsStream<NutsId> sourceIds();

    /**
     * solver name used to compute the dependencies
     * @return solver name used to compute the dependencies
     */
    String solver();

    /**
     * filter used to compute the dependencies
     * @return filter used to compute the dependencies
     */
    NutsDependencyFilter filter();

    /**
     * return immediate dependencies (not including sources)
     * @return immediate dependencies
     */
    NutsStream<NutsDependency> immediate();

    /**
     * return transitive (all but sources) dependencies
     * @return transitive (all but sources) dependencies
     */

    NutsStream<NutsDependency> transitive();

    /**
     * transitive dependencies merged with ids, which may constitute a full classpath
     *
     * @return transitive dependencies merged with ids, which may constitute a full classpath
     */
    NutsStream<NutsDependency> transitiveWithSource();

    /**
     * return all or some of the transitive dependencies of the current Artifact as Tree result of the search command
     *
     * @return all or some of the transitive dependencies of the current Artifact as Tree result of the search command.
     */
    NutsStream<NutsDependencyTreeNode> transitiveNodes();

    /**
     * return source Nodes tree including sources and their immediate dependencies at each Node level
     * @return source Nodes tree including sources and their immediate dependencies at each Node level
     */
    NutsStream<NutsDependencyTreeNode> sourceNodes();
}
